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

import com.typesafe.config.{ConfigFactory, ConfigRenderOptions}
import configs.ConfigObject
import configs.ConfigUtil.{quoteString => q}
import configs.testutil.instance.string._
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{Gen, Scalaprops}
import scalaz.syntax.foldable._

object ConstructSyntaxTest extends Scalaprops {

  import configs.syntax.construct._

  val `object` =
    forAll { (s: String, n: Int, xs: List[Int]) =>
      val obj = % {
        'string := s
        'int := n
        "value-list" := xs
      }
      obj == ConfigFactory.parseString(
        s"""string = ${q(s)}
           |int = $n
           |value-list = ${xs.mkString("[", ",", "]")}
           |""".stripMargin).root()
    }

  val `nested object` =
    forAll { (n: Int) =>
      val obj = % {
        'object := % {
          'value := n
        }
      }
      obj == ConfigFactory.parseString(
        s"""object {
           |  value = $n
           |}
           |""".stripMargin).root()
    }

  val list =
    forAll { (xs: List[Int]) =>
      val obj = % {
        "list" := \(xs: _*)
      }
      obj == ConfigFactory.parseString(
        s"""list = ${xs.mkString("[", ",", "]")}
           |""".stripMargin).root()
    }


  private val commentGen = Gen.asciiString

  private def render(co: ConfigObject): String =
    co.render(ConfigRenderOptions.defaults().setJson(false).setOriginComments(false))

  val `object with comment` =
    forAllG(commentGen) { comment =>
      val obj = % {
        "obj" := %#(comment) {
          "value" := 42
        }
      }
      render(obj) ==
        s"""# $comment
           |obj {
           |    value=42
           |}
           |""".stripMargin
    }

  val `value with comment` =
    forAllG(commentGen) { comment =>
      val obj = % {
        "value" := 42 <# comment
      }
      render(obj) ==
        s"""# $comment
           |value=42
           |""".stripMargin
    }

  val `list with comment` =
    forAllG(commentGen) { comment =>
      val obj = % {
        "list" := \#(comment)(
          1 <# "elem 1",
          2 <# "elem 2",
          3 <# "elem 3"
        )
      }
      render(obj) ==
        s"""# $comment
           |list=[
           |    # elem 1
           |    1,
           |    # elem 2
           |    2,
           |    # elem 3
           |    3
           |]
           |""".stripMargin
    }

  val `object with multiline comments` =
    forAllG(Gen.listOf1(commentGen)) { comments =>
      val obj = % {
        "obj" := %#(comments.toList: _*) {
          "value" := 42
        }
      }
      render(obj) ==
        s"""${comments.map("# " + _).intercalate("\n")}
           |obj {
           |    value=42
           |}
           |""".stripMargin
    }

  val `value with multiline comments` =
    forAllG(Gen.listOf1(commentGen)) { comments =>
      val obj = % {
        "value" := 42 <# (comments.toList: _*)
      }
      render(obj) ==
        s"""${comments.map("# " + _).intercalate("\n")}
           |value=42
           |""".stripMargin
    }

  val `list with multiline comments` =
    forAllG(Gen.listOf1(commentGen)) { comments =>
      val obj = % {
        "list" := \#(comments.toList: _*)(
          1 <# "elem 1",
          2 <# "elem 2",
          3 <# "elem 3"
        )
      }
      render(obj) ==
        s"""${comments.map("# " + _).intercalate("\n")}
           |list=[
           |    # elem 1
           |    1,
           |    # elem 2
           |    2,
           |    # elem 3
           |    3
           |]
           |""".stripMargin
    }

}
