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

import com.github.kxbmap.configs.instance.AllConfigs
import com.typesafe.config.{Config, ConfigException, ConfigValue}
import scala.annotation.implicitNotFound


@implicitNotFound("No implicit Configs defined for ${T}.")
trait Configs[T] {

  def get(config: Config, path: String): T

  def extract(config: Config): T = get(config.atPath(Configs.DummyPath), Configs.DummyPath)

  def extract(value: ConfigValue): T = get(value.atPath(Configs.DummyPath), Configs.DummyPath)

  def map[U](f: T => U): Configs[U] = (c, p) => f(get(c, p))

  def orElse[U >: T](other: Configs[U]): Configs[U] = (c, p) =>
    try
      get(c, p)
    catch {
      case _: ConfigException =>
        other.get(c, p)
    }
}

object Configs extends AllConfigs {

  private final val DummyPath = "configs-extract-path"

  @inline
  def apply[T](implicit T: Configs[T]): Configs[T] = T


  def of[T]: Configs[T] = macro macros.ConfigsMacro.materialize[T]

  def bean[T]: Configs[T] = macro macros.BeanConfigsMacro.materializeT[T]

  def bean[T](newInstance: => T): Configs[T] = macro macros.BeanConfigsMacro.materializeI[T]


  def from[T](f: (Config, String) => T): Configs[T] = f(_, _)

  def onPath[T](f: Config => T): Configs[T] = (c, p) => f(c.getConfig(p))


  @deprecated("Use Configs.onPath", "0.3.0")
  def configs[T](f: Config => T): Configs[T] = onPath(f)

  @deprecated("Use Configs.from", "0.3.0")
  def atPath[T](f: (Config, String) => T): Configs[T] = from(f)

}
