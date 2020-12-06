/*
 * Copyright 2013-2016 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package configs

import com.typesafe.config.ConfigException
import java.util.concurrent.TimeUnit
import java.{lang => jl, math => jm, time => jt, util => ju}

import scala.annotation.compileTimeOnly
import scala.collection.compat._
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.jdk.CollectionConverters._

trait ConfigReader[A] {

  protected def get(config: Config, path: String): Result[A]

  /**
   * reduce results for multiple naming strategies
   */
  protected def reduce(results: Seq[Result[A]]): Result[A] =
    // default is: take first success, if all failed the last failure
    results.find( r => r.isSuccess ).getOrElse(results.last)


  final def read(config: Config, path: String): Result[A] = {
    get(config, path).pushPath(path)
  }

  final def read(config: Config, path: Seq[String]): Result[A] = {
    reduce(path.map(read(config, _)))
  }

  final def extract(config: Config): Result[A] = {
    val p = "extract"
    get(config.atKey(p), p)
  }

  final def extractValue(value: ConfigValue): Result[A] = {
    val p = "extractValue"
    get(value.atKey(p), p)
  }

  final def map[B](f: A => B): ConfigReader[B] =
    get(_, _).map(f)

  final def rmap[B](f: A => Result[B]): ConfigReader[B] =
    get(_, _).flatMap(f)

  final def flatMap[B](f: A => ConfigReader[B]): ConfigReader[B] =
    (c, p) => get(c, p).flatMap(f(_).get(c, p))

  final def orElse[B >: A](fallback: ConfigReader[B]): ConfigReader[B] =
    (c, p) => get(c, p).orElse(fallback.get(c, p))

  final def transform[B](fail: ConfigError => ConfigReader[B], succ: A => ConfigReader[B]): ConfigReader[B] =
    (c, p) => get(c, p).fold(fail, succ).get(c, p)

  final def as[B >: A]: ConfigReader[B] =
    this.asInstanceOf[ConfigReader[B]]

}

object ConfigReader extends ConfigReaderInstances {

  @inline
  def apply[A](implicit A: ConfigReader[A]): ConfigReader[A] = A


  def derive[A](implicit naming: ConfigKeyNaming[A]): ConfigReader[A] =
    macro macros.ConfigReaderMacro.derive[A]

  @deprecated("Use derive[A] or auto derivation", "0.5.0")
  def deriveBean[A](implicit naming: ConfigKeyNaming[A]): ConfigReader[A] =
    macro macros.ConfigReaderMacro.derive[A]

  def deriveBeanWith[A](newInstance: => A)(implicit naming: ConfigKeyNaming[A]): ConfigReader[A] =
    macro macros.ConfigReaderMacro.deriveBeanWith[A]


  def from[A](f: (Config, String) => Result[A]): ConfigReader[A] =
    (c, p) => Result.Try(f(c, p)).flatten

  def fromConfig[A](f: Config => Result[A]): ConfigReader[A] =
    ConfigReader[Config].rmap(f)

  def fromTry[A](f: (Config, String) => A): ConfigReader[A] =
    (c, p) => Result.Try(f(c, p))

  def fromConfigTry[A](f: Config => A): ConfigReader[A] =
    fromTry((c, p) => f(c.getConfig(p)))

  def withResult[A](result: Result[A]): ConfigReader[A] =
    (_, _) => result

  def successful[A](a: A): ConfigReader[A] =
    withResult(Result.successful(a))

  def failure[A](msg: String): ConfigReader[A] =
    withResult(Result.failure(ConfigError(msg)))

  def get[A](path: String)(implicit A: ConfigReader[A]): ConfigReader[A] =
    fromConfig(A.read(_, path))

}


sealed abstract class ConfigReaderInstances3 {

  implicit def autoDeriveConfigReader[A](implicit naming: ConfigKeyNaming[A]): ConfigReader[A] =
    macro macros.ConfigReaderMacro.derive[A]

}

sealed abstract class ConfigReaderInstances2 extends ConfigReaderInstances3 {

  @compileTimeOnly(
    "cannot derive for `java.util.Map[A, B]`: " +
      "`A` is not a StringConverter instance " +
      "and `B` is not a ConfigReader instance")
  implicit def errorJavaMapConfigReader2[A, B]: ConfigReader[ju.Map[A, B]] =
    sys.error("compileTimeOnly")

}

