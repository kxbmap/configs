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
import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration.{Duration, FiniteDuration}

trait Configs[A] {

  protected def get0(config: Config, path: String): Result[A]

  def get(config: Config, path: String): Result[A] =
    get0(config, path).pushPath(path)

  def extract(config: Config, key: String = "extract"): Result[A] =
    get(config.atKey(key), key)

  def extractValue(value: ConfigValue, key: String = "extract"): Result[A] =
    get(value.atKey(key), key)

  def map[B](f: A => B): Configs[B] =
    get0(_, _).map(f)

  def rmap[B](f: A => Result[B]): Configs[B] =
    get0(_, _).flatMap(f)

  def flatMap[B](f: A => Configs[B]): Configs[B] =
    (c, p) => get0(c, p).flatMap(f(_).get0(c, p))

  def orElse[B >: A](fallback: Configs[B]): Configs[B] =
    (c, p) => get0(c, p).orElse(fallback.get0(c, p))

  def transform[B](fail: ConfigError => Configs[B], succ: A => Configs[B]): Configs[B] =
    (c, p) => get0(c, p).fold(fail, succ).get0(c, p)

  def as[B >: A]: Configs[B] =
    this.asInstanceOf[Configs[B]]

}

object Configs extends ConfigsInstances {

  @inline
  def apply[A](implicit A: Configs[A]): Configs[A] = A


  def derive[A]: Configs[A] =
    macro macros.ConfigsMacro.derive[A]

  @deprecated("Use derive[A] or auto derivation", "0.5.0")
  def deriveBean[A]: Configs[A] =
    macro macros.ConfigsMacro.derive[A]

  def deriveBeanWith[A](newInstance: => A): Configs[A] =
    macro macros.ConfigsMacro.deriveBeanWith[A]


  def from[A](f: (Config, String) => Result[A]): Configs[A] =
    (c, p) => Result.Try(f(c, p)).flatten

  def fromConfig[A](f: Config => Result[A]): Configs[A] =
    Configs[Config].rmap(f)

  def fromTry[A](f: (Config, String) => A): Configs[A] =
    (c, p) => Result.Try(f(c, p))

  def fromConfigTry[A](f: Config => A): Configs[A] =
    fromTry((c, p) => f(c.getConfig(p)))

  def successful[A](a: A): Configs[A] =
    (_, _) => Result.successful(a)

  def failure[A](msg: String): Configs[A] =
    (_, _) => Result.failure(ConfigError(msg))

  def get[A](path: String)(implicit A: Configs[A]): Configs[A] =
    fromConfig(A.get(_, path))

}


sealed abstract class ConfigsInstances0 {

  implicit def autoDeriveConfigs[A]: Configs[A] =
    macro macros.ConfigsMacro.derive[A]

}

sealed abstract class ConfigsInstances extends ConfigsInstances0 {

  implicit def javaListConfigs[A](implicit A: Configs[A]): Configs[ju.List[A]] =
    Configs[ConfigList].rmap { xs =>
      Result.sequence(
        xs.asScala.zipWithIndex.map {
          case (x, i) => A.extractValue(x, i.toString)
        })
        .map(_.asJava)
    }

  implicit def javaIterableConfigs[A](implicit C: Configs[ju.List[A]]): Configs[jl.Iterable[A]] =
    C.as[jl.Iterable[A]]

  implicit def javaCollectionConfigs[A](implicit C: Configs[ju.List[A]]): Configs[ju.Collection[A]] =
    C.as[ju.Collection[A]]

  implicit def javaSetConfigs[A](implicit C: Configs[ju.List[A]]): Configs[ju.Set[A]] =
    C.map(_.asScala.toSet.asJava)

  implicit def javaMapConfigs[A, B](implicit A: FromString[A], B: Configs[B]): Configs[ju.Map[A, B]] =
    Configs.fromConfig { c =>
      Result.sequence(
        c.root().asScala.keysIterator.map { k =>
          val p = ConfigUtil.joinPath(k)
          Result.tuple2(A.read(k).pushPath(p), B.get(c, p))
        })
        .map(_.toMap.asJava)
    }


  implicit def cbfJListConfigs[F[_], A](implicit C: Configs[ju.List[A]], cbf: CanBuildFrom[Nothing, A, F[A]]): Configs[F[A]] =
    C.map(_.asScala.to[F])

