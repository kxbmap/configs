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

package object configs {

  type Config = com.typesafe.config.Config

  type ConfigValue = com.typesafe.config.ConfigValue

  type ConfigList = com.typesafe.config.ConfigList

  type ConfigObject = com.typesafe.config.ConfigObject

  type MemorySize = com.typesafe.config.ConfigMemorySize

}

package configs {

  import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
  import scala.collection.convert.decorateAsJava._

  object Config {

    def empty: Config = ConfigFactory.empty()

    def apply(configObject: ConfigObject): Config =
      configObject.toConfig

    def unapply(config: Config): Option[ConfigObject] =
      Some(config.root())

  }

  object ConfigValue {

    val Null: ConfigValue = from(null)
    val True: ConfigValue = from(true)
    val False: ConfigValue = from(false)

    def from(any: Any): ConfigValue =
      ConfigValueFactory.fromAnyRef(any)

    def from(any: Any, originDescription: String): ConfigValue =
      ConfigValueFactory.fromAnyRef(any, originDescription)

  }

  object ConfigList {

    def empty: ConfigList = from(Nil)

    def from(seq: Seq[Any]): ConfigList =
      ConfigValueFactory.fromIterable(seq.asInstanceOf[Seq[AnyRef]].asJava)

    def from(seq: Seq[Any], originDescription: String): ConfigList =
      ConfigValueFactory.fromIterable(seq.asInstanceOf[Seq[AnyRef]].asJava, originDescription)

  }

  object ConfigObject {

    def empty: ConfigObject = from(Map.empty)

    def from(map: Map[String, Any]): ConfigObject =
      ConfigValueFactory.fromMap(map.asInstanceOf[Map[String, AnyRef]].asJava)

    def from(map: Map[String, Any], originDescription: String): ConfigObject =
      ConfigValueFactory.fromMap(map.asInstanceOf[Map[String, AnyRef]].asJava, originDescription)

  }

  object MemorySize {

    final val Zero = MemorySize(0L)

    final val MinValue = Zero
    final val MaxValue = MemorySize(Long.MaxValue)

    def apply(bytes: Long): MemorySize =
      com.typesafe.config.ConfigMemorySize.ofBytes(bytes)

    def unapply(memorySize: MemorySize): Option[Long] =
      Some(memorySize.toBytes)

  }

}
