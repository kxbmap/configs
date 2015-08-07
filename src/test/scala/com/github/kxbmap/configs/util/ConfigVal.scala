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
import java.{util => ju}
import scala.collection.JavaConverters._

trait ConfigVal[A] {

  def configValue(value: A): ConfigValue

  def contramap[B](f: B => A): ConfigVal[B] = b => configValue(f(b))

}

object ConfigVal extends Value0 {

  def apply[A](implicit v: ConfigVal[A]): ConfigVal[A] = v

  def fromMap[A](f: A => Map[String, ConfigValue]): ConfigVal[A] =
    ConfigVal[Map[String, ConfigValue]].contramap(f)


  implicit def traversableConfigVal[F[_], A: ConfigVal](implicit ev: F[A] <:< Traversable[A]): ConfigVal[F[A]] =
    fa => ConfigValueFactory.fromAnyRef(fa.map(_.cv).toSeq.asJava)

  implicit def arrayConfigVal[A: ConfigVal]: ConfigVal[Array[A]] =
    ConfigVal[Seq[A]].contramap(_.toSeq)

  implicit def javaListConfigVal[A: ConfigVal]: ConfigVal[ju.List[A]] =
    ConfigVal[Seq[A]].contramap(_.asScala)

  implicit def javaMapConfigVal[A: ConfigVal]: ConfigVal[ju.Map[String, A]] =
    ConfigVal.fromMap(_.asScala.mapValues(_.cv).toMap)

  implicit def javaSetConfigVal[A: ConfigVal]: ConfigVal[ju.Set[A]] =
    ConfigVal[List[A]].contramap(_.asScala.toList)

  implicit def optionConfigVal[A: ConfigVal]: ConfigVal[Option[A]] =
    o => ConfigValueFactory.fromAnyRef(o.map(_.cv).orNull)

  implicit def stringMapConfigVal[A: ConfigVal]: ConfigVal[Map[String, A]] =
    m => ConfigValueFactory.fromAnyRef(m.mapValues(_.cv).asJava)

  implicit def symbolMapConfigVal[A: ConfigVal]: ConfigVal[Map[Symbol, A]] =
    ConfigVal[Map[String, A]].contramap(_.map(t => t._1.name -> t._2))

  implicit val charConfigVal: ConfigVal[Char] =
    ConfigVal[Int].contramap(_.toInt)

  implicit val javaCharacterConfigVal: ConfigVal[java.lang.Character] =
    ConfigVal[Int].contramap(_.charValue())

  implicit val configConfigVal: ConfigVal[Config] =
    _.root()

}

trait Value0 {

  private[this] final val _anyValue: ConfigVal[Any] = ConfigValueFactory.fromAnyRef

  implicit def anyConfigVal[A]: ConfigVal[A] = _anyValue.asInstanceOf[ConfigVal[A]]

}