  implicit def cbfJMapConfigs[M[_, _], A, B](implicit C: Configs[ju.Map[A, B]], cbf: CanBuildFrom[Nothing, (A, B), M[A, B]]): Configs[M[A, B]] =
    C.map(_.asScala.to[({type F[_] = M[A, B]})#F])


  implicit def optionConfigs[A](implicit A: Configs[A]): Configs[Option[A]] =
    Configs.from { (c, p) =>
      if (c.hasPathOrNull(p))
        A.get(c, p).map(Some(_)).handle {
          case ConfigError(ConfigError.NullValue(_, `p` :: Nil), es) if es.isEmpty => None
        }.popPath
      else
        Result.successful(None)
    }

  implicit def javaOptionalConfigs[A: Configs]: Configs[ju.Optional[A]] =
    optionConfigs[A].map(_.fold(ju.Optional.empty[A]())(ju.Optional.of))

  implicit lazy val javaOptionalIntConfigs: Configs[ju.OptionalInt] =
    optionConfigs[Int].map(_.fold(ju.OptionalInt.empty())(ju.OptionalInt.of))

  implicit lazy val javaOptionalLongConfigs: Configs[ju.OptionalLong] =
    optionConfigs[Long].map(_.fold(ju.OptionalLong.empty())(ju.OptionalLong.of))

  implicit lazy val javaOptionalDoubleConfigs: Configs[ju.OptionalDouble] =
    optionConfigs[Double].map(_.fold(ju.OptionalDouble.empty())(ju.OptionalDouble.of))


  implicit def resultConfigs[A](implicit A: Configs[A]): Configs[Result[A]] =
    (c, p) => Result.successful(A.get(c, p))


  implicit def readStringConfigs[A](implicit A: FromString[A]): Configs[A] =
    Configs[String].rmap(A.read)


  private[this] def bigDecimal(expected: String): Configs[BigDecimal] =
    Configs.fromTry { (c, p) =>
      val s = c.getString(p)
      try BigDecimal(s) catch {
        case e: NumberFormatException =>
          throw new ConfigException.WrongType(c.origin(), p, expected, s"STRING value '$s'", e)
      }
    }

  private[this] def bigInt(expected: String): Configs[BigInt] =
    bigDecimal(expected).map(_.toBigInt)

  private[this] def integral[A](expected: String, valid: BigInt => Boolean, value: BigInt => A): Configs[A] =
    bigInt(expected).flatMap { n =>
      Configs.fromTry { (c, p) =>
        if (valid(n)) value(n)
        else throw new ConfigException.WrongType(c.origin(), p, expected, s"out-of-range value $n")
      }
    }

  implicit lazy val byteConfigs: Configs[Byte] =
    integral("byte (8-bit integer)", _.isValidByte, _.toByte)

  implicit lazy val javaByteConfigs: Configs[jl.Byte] =
    byteConfigs.asInstanceOf[Configs[jl.Byte]]

  implicit lazy val shortConfigs: Configs[Short] =
    integral("short (16-bit integer)", _.isValidShort, _.toShort)

  implicit lazy val javaShortConfigs: Configs[jl.Short] =
    shortConfigs.asInstanceOf[Configs[jl.Short]]

  implicit lazy val intConfigs: Configs[Int] =
    integral("int (32-bit integer)", _.isValidInt, _.toInt)

  implicit lazy val javaIntegerConfigs: Configs[jl.Integer] =
    intConfigs.asInstanceOf[Configs[jl.Integer]]

  implicit lazy val longConfigs: Configs[Long] =
    integral("long (64-bit integer)", _.isValidLong, _.toLong)

  implicit lazy val javaLongConfigs: Configs[jl.Long] =
    longConfigs.asInstanceOf[Configs[jl.Long]]


  implicit lazy val floatConfigs: Configs[Float] =
    Configs.fromTry(_.getDouble(_).toFloat)

  implicit lazy val javaFloatConfigs: Configs[jl.Float] =
    floatConfigs.asInstanceOf[Configs[jl.Float]]


  implicit lazy val doubleConfigs: Configs[Double] =
    Configs.fromTry(_.getDouble(_))

  implicit lazy val javaDoubleConfigs: Configs[jl.Double] =
    doubleConfigs.asInstanceOf[Configs[jl.Double]]


  implicit lazy val bigIntConfigs: Configs[BigInt] =
    bigInt("integer")

  implicit lazy val bigIntegerConfigs: Configs[jm.BigInteger] =
    bigIntConfigs.map(_.bigInteger)


  implicit lazy val bigDecimalConfigs: Configs[BigDecimal] =
    bigDecimal("decimal")

  implicit lazy val javaBigDecimalConfigs: Configs[jm.BigDecimal] =
    bigDecimalConfigs.map(_.bigDecimal)


  implicit lazy val booleanConfigs: Configs[Boolean] =
    Configs.fromTry(_.getBoolean(_))

  implicit lazy val javaBooleanConfigs: Configs[jl.Boolean] =
    booleanConfigs.asInstanceOf[Configs[jl.Boolean]]


  implicit lazy val charConfigs: Configs[Char] =
    Configs.fromTry { (c, p) =>
      val s = c.getString(p)
      if (s.length == 1) s(0)
      else throw new ConfigException.WrongType(c.origin(), p, "single BMP char", s"STRING value '$s'")
    }

  implicit lazy val charJListConfigs: Configs[ju.List[Char]] =
    Configs.fromTry((c, p) => ju.Arrays.asList(c.getString(p).toCharArray: _*))

  implicit lazy val javaCharConfigs: Configs[jl.Character] =
    charConfigs.asInstanceOf[Configs[jl.Character]]

  implicit lazy val javaCharListConfigs: Configs[ju.List[jl.Character]] =
    charJListConfigs.asInstanceOf[Configs[ju.List[jl.Character]]]


  implicit lazy val stringConfigs: Configs[String] =
    Configs.fromTry(_.getString(_))


  implicit lazy val javaDurationConfigs: Configs[jt.Duration] =
    Configs.fromTry(_.getDuration(_))

  implicit lazy val finiteDurationConfigs: Configs[FiniteDuration] =
    Configs.fromTry(_.getDuration(_, TimeUnit.NANOSECONDS)).map(Duration.fromNanos)

  implicit lazy val durationConfigs: Configs[Duration] =
    finiteDurationConfigs.orElse(Configs.fromTry { (c, p) =>
      c.getString(p) match {
        case "Infinity" | "+Infinity" => Duration.Inf
        case "-Infinity" => Duration.MinusInf
        case "Undefined" | "NaN" | "+NaN" | "-NaN" => Duration.Undefined
        case s => throw new ConfigException.BadValue(c.origin(), p, s"Could not parse duration '$s'")
      }
    })


  implicit lazy val configConfigs: Configs[Config] =
    new Configs[Config] {
      protected def get0(config: Config, path: String): Result[Config] =
        Result.Try(config.getConfig(path))

      override def extract(config: Config, key: String): Result[Config] =
        Result.successful(config)

      override def extractValue(value: ConfigValue, key: String): Result[Config] =
        value match {
          case co: ConfigObject => Result.successful(co.toConfig)
          case _ => super.extractValue(value, key)
        }
    }


  implicit lazy val configValueConfigs: Configs[ConfigValue] =
    new Configs[ConfigValue] {
      protected def get0(config: Config, path: String): Result[ConfigValue] =
        Result.Try(config.getValue(path))

      override def extract(config: Config, key: String): Result[ConfigValue] =
        Result.successful(config.root())

      override def extractValue(value: ConfigValue, key: String): Result[ConfigValue] =
        Result.successful(value)
    }

  implicit lazy val configListConfigs: Configs[ConfigList] =
    Configs.fromTry(_.getList(_))

  implicit lazy val configValueJListConfigs: Configs[ju.List[ConfigValue]] =
    configListConfigs.as[ju.List[ConfigValue]]


  implicit lazy val configObjectConfigs: Configs[ConfigObject] =
    Configs.fromTry(_.getObject(_))

  implicit lazy val configValueJMapConfigs: Configs[ju.Map[String, ConfigValue]] =
    configObjectConfigs.as[ju.Map[String, ConfigValue]]


  implicit lazy val memorySizeConfigs: Configs[MemorySize] =
    Configs.fromTry(_.getMemorySize(_))


  implicit lazy val javaPropertiesConfigs: Configs[ju.Properties] =
    Configs[ju.Map[String, String]].map { m =>
      val p = new ju.Properties()
      p.putAll(m)
      p
    }

  implicit def withOriginConfigs[A](implicit A: Configs[A]): Configs[(A, ConfigOrigin)] =
    (c, p) => A.get(c, p).flatMap { a =>
      try
        Result.successful((a, c.getValue(p).origin()))
      catch {
        case e: ConfigException.Null => Result.successful((a, e.origin()))
        case _: ConfigException.Missing => Result.failure(ConfigError(s"no origin for '$p'"))
      }
    }

}
