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

package configs.testutil

import com.typesafe.config.ConfigFactory
import configs.testutil.instance.string._
import configs.{Config, Configs, Result, ConfigWriter}
import scalaprops.Or.Empty
import scalaprops.Property.forAll
import scalaprops.{:-:, Gen, Or, Properties}
import scalaz.Equal

object fun {

  private def xxx(x: Any): Unit = {
    println(s"${Console.RED}xxx${Console.RESET} $x")
  }

  def check[A: CheckParam : Configs : ConfigWriter : Gen : Equal]: Properties[Unit :-: String :-: Empty] =
    Properties.list(
      encodeDecode[A],
      pushPath[A]
    )

  def check[A: CheckParam : Configs : ConfigWriter : Gen : Equal](id: String): Properties[String :-: String :-: Empty] =
    check[A].mapId {
      case Or.L(_) => Or.L(id)
      case Or.R(r) => Or.R(r)
    }

  private def encodeDecode[A: CheckParam : Configs : ConfigWriter : Gen : Equal]: Properties[String] =
    Properties.single("encode/decode", forAll { value: A =>
      CheckParam[A].exceptEncodeDecode(value) || {
        val path = "path"
        val m = ConfigWriter[A].append(Map.empty, path, value)
        val encoded = m.get(path)
        val config = encoded.foldLeft(Config.empty)(_.withValue(path, _))
        val decoded = Configs[A].get(config, path)
        val result = decoded.exists(Equal[A].equal(_, value))
        if (!result) {
          println()
          xxx(s"encoded: ${encoded.getOrElse("<missing>")}")
          xxx(s"decoded: ${decoded.valueOr(e => s"<failure>: $e")}")
        }
        result
      }
    })

  private def pushPath[A: CheckParam : Configs]: Properties[String] =
    Properties.single("push path", forAll {
      val c1 = Config.empty
      val c2 = ConfigFactory.parseString("path = 42")
      val c3 = ConfigFactory.parseString("path = []")
      val result = Result.tuple3(
        Configs[A].get(c1, "path"),
        Configs[A].get(c2, "path"),
        Configs[A].get(c3, "path")
      )
      if (CheckParam[A].checkPushPath)
        result.failed.exists {
          _.entries.map(_.paths).forall(_ == List("path"))
        }
      else result.isSuccess
    })


  implicit class EnrichProperties[A](private val self: Properties[A]) extends AnyVal {
    def x[B](that: Properties[B]): Properties[Unit :-: A :-: B :-: Or.Empty] =
      self.product(that)
  }

  abstract class CheckParam[A] {
    def exceptEncodeDecode(a: A): Boolean = false
    def checkPushPath: Boolean = true
  }

  object CheckParam {

    def apply[A](implicit A: CheckParam[A]): CheckParam[A] = A

    implicit def default[A]: CheckParam[A] = new CheckParam[A] {}

  }

}
