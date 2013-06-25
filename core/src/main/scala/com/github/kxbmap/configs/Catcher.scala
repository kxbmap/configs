/*
 * Copyright 2013 Tsukasa Kitachi
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

package com.github.kxbmap.configs

import com.typesafe.config.ConfigException
import scala.util.control.Exception

object Catcher {

  @inline def apply[T: Catcher]: Catcher[T] = implicitly[Catcher[T]]

  def missing[T]: Catcher[T] = Implicits.missing[T]
  def configException[T]: Catcher[T] = Implicits.configException[T]
  def nonFatal[T]: Catcher[T] = Implicits.nonFatal[T]

  object Implicits {
    implicit def missing[T]: Catcher[T] = {
      case x: ConfigException.Missing => throw x
    }

    implicit def configException[T]: Catcher[T] = {
      case x: ConfigException => throw x
    }

    implicit def nonFatal[T]: Catcher[T] = Exception.nonFatalCatcher[T]
  }

}
