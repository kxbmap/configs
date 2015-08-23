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

import com.github.kxbmap.configs.Configs
import com.github.kxbmap.configs.simple._
import com.github.kxbmap.configs.util._
import com.typesafe.config.{ConfigException, ConfigFactory}
import java.{util => ju}
import scalaprops.Scalaprops
import scalaz.std.java.enum._

object JavaEnumConfigsTest extends Scalaprops {

  val enum = check[JavaEnum]

  val enumJList = {
    implicit val h = hideConfigs[JavaEnum]
    check[ju.List[JavaEnum]]
  }

  val badValue = intercept {
    val config = ConfigFactory.parseString("bad-value = FOOBAR")
    Configs[JavaEnum].get(config, "bad-value")
  } {
    case e: ConfigException.BadValue =>
      Seq("bad-value", "FOOBAR").forall(e.getMessage.contains)
  }

  implicit lazy val javaEnumToConfigValue: ToConfigValue[JavaEnum] = _.name().toConfigValue

}
