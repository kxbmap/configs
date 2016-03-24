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

import com.typesafe.config.{ConfigFactory, ConfigUtil}
import configs.{Configs, ToConfig}
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Property}
import scalaz.Equal

object fun {

  val q: String => String = ConfigUtil.quoteString

  private def xxx(x: Any): Unit = {
    println(s"${Console.RED}xxx${Console.RESET} $x")
  }

  def check[A: Configs : ToConfig : Gen : Equal]: Property =
    checkExcept[A](_ => false)

  def check[A: Configs : ToConfig : Gen : Equal](id: String): Properties[String] =
    check[A].toProperties(id)

  def checkExcept[A: Configs : ToConfig : Gen : Equal](except: A => Boolean): Property =
    forAll { value: A =>
      if (except(value)) true
      else {
        val path = "path"
        val encoded = ToConfig[A].toValueOption(value)
        val config = encoded.foldLeft(ConfigFactory.empty())(_.withValue(path, _))
        val decoded = Configs[A].get(config, path)
        val result = decoded.exists(Equal[A].equal(_, value))
        if (!result) {
          println()
          xxx(s"value  : $value")
          xxx(s"encoded: ${encoded.getOrElse("<missing>")}")
          xxx(s"decoded: ${decoded.valueOr(e => s"<failure>: $e")}")
        }
        result
      }
    }

}
