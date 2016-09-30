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

import com.typesafe.config.{ConfigFactory, ConfigValueFactory}
import configs.{Config, ConfigList, ConfigObject, ConfigValue}
import java.net.InetAddress
import scala.collection.JavaConverters._
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object CreateSyntaxTest extends Scalaprops {

  val config = forAll {
    val c = Config(
      "foo" := 42,
      "bar" := "xxx",
      InetAddress.getByName("127.0.0.1") := 123d,
      "baz" := ConfigObject(
        "qux" := "???"
      )
    )
    c == ConfigFactory.parseString(
      """foo = 42
        |bar = xxx
        |"127.0.0.1" = 123.0
        |baz.qux = "???"
        |""".stripMargin)
  }

  val configValue = forAll { n: Int =>
    val cv = ConfigValue(n)
    cv == ConfigValueFactory.fromAnyRef(n)
  }

  val configList = forAll { xs: List[Int] =>
    val cl = ConfigList(xs: _*)
    cl == ConfigValueFactory.fromIterable(xs.asJava)
  }

  val configObject = forAll {
    val co = ConfigObject(
      "foo" := 42,
      "bar" := "xxx",
      InetAddress.getByName("127.0.0.1") := 123d
    )
    co == ConfigValueFactory.fromMap(Map(
      "foo" -> 42,
      "bar" -> "xxx",
      "127.0.0.1" -> 123d
    ).asJava)
  }

}
