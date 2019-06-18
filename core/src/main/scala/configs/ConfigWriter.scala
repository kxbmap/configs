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

import com.typesafe.config.ConfigValueFactory
import configs.internal.CollectionConverters._
import java.{lang => jl, math => jm, time => jt, util => ju}
import scala.annotation.compileTimeOnly
import scala.concurrent.duration._

trait ConfigWriter[A] {
  self =>

  def write(a: A): ConfigValue

  def append(map: Map[String, ConfigValue], key: String, a: A): Map[String, ConfigValue] =
    map.updated(key, write(a))

  final def contramap[B](f: B => A): ConfigWriter[B] =
    new ConfigWriter[B] {
      def write(b: B): ConfigValue = self.write(f(b))

      override def append(map: Map[String, ConfigValue], key: String, b: B): Map[String, ConfigValue] =
        self.append(map, key, f(b))
    }

  final def as[B <: A]: ConfigWriter[B] =
    this.asInstanceOf[ConfigWriter[B]]

}

object ConfigWriter extends ConfigWriterInstances {

  @inline
  def apply[A](implicit A: ConfigWriter[A]): ConfigWriter[A] = A


  def derive[A](implicit naming: ConfigKeyNaming[A]): ConfigWriter[A] =
    macro macros.ConfigWriterMacro.derive[A]

  def from[A](f: A => ConfigValue): ConfigWriter[A] =
    f(_)

  def by[A, B](f: A => B)(implicit B: ConfigWriter[B]): ConfigWriter[A] =
    B.contramap(f)

}


sealed abstract class ConfigWriterInstances3 {

  implicit def autoDeriveConfigWriter[A](implicit naming: ConfigKeyNaming[A]): ConfigWriter[A] =
    macro macros.ConfigWriterMacro.derive[A]

}

sealed abstract class ConfigWriterInstances2 extends ConfigWriterInstances3 {

  @compileTimeOnly(
    "cannot derive for `M[A, B]`: " +
      "`A` is not a StringConverter instance " +
      "and `B` is not a ConfigWriter instance")
  implicit def errorMapConfigWriter2[M[X, Y] <: collection.Map[X, Y], A, B]: ConfigWriter[M[A, B]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly(
    "cannot derive for `M[A, B]`: " +
      "`A` is not a StringConverter instance " +
      "and `B` is not a ConfigWriter instance")
  implicit def errorJavaMapConfigWriter2[M[X, Y] <: ju.Map[X, Y], A, B]: ConfigWriter[M[A, B]] =
    sys.error("compileTimeOnly")

}

sealed abstract class ConfigWriterInstances1 extends ConfigWriterInstances2 {

  @compileTimeOnly("cannot derive for `M[A, B]`: `A` is not a StringConverter instance")
  implicit def errorMapConfigWriter1[M[X, Y] <: collection.Map[X, Y], A, B: ConfigWriter]: ConfigWriter[M[A, B]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly("cannot derive for `M[A, B]`: `A` is not a StringConverter instance")
  implicit def errorJavaMapConfigWriter1[M[X, Y] <: ju.Map[X, Y], A, B: ConfigWriter]: ConfigWriter[M[A, B]] =
    sys.error("compileTimeOnly")

}

sealed abstract class ConfigWriterInstances0 extends ConfigWriterInstances1 {

