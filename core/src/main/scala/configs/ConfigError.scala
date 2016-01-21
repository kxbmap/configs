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

import com.typesafe.config.{ConfigException, ConfigOrigin}

sealed abstract class ConfigError extends Product with Serializable {

  def message: String

  def origin: Option[ConfigOrigin]

  def throwable: Throwable

  def toConfigException: ConfigException
}

object ConfigError {

  def fromThrowable(throwable: Throwable): ConfigError =
    throwable match {
      case e: ConfigException.Missing => Missing(e)
      case e: ConfigException         => Config(e)
      case e                          => Generic(e)
    }


  case class Missing(throwable: ConfigException.Missing) extends ConfigError {

    def message: String =
      throwable.getMessage

    def origin: Option[ConfigOrigin] =
      Option(throwable.origin())

    def toConfigException: ConfigException =
      throwable
  }

  case class Config(throwable: ConfigException) extends ConfigError {

    def message: String =
      throwable.getMessage

    def origin: Option[ConfigOrigin] =
      Option(throwable.origin())

    def toConfigException: ConfigException =
      throwable
  }

  case class Generic(throwable: Throwable) extends ConfigError {

    def message: String =
      throwable.getMessage

    def origin: Option[ConfigOrigin] =
      None

    def toConfigException: ConfigException =
      new ConfigException.Generic(throwable.getMessage, throwable)
  }

}
