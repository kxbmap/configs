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

import com.typesafe.config.{Config, ConfigException, ConfigList, ConfigMemorySize, ConfigObject, ConfigUtil, ConfigValue}
import java.util.concurrent.TimeUnit
import java.{lang => jl, math => jm, time => jt, util => ju}
import scala.collection.convert.decorateAll._
import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.ClassTag
import scala.util.{Failure, Success, Try}

trait Configs[A] {
  self =>

  def get(config: Config, path: String): Result[A]

  def extractKey: String = "extract"

  def extract(config: Config): Result[A] =
    get(config.atKey(extractKey), extractKey)

  def extract(value: ConfigValue): Result[A] =
    get(value.atKey(extractKey), extractKey)

  def map[B](f: A => B): Configs[B] =
    get(_, _).map(f)

  def flatMap[B](f: A => Configs[B]): Configs[B] =
    (c, p) => get(c, p).flatMap(f(_).get(c, p))

  def orElse[B >: A](fallback: Configs[B]): Configs[B] =
    (c, p) => get(c, p).orElse(fallback.get(c, p))

  def withPath: Configs[A] =
    new Configs[A] {
      override def get(config: Config, path: String): Result[A] =
        self.get(config, path).mapError(_.withPath(path))

      override def withPath: Configs[A] = this
    }

  def withExtractKey(key: String): Configs[A] =
    new Configs[A] {
      def get(config: Config, path: String): Result[A] =
        self.get(config, path)

      override def extractKey: String = key
    }

  def as[B >: A]: Configs[B] =
    this.asInstanceOf[Configs[B]]

}

object Configs extends ConfigsInstances {

  @inline
  def apply[A](implicit A: Configs[A]): Configs[A] = A


  def derive[A]: Configs[A] =
    macro macros.ConfigsMacro.deriveConfigs[A]

  def deriveBean[A]: Configs[A] =
    macro macros.BeanConfigsMacro.deriveBeanConfigsA[A]

  def deriveBean[A](newInstance: => A): Configs[A] =
    macro macros.BeanConfigsMacro.deriveBeanConfigsI[A]


  def from[A](f: (Config, String) => Result[A]): Configs[A] =
    withPath((c, p) => Result.Try(f(c, p)).flatten)

  def from[A](f: Config => Result[A]): Configs[A] =
    from((c, p) => f(c.getConfig(p)))

  def Try[A](f: (Config, String) => A): Configs[A] =
    withPath((c, p) => Result.Try(f(c, p)))

  def Try[A](f: Config => A): Configs[A] =
    Try((c, p) => f(c.getConfig(p)))

  def successful[A](a: A): Configs[A] =
    withPath((_, _) => Result.successful(a))

  def failure[A](msg: String): Configs[A] =
    withPath((_, _) => Result.failure(ConfigError(msg)))

  def get[A](path: String)(implicit A: Configs[A]): Configs[A] =
    (c, p) => Configs[Config].get(c, p).flatMap(A.get(_, path))

  def withPath[A](configs: Configs[A]): Configs[A] =
    configs.withPath

}


sealed abstract class ConfigsInstances1 {

  implicit def autoDerivationConfigs[A]: Configs[A] =
    macro macros.ConfigsMacro.deriveConfigs[A]

}

sealed abstract class ConfigsInstances0 extends ConfigsInstances1 {

  implicit def javaListConfigs[A](implicit A: Configs[A]): Configs[ju.List[A]] =
    Configs.from { (c, p) =>
      Result.sequence(
        c.getList(p).asScala.zipWithIndex.map {
          case (v, i) => A.withExtractKey(i.toString).extract(v)
        })
        .map(_.asJava)
    }

}

sealed abstract class ConfigsInstances extends ConfigsInstances0 {

  implicit def javaIterableConfigs[A](implicit C: Configs[ju.List[A]]): Configs[jl.Iterable[A]] =
    C.as[jl.Iterable[A]]

  implicit def javaCollectionConfigs[A](implicit C: Configs[ju.List[A]]): Configs[ju.Collection[A]] =
    C.as[ju.Collection[A]]

  implicit def javaSetConfigs[A](implicit C: Configs[ju.List[A]]): Configs[ju.Set[A]] =
    C.map(_.asScala.toSet.asJava)

  implicit def javaMapConfigs[A, B](implicit A: Converter[String, A], B: Configs[B]): Configs[ju.Map[A, B]] =
    Configs.from { c =>
      Result.sequence(
        c.root().asScala.keysIterator.map {
          k => Result.tuple2(A.convert(k), B.get(c, ConfigUtil.quoteString(k)))
        })
        .map(_.toMap.asJava)
    }