sealed abstract class ConfigReaderInstances1 extends ConfigReaderInstances2 {

  @compileTimeOnly("cannot derive for `java.util.Map[A, B]`: `A` is not a StringConverter instance")
  implicit def errorJavaMapConfigReader1[A, B: ConfigReader]: ConfigReader[ju.Map[A, B]] =
    sys.error("compileTimeOnly")

}

sealed abstract class ConfigReaderInstances0 extends ConfigReaderInstances1 {

  @compileTimeOnly("cannot derive for `java.util.List[A]`: `A` is not a ConfigReader instance")
  implicit def errorJavaListConfigReader[A]: ConfigReader[ju.List[A]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly("cannot derive for `java.util.Map[A, B]`: `B` is not a ConfigReader instance")
  implicit def errorJavaMapConfigReader0[A: StringConverter, B]: ConfigReader[ju.Map[A, B]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly("cannot derive for `Option[A]`: `A` is not a ConfigReader instance")
  implicit def errorOptionConfigReader[A]: ConfigReader[Option[A]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly("cannot derive for `java.util.Optional[A]`: `A` is not a ConfigReader instance")
  implicit def errorJavaOptionalConfigReader[A]: ConfigReader[ju.Optional[A]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly("cannot derive for `Result[A]`: `A` is not a ConfigReader instance")
  implicit def errorResultConfigReader[A]: ConfigReader[Result[A]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly("cannot derive for `(A, ConfigOrigin)`: `A` is not a ConfigReader instance")
  implicit def errorWithOriginConfigReader[A]: ConfigReader[(A, ConfigOrigin)] =
    sys.error("compileTimeOnly")

}

sealed abstract class ConfigReaderInstances extends ConfigReaderInstances0 {

  implicit def javaListConfigReader[A](implicit A: ConfigReader[A]): ConfigReader[ju.List[A]] =
    ConfigReader[ConfigList].rmap { xs =>
      Result.traverse(xs.asScala.zipWithIndex) {
        case (x, i) => A.extractValue(x).pushPath(i.toString)
      }.map(_.asJava)
    }

  implicit def javaIterableConfigReader[A](implicit C: ConfigReader[ju.List[A]]): ConfigReader[jl.Iterable[A]] =
    C.as[jl.Iterable[A]]

  implicit def javaCollectionConfigReader[A](implicit C: ConfigReader[ju.List[A]]): ConfigReader[ju.Collection[A]] =
    C.as[ju.Collection[A]]

  implicit def javaSetConfigReader[A](implicit C: ConfigReader[ju.List[A]]): ConfigReader[ju.Set[A]] =
    C.map(_.asScala.toSet.asJava)

  implicit def javaMapConfigReader[A, B](implicit A: StringConverter[A], B: ConfigReader[B]): ConfigReader[ju.Map[A, B]] =
    ConfigReader.fromConfig { c =>
      val m: Result[Map[A, B]] =
        Result.traverse(c.root().asScala.keysIterator) { k =>
          val p = ConfigUtil.joinPath(k)
          Result.tuple2(A.fromString(k).pushPath(p), B.read(c, p))
        }.map(_.toMap)
      m.map(_.asJava)
    }


  implicit def cbfJListConfigReader[F[_], A](implicit C: ConfigReader[ju.List[A]], F: Factory[A, F[A]]): ConfigReader[F[A]] =
    C.map(c => F.fromSpecific(c.asScala))

  implicit def cbfJMapConfigReader[M[_, _], A, B](implicit C: ConfigReader[ju.Map[A, B]], F: Factory[(A, B), M[A, B]]): ConfigReader[M[A, B]] =
    C.map(c => F.fromSpecific(c.asScala))

  /**
   * Reader for Option[A] must consider how to combine empty results for different naming strategies
   */
  implicit def optionConfigReader[A](implicit A: ConfigReader[A]): ConfigReader[Option[A]] = new ConfigReader[Option[A]] {
    override def get(c: Config, p: String): Result[Option[A]] = {
      Result.Try {
        if (c.hasPathOrNull(p))
          A.read(c, p).map(Some(_)).handle {
            case ConfigError(ConfigError.NullValue(_, `p` :: Nil), es) if es.isEmpty => None
          }.popPath
        else
          Result.successful(None)
      }.flatten
    }
    override def reduce(results: Seq[Result[Option[A]]]): Result[Option[A]] =
      // take first success option that is defined, else first success option that is empty, else last failure
      results.find( r => r.isSuccess && r.value.isDefined)
        .orElse(results.find(_.isSuccess))
        .getOrElse(results.last)
  }

  implicit def javaOptionalConfigReader[A: ConfigReader]: ConfigReader[ju.Optional[A]] =
    optionConfigReader[A].map(_.fold(ju.Optional.empty[A]())(ju.Optional.of))

  implicit lazy val javaOptionalIntConfigReader: ConfigReader[ju.OptionalInt] =
    optionConfigReader[Int].map(_.fold(ju.OptionalInt.empty())(ju.OptionalInt.of))

  implicit lazy val javaOptionalLongConfigReader: ConfigReader[ju.OptionalLong] =
    optionConfigReader[Long].map(_.fold(ju.OptionalLong.empty())(ju.OptionalLong.of))

  implicit lazy val javaOptionalDoubleConfigReader: ConfigReader[ju.OptionalDouble] =
    optionConfigReader[Double].map(_.fold(ju.OptionalDouble.empty())(ju.OptionalDouble.of))


  implicit def resultConfigReader[A](implicit A: ConfigReader[A]): ConfigReader[Result[A]] =
    (c, p) => Result.successful(A.read(c, p))


  implicit def fromStringConfigReader[A](implicit A: StringConverter[A]): ConfigReader[A] =
    ConfigReader[String].rmap(A.fromString)


  private[this] def bigDecimal(expected: String): ConfigReader[BigDecimal] =
    ConfigReader.fromTry { (c, p) =>
      val s = c.getString(p)
      try BigDecimal(s) catch {
        case e: NumberFormatException =>
          throw new ConfigException.WrongType(c.origin(), p, expected, s"STRING value '$s'", e)
      }
    }

  private[this] def bigInt(expected: String): ConfigReader[BigInt] =
    bigDecimal(expected).map(_.toBigInt)

  private[this] def integral[A](expected: String, valid: BigInt => Boolean, value: BigInt => A): ConfigReader[A] =
    bigInt(expected).flatMap { n =>
      ConfigReader.fromTry { (c, p) =>
        if (valid(n)) value(n)
        else throw new ConfigException.WrongType(c.origin(), p, expected, s"out-of-range value $n")
      }
    }

  implicit lazy val byteConfigReader: ConfigReader[Byte] =
    integral("byte (8-bit integer)", _.isValidByte, _.toByte)

  implicit lazy val javaByteConfigReader: ConfigReader[jl.Byte] =
    byteConfigReader.asInstanceOf[ConfigReader[jl.Byte]]

  implicit lazy val shortConfigReader: ConfigReader[Short] =
    integral("short (16-bit integer)", _.isValidShort, _.toShort)

  implicit lazy val javaShortConfigReader: ConfigReader[jl.Short] =
    shortConfigReader.asInstanceOf[ConfigReader[jl.Short]]

  implicit lazy val intConfigReader: ConfigReader[Int] =
    integral("int (32-bit integer)", _.isValidInt, _.toInt)

  implicit lazy val javaIntegerConfigReader: ConfigReader[jl.Integer] =
    intConfigReader.asInstanceOf[ConfigReader[jl.Integer]]

  implicit lazy val longConfigReader: ConfigReader[Long] =
    integral("long (64-bit integer)", _.isValidLong, _.toLong)

  implicit lazy val javaLongConfigReader: ConfigReader[jl.Long] =
    longConfigReader.asInstanceOf[ConfigReader[jl.Long]]


  implicit lazy val floatConfigReader: ConfigReader[Float] =
    ConfigReader.fromTry(_.getDouble(_).toFloat)

  implicit lazy val javaFloatConfigReader: ConfigReader[jl.Float] =
    floatConfigReader.asInstanceOf[ConfigReader[jl.Float]]


  implicit lazy val doubleConfigReader: ConfigReader[Double] =
    ConfigReader.fromTry(_.getDouble(_))

  implicit lazy val javaDoubleConfigReader: ConfigReader[jl.Double] =
    doubleConfigReader.asInstanceOf[ConfigReader[jl.Double]]


  implicit lazy val bigIntConfigReader: ConfigReader[BigInt] =
    bigInt("integer")

  implicit lazy val bigIntegerConfigReader: ConfigReader[jm.BigInteger] =
    bigIntConfigReader.map(_.bigInteger)


  implicit lazy val bigDecimalConfigReader: ConfigReader[BigDecimal] =
    bigDecimal("decimal")

  implicit lazy val javaBigDecimalConfigReader: ConfigReader[jm.BigDecimal] =
    bigDecimalConfigReader.map(_.bigDecimal)


  implicit lazy val booleanConfigReader: ConfigReader[Boolean] =
    ConfigReader.fromTry(_.getBoolean(_))

  implicit lazy val javaBooleanConfigReader: ConfigReader[jl.Boolean] =
    booleanConfigReader.asInstanceOf[ConfigReader[jl.Boolean]]


  implicit lazy val charConfigReader: ConfigReader[Char] =
    ConfigReader.fromTry { (c, p) =>
      val s = c.getString(p)
      if (s.length == 1) s(0)
      else throw new ConfigException.WrongType(c.origin(), p, "single BMP char", s"STRING value '$s'")
    }

  implicit lazy val charJListConfigReader: ConfigReader[ju.List[Char]] =
    ConfigReader.fromTry((c, p) => ju.Arrays.asList(c.getString(p).toCharArray: _*))

  implicit lazy val javaCharConfigReader: ConfigReader[jl.Character] =
    charConfigReader.asInstanceOf[ConfigReader[jl.Character]]

  implicit lazy val javaCharListConfigReader: ConfigReader[ju.List[jl.Character]] =
    charJListConfigReader.asInstanceOf[ConfigReader[ju.List[jl.Character]]]


  implicit lazy val stringConfigReader: ConfigReader[String] =
    ConfigReader.fromTry(_.getString(_))


  implicit lazy val javaDurationConfigReader: ConfigReader[jt.Duration] =
    ConfigReader.fromTry(_.getDuration(_))

  implicit lazy val finiteDurationConfigReader: ConfigReader[FiniteDuration] =
    ConfigReader.fromTry(_.getDuration(_, TimeUnit.NANOSECONDS)).map(Duration.fromNanos)

  implicit lazy val durationConfigReader: ConfigReader[Duration] =
    finiteDurationConfigReader.orElse(ConfigReader.fromTry { (c, p) =>
      c.getString(p) match {
        case "Infinity" | "+Infinity" => Duration.Inf
        case "-Infinity" => Duration.MinusInf
        case "Undefined" | "NaN" | "+NaN" | "-NaN" => Duration.Undefined
        case s => throw new ConfigException.BadValue(c.origin(), p, s"Could not parse duration '$s'")
      }
    })


  implicit lazy val configConfigReader: ConfigReader[Config] =
    ConfigReader.fromTry(_.getConfig(_))

  implicit lazy val configValueConfigReader: ConfigReader[ConfigValue] =
    ConfigReader.fromTry(_.getValue(_))

  implicit lazy val configListConfigReader: ConfigReader[ConfigList] =
    ConfigReader.fromTry(_.getList(_))

  implicit lazy val configValueJListConfigReader: ConfigReader[ju.List[ConfigValue]] =
    configListConfigReader.as[ju.List[ConfigValue]]


  implicit lazy val configObjectConfigReader: ConfigReader[ConfigObject] =
    ConfigReader.fromTry(_.getObject(_))

  implicit lazy val configValueJMapConfigReader: ConfigReader[ju.Map[String, ConfigValue]] =
    configObjectConfigReader.as[ju.Map[String, ConfigValue]]


  implicit lazy val configMemorySizeConfigReader: ConfigReader[ConfigMemorySize] =
    ConfigReader.fromTry(_.getMemorySize(_))


  implicit lazy val javaPropertiesConfigReader: ConfigReader[ju.Properties] =
    ConfigReader[ju.Map[String, String]].map { m =>
      val p = new ju.Properties()
      (p: java.util.Hashtable[AnyRef, AnyRef]).putAll(m)
      p
    }

}
