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

package com.github.kxbmap.configs.instance

import com.github.kxbmap.configs.Configs
import com.typesafe.config.ConfigUtil
import scala.collection.JavaConversions._

trait MapConfigs {

  private def mapConfigs[A, B: Configs](key: String => A): Configs[Map[A, B]] = Configs.onPath { c =>
    c.root().keysIterator.map(k => key(k) -> Configs[B].get(c, ConfigUtil.quoteString(k))).toMap
  }

  implicit def stringMapConfigs[A: Configs]: Configs[Map[String, A]] = mapConfigs(identity)

  implicit def symbolMapConfigs[A: Configs]: Configs[Map[Symbol, A]] = mapConfigs(Symbol.apply)

}
