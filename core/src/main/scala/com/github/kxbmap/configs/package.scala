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

package com.github.kxbmap.configs

import com.typesafe.config.Config
import scala.annotation.implicitNotFound


@implicitNotFound("import com.github.kxbmap.configs.simple._ or com.github.kxbmap.configs.auto._ required")
final class RequireImport private()

object RequireImport {
  sealed trait Instance {
    implicit val requireImport: RequireImport = new RequireImport
  }
}


object `package` {

  object simple
    extends DefaultConfigsInstances
    with RequireImport.Instance

  object auto
    extends DefaultConfigsInstances
    with MacroConfigsInstances
    with RequireImport.Instance


  object syntax {

    implicit class ConfigOps(private val self: Config) extends AnyVal {

      import simple._

      def extract[A: Configs]: A =
        Configs[A].extract(self)

      def get[A: Configs](path: String): A =
        Configs[A].get(self, path)

      def getOpt[A: Configs](path: String): Option[A] =
        get[Option[A]](path)

      @deprecated("Use getOpt instead", "0.3.0")
      def opt[A: Configs](path: String): Option[A] =
        get[Option[A]](path)

      def getOrElse[A: Configs](path: String, default: => A): A =
        getOpt[A](path).getOrElse(default)

    }

  }


  private[configs] implicit class PipeOps[A](private val self: A) extends AnyVal {

    def |>[B](f: A => B): B = f(self)

  }



  @deprecated("Use Configs[A] instead", "0.3.0")
  type AtPath[A] = Configs[A]


  @deprecated("Use ConfigOps instead", "0.3.0")
  implicit class EnrichTypesafeConfig(val c: Config) extends AnyVal {

    import simple._

    @deprecated("import com.github.kxbmap.configs.syntax._ instead", "0.3.0")
    def extract[A: Configs]: A =
      Configs[A].extract(c)

    @deprecated("import com.github.kxbmap.configs.syntax._ instead", "0.3.0")
    def get[A: Configs](path: String): A =
      Configs[A].get(c, path)

    @deprecated("import com.github.kxbmap.configs.syntax._ instead", "0.3.0")
    def opt[A: Configs](path: String): Option[A] =
      get[Option[A]](path)

    @deprecated("import com.github.kxbmap.configs.syntax._ instead", "0.3.0")
    def getOrElse[A: Configs](path: String, default: => A): A =
      opt[A](path).getOrElse(default)

  }

}
