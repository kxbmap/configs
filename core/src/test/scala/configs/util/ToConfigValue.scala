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

package configs.util

import com.typesafe.config.ConfigValueFactory.fromAnyRef
import com.typesafe.config.{Config, ConfigMemorySize, ConfigValue}
import configs._
import java.{lang => jl, time => jt, util => ju}
import scala.collection.convert.decorateAll._

trait ToConfigValue[A] {

  def toConfigValue(value: A): ConfigValue

  def contramap[B](f: B => A): ToConfigValue[B] = f(_) |> toConfigValue

}

object ToConfigValue {

  def apply[A](implicit v: ToConfigValue[A]): ToConfigValue[A] = v

  def fromMap[A](f: A => Map[String, ConfigValue]): ToConfigValue[A] =
    ToConfigValue[Map[String, ConfigValue]].contramap(f)


  private[this] final val any: ToConfigValue[Any] =
    _.toString |> fromAnyRef

  implicit def anyToConfigValue[A]: ToConfigValue[A] =
    any.asInstanceOf[ToConfigValue[A]]


  implicit def configValueToConfigValue[A <: ConfigValue]: ToConfigValue[A] =
    identity(_)

  
  implicit val javaIntegerToConfigValue: ToConfigValue[jl.Integer] =
    fromAnyRef(_)
  
  implicit val javaLongToConfigValue: ToConfigValue[jl.Long] =
    fromAnyRef(_)

  implicit val javaDoubleToConfigValue: ToConfigValue[jl.Double] =
    fromAnyRef(_)

  implicit val javaByteToConfigValue: ToConfigValue[jl.Byte] =
    javaIntegerToConfigValue.contramap(_.intValue())

  implicit val javaShortToConfigValue: ToConfigValue[jl.Short] =
    javaIntegerToConfigValue.contramap(_.intValue())

  implicit val javaFloatToConfigValue: ToConfigValue[jl.Float] =
    javaDoubleToConfigValue.contramap(_.doubleValue())

  implicit val byteToConfigValue: ToConfigValue[Byte] =
    javaIntegerToConfigValue.contramap(Int.box(_))

  implicit val shortToConfigValue: ToConfigValue[Short] =
    javaIntegerToConfigValue.contramap(Int.box(_))

  implicit val intToConfigValue: ToConfigValue[Int] =
    javaIntegerToConfigValue.asInstanceOf[ToConfigValue[Int]]

  implicit val longToConfigValue: ToConfigValue[Long] =
    javaLongToConfigValue.asInstanceOf[ToConfigValue[Long]]
  
  implicit val floatToConfigValue: ToConfigValue[Float] =
    javaDoubleToConfigValue.contramap(Double.box(_))

  implicit val doubleToConfigValue: ToConfigValue[Double] =
    javaDoubleToConfigValue.asInstanceOf[ToConfigValue[Double]]

  
  implicit val configMemorySizeToConfigValue: ToConfigValue[ConfigMemorySize] =
    fromAnyRef(_)

  implicit val javaDurationToConfigValue: ToConfigValue[jt.Duration] =
    ToConfigValue[String].contramap(d => s"${d.toNanos}ns")

  
  implicit def javaIterableToConfigValue[F[_], A: ToConfigValue](implicit ev: F[A] <:< jl.Iterable[A]): ToConfigValue[F[A]] =
    ev(_).asScala.map(_.toConfigValue).asJava |> fromAnyRef

  implicit def traversableToConfigValue[F[_], A: ToConfigValue](implicit ev: F[A] <:< Traversable[A]): ToConfigValue[F[A]] =
    ToConfigValue[jl.Iterable[A]].contramap(_.toIterable.asJava)

  implicit def arrayToConfigValue[A: ToConfigValue]: ToConfigValue[Array[A]] =
    ToConfigValue[jl.Iterable[A]].contramap(_.toIterable.asJava)

  implicit def javaStringMapToConfigValue[A: ToConfigValue]: ToConfigValue[ju.Map[String, A]] =
    _.asScala.mapValues(_.toConfigValue).asJava |> fromAnyRef

  implicit def javaSymbolMapToConfigValue[A: ToConfigValue]: ToConfigValue[ju.Map[Symbol, A]] =
    _.asScala.map(t => t._1.name -> t._2.toConfigValue).asJava |> fromAnyRef

  implicit def mapToConfigValue[M[_, _], A, B](implicit T: ToConfigValue[ju.Map[A, B]], ev: M[A, B] <:< collection.Map[A, B]): ToConfigValue[M[A, B]] =
    T.contramap(_.toMap.asJava)

  implicit def optionToConfigValue[A: ToConfigValue]: ToConfigValue[Option[A]] =
    _.map(_.toConfigValue).orNull |> fromAnyRef

  implicit val charJListToConfigValue: ToConfigValue[ju.List[Char]] =
    xs => fromAnyRef(new String(xs.asScala.toArray))

  implicit def charTraversableToConfigValue[F[_]](implicit ev: F[Char] <:< Traversable[Char]): ToConfigValue[F[Char]] =
    fa => fromAnyRef(new String(fa.toArray))

  implicit val javaCharacterToConfigValue: ToConfigValue[jl.Character] =
    ToConfigValue[Char].contramap(_.charValue())

  implicit val javaCharacterJListToConfigValue: ToConfigValue[ju.List[jl.Character]] =
    charJListToConfigValue.contramap(_.asScala.map(Char.unbox).asJava)

  implicit val configToConfigValue: ToConfigValue[Config] =
    _.root()

}
