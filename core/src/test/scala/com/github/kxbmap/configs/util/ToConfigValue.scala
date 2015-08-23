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

import com.typesafe.config.{Config, ConfigValue, ConfigValueFactory}
import java.{util => ju, math => jm}
import scala.collection.JavaConverters._

trait ToConfigValue[A] {

  def toConfigValue(value: A): ConfigValue

  def contramap[B](f: B => A): ToConfigValue[B] = b => toConfigValue(f(b))

}

object ToConfigValue extends ToConfigValue0 {

  def apply[A](implicit v: ToConfigValue[A]): ToConfigValue[A] = v

  def fromMap[A](f: A => Map[String, ConfigValue]): ToConfigValue[A] =
    ToConfigValue[Map[String, ConfigValue]].contramap(f)


  implicit def traversableToConfigValue[F[_], A: ToConfigValue](implicit ev: F[A] <:< Traversable[A]): ToConfigValue[F[A]] =
    fa => ConfigValueFactory.fromAnyRef(fa.map(_.toConfigValue).toSeq.asJava)

  implicit def arrayToConfigValue[A: ToConfigValue]: ToConfigValue[Array[A]] =
    ToConfigValue[Seq[A]].contramap(_.toSeq)

  implicit def javaListToConfigValue[A: ToConfigValue]: ToConfigValue[ju.List[A]] =
    ToConfigValue[Seq[A]].contramap(_.asScala)

  implicit def javaStringMapToConfigValue[A: ToConfigValue]: ToConfigValue[ju.Map[String, A]] =
    ToConfigValue.fromMap(_.asScala.mapValues(_.toConfigValue).toMap)

  implicit def javaSymbolMapToConfigValue[A: ToConfigValue]: ToConfigValue[ju.Map[Symbol, A]] =
    ToConfigValue.fromMap(_.asScala.map(t => t._1.name -> t._2.toConfigValue).toMap)

  implicit def javaSetToConfigValue[A: ToConfigValue]: ToConfigValue[ju.Set[A]] =
    ToConfigValue[List[A]].contramap(_.asScala.toList)

  implicit def optionToConfigValue[A: ToConfigValue]: ToConfigValue[Option[A]] =
    o => ConfigValueFactory.fromAnyRef(o.map(_.toConfigValue).orNull)

  implicit def stringMapToConfigValue[A: ToConfigValue]: ToConfigValue[Map[String, A]] =
    m => ConfigValueFactory.fromAnyRef(m.mapValues(_.toConfigValue).asJava)

  implicit def symbolMapToConfigValue[A: ToConfigValue]: ToConfigValue[Map[Symbol, A]] =
    ToConfigValue[Map[String, A]].contramap(_.map(t => t._1.name -> t._2))

  implicit val bigIntToConfigValue: ToConfigValue[BigInt] =
    ToConfigValue[String].contramap(_.toString())

  implicit val bigIntegerToConfigValue: ToConfigValue[jm.BigInteger] =
    ToConfigValue[String].contramap(_.toString)

  implicit val bigDecimalToConfigValue: ToConfigValue[BigDecimal] =
    ToConfigValue[String].contramap(_.toString())

  implicit val javaBigDecimalToConfigValue: ToConfigValue[jm.BigDecimal] =
    ToConfigValue[String].contramap(_.toString)

  implicit val charToConfigValue: ToConfigValue[Char] =
    ToConfigValue[String].contramap(_.toString)

  implicit val javaCharacterToConfigValue: ToConfigValue[java.lang.Character] =
    ToConfigValue[String].contramap(_.toString)

  implicit val charJListToConfigValue: ToConfigValue[ju.List[Char]] =
    ToConfigValue[String].contramap(xs => new String(xs.asScala.toArray))

  implicit val javaCharacterJListToConfigValue: ToConfigValue[ju.List[java.lang.Character]] =
    charJListToConfigValue.contramap(_.asScala.map(Char.unbox).asJava)

  implicit def charTraversableToConfigValue[F[_]](implicit ev: F[Char] <:< Traversable[Char]): ToConfigValue[F[Char]] =
    fa => ConfigValueFactory.fromAnyRef(new String(fa.toArray))

  implicit val charArrayToConfigValue: ToConfigValue[Array[Char]] =
    ToConfigValue[String].contramap(new String(_))

  implicit val configToConfigValue: ToConfigValue[Config] =
    _.root()

}

trait ToConfigValue0 {

  private[this] final val _anyValue: ToConfigValue[Any] =
    ConfigValueFactory.fromAnyRef(_)

  implicit def anyToConfigValue[A]: ToConfigValue[A] =
    _anyValue.asInstanceOf[ToConfigValue[A]]

}
