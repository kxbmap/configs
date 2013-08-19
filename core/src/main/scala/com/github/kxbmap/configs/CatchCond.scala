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
import scala.util.control.NonFatal

object CatchCond {

  def missing: CatchCond          = Implicit.missing
  def configException: CatchCond  = Implicit.configException
  def nonFatal: CatchCond         = Implicit.nonFatal

  object Implicit {
    implicit lazy val missing: CatchCond = {
      case _: ConfigException.Missing => true
      case _                          => false
    }

    implicit lazy val configException: CatchCond = {
      case _: ConfigException => true
      case _                  => false
    }

    implicit lazy val nonFatal: CatchCond = NonFatal.apply
  }
}
