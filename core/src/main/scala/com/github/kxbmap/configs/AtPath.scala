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

import com.typesafe.config.Config

object AtPath {
  @inline def of[T: AtPath]: AtPath[T] = implicitly[AtPath[T]]

  @inline def apply[T](f: (Config, String) => T): AtPath[T] = Configs { c => f(c, _) }

  def mapBy[S: AtPath, T](f: S => T): AtPath[T] = AtPath.of[S].map { _ andThen f }

  def mapListBy[S, T](f: S => T)(implicit ev: AtPath[List[S]]): AtPath[List[T]] = mapBy { (_: List[S]).map(f) }
}
