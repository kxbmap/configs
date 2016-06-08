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

final case class ConfigError(head: ConfigError.Entry, tail: Vector[ConfigError.Entry] = Vector.empty) {

  def entries: Vector[ConfigError.Entry] =
    head +: tail

  def messages: Seq[String] =
    entries.map(_.messageWithPath)

  def +(that: ConfigError): ConfigError =
    copy(tail = tail ++ that.entries)

  def pushPath(path: String): ConfigError =
    ConfigError(head.pushPath(path), tail.map(_.pushPath(path)))

  def popPath: ConfigError =
    ConfigError(head.popPath, tail.map(_.popPath))

  def configException: ConfigException = {
    val msg =
      if (tail.isEmpty) head.messageWithPath
      else s"${head.messageWithPath} (and suppressed ${tail.size} error(s))"
    configException(msg)
  }

  def configException(message: String): ConfigException =
    suppressBy(new ConfigException.Generic(message))

  def suppressBy[E <: Throwable](exception: E): E = {
    entries.foreach(e => exception.addSuppressed(e.throwable))
    exception
  }

}

object ConfigError {

  def apply(message: String): ConfigError =
    ConfigError(Generic(message))

  def fromThrowable(throwable: Throwable): ConfigError =
    ConfigError(throwable match {
      case e: ConfigException.Null => NullValue(e)
      case e => Exceptional(e)
    })


  sealed abstract class Entry extends Product with Serializable {

    def message: String

    def paths: List[String]

    final def messageWithPath: String =
      s"[${paths.mkString(".")}] $message"

    def pushPath(path: String): Entry

    def popPath: Entry

    def throwable: Throwable
  }

  final case class NullValue(throwable: ConfigException.Null, paths: List[String] = Nil) extends Entry {

    def message: String =
      s"${throwable.getMessage}"

    def pushPath(path: String): Entry =
      copy(paths = path :: paths)

    def popPath: Entry = paths match {
      case Nil => this
      case _ :: ps => copy(paths = ps)
    }
  }

  final case class Exceptional(throwable: Throwable, paths: List[String] = Nil) extends Entry {

    def message: String =
      s"${throwable.getMessage}"

    def pushPath(path: String): Entry =
      copy(paths = path :: paths)

    def popPath: Entry = paths match {
      case Nil => this
      case _ :: ps => copy(paths = ps)
    }
  }

  final case class Generic(message: String, paths: List[String] = Nil) extends Entry {

    def pushPath(path: String): Entry =
      copy(paths = path :: paths)

    def popPath: Entry = paths match {
      case Nil => this
      case _ :: ps => copy(paths = ps)
    }

    def throwable: Throwable =
      new ConfigException.Generic(message)
  }

}