  implicit def cbfJListConfigs[F[_], A](implicit C: Configs[ju.List[A]], cbf: CanBuildFrom[Nothing, A, F[A]]): Configs[F[A]] =
    C.map(_.asScala.to[F])

  implicit def cbfJMapConfigs[M[_, _], A, B](implicit C: Configs[ju.Map[A, B]], cbf: CanBuildFrom[Nothing, (A, B), M[A, B]]): Configs[M[A, B]] =
    C.map(_.asScala.to[({type F[_] = M[A, B]})#F])


  implicit def optionConfigs[A](implicit A: Configs[A]): Configs[Option[A]] =
    (c, p) =>
      if (c.hasPathOrNull(p))
        A.get(c, p).map(Some(_)).handle {
          case ConfigError(ConfigError.NullValue(_, `p` :: Nil), es) if es.isEmpty => None
        }
      else
        Result.successful(None)

  implicit def tryConfigs[A](implicit A: Configs[A]): Configs[Try[A]] =
    A.get(_, _).map(Success(_)).handle {
      case e => Failure(e.throwable)
    }

  implicit def eitherConfigs[E <: Throwable, A](implicit E: ClassTag[E], A: Configs[A]): Configs[Either[E, A]] =
    A.get(_, _).map(Right(_)).handle {
      case e if E.runtimeClass.isAssignableFrom(e.throwable.getClass) =>
        Left(e.throwable.asInstanceOf[E])
    }


  def convertConfigs[A, B](implicit A: Configs[A], C: Converter[A, B]): Configs[B] =
    (c, p) => A.get(c, p).flatMap(C.convert(_).mapError(_.withPath(p)))

  def convertJListConfigs[A, B](implicit A: Configs[ju.List[A]], C: Converter[A, B]): Configs[ju.List[B]] =
    (c, p) =>
      A.get(c, p).flatMap { as =>
        Result.sequence(
          as.asScala.zipWithIndex.map {
            case (a, i) => C.convert(a).mapError(_.withPath(i.toString))
          })
          .map(_.asJava).mapError(_.withPath(p))
      }

  implicit def convertStringConfigs[A: Converter.FromString]: Configs[A] =
    convertConfigs[String, A]

  implicit def convertStringJListConfigs[A: Converter.FromString]: Configs[ju.List[A]] =
    convertJListConfigs[String, A]


  private[this] def toByte(n: Number, c: Config, p: String): Byte = {
    val l = n.longValue()
    if (l.isValidByte) l.toByte
    else throw new ConfigException.WrongType(c.origin(), p, "byte (8-bit integer)", s"out-of-range value $l")
  }

  implicit lazy val byteConfigs: Configs[Byte] =
    Configs.Try((c, p) => toByte(c.getNumber(p), c, p))

  implicit lazy val byteJListConfigs: Configs[ju.List[Byte]] =
    Configs.Try((c, p) => c.getNumberList(p).asScala.map(toByte(_, c, p)).asJava)

  implicit lazy val javaByteConfigs: Configs[jl.Byte] =
    byteConfigs.asInstanceOf[Configs[jl.Byte]]

  implicit lazy val javaByteListConfigs: Configs[ju.List[jl.Byte]] =
    byteJListConfigs.asInstanceOf[Configs[ju.List[jl.Byte]]]


  private[this] def toShort(n: Number, c: Config, p: String): Short = {
    val l = n.longValue()
    if (l.isValidShort) l.toShort
    else throw new ConfigException.WrongType(c.origin(), p, "short (16-bit integer)", s"out-of-range value $l")
  }

  implicit lazy val shortConfigs: Configs[Short] =
    Configs.Try((c, p) => toShort(c.getNumber(p), c, p))

  implicit lazy val shortJListConfigs: Configs[ju.List[Short]] =
    Configs.Try((c, p) => c.getNumberList(p).asScala.map(toShort(_, c, p)).asJava)

  implicit lazy val javaShortConfigs: Configs[jl.Short] =
    shortConfigs.asInstanceOf[Configs[jl.Short]]

  implicit lazy val javaShortListConfigs: Configs[ju.List[jl.Short]] =
    shortJListConfigs.asInstanceOf[Configs[ju.List[jl.Short]]]


  implicit lazy val intConfigs: Configs[Int] =
    Configs.Try(_.getInt(_))

  implicit lazy val intJListConfigs: Configs[ju.List[Int]] =
    javaIntegerListConfigs.asInstanceOf[Configs[ju.List[Int]]]

  implicit lazy val javaIntegerConfigs: Configs[jl.Integer] =
    intConfigs.asInstanceOf[Configs[jl.Integer]]

  implicit lazy val javaIntegerListConfigs: Configs[ju.List[jl.Integer]] =
    Configs.Try(_.getIntList(_))


  private[this] def parseLong(s: String, c: Config, p: String): Long = {
    val n =
      try BigDecimal(s).toBigInt() catch {
        case e: NumberFormatException =>
          throw new ConfigException.WrongType(c.origin(), p, "long (64-bit integer)", s"STRING value $s", e)
      }
    if (n.isValidLong) n.longValue()
    else throw new ConfigException.WrongType(c.origin(), p, "long (64-bit integer)", s"out-of-range value $n")
  }

  implicit lazy val longConfigs: Configs[Long] =
    Configs.Try((c, p) => parseLong(c.getString(p), c, p))

  implicit lazy val longJListConfigs: Configs[ju.List[Long]] =
    Configs.Try((c, p) => c.getStringList(p).asScala.map(parseLong(_, c, p)).asJava)

  implicit lazy val javaLongConfigs: Configs[jl.Long] =
    longConfigs.asInstanceOf[Configs[jl.Long]]

  implicit lazy val javaLongListConfigs: Configs[ju.List[jl.Long]] =
    longJListConfigs.asInstanceOf[Configs[ju.List[jl.Long]]]


  implicit lazy val floatConfigs: Configs[Float] =
    Configs.Try(_.getDouble(_).toFloat)

  implicit lazy val floatJListConfigs: Configs[ju.List[Float]] =
    Configs.Try(_.getDoubleList(_).asScala.map(_.floatValue()).asJava)

  implicit lazy val javaFloatConfigs: Configs[jl.Float] =
    floatConfigs.asInstanceOf[Configs[jl.Float]]

  implicit lazy val javaFloatListConfigs: Configs[ju.List[jl.Float]] =
    floatJListConfigs.asInstanceOf[Configs[ju.List[jl.Float]]]


  implicit lazy val doubleConfigs: Configs[Double] =
    Configs.Try(_.getDouble(_))

  implicit lazy val doubleJListConfigs: Configs[ju.List[Double]] =
    javaDoubleListConfigs.asInstanceOf[Configs[ju.List[Double]]]

  implicit lazy val javaDoubleConfigs: Configs[jl.Double] =
    doubleConfigs.asInstanceOf[Configs[jl.Double]]

  implicit lazy val javaDoubleListConfigs: Configs[ju.List[jl.Double]] =
    Configs.Try(_.getDoubleList(_))


  implicit lazy val bigIntConfigs: Configs[BigInt] =
    bigDecimalConfigs.map(_.toBigInt())

  implicit lazy val bigIntJListConfigs: Configs[ju.List[BigInt]] =
    bigDecimalJListConfigs.map(_.asScala.map(_.toBigInt()).asJava)

  implicit lazy val bigIntegerConfigs: Configs[jm.BigInteger] =
    javaBigDecimalConfigs.map(_.toBigInteger)

  implicit lazy val bigIntegerJListConfigs: Configs[ju.List[jm.BigInteger]] =
    javaBigDecimalListConfigs.map(_.asScala.map(_.toBigInteger).asJava)


  implicit lazy val bigDecimalConfigs: Configs[BigDecimal] =
    stringConfigs.map(BigDecimal.apply)

  implicit lazy val bigDecimalJListConfigs: Configs[ju.List[BigDecimal]] =
    stringJListConfigs.map(_.asScala.map(BigDecimal.apply).asJava)

  implicit lazy val javaBigDecimalConfigs: Configs[jm.BigDecimal] =
    stringConfigs.map(new jm.BigDecimal(_))

  implicit lazy val javaBigDecimalListConfigs: Configs[ju.List[jm.BigDecimal]] =
    stringJListConfigs.map(_.asScala.map(new jm.BigDecimal(_)).asJava)


  implicit lazy val booleanConfigs: Configs[Boolean] =
    Configs.Try(_.getBoolean(_))

  implicit lazy val booleanJListConfigs: Configs[ju.List[Boolean]] =
    javaBooleanListConfigs.asInstanceOf[Configs[ju.List[Boolean]]]

  implicit lazy val javaBooleanConfigs: Configs[jl.Boolean] =
    booleanConfigs.asInstanceOf[Configs[jl.Boolean]]

  implicit lazy val javaBooleanListConfigs: Configs[ju.List[jl.Boolean]] =
    Configs.Try(_.getBooleanList(_))


  implicit lazy val charConfigs: Configs[Char] =
    Configs.Try { (c, p) =>
      val s = c.getString(p)
      if (s.length == 1) s(0)
      else throw new ConfigException.WrongType(c.origin(), p, "single BMP char", s"STRING value '$s'")
    }

  implicit lazy val charJListConfigs: Configs[ju.List[Char]] =
    Configs.Try((c, p) => ju.Arrays.asList(c.getString(p).toCharArray: _*))

  implicit lazy val javaCharConfigs: Configs[jl.Character] =
    charConfigs.asInstanceOf[Configs[jl.Character]]

  implicit lazy val javaCharListConfigs: Configs[ju.List[jl.Character]] =
    charJListConfigs.asInstanceOf[Configs[ju.List[jl.Character]]]


  implicit lazy val stringConfigs: Configs[String] =
    Configs.Try(_.getString(_))

  implicit lazy val stringJListConfigs: Configs[ju.List[String]] =
    Configs.Try(_.getStringList(_))


  implicit lazy val javaDurationConfigs: Configs[jt.Duration] =
    Configs.Try(_.getDuration(_))

  implicit lazy val javaDurationListConfigs: Configs[ju.List[jt.Duration]] =
    Configs.Try(_.getDurationList(_))


  implicit lazy val finiteDurationConfigs: Configs[FiniteDuration] =
    Configs.Try(_.getDuration(_, TimeUnit.NANOSECONDS)).map(Duration.fromNanos)

  implicit lazy val finiteDurationJListConfigs: Configs[ju.List[FiniteDuration]] =
    Configs.Try(_.getDurationList(_, TimeUnit.NANOSECONDS).asScala.map(Duration.fromNanos(_)).asJava)


  implicit lazy val durationConfigs: Configs[Duration] =
    finiteDurationConfigs.orElse(Configs.Try { (c, p) =>
      c.getString(p) match {
        case "infinity" | "+infinity" => Duration.Inf
        case "-infinity" => Duration.MinusInf
        case "undefined" => Duration.Undefined
        case s => throw new ConfigException.BadValue(c.origin(), p, s"Could not parse duration '$s'")
      }
    })


  implicit lazy val configConfigs: Configs[Config] =
    Configs.withPath(new Configs[Config] {
      def get(config: Config, path: String): Result[Config] =
        Result.Try(config.getConfig(path))

      override def extract(config: Config): Result[Config] =
        Result.successful(config)

      override def extract(value: ConfigValue): Result[Config] =
        value match {
          case co: ConfigObject => Result.successful(co.toConfig)
          case _ => super.extract(value)
        }
    })

  implicit lazy val configJListConfigs: Configs[ju.List[Config]] =
    Configs.Try(_.getConfigList(_).asInstanceOf[ju.List[Config]])


  implicit lazy val configValueConfigs: Configs[ConfigValue] =
    Configs.withPath(new Configs[ConfigValue] {
      def get(config: Config, path: String): Result[ConfigValue] =
        Result.Try(config.getValue(path))

      override def extract(config: Config): Result[ConfigValue] =
        Result.successful(config.root())

      override def extract(value: ConfigValue): Result[ConfigValue] =
        Result.successful(value)
    })

  implicit lazy val configValueJListConfigs: Configs[ju.List[ConfigValue]] =
    Configs.Try(_.getList(_))

  implicit lazy val configValueJMapConfigs: Configs[ju.Map[String, ConfigValue]] =
    Configs.Try(_.getObject(_))

  implicit def configValueJMapKeyConfigs[A](implicit A: Converter[String, A]): Configs[ju.Map[A, ConfigValue]] =
    configObjectConfigs.get(_, _).flatMap { co =>
      Result.sequence(
        co.asScala.map {
          case (k, v) => A.convert(k).map(_ -> v)
        })
        .map(_.toMap.asJava)
    }


  implicit lazy val configListConfigs: Configs[ConfigList] =
    Configs.Try(_.getList(_))


  implicit lazy val configObjectConfigs: Configs[ConfigObject] =
    Configs.Try(_.getObject(_))

  implicit lazy val configObjectJListConfigs: Configs[ju.List[ConfigObject]] =
    Configs.Try(_.getObjectList(_).asInstanceOf[ju.List[ConfigObject]])


  implicit lazy val configMemorySizeConfigs: Configs[ConfigMemorySize] =
    Configs.Try(_.getMemorySize(_))

  implicit lazy val configMemorySizeJListConfigs: Configs[ju.List[ConfigMemorySize]] =
    Configs.Try(_.getMemorySizeList(_))


  implicit lazy val javaPropertiesConfigs: Configs[ju.Properties] =
    Configs[ju.Map[String, String]].map { m =>
      val p = new ju.Properties()
      p.putAll(m)
      p
    }

}
