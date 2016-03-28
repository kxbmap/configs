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

package configs.syntax

import com.typesafe.config.ConfigFactory
import configs.testutil.instance.string._
import configs.{ConfigObject, Result, ToConfig}
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object ConfigOpsTest extends Scalaprops {

  val extract = forAll { m: Map[String, Int] =>
    val config = ConfigObject.from(m).toConfig
    config.extract[Map[String, Int]] == Result.successful(m)
  }

  val get = forAll { n: Int =>
    val config = ConfigFactory.parseString(s"path = $n")
    config.get[Int]("path") == Result.successful(n)
  }

  val getOrElse = forAll { (n: Option[Int], m: Int) =>
    val config = ToConfig[Option[Int]].toValue(n).atKey("path")
    config.getOrElse[Int]("path", m) == Result.successful(n.getOrElse(m))
  }

}
