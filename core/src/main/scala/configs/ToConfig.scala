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
import scala.collection.breakOut
import scala.collection.convert.decorateAsScala._
import scala.concurrent.duration._

trait ToConfig[A] {
  self =>

  def toValue(a: A): ConfigValue

  def append(map: Map[String, ConfigValue], key: String, a: A): Map[String, ConfigValue] =
    map.updated(key, toValue(a))

  def contramap[B](f: B => A): ToConfig[B] =
    new ToConfig[B] {
      def toValue(a: B): ConfigValue =
        self.toValue(f(a))
      override def append(map: Map[String, ConfigValue], key: String, a: B): Map[String, ConfigValue] =
        self.append(map, key, f(a))
    }

}

object ToConfig extends ToConfigInstances {

  @inline
  def apply[A](implicit A: ToConfig[A]): ToConfig[A] = A


  def derive[A]: ToConfig[A] =
    macro macros.ToConfigMacro.derive[A]

  def from[A](f: A => ConfigValue): ToConfig[A] =
    f(_)

  def by[A, B](f: A => B)(implicit B: ToConfig[B]): ToConfig[A] =
    B.contramap(f)

}


sealed abstract class ToConfigInstances0 {

  implicit def autoDeriveToConfig[A]: ToConfig[A] =
    macro macros.ToConfigMacro.derive[A]

}

sealed abstract class ToConfigInstances extends ToConfigInstances0 {

  private[this] val any: ToConfig[Any] = ConfigValue.from
  def fromAny[A]: ToConfig[A] = any.asInstanceOf[ToConfig[A]]

  implicit val longToConfig: ToConfig[Long] = fromAny[Long]
  implicit val intToConfig: ToConfig[Int] = fromAny[Int]
  implicit val shortToConfig: ToConfig[Short] = fromAny[Short]
  implicit val byteToConfig: ToConfig[Byte] = fromAny[Byte]
  implicit val doubleToConfig: ToConfig[Double] = fromAny[Double]
  implicit val floatToConfig: ToConfig[Float] = fromAny[Float]
  implicit val booleanToConfig: ToConfig[Boolean] = fromAny[Boolean]

  implicit val javaLongToConfig: ToConfig[jl.Long] = fromAny[jl.Long]
  implicit val javaIntegerToConfig: ToConfig[jl.Integer] = fromAny[jl.Integer]
  implicit val javaShortToConfig: ToConfig[jl.Short] = fromAny[jl.Short]
  implicit val javaByteToConfig: ToConfig[jl.Byte] = fromAny[jl.Byte]
  implicit val javaDoubleToConfig: ToConfig[jl.Double] = fromAny[jl.Double]
  implicit val javaFloatToConfig: ToConfig[jl.Float] = fromAny[jl.Float]
  implicit val javaBooleanToConfig: ToConfig[jl.Boolean] = fromAny[jl.Boolean]

  implicit val stringToConfig: ToConfig[String] = fromAny[String]

  implicit lazy val charToConfig: ToConfig[Char] = ToConfig.by(String.valueOf)
  implicit lazy val javaCharacterToConfig: ToConfig[jl.Character] = ToConfig.by(_.toString)

  implicit lazy val bigIntToConfig: ToConfig[BigInt] = ToConfig.by(_.toString)
  implicit lazy val bigDecimalToConfig: ToConfig[BigDecimal] = ToConfig.by(_.toString)
  implicit lazy val javaBigIntegerToConfig: ToConfig[jm.BigInteger] = ToConfig.by(_.toString)
  implicit lazy val javaBigDecimalToConfig: ToConfig[jm.BigDecimal] = ToConfig.by(_.toString)


  implicit def showStringToConfig[A](implicit A: FromString[A]): ToConfig[A] =
    ToConfig.by(A.show)


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

  implicit lazy val finiteDurationToConfig: ToConfig[FiniteDuration] =
    ToConfig.by(d => durationString(d.length, d.unit))

