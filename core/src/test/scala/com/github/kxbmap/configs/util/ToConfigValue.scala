/*
 * Copyright 2013-2015 Tsukasa Kitachi
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

package com.github.kxbmap.configs.util

import com.github.kxbmap.configs._
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import com.typesafe.config.{Config, ConfigValue}
import java.{lang => jl, math => jm, util => ju}
import scala.collection.JavaConverters._

trait ToConfigValue[A] {

  def toConfigValue(value: A): ConfigValue

  def contramap[B](f: B => A): ToConfigValue[B] = f(_) |> toConfigValue

}

object ToConfigValue {

  def apply[A](implicit v: ToConfigValue[A]): ToConfigValue[A] = v

  def fromMap[A](f: A => Map[String, ConfigValue]): ToConfigValue[A] =
    ToConfigValue[Map[String, ConfigValue]].contramap(f)


  private[this] final val _anyValue: ToConfigValue[Any] =
    fromAnyRef(_)

  implicit def anyToConfigValue[A]: ToConfigValue[A] =
    _anyValue.asInstanceOf[ToConfigValue[A]]


  implicit def javaCollectionToConfigValue[F[_], A: ToConfigValue](implicit ev: F[A] <:< ju.Collection[A]): ToConfigValue[F[A]] =
    ev(_).asScala.map(_.toConfigValue).toList.asJava |> fromAnyRef

  implicit def traversableToConfigValue[F[_], A: ToConfigValue](implicit ev: F[A] <:< Traversable[A]): ToConfigValue[F[A]] =
    ToConfigValue[ju.Collection[A]].contramap(_.toSeq.asJavaCollection)

  implicit def arrayToConfigValue[A: ToConfigValue]: ToConfigValue[Array[A]] =
    ToConfigValue[ju.Collection[A]].contramap(_.toSeq.asJavaCollection)

  implicit def javaStringMapToConfigValue[A: ToConfigValue]: ToConfigValue[ju.Map[String, A]] =
    _.asScala.mapValues(_.toConfigValue).asJava |> fromAnyRef

  implicit def javaSymbolMapToConfigValue[A: ToConfigValue]: ToConfigValue[ju.Map[Symbol, A]] =
    _.asScala.map(t => t._1.name -> t._2.toConfigValue).asJava |> fromAnyRef

  implicit def mapToConfigValue[M[_, _], A, B](implicit T: ToConfigValue[ju.Map[A, B]], ev: M[A, B] <:< collection.Map[A, B]): ToConfigValue[M[A, B]] =
    T.contramap(_.toMap.asJava)

  implicit def optionToConfigValue[A: ToConfigValue]: ToConfigValue[Option[A]] =
    _.map(_.toConfigValue).orNull |> fromAnyRef

  implicit val bigIntToConfigValue: ToConfigValue[BigInt] =
    _.toString |> fromAnyRef

  implicit val bigIntegerToConfigValue: ToConfigValue[jm.BigInteger] =
    _.toString |> fromAnyRef

  implicit val bigDecimalToConfigValue: ToConfigValue[BigDecimal] =
    _.toString |> fromAnyRef

  implicit val javaBigDecimalToConfigValue: ToConfigValue[jm.BigDecimal] =
    _.toString |> fromAnyRef

  implicit val charToConfigValue: ToConfigValue[Char] =
    _.toString |> fromAnyRef

  implicit val charJListToConfigValue: ToConfigValue[ju.List[Char]] =
    xs => fromAnyRef(new String(xs.asScala.toArray))

  implicit def charTraversableToConfigValue[F[_]](implicit ev: F[Char] <:< Traversable[Char]): ToConfigValue[F[Char]] =
    fa => fromAnyRef(new String(fa.toArray))

  implicit val javaCharacterToConfigValue: ToConfigValue[jl.Character] =
    charToConfigValue.contramap(_.charValue())

  implicit val javaCharacterJListToConfigValue: ToConfigValue[ju.List[jl.Character]] =
    charJListToConfigValue.contramap(_.asScala.map(Char.unbox).asJava)

  implicit val configToConfigValue: ToConfigValue[Config] =
    _.root()

}
