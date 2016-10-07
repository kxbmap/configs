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

package configs

import com.typesafe.config.ConfigException

case class WithOrigin[A](value: A, origin: ConfigOrigin)

object WithOrigin {

  implicit def withOriginConfigReader[A](implicit A: ConfigReader[A]): ConfigReader[WithOrigin[A]] =
    (c, p) => A.read(c, p).flatMap { a =>
      try
        Result.successful(WithOrigin(a, c.getValue(p).origin()))
      catch {
        case e: ConfigException if e.origin() != null =>
          Result.successful(WithOrigin(a, e.origin()))
        case _: ConfigException =>
          Result.failure(ConfigError(s"no origin for path '$p', value '$a'"))
      }
    }

}
