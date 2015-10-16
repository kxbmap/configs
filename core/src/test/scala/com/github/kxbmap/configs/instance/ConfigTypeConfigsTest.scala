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

import com.github.kxbmap.configs.util._
import com.typesafe.config.{Config, ConfigList, ConfigObject, ConfigValue}
import java.{util => ju}
import scalaprops.Scalaprops
import scalaz.std.string._

object ConfigTypeConfigsTest extends Scalaprops {

  val config = check[Config].ignore("config 1.2.x incompatible (ConfigImpl.fromAnyRef)")

  val configJList = {
    implicit val h = hideConfigs[Config]
    check[ju.List[Config]].ignore("config 1.2.x incompatible (ConfigImpl.fromAnyRef)")
  }


  val configValue = check[ConfigValue].ignore("config 1.2.x incompatible (ConfigImpl.fromAnyRef)")

  val configValueJList = {
    implicit val h = hideConfigs[ConfigValue]
    check[ju.List[ConfigValue]].ignore("config 1.2.x incompatible (ConfigImpl.fromAnyRef)")
  }

  val configValueJMap = {
    implicit val h = hideConfigs[ConfigValue]
    val string = check[ju.Map[String, ConfigValue]]("string map")
    val symbol = check[ju.Map[Symbol, ConfigValue]]("symbol map")
    string.product(symbol).ignore("config 1.2.x incompatible (ConfigImpl.fromAnyRef)")
  }


  val configList = check[ConfigList].ignore("config 1.2.x incompatible (ConfigImpl.fromAnyRef)")


  val configObject = check[ConfigObject].ignore("config 1.2.x incompatible (ConfigImpl.fromAnyRef)")

  val configObjectJList = {
    implicit val h = hideConfigs[ConfigObject]
    check[ju.List[ConfigObject]].ignore("config 1.2.x incompatible (ConfigImpl.fromAnyRef)")
  }

}
