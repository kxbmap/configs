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

import com.typesafe.config.{ConfigFactory, ConfigOriginFactory, ConfigValueFactory}
import java.net.URL
import scala.collection.JavaConverters._

object Config {

  def empty: Config = ConfigFactory.empty()

  def unapply(config: Config): Option[ConfigObject] =
    Some(config.root())

}

object ConfigValue {

  final val Null = from(null)
  final val True = from(true)
  final val False = from(false)

  def from(any: Any): ConfigValue =
    ConfigValueFactory.fromAnyRef(any)

  def from(any: Any, originDescription: String): ConfigValue =
    ConfigValueFactory.fromAnyRef(any, originDescription)

}

object ConfigList {

  def empty: ConfigList = from(Nil)

  def from(seq: Seq[Any]): ConfigList =
    ConfigValueFactory.fromIterable(seq.asJava)

  def from(seq: Seq[Any], originDescription: String): ConfigList =
    ConfigValueFactory.fromIterable(seq.asJava, originDescription)

}

object ConfigObject {

  def empty: ConfigObject = from(Map.empty)

  def from(map: Map[String, Any]): ConfigObject =
    ConfigValueFactory.fromMap(map.asJava)

  def from(map: Map[String, Any], originDescription: String): ConfigObject =
    ConfigValueFactory.fromMap(map.asJava, originDescription)

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

object ConfigUtil {

  import com.typesafe.config.{ConfigUtil => Impl}

  def quoteString(s: String): String =
    Impl.quoteString(s)

  def joinPath(element: String, elements: String*): String =
    Impl.joinPath(element +: elements: _*)

  def joinPath(elements: Seq[String]): String =
    Impl.joinPath(elements.asJava)

  def splitPath(path: String): List[String] =
    Impl.splitPath(path).asScala.toList

}

object ConfigOrigin {

  def default: ConfigOrigin = ConfigOriginFactory.newSimple()

  def simple(description: String): ConfigOrigin = ConfigOriginFactory.newSimple(description)

  def file(filename: String): ConfigOrigin = ConfigOriginFactory.newFile(filename)

  def url(url: URL): ConfigOrigin = ConfigOriginFactory.newURL(url)

}
