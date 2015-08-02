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
import scala.util.control.NonFatal


@implicitNotFound("No implicit Configs defined for ${A}.")
trait Configs[A] {

  def get(config: Config, path: String): A

  def extract(config: Config): A = get(config.atPath(Configs.DummyPath), Configs.DummyPath)

  def extract(value: ConfigValue): A = get(value.atPath(Configs.DummyPath), Configs.DummyPath)

  def map[B](f: A => B): Configs[B] = (c, p) => f(get(c, p))

  def orElse[B >: A](other: Configs[B]): Configs[B] = (c, p) =>
    try
      get(c, p)
    catch {
      case suppress: ConfigException =>
        try
          other.get(c, p)
        catch {
          case NonFatal(e) =>
            e.addSuppressed(suppress)
            throw e
        }
    }
}

object Configs extends AllConfigs {

  private final val DummyPath = "configs-extract-path"

  @inline
  def apply[A](implicit A: Configs[A]): Configs[A] = A


  def of[A]: Configs[A] = macro macros.ConfigsMacro.materialize[A]

  def bean[A]: Configs[A] = macro macros.BeanConfigsMacro.materializeA[A]

  def bean[A](newInstance: => A): Configs[A] = macro macros.BeanConfigsMacro.materializeI[A]


  def from[A](f: (Config, String) => A): Configs[A] = f(_, _)

  def onPath[A](f: Config => A): Configs[A] = (c, p) => f(c.getConfig(p))


  @deprecated("Use Configs.onPath", "0.3.0")
  def configs[A](f: Config => A): Configs[A] = onPath(f)

  @deprecated("Use Configs.from", "0.3.0")
  def atPath[A](f: (Config, String) => A): Configs[A] = from(f)

}
