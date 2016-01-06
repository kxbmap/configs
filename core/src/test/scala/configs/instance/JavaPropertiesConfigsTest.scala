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

import configs.util._
import java.{util => ju}
import scala.collection.convert.decorateAsScala._
import scalaprops.{Gen, Scalaprops}
import scalaz.Equal
import scalaz.std.map._
import scalaz.std.string._

object JavaPropertiesConfigsTest extends Scalaprops {

  val javaProperties = check[ju.Properties]


  implicit lazy val javaPropertiesGen: Gen[ju.Properties] =
    Gen[Map[String, String]].map { m =>
      val p = new ju.Properties()
      m.foreach {
        case (k, v) => p.setProperty(k, v)
      }
      p
    }

  implicit lazy val javaPropertiesEqual: Equal[ju.Properties] =
    Equal.equalBy(toMap)

  implicit lazy val javaPropertiesToConfigValue: ToConfigValue[ju.Properties] =
    ToConfigValue[Map[String, String]].contramap(toMap)

  private def toMap(p: ju.Properties): Map[String, String] =
    p.asScala.map {
      case (k: String, v: String) => (k, v)
      case (k, v)                 => sys.error(s"$k = $v")
    }.toMap

}
