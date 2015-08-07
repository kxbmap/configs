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

package com.github.kxbmap.configs.instance

import com.github.kxbmap.configs.ConfigProp
import com.typesafe.config.{Config, ConfigList, ConfigMemorySize, ConfigObject, ConfigValue}
import java.{util => ju}
import scalaprops.{Properties, Scalaprops}
import scalaz.std.list._
import scalaz.std.stream._
import scalaz.std.string._
import scalaz.std.vector._

object ConfigTypeConfigsTest extends Scalaprops with ConfigProp {

  val config = check[Config]
  val configJavaList = check[ju.List[Config]]
  val configCollections = Properties.list(
    check[List[Config]].mapId("list " + _),
    check[Vector[Config]].mapId("vector " + _),
    check[Stream[Config]].mapId("stream " + _),
    check[Array[Config]].mapId("array " + _),
    check[Set[Config]].mapId("set " + _)
  )

  val configValue = check[ConfigValue]
  val configValueCollections = Properties.list(
    check[List[ConfigValue]].mapId("list " + _),
    check[Vector[ConfigValue]].mapId("vector " + _),
    check[Stream[ConfigValue]].mapId("stream " + _),
    check[Array[ConfigValue]].mapId("array " + _),
    check[Map[String, ConfigValue]].mapId("string map " + _),
    check[Map[Symbol, ConfigValue]].mapId("symbol map " + _),
    check[Set[ConfigValue]].mapId("set " + _)
  )
  val configValueJavaCollections = Properties.list(
    check[ju.List[ConfigValue]].mapId("list " + _),
    check[ju.Map[String, ConfigValue]].mapId("map " + _),
    check[ju.Set[ConfigValue]].mapId("set " + _)
  )

  val configList = check[ConfigList]

  val configObject = check[ConfigObject]
  val configObjectJavaList = check[ju.List[ConfigObject]]
  val configObjectCollections = Properties.list(
    check[List[ConfigObject]].mapId("list " + _),
    check[Vector[ConfigObject]].mapId("vector " + _),
    check[Stream[ConfigObject]].mapId("stream " + _),
    check[Array[ConfigObject]].mapId("array " + _),
    check[Set[ConfigObject]].mapId("set " + _)
  )

  val configMemorySize = check[ConfigMemorySize]
  val configMemorySizeJavaList = check[ju.List[ConfigMemorySize]]
  val configMemorySizeCollections = Properties.list(
    check[List[ConfigMemorySize]].mapId("list " + _),
    check[Vector[ConfigMemorySize]].mapId("vector " + _),
    check[Stream[ConfigMemorySize]].mapId("stream " + _),
    check[Array[ConfigMemorySize]].mapId("array " + _),
    check[Set[ConfigMemorySize]].mapId("set " + _)
  )

}
