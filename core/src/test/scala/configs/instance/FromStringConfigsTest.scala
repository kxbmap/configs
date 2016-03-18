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

import com.typesafe.config.ConfigFactory
import configs.util._
import configs.{Configs, FromString}
import java.{util => ju}
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Equal
import scalaz.std.string._

object FromStringConfigsTest extends Scalaprops {

  case class Foo(value: String) {
    require(value.forall(_.isLower))
  }

  case class Bar(fooList: List[Foo])

  val fromString = check[Foo]

  val fromStringJList = check[ju.List[Foo]]

  val errorsWithPath = {
    val config = ConfigFactory.parseString(
      """foo = AAA
        |bar1 = {
        |  foo-list = [BBB, CCC, DDD]
        |}
        |bar2 = {}
        |""".stripMargin)
    val bar = Configs[Bar]
    val p1 = forAll {
      val result = Configs[Foo].get(config, "foo")
      result.failed.exists(_.head.paths == List("foo"))
    }
    val p2 = forAll {
      val result = bar.get(config, "bar1")
      result.failed.exists(
        _.entries.map(_.paths).zipWithIndex.forall {
          case ("bar1" :: "foo-list" :: s :: Nil, i) => s == i.toString
          case _ => false
        })
    }
    val p3 = forAll {
      val result = bar.get(config, "bar2")
      result.failed.exists(_.head.paths == List("bar2", "foo-list"))
    }
    Properties.list(
      p1.toProperties("get path"),
      p2.toProperties("list index"),
      p3.toProperties("missing path")
    )
  }

  implicit lazy val fooFromString: FromString[Foo] =
    FromString.Try(Foo)

  implicit lazy val fooGen: Gen[Foo] =
    Gen.alphaLowerString.map(Foo)

  implicit lazy val fooEqual: Equal[Foo] =
    Equal.equalA[Foo]

  implicit lazy val fooToConfigValue: ToConfigValue[Foo] =
    ToConfigValue[String].contramap(_.value)

}