  implicit lazy val durationToConfig: ToConfig[Duration] =
    ToConfig.by {
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

  implicit lazy val javaDurationToConfig: ToConfig[jt.Duration] =
    ToConfig.by(_.toNanos match {
      case ns if ns % NanosPerDay == 0 => durationString(ns / NanosPerDay, DAYS)
      case ns if ns % NanosPerHours == 0 => durationString(ns / NanosPerHours, HOURS)
      case ns if ns % NanosPerMinutes == 0 => durationString(ns / NanosPerMinutes, MINUTES)
      case ns if ns % NanosPerSecond == 0 => durationString(ns / NanosPerSecond, SECONDS)
      case ns if ns % NanosPerMilli == 0 => durationString(ns / NanosPerMilli, MILLISECONDS)
      case ns if ns % NanosPerMicro == 0 => durationString(ns / NanosPerMicro, MICROSECONDS)
      case ns => durationString(ns, NANOSECONDS)
    })


  implicit lazy val configToConfig: ToConfig[Config] = _.root()
  implicit lazy val configValueToConfig: ToConfig[ConfigValue] = v => v
  implicit lazy val configListToConfig: ToConfig[ConfigList] = v => v
  implicit lazy val configObjectToConfig: ToConfig[ConfigObject] = v => v

  implicit val memorySizeToConfig: ToConfig[MemorySize] =
    fromAny[MemorySize]


  implicit def iterableToConfig[F[X] <: Iterable[X], A](implicit A: ToConfig[A]): ToConfig[F[A]] =
    xs => ConfigList.from(xs.map(A.toValue)(breakOut))

  implicit def charIterableToConfig[F[X] <: Iterable[X]]: ToConfig[F[Char]] =
    ToConfig.by(cs => new String(cs.toArray))

  implicit def javaCharacterIterableToConfig[F[X] <: Iterable[X]]: ToConfig[F[jl.Character]] =
    ToConfig.by(_.map(_.charValue()))

  implicit def arrayToConfig[A](implicit A: ToConfig[Iterable[A]]): ToConfig[Array[A]] =
    A.contramap(a => a)

  implicit def javaIterableToConfig[F[X] <: jl.Iterable[X], A](implicit A: ToConfig[Iterable[A]]): ToConfig[F[A]] =
    A.contramap(_.asScala)


  implicit def mapToConfig[M[X, Y] <: collection.Map[X, Y], A, B](implicit A: FromString[A], B: ToConfig[B]): ToConfig[M[A, B]] =
    m => ConfigObject.from(m.map(t => (A.show(t._1), B.toValue(t._2)))(breakOut))

  implicit def javaMapToConfig[M[X, Y] <: ju.Map[X, Y], A, B](implicit A: FromString[A], B: ToConfig[B]): ToConfig[M[A, B]] =
    ToConfig.by(_.asScala)

  implicit lazy val javaPropertiesToConfig: ToConfig[ju.Properties] =
    ToConfig.by(_.asScala)


  implicit def optionToConfig[A](implicit A: ToConfig[A]): ToConfig[Option[A]] =
    new ToConfig[Option[A]] {
      def toValue(a: Option[A]): ConfigValue =
        a.fold(ConfigValue.Null)(A.toValue)

      override def append(map: Map[String, ConfigValue], key: String, a: Option[A]): Map[String, ConfigValue] =
        a.fold(map)(v => map.updated(key, A.toValue(v)))
    }

  implicit def javaOptionalToConfig[A](implicit A: ToConfig[A]): ToConfig[ju.Optional[A]] =
    ToConfig.by(o => if (o.isPresent) Some(o.get) else None)

  implicit lazy val javaOptionalIntToConfig: ToConfig[ju.OptionalInt] =
    ToConfig.by(o => if (o.isPresent) Some(o.getAsInt) else None)

  implicit lazy val javaOptionalLongToConfig: ToConfig[ju.OptionalLong] =
    ToConfig.by(o => if (o.isPresent) Some(o.getAsLong) else None)

  implicit lazy val javaOptionalDoubleToConfig: ToConfig[ju.OptionalDouble] =
    ToConfig.by(o => if (o.isPresent) Some(o.getAsDouble) else None)

}
