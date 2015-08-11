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

package com.github.kxbmap.configs.testkit

import com.typesafe.config.{ConfigException, ConfigValue}
import scalaz.Need

trait IsWrongType[A] {
  def check(a: Need[A]): Boolean
}

object IsWrongType {

  def apply[A](implicit A: IsWrongType[A]): IsWrongType[A] = A

  implicit def defaultIsWrongType[A]: IsWrongType[A] = default.asInstanceOf[IsWrongType[A]]

  private[this] final val default: IsWrongType[Any] = a =>
    intercept0(a.value) {
      case _: ConfigException.WrongType => true
    }

  implicit val configValueIsWrongType: IsWrongType[ConfigValue] = a => {
    a.value
    true
  }

}
