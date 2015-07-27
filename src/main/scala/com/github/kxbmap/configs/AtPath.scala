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

@deprecated("Use Configs", "0.3.0")
object AtPath {

  @deprecated("Use Configs", "0.3.0")
  def apply[T](implicit T: Configs[T]): Configs[T] = T

  @deprecated("Use Configs.map", "0.3.0")
  def by[S: Configs, T](f: S => T): Configs[T] = Configs[S].map(f)

  @deprecated("Use Configs", "0.3.0")
  def listBy[C[_], S, T](f: S => T)(implicit ev: Configs[Seq[S]], cbf: CBF[C, T]): Configs[C[T]] =
    ev.map(_.map(f)(collection.breakOut))

}
