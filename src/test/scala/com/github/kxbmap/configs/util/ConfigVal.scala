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
import scala.collection.JavaConverters._

trait ConfigVal[A] {

  def rawValue(value: A): Any

  def configValue(value: A): ConfigValue = ConfigValueFactory.fromAnyRef(rawValue(value))

  def contramap[B](f: B => A): ConfigVal[B] = b => configValue(f(b))

}

object ConfigVal extends Value0 {

  def apply[A](implicit v: ConfigVal[A]): ConfigVal[A] = v

  def asMap[A](f: A => Map[String, ConfigValue]): ConfigVal[A] =
    ConfigVal[Map[String, ConfigValue]].contramap(f)


  implicit def traversableConfigVal[F[_], A: ConfigVal](implicit ev: F[A] <:< Traversable[A]): ConfigVal[F[A]] =
    _.map(_.configValue).toSeq.asJava

  implicit def arrayConfigVal[A: ConfigVal]: ConfigVal[Array[A]] =
    _.map(_.configValue).toSeq.asJava

  implicit def javaListConfigVal[A: ConfigVal]: ConfigVal[java.util.List[A]] =
    _.asScala.configValue

  implicit def optionConfigVal[A: ConfigVal]: ConfigVal[Option[A]] =
    _.map(_.configValue).orNull

  implicit def stringMapConfigVal[A: ConfigVal]: ConfigVal[Map[String, A]] =
    _.mapValues(_.configValue).asJava

  implicit val configConfigVal: ConfigVal[Config] =
    _.root()

}

trait Value0 {

  private[this] final val _anyValue: ConfigVal[Any] = v => v

  implicit def anyConfigVal[A]: ConfigVal[A] = _anyValue.asInstanceOf[ConfigVal[A]]

}
