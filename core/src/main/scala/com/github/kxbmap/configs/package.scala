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

package com.github.kxbmap

import com.typesafe.config.Config
import scala.annotation.implicitNotFound


package object configs {

  @implicitNotFound("No implicit AtPath defined for ${T}.")
  type AtPath[T] = Configs[String => T]


  @implicitNotFound("No implicit Catch found.")
  type Catch = Throwable => Boolean


  final implicit class EnrichTypesafeConfig(val c: Config) extends AnyVal {
    def extract[T: Configs]: T = Configs.of[T].extract(c)

    def get[T: AtPath](path: String): T = extract[String => T].apply(path)

    def getOrElse[T](path: String, default: => T)(implicit ev1: AtPath[T], ev2: Catch): T =
      opt[T](path).getOrElse(default)

    def opt[T](path: String)(implicit ev1: AtPath[T], ev2: Catch): Option[T] =
      get[Option[T]](path)

    def either[T](path: String)(implicit ev1: AtPath[T], ev2: Catch): Either[Throwable, T] =
      get[Either[Throwable, T]](path)
  }

}
