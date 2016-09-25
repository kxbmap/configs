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

package configs.instance

import com.typesafe.config.ConfigValueType
import configs.testutil.fun._
import configs.testutil.instance.collection._
import configs.testutil.instance.config._
import configs.testutil.instance.string._
import configs.{Config, ConfigList, ConfigObject, ConfigValue, Configs, MemorySize}
import java.{util => ju}
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object ConfigTypesTest extends Scalaprops {

  val config = check[Config] x
    forAll { key: String =>
      Configs[Config].extractValue(ConfigValue.Null, key).failed.exists {
        _.head.paths == List(key)
      }
    }.toProperties("use extractValue key")

  val configValue = {
    implicit val param: CheckParam[ConfigValue] = new CheckParam[ConfigValue] {
      override def exceptEncodeDecode(a: ConfigValue): Boolean =
        a.valueType() == ConfigValueType.NULL
    }
    check[ConfigValue]
  }

  val configValueJList = check[ju.List[ConfigValue]]

  val configValueJMap = check[ju.Map[String, ConfigValue]]

  val configList = check[ConfigList]

  val configObject = check[ConfigObject]

  val memorySize = check[MemorySize]

}
