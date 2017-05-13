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

import com.typesafe.config.ConfigFactory
import scala.collection.JavaConverters._

object Config {

  def empty: Config = ConfigFactory.empty()

  def empty(originDescription: String): Config = ConfigFactory.empty(originDescription)


  def load()(
      implicit
      parseOptions: ConfigParseOptions = ConfigParseOptions.defaults,
      resolveOptions: ConfigResolveOptions = ConfigResolveOptions.defaults): Result[Config] =
    Result.Try(ConfigFactory.load(parseOptions, resolveOptions))


  def parse(source: ConfigSource.Parsable): Result[Config] = Result.Try {
    import ConfigSource._
    source match {
      case FromURL(url, opts) => ConfigFactory.parseURL(url, opts)
      case FromFile(file, opts) => ConfigFactory.parseFile(file, opts)
      case FromFileAnySyntax(basename, opts) => ConfigFactory.parseFileAnySyntax(basename, opts)
      case FromPath(path, opts) => ConfigFactory.parseFile(path.toFile, opts)
      case FromResources(resource, None, opts) => ConfigFactory.parseResources(resource, opts)
      case FromResources(resource, Some(klass), opts) => ConfigFactory.parseResources(klass, resource, opts)
      case FromResourcesAnySyntax(basename, None, opts) => ConfigFactory.parseResourcesAnySyntax(basename, opts)
      case FromResourcesAnySyntax(basename, Some(klass), opts) => ConfigFactory.parseResourcesAnySyntax(klass, basename, opts)
      case FromProperties(props, opts) => ConfigFactory.parseProperties(props, opts)
      case FromReader(reader, opts) => ConfigFactory.parseReader(reader, opts)
      case FromString(string, opts) => ConfigFactory.parseString(string, opts)
      case FromMap(map) => ConfigFactory.parseMap(map.asJava)
    }
  }

  def mergeAll(sources: ConfigSource*)(
      implicit resolveOptions: ConfigResolveOptions = ConfigResolveOptions.defaults): Result[Config] = {
    import ConfigSource._
    Result.traverse(sources) {
      case source: Parsable => parse(source)
      case FromConfig(config) => Result.Success(config)
      case Error(error) => Result.Failure(error)
    }.map(_.foldLeft(empty)(_.withFallback(_)).resolve(resolveOptions))
  }


  def defaultApplication()(
      implicit parseOptions: ConfigParseOptions = ConfigParseOptions.defaults): Result[Config] =
    Result.Try(ConfigFactory.defaultApplication(parseOptions))

  def defaultApplication(loader: ClassLoader): Result[Config] =
    Result.Try(ConfigFactory.defaultApplication(loader))

  def defaultReference(): Result[Config] =
    Result.Try(ConfigFactory.defaultReference())

  def defaultReference(loader: ClassLoader): Result[Config] =
    Result.Try(ConfigFactory.defaultReference(loader))

  def defaultOverrides(): Result[Config] =
    Result.Try(ConfigFactory.defaultOverrides())

  def defaultOverrides(loader: ClassLoader): Result[Config] =
    Result.Try(ConfigFactory.defaultOverrides(loader))

  def systemProperties(): Result[Config] =
    Result.Try(ConfigFactory.systemProperties())

  def systemEnvironment(): Result[Config] =
    Result.Try(ConfigFactory.systemEnvironment())


  def invalidateCaches(): Result[Unit] =
    Result.Try(ConfigFactory.invalidateCaches())

}
