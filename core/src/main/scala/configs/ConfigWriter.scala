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

import java.{lang => jl, math => jm, time => jt, util => ju}
import scala.collection.JavaConverters._
import scala.collection.breakOut
import scala.concurrent.duration._

trait ConfigWriter[A] {

  def write(a: A): ConfigValue

  def append(map: Map[String, ConfigValue], key: String, a: A): Map[String, ConfigValue] =
    map.updated(key, write(a))

  def contramap[B](f: B => A): ConfigWriter[B] =
    b => write(f(b))

}

object ConfigWriter extends ConfigWriterInstances {

  @inline
  def apply[A](implicit A: ConfigWriter[A]): ConfigWriter[A] = A


  def derive[A]: ConfigWriter[A] =
    macro macros.ConfigWriterMacro.derive[A]

  def from[A](f: A => ConfigValue): ConfigWriter[A] =
    f(_)

  def by[A, B](f: A => B)(implicit B: ConfigWriter[B]): ConfigWriter[A] =
    B.contramap(f)

}


sealed abstract class ConfigWriterInstances0 {

  implicit def autoDeriveConfigWriter[A]: ConfigWriter[A] =
    macro macros.ConfigWriterMacro.derive[A]

}

sealed abstract class ConfigWriterInstances extends ConfigWriterInstances0 {

  private[this] val any: ConfigWriter[Any] = ConfigValue.from
  def fromAny[A]: ConfigWriter[A] = any.asInstanceOf[ConfigWriter[A]]

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

  implicit lazy val charConfigWriter: ConfigWriter[Char] = ConfigWriter.by(String.valueOf)
  implicit lazy val javaCharacterConfigWriter: ConfigWriter[jl.Character] = ConfigWriter.by(_.toString)

  implicit lazy val bigIntConfigWriter: ConfigWriter[BigInt] = ConfigWriter.by(_.toString)
  implicit lazy val bigDecimalConfigWriter: ConfigWriter[BigDecimal] = ConfigWriter.by(_.toString)
  implicit lazy val javaBigIntegerConfigWriter: ConfigWriter[jm.BigInteger] = ConfigWriter.by(_.toString)
  implicit lazy val javaBigDecimalConfigWriter: ConfigWriter[jm.BigDecimal] = ConfigWriter.by(_.toString)


  implicit def showStringConfigWriter[A](implicit A: StringConverter[A]): ConfigWriter[A] =
    ConfigWriter.by(A.to)


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

  implicit lazy val finiteDurationConfigWriter: ConfigWriter[FiniteDuration] =
    ConfigWriter.by(d => durationString(d.length, d.unit))

  implicit lazy val durationConfigWriter: ConfigWriter[Duration] =
    ConfigWriter.by {
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

  implicit lazy val javaDurationConfigWriter: ConfigWriter[jt.Duration] =
    ConfigWriter.by(_.toNanos match {
      case ns if ns % NanosPerDay == 0 => durationString(ns / NanosPerDay, DAYS)
      case ns if ns % NanosPerHours == 0 => durationString(ns / NanosPerHours, HOURS)
      case ns if ns % NanosPerMinutes == 0 => durationString(ns / NanosPerMinutes, MINUTES)
      case ns if ns % NanosPerSecond == 0 => durationString(ns / NanosPerSecond, SECONDS)
      case ns if ns % NanosPerMilli == 0 => durationString(ns / NanosPerMilli, MILLISECONDS)
      case ns if ns % NanosPerMicro == 0 => durationString(ns / NanosPerMicro, MICROSECONDS)
      case ns => durationString(ns, NANOSECONDS)
    })


  implicit lazy val configConfigWriter: ConfigWriter[Config] = _.root()
  implicit lazy val configValueConfigWriter: ConfigWriter[ConfigValue] = v => v
  implicit lazy val configListConfigWriter: ConfigWriter[ConfigList] = v => v
  implicit lazy val configObjectConfigWriter: ConfigWriter[ConfigObject] = v => v

  implicit val configMemorySizeConfigWriter: ConfigWriter[ConfigMemorySize] =
    fromAny[ConfigMemorySize]


  implicit def iterableConfigWriter[F[X] <: Iterable[X], A](implicit A: ConfigWriter[A]): ConfigWriter[F[A]] =
    xs => ConfigList.from(xs.map(A.write)(breakOut))

  implicit def charIterableConfigWriter[F[X] <: Iterable[X]]: ConfigWriter[F[Char]] =
    ConfigWriter.by(cs => new String(cs.toArray))

  implicit def javaCharacterIterableConfigWriter[F[X] <: Iterable[X]]: ConfigWriter[F[jl.Character]] =
    ConfigWriter.by(_.map(_.charValue()))

  implicit def arrayConfigWriter[A](implicit A: ConfigWriter[Iterable[A]]): ConfigWriter[Array[A]] =
    A.contramap(a => a)

  implicit def javaIterableConfigWriter[F[X] <: jl.Iterable[X], A](implicit A: ConfigWriter[Iterable[A]]): ConfigWriter[F[A]] =
    A.contramap(_.asScala)


  implicit def mapConfigWriter[M[X, Y] <: collection.Map[X, Y], A, B](implicit A: StringConverter[A], B: ConfigWriter[B]): ConfigWriter[M[A, B]] =
    m => ConfigObject.from(m.map(t => (A.to(t._1), B.write(t._2)))(breakOut))

  implicit def javaMapConfigWriter[M[X, Y] <: ju.Map[X, Y], A: StringConverter, B: ConfigWriter]: ConfigWriter[M[A, B]] =
    ConfigWriter.by(_.asScala)

  implicit lazy val javaPropertiesConfigWriter: ConfigWriter[ju.Properties] =
    ConfigWriter.by(_.asScala)


  implicit def optionConfigWriter[A](implicit A: ConfigWriter[A]): ConfigWriter[Option[A]] =
    new ConfigWriter[Option[A]] {
      def write(a: Option[A]): ConfigValue =
        a.fold(ConfigValue.Null)(A.write)

      override def append(map: Map[String, ConfigValue], key: String, a: Option[A]): Map[String, ConfigValue] =
        a.fold(map)(v => map.updated(key, A.write(v)))
    }

  implicit def javaOptionalConfigWriter[A](implicit A: ConfigWriter[A]): ConfigWriter[ju.Optional[A]] =
    ConfigWriter.by(o => if (o.isPresent) Some(o.get) else None)

  implicit lazy val javaOptionalIntConfigWriter: ConfigWriter[ju.OptionalInt] =
    ConfigWriter.by(o => if (o.isPresent) Some(o.getAsInt) else None)

  implicit lazy val javaOptionalLongConfigWriter: ConfigWriter[ju.OptionalLong] =
    ConfigWriter.by(o => if (o.isPresent) Some(o.getAsLong) else None)

  implicit lazy val javaOptionalDoubleConfigWriter: ConfigWriter[ju.OptionalDouble] =
    ConfigWriter.by(o => if (o.isPresent) Some(o.getAsDouble) else None)

}
