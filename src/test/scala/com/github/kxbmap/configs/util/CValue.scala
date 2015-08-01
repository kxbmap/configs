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

import com.typesafe.config.{ConfigValue, ConfigValueFactory}
import scala.collection.JavaConverters._

trait CValue[T] {

  def toAny(value: T): Any

  def toConfigValue(value: T): ConfigValue = ConfigValueFactory.fromAnyRef(toAny(value))

  def contramap[U](f: U => T): CValue[U] = u => CValue[T].toConfigValue(f(u))

}

object CValue extends Value0 {

  def apply[T](implicit v: CValue[T]): CValue[T] = v

  implicit def listCValue[T: CValue]: CValue[List[T]] =
    _.map(CValue[T].toConfigValue).asJava

  implicit def vectorCValue[T: CValue]: CValue[Vector[T]] =
    _.map(CValue[T].toConfigValue).asJava

  implicit def streamCValue[T: CValue]: CValue[Stream[T]] =
    _.map(CValue[T].toConfigValue).asJava

  implicit def arrayCValue[T: CValue]: CValue[Array[T]] =
    _.map(CValue[T].toConfigValue).toList.asJava

  implicit def javaListCValue[T: CValue]: CValue[java.util.List[T]] =
    _.asScala.map(CValue[T].toConfigValue).asJava

}

trait Value0 {

  private[this] final val _anyValue: CValue[Any] = v => v

  implicit def anyCValue[T]: CValue[T] = _anyValue.asInstanceOf[CValue[T]]

}