  @compileTimeOnly("cannot derive for `F[A]`: `A` is not a ConfigWriter instance")
  implicit def errorIterableConfigWriter[F[X] <: Iterable[X], A]: ConfigWriter[F[A]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly("cannot derive for `M[A, B]`: `B` is not a ConfigWriter instance")
  implicit def errorMapConfigWriter0[M[X, Y] <: collection.Map[X, Y], A: StringConverter, B]: ConfigWriter[M[A, B]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly("cannot derive for `M[A, B]`: `B` is not a ConfigWriter instance")
  implicit def errorJavaMapConfigWriter0[M[X, Y] <: ju.Map[X, Y], A: StringConverter, B]: ConfigWriter[M[A, B]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly("cannot derive for `Option[A]`: `A` is not a ConfigWriter instance")
  implicit def errorOptionConfigWriter[A]: ConfigWriter[Option[A]] =
    sys.error("compileTimeOnly")

  @compileTimeOnly("cannot derive for `java.util.Optional[A]`: `A` is not a ConfigWriter instance")
  implicit def errorJavaOptionalConfigWriter[A]: ConfigWriter[ju.Optional[A]] =
    sys.error("compileTimeOnly")

}

sealed abstract class ConfigWriterInstances extends ConfigWriterInstances0 {

  private[this] val any: ConfigWriter[Any] = ConfigValueFactory.fromAnyRef(_)

  private def fromAny[A]: ConfigWriter[A] = any.asInstanceOf[ConfigWriter[A]]

  implicit val longConfigWriter: ConfigWriter[Long] = fromAny[Long]
  implicit val intConfigWriter: ConfigWriter[Int] = fromAny[Int]
  implicit val shortConfigWriter: ConfigWriter[Short] = fromAny[Short]
  implicit val byteConfigWriter: ConfigWriter[Byte] = fromAny[Byte]
  implicit val doubleConfigWriter: ConfigWriter[Double] = fromAny[Double]
  implicit val floatConfigWriter: ConfigWriter[Float] = fromAny[Float]
  implicit val booleanConfigWriter: ConfigWriter[Boolean] = fromAny[Boolean]

  implicit val javaLongConfigWriter: ConfigWriter[jl.Long] = fromAny[jl.Long]
  implicit val javaIntegerConfigWriter: ConfigWriter[jl.Integer] = fromAny[jl.Integer]
  implicit val javaShortConfigWriter: ConfigWriter[jl.Short] = fromAny[jl.Short]
  implicit val javaByteConfigWriter: ConfigWriter[jl.Byte] = fromAny[jl.Byte]
  implicit val javaDoubleConfigWriter: ConfigWriter[jl.Double] = fromAny[jl.Double]
  implicit val javaFloatConfigWriter: ConfigWriter[jl.Float] = fromAny[jl.Float]
  implicit val javaBooleanConfigWriter: ConfigWriter[jl.Boolean] = fromAny[jl.Boolean]

  implicit val stringConfigWriter: ConfigWriter[String] = fromAny[String]

  implicit val charConfigWriter: ConfigWriter[Char] =
    stringConfigWriter.contramap(String.valueOf)

  implicit val javaCharacterConfigWriter: ConfigWriter[jl.Character] =
    stringConfigWriter.contramap(_.toString)

  implicit val bigIntConfigWriter: ConfigWriter[BigInt] =
    stringConfigWriter.contramap(_.toString)

  implicit val bigDecimalConfigWriter: ConfigWriter[BigDecimal] =
    stringConfigWriter.contramap(_.toString)

  implicit val javaBigIntegerConfigWriter: ConfigWriter[jm.BigInteger] =
    stringConfigWriter.contramap(_.toString)

  implicit val javaBigDecimalConfigWriter: ConfigWriter[jm.BigDecimal] =
    stringConfigWriter.contramap(_.toString)


  implicit def toStringConfigWriter[A](implicit A: StringConverter[A]): ConfigWriter[A] =
    stringConfigWriter.contramap(A.toString)


  private[this] def durationString(length: Long, unit: TimeUnit): String = {
    val u = unit match {
      case DAYS => "d"
      case HOURS => "h"
      case MINUTES => "m"
      case SECONDS => "s"
      case MILLISECONDS => "ms"
      case MICROSECONDS => "us"
      case NANOSECONDS => "ns"
    }
    s"$length$u"
  }

  implicit val finiteDurationConfigWriter: ConfigWriter[FiniteDuration] =
    stringConfigWriter.contramap(d => durationString(d.length, d.unit))

  implicit val durationConfigWriter: ConfigWriter[Duration] =
    stringConfigWriter.contramap {
      case d: FiniteDuration => durationString(d.length, d.unit)
      case Duration.Inf => "Infinity"
      case Duration.MinusInf => "-Infinity"
      case _ => "NaN"
    }

  private[this] final val NanosPerMicro = 1000L
  private[this] final val NanosPerMilli = NanosPerMicro * 1000L
  private[this] final val NanosPerSecond = NanosPerMilli * 1000L
  private[this] final val NanosPerMinutes = NanosPerSecond * 60L
  private[this] final val NanosPerHours = NanosPerMinutes * 60L
  private[this] final val NanosPerDay = NanosPerHours * 24L

  implicit val javaDurationConfigWriter: ConfigWriter[jt.Duration] =
    stringConfigWriter.contramap(_.toNanos match {
      case ns if ns % NanosPerDay == 0 => durationString(ns / NanosPerDay, DAYS)
      case ns if ns % NanosPerHours == 0 => durationString(ns / NanosPerHours, HOURS)
      case ns if ns % NanosPerMinutes == 0 => durationString(ns / NanosPerMinutes, MINUTES)
      case ns if ns % NanosPerSecond == 0 => durationString(ns / NanosPerSecond, SECONDS)
      case ns if ns % NanosPerMilli == 0 => durationString(ns / NanosPerMilli, MILLISECONDS)
      case ns if ns % NanosPerMicro == 0 => durationString(ns / NanosPerMicro, MICROSECONDS)
      case ns => durationString(ns, NANOSECONDS)
    })


  implicit val configConfigWriter: ConfigWriter[Config] = _.root()
  implicit val configValueConfigWriter: ConfigWriter[ConfigValue] = v => v
  implicit val configListConfigWriter: ConfigWriter[ConfigList] = v => v
  implicit val configObjectConfigWriter: ConfigWriter[ConfigObject] = v => v

  implicit val configMemorySizeConfigWriter: ConfigWriter[ConfigMemorySize] =
    fromAny[ConfigMemorySize]


  implicit def iterableConfigWriter[F[X] <: Iterable[X], A](implicit A: ConfigWriter[A]): ConfigWriter[F[A]] =
    xs => ConfigList.fromSeq(xs.map(A.write).toSeq).value

  implicit def charIterableConfigWriter[F[X] <: Iterable[X]]: ConfigWriter[F[Char]] =
    ConfigWriter.by(cs => new String(cs.toArray))

  implicit def javaCharacterIterableConfigWriter[F[X] <: Iterable[X]]: ConfigWriter[F[jl.Character]] =
    ConfigWriter.by(_.map(_.charValue()))

  implicit def arrayConfigWriter[A](implicit A: ConfigWriter[Iterable[A]]): ConfigWriter[Array[A]] =
    A.contramap(a => a)

  implicit def javaIterableConfigWriter[F[X] <: jl.Iterable[X], A](implicit A: ConfigWriter[Iterable[A]]): ConfigWriter[F[A]] =
    A.contramap(_.asScala)


  implicit def mapConfigWriter[M[X, Y] <: collection.Map[X, Y], A, B](implicit A: StringConverter[A], B: ConfigWriter[B]): ConfigWriter[M[A, B]] =
    m => ConfigObject.fromMap(m.map(t => (A.toString(t._1), B.write(t._2))).toMap).value

  implicit def javaMapConfigWriter[M[X, Y] <: ju.Map[X, Y], A: StringConverter, B: ConfigWriter]: ConfigWriter[M[A, B]] =
    ConfigWriter.by(_.asScala)

  implicit val javaPropertiesConfigWriter: ConfigWriter[ju.Properties] =
    mapConfigWriter[collection.Map, String, String].contramap(_.asScala)


  implicit def optionConfigWriter[A](implicit A: ConfigWriter[A]): ConfigWriter[Option[A]] =
    new ConfigWriter[Option[A]] {
      def write(a: Option[A]): ConfigValue =
        a.fold(ConfigValue.Null)(A.write)

      override def append(map: Map[String, ConfigValue], key: String, a: Option[A]): Map[String, ConfigValue] =
        a.fold(map)(v => map.updated(key, A.write(v)))
    }

  implicit def javaOptionalConfigWriter[A: ConfigWriter]: ConfigWriter[ju.Optional[A]] =
    optionConfigWriter[A].contramap(o => if (o.isPresent) Some(o.get) else None)

  implicit val javaOptionalIntConfigWriter: ConfigWriter[ju.OptionalInt] =
    optionConfigWriter[Int].contramap(o => if (o.isPresent) Some(o.getAsInt) else None)

  implicit val javaOptionalLongConfigWriter: ConfigWriter[ju.OptionalLong] =
    optionConfigWriter[Long].contramap(o => if (o.isPresent) Some(o.getAsLong) else None)

  implicit val javaOptionalDoubleConfigWriter: ConfigWriter[ju.OptionalDouble] =
    optionConfigWriter[Double].contramap(o => if (o.isPresent) Some(o.getAsDouble) else None)

}
