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

import scala.collection.generic.CanBuildFrom

@deprecated("Use Configs", "0.3.0")
object AtPath {

  @deprecated("Use Configs", "0.3.0")
  def apply[A](implicit A: Configs[A]): Configs[A] = A

  @deprecated("Use Configs.map", "0.3.0")
  def by[A: Configs, B](f: A => B): Configs[B] = Configs[A].map(f)

  @deprecated("Use Configs", "0.3.0")
  def listBy[F[_], A, B](f: A => B)(implicit ev: Configs[Seq[A]], cbf: CanBuildFrom[Nothing, B, F[B]]): Configs[F[B]] =
    ev.map(_.map(f)(collection.breakOut))

}
