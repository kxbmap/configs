/*
 * Copyright 2013-2016 Tsukasa Kitachi
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

package configs.syntax

import com.typesafe.config.Config
import configs.Configs

object exception {

  implicit class ConfigOps(private val self: Config) extends AnyVal {

    def extract[A](implicit A: Configs[A]): A =
      A.extract(self).getOrThrow

    def get[A](path: String)(implicit A: Configs[A]): A =
      A.get(self, path).getOrThrow

    def getOpt[A: Configs](path: String): Option[A] =
      get[Option[A]](path)

    def getOrElse[A: Configs](path: String, default: => A): A =
      getOpt[A](path).getOrElse(default)

  }

}
