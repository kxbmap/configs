/*
 * Copyright 2013-2017 Tsukasa Kitachi
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

import java.io.{File, Reader}
import java.net.URL
import java.nio.file.Path
import java.util.Properties

sealed trait ConfigSource

object ConfigSource {

  sealed trait Parsable extends ConfigSource


  def fromURL(url: URL)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    FromURL(url, parseOptions)

  def fromFile(file: File)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    FromFile(file, parseOptions)

  def fromFileAnySyntax(fileBasename: File)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    FromFileAnySyntax(fileBasename, parseOptions)

  def fromPath(path: Path)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    FromPath(path, parseOptions)

  def fromResources(resource: String, klass: Option[Class[_]] = None)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    FromResources(resource, klass, parseOptions)

  def fromResourcesAnySyntax(resourceBasename: String, klass: Option[Class[_]] = None)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    FromResourcesAnySyntax(resourceBasename, klass, parseOptions)

  def fromProperties(properties: Properties)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    FromProperties(properties, parseOptions)

  def fromReader(reader: Reader)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    FromReader(reader, parseOptions)

  def fromString(string: String)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    FromString(string, parseOptions)

  def fromMap(map: Map[String, _]): Parsable =
    FromMap(map)


  def fromConfig(config: Config): ConfigSource =
    FromConfig(config)

  def fromConfigResult(result: Result[Config]): ConfigSource =
    result match {
      case Result.Success(c) => FromConfig(c)
      case Result.Failure(e) => Error(e)
    }


  private[configs] case class FromURL(
      url: URL,
      parseOptions: ConfigParseOptions) extends Parsable

  private[configs] case class FromFile(
      file: File,
      parseOptions: ConfigParseOptions) extends Parsable

  private[configs] case class FromFileAnySyntax(
      fileBasename: File,
      parseOptions: ConfigParseOptions) extends Parsable

  private[configs] case class FromPath(
      path: Path,
      parseOptions: ConfigParseOptions) extends Parsable

  private[configs] case class FromResources(
      resource: String,
      klass: Option[Class[_]],
      parseOptions: ConfigParseOptions) extends Parsable

  private[configs] case class FromResourcesAnySyntax(
      resourceBasename: String,
      klass: Option[Class[_]],
      parseOptions: ConfigParseOptions) extends Parsable

  private[configs] case class FromProperties(
      properties: Properties,
      parseOptions: ConfigParseOptions) extends Parsable

  private[configs] case class FromReader(
      reader: Reader,
      parseOptions: ConfigParseOptions) extends Parsable

  private[configs] case class FromString(
      string: String,
      parseOptions: ConfigParseOptions) extends Parsable

  private[configs] case class FromMap(map: Map[String, _]) extends Parsable

  private[configs] case class FromConfig(config: Config) extends ConfigSource

  private[configs] case class Error(error: ConfigError) extends ConfigSource


  // implicit conversions

  implicit def urlToConfigSource(url: URL)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    fromURL(url)

  implicit def fileToConfigSource(file: File)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    fromFile(file)

  implicit def pathToConfigSource(path: Path)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    fromPath(path)

  implicit def propertiesToConfigSource(properties: Properties)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    fromProperties(properties)

  implicit def readerToConfigSource(reader: Reader)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    fromReader(reader)

  implicit def stringToConfigSource(string: String)(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Parsable =
    fromString(string)

  implicit def mapToConfigSource(map: Map[String, _]): Parsable =
    fromMap(map)

  implicit def configToConfigSource(config: Config): ConfigSource =
    fromConfig(config)

  implicit def configResultToConfigSource(result: Result[Config]): ConfigSource =
    fromConfigResult(result)

}
