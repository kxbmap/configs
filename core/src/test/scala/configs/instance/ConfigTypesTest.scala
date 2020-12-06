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
import configs.testutil.instance.math._
import configs.testutil.instance.string._
import configs.{Config, ConfigList, ConfigMemorySize, ConfigObject, ConfigValue}
import java.{util => ju}
import scalaprops.Property.forAllG
import scalaprops.Scalaprops

object ConfigTypesTest extends Scalaprops {

  val config = check[Config]

  val configValue = {
    implicit val param: CheckParam[ConfigValue] = new CheckParam[ConfigValue] {
      override def exceptRoundtrip(a: ConfigValue): Boolean =
        a.valueType() == ConfigValueType.NULL
    }
    check[ConfigValue]
  }

  val configValueJList = check[ju.List[ConfigValue]]

  val configValueJMap = check[ju.Map[String, ConfigValue]]

  val configList = check[ConfigList]

  val configObject = check[ConfigObject]

  val configMemorySize = {
    // Workaround
    import BigInt._
    implicit val param: CheckParam[ConfigMemorySize] = new CheckParam[ConfigMemorySize] {
      override def exceptRoundtrip(a: ConfigMemorySize): Boolean =
        !a.toBytesBigInteger.isValidLong
    }
    check[ConfigMemorySize]
  } x
    forAllG(nonNegativeBigInt) { n =>
      val ConfigMemorySize(m) = ConfigMemorySize(n)
      m == n
    }.toProperties("unapply")

}
