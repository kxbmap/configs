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

import com.typesafe.config.{ConfigException, ConfigUtil}

case class ConfigError(head: ConfigError.Entry, tail: Vector[ConfigError.Entry] = Vector.empty) {

  def entries: Vector[ConfigError.Entry] =
    head +: tail

  def messages: Seq[String] =
    entries.map(_.messageWithPath)

  def +(that: ConfigError): ConfigError =
    copy(tail = tail ++ that.entries)

  def withPath(path: String): ConfigError =
    ConfigError(head.pushPath(path), tail.map(_.pushPath(path)))

  def throwable: Throwable =
    head.throwable
}

object ConfigError {

  def apply(message: String): ConfigError =
    ConfigError(Generic(message))

  def fromThrowable(throwable: Throwable): ConfigError =
    ConfigError(throwable match {
      case e: ConfigException.Null => NullValue(e)
      case e => Except(e)
    })


  sealed abstract class Entry extends Product with Serializable {

    def message: String

    def paths: List[String]

    def messageWithPath: String =
      if (paths.nonEmpty) s"${ConfigUtil.joinPath(paths: _*)}: $message" else message

    def pushPath(path: String): Entry

    def throwable: Throwable
  }

  case class NullValue(throwable: ConfigException.Null, paths: List[String] = Nil) extends Entry {

    def message: String =
      throwable.getMessage

    def pushPath(path: String): Entry =
      copy(paths = path :: paths)
  }

  case class Except(throwable: Throwable, paths: List[String] = Nil) extends Entry {

    def message: String =
      throwable.getMessage

    def pushPath(path: String): Entry =
      copy(paths = path :: paths)
  }

  case class Generic(message: String, paths: List[String] = Nil) extends Entry {

    def pushPath(path: String): Entry =
      copy(paths = path :: paths)

    def throwable: Throwable =
      new ConfigException.Generic(message)
  }

}
