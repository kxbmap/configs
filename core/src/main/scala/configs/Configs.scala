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

import com.typesafe.config.{Config, ConfigValue}


trait Configs[A] {
  self =>

  def get(config: Config, path: String): Result[A]

  def extractKey: String = "extract"

  def extract(config: Config): Result[A] =
    get(config.atKey(extractKey), extractKey)

  def extract(value: ConfigValue): Result[A] =
    get(value.atKey(extractKey), extractKey)

  def map[B](f: A => B): Configs[B] =
    get(_, _).map(f)

  def flatMap[B](f: A => Configs[B]): Configs[B] =
    (c, p) => get(c, p).flatMap(f(_).get(c, p))

  def orElse[B >: A](fallback: Configs[B]): Configs[B] =
    (c, p) => get(c, p).orElse(fallback.get(c, p))

  def withPath: Configs[A] =
    (c, p) => get(c, p).mapError(_.withPath(p))

  def withExtractKey(key: String): Configs[A] =
    new Configs[A] {
      def get(config: Config, path: String): Result[A] =
        self.get(config, path)

      override def extractKey: String =
        key
    }

}

object Configs extends ConfigsInstances {

  @inline
  def apply[A](implicit A: Configs[A]): Configs[A] = A


  def derive[A]: Configs[A] =
    macro macros.ConfigsMacro.deriveConfigs[A]

  def bean[A]: Configs[A] =
    macro macros.BeanConfigsMacro.deriveBeanConfigsA[A]

  def bean[A](newInstance: => A): Configs[A] =
    macro macros.BeanConfigsMacro.deriveBeanConfigsI[A]


  def from[A](f: (Config, String) => Result[A]): Configs[A] =
    withPath((c, p) => Result.Try(f(c, p)).flatten)

  def fromConfig[A](f: Config => Result[A]): Configs[A] =
    from((c, p) => f(c.getConfig(p)))

  def fromTry[A](f: (Config, String) => A): Configs[A] =
    withPath((c, p) => Result.Try(f(c, p)))

  def fromConfigTry[A](f: Config => A): Configs[A] =
    fromTry((c, p) => f(c.getConfig(p)))

  def failure[A](msg: String): Configs[A] =
    withPath((c, p) => Result.failure(ConfigError(msg)))

  def get[A](path: String)(implicit A: Configs[A]): Configs[A] =
    (c, p) => Configs[Config].get(c, p).flatMap(A.get(_, path))

  def withPath[A](configs: Configs[A]): Configs[A] =
    configs.withPath

}
