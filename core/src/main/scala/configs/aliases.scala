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

import com.typesafe.config.{ConfigOriginFactory, ConfigValueFactory}
import java.net.URL
import scala.collection.JavaConverters._

object ConfigValue {

  val Null = ConfigValueFactory.fromAnyRef(null)
  val True = ConfigValueFactory.fromAnyRef(true)
  val False = ConfigValueFactory.fromAnyRef(false)

  def fromAny(any: Any): Result[ConfigValue] =
    Result.Try(ConfigValueFactory.fromAnyRef(any))

  def fromAny(any: Any, originDescription: String): Result[ConfigValue] =
    Result.Try(ConfigValueFactory.fromAnyRef(any, originDescription))

}

object ConfigList {

  def empty: ConfigList = ConfigValueFactory.fromIterable(Nil.asJava)

  def fromSeq(seq: Seq[Any]): Result[ConfigList] =
    Result.Try(ConfigValueFactory.fromIterable(seq.asJava))

  def fromSeq(seq: Seq[Any], originDescription: String): Result[ConfigList] =
    Result.Try(ConfigValueFactory.fromIterable(seq.asJava, originDescription))

}

object ConfigObject {

  def empty: ConfigObject = ConfigValueFactory.fromMap(Map.empty[String, Any].asJava)

  def fromMap(map: Map[String, Any]): Result[ConfigObject] =
    Result.Try(ConfigValueFactory.fromMap(map.asJava))

  def fromMap(map: Map[String, Any], originDescription: String): Result[ConfigObject] =
    Result.Try(ConfigValueFactory.fromMap(map.asJava, originDescription))

}

object ConfigMemorySize {

  final val Zero = ConfigMemorySize(0L)

  final val MinValue = Zero
  final val MaxValue = ConfigMemorySize(Long.MaxValue)

  def apply(bytes: Long): ConfigMemorySize =
    com.typesafe.config.ConfigMemorySize.ofBytes(bytes)

  def unapply(memorySize: ConfigMemorySize): Option[Long] =
    Some(memorySize.toBytes)

  implicit val configMemorySizeOrdering: Ordering[ConfigMemorySize] =
    Ordering.by(_.toBytes)

}

object ConfigOrigin {

  def simple: ConfigOrigin = ConfigOriginFactory.newSimple()

  def simple(description: String): ConfigOrigin = ConfigOriginFactory.newSimple(description)

  def file(filename: String): ConfigOrigin = ConfigOriginFactory.newFile(filename)

  def url(url: URL): ConfigOrigin = ConfigOriginFactory.newURL(url)

}

object ConfigParseOptions {

  import com.typesafe.config.{ConfigParseOptions => Impl}

  def defaults: ConfigParseOptions = Impl.defaults()

}

object ConfigResolveOptions {

  import com.typesafe.config.{ConfigResolveOptions => Impl}

  def defaults: ConfigResolveOptions = Impl.defaults()

  def noSystem: ConfigResolveOptions = Impl.noSystem()

}

object ConfigRenderOptions {

  import com.typesafe.config.{ConfigRenderOptions => Impl}

  def defaults: ConfigRenderOptions = Impl.defaults()

  def concise: ConfigRenderOptions = Impl.concise()

  def hocon: ConfigRenderOptions = defaults.setJson(false)

}
