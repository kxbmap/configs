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
import configs.{Config, ConfigReader, ConfigWriter, Result}
import scalaprops.Or.Empty
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{:-:, Gen, Or, Properties}
import scalaz.Equal

object fun {

  private def xxx(x: Any): Unit = {
    println(s"${Console.RED}xxx${Console.RESET} $x")
  }

  def check[A: CheckParam : ConfigReader : ConfigWriter : Gen : Equal]: Properties[Unit :-: String :-: Empty] =
    Properties.list(
      roundtrip[A],
      pushPath[A]
    )

  def check[A: CheckParam : ConfigReader : ConfigWriter : Gen : Equal](id: String): Properties[String :-: String :-: Empty] =
    check[A].mapId {
      case Or.L(_) => Or.L(id)
      case Or.R(r) => Or.R(r)
    }

  private def roundtrip[A: CheckParam : ConfigReader : ConfigWriter : Gen : Equal]: Properties[String] =
    Properties.single("roundtrip", forAll { value: A =>
      CheckParam[A].exceptRoundtrip(value) || {
        val path = "path"
        val m = ConfigWriter[A].append(Map.empty, path, value)
        val wrote = m.get(path)
        val config = wrote.foldLeft(Config.empty)(_.withValue(path, _))
        val read = ConfigReader[A].read(config, path)
        val result = read.exists(Equal[A].equal(_, value))
        if (!result) {
          println()
          xxx(s"wrote: ${wrote.getOrElse("<missing>")}")
          xxx(s"read : ${read.valueOr(e => s"<failure>: $e")}")
        }
        result
      }
    })

  private def pushPath[A: CheckParam](implicit reader: ConfigReader[A]): Properties[String] =
    Properties.single("push path", forAllG(pathStringGen) { path =>
      val c1 = Config.empty
      val c2 = ConfigFactory.parseString(s"$path = 42")
      val c3 = ConfigFactory.parseString(s"$path = []")
      val result = Result.tuple3(
        reader.read(c1, path),
        reader.read(c2, path),
        reader.read(c3, path)
      )
      if (CheckParam[A].alwaysSuccess)
        result.isSuccess
      else
        result.failed.exists {
          _.entries.map(_.pathString).forall(_ == path)
        }
    })


  implicit class RichProperties[A](private val self: Properties[A]) extends AnyVal {
    def x[B](that: Properties[B]): Properties[Unit :-: A :-: B :-: Or.Empty] =
      self.product(that)
  }

  abstract class CheckParam[A] {
    def exceptRoundtrip(a: A): Boolean = false
    def alwaysSuccess: Boolean = false
  }

  object CheckParam {

    def apply[A](implicit A: CheckParam[A]): CheckParam[A] = A

    implicit def default[A]: CheckParam[A] = new CheckParam[A] {}

  }

}
