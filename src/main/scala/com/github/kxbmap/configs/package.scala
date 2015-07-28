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

package com.github.kxbmap

import com.typesafe.config.Config


package object configs {

  @deprecated("Use Configs[T]", "0.3.0")
  type AtPath[T] = Configs[T]


  final implicit class EnrichTypesafeConfig(val c: Config) extends AnyVal {

    def extract[T: Configs]: T = Configs[T].extract(c)

    def get[T: Configs](path: String): T = Configs[T].get(c, path)

    def opt[T: Configs](path: String): Option[T] = get[Option[T]](path)

    def getOrElse[T: Configs](path: String, default: => T): T = opt[T](path).getOrElse(default)
  }

}
