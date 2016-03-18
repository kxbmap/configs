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

import com.typesafe.config.{Config, ConfigList, ConfigMemorySize, ConfigObject, ConfigValue, ConfigValueFactory}
import configs.util._
import java.{util => ju}
import scala.collection.convert.decorateAsJava._
import scalaprops.{Gen, Scalaprops}
import scalaz.std.string._
import scalaz.{Equal, Need}

object ConfigTypeConfigsTest extends Scalaprops {

  val config = check[Config]

  val configValue = check[ConfigValue]

  val configValueJList = check[ju.List[ConfigValue]]

  val configValueJMap = check[ju.Map[String, ConfigValue]]

  val configList = check[ConfigList]

  val configObject = check[ConfigObject]

  val configMemorySize = check[ConfigMemorySize]


  implicit lazy val configEqual: Equal[Config] =
    Equal.equalA[Config]

  implicit lazy val configGen: Gen[Config] =
    configObjectGen.map(_.toConfig)

  implicit lazy val configValueEqual: Equal[ConfigValue] =
    Equal.equalA[ConfigValue]

  implicit lazy val configListEqual: Equal[ConfigList] =
    Equal.equalA[ConfigList]

  implicit lazy val configObjectEqual: Equal[ConfigObject] =
    Equal.equalA[ConfigObject]

  implicit def genConfigValue[A: Gen : ToConfigValue]: Gen[ConfigValue :@ A] =
    Gen[A].map(_.toConfigValue).tag[A]

  implicit lazy val configNumberGen: Gen[ConfigValue :@ Number] =
    Gen.oneOf(
      Gen[ConfigValue :@ Byte].untag,
      Gen[ConfigValue :@ Int].untag,
      Gen[ConfigValue :@ Long].untag,
      Gen[ConfigValue :@ Double].untag
    ).tag[Number]

  implicit lazy val configListGen: Gen[ConfigList] =
    Gen.list(configValueGen).map(_.asJava).map(ConfigValueFactory.fromIterable)

  implicit lazy val configValueJListGen: Gen[ju.List[ConfigValue]] =
    Gen[ConfigList].as[ju.List[ConfigValue]]

  implicit lazy val configObjectGen: Gen[ConfigObject] =
    Gen.mapGen(Gen[String], configValueGen).map(_.asJava).map(ConfigValueFactory.fromMap)

  implicit lazy val configValueJavaMapGen: Gen[ju.Map[String, ConfigValue]] =
    Gen[ConfigObject].as[ju.Map[String, ConfigValue]]

  implicit lazy val configValueGen: Gen[ConfigValue] =
    Gen.lazyFrequency(
      40 -> Need(Gen[ConfigValue :@ String].untag),
      40 -> Need(Gen[ConfigValue :@ Number].untag),
      10 -> Need(Gen[ConfigValue :@ Boolean].untag),
      5 -> Need(configListGen.as[ConfigValue]),
      5 -> Need(configObjectGen.as[ConfigValue])
    ).mapSize(_ / 2)


  implicit lazy val configMemorySizeEqual: Equal[ConfigMemorySize] =
    Equal.equalA[ConfigMemorySize]

  implicit lazy val configMemorySizeGen: Gen[ConfigMemorySize] =
    Gen.nonNegativeLong.map(ConfigMemorySize.ofBytes)

}
