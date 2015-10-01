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

  private[configs] implicit class PipeOps[A](private val self: A) extends AnyVal {

    def |>[B](f: A => B): B = f(self)

  }


  @deprecated("Use Configs[A] instead", "0.3.0")
  type AtPath[A] = Configs[A]


  @deprecated("Use ConfigOps instead", "0.3.0")
  implicit class EnrichTypesafeConfig(val c: Config) extends AnyVal {

    @deprecated("import com.github.kxbmap.configs.syntax._ instead", "0.3.0")
    def extract[A: Configs]: A =
      new syntax.ConfigOps(c).extract[A]

    @deprecated("import com.github.kxbmap.configs.syntax._ instead", "0.3.0")
    def get[A: Configs](path: String): A =
      new syntax.ConfigOps(c).get[A](path)

    @deprecated("import com.github.kxbmap.configs.syntax._ instead", "0.3.0")
    def opt[A: Configs](path: String): Option[A] =
      new syntax.ConfigOps(c).getOpt[A](path)

    @deprecated("import com.github.kxbmap.configs.syntax._ instead", "0.3.0")
    def getOrElse[A: Configs](path: String, default: => A): A =
      new syntax.ConfigOps(c).getOrElse(path, default)

  }

}
