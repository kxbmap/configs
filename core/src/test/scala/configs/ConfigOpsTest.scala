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

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import configs.util._
import scala.collection.convert.decorateAsJava._
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object ConfigOpsTest extends Scalaprops {

  import configs.syntax._

  val extract = forAll { m: Map[String, String] =>
    val config = ConfigValueFactory.fromMap(m.asJava).toConfig
    config.extract[Map[String, String]] == Result.successful(m)
  }

  val get = forAll { n: Int =>
    val config = ConfigFactory.parseString(s"path = $n")
    config.get[Int]("path") == Result.successful(n)
  }

  val getOrElse = forAll { (n: Option[Int], m: Int) =>
    val config = n.toConfigValue.atKey("path")
    config.getOrElse[Int]("path", m) == Result.successful(n.getOrElse(m))
  }

}
