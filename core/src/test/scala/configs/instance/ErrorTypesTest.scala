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
import configs.testutil.fun._
import configs.testutil.instance.anyVal._
import configs.testutil.instance.result.success._
import configs.testutil.instance.string._
import configs.{Config, ConfigError, ConfigReader, Result}
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}

object ErrorTypesTest extends Scalaprops {

  implicit def resultCheckParam[A]: CheckParam[Result[A]] =
    new CheckParam[Result[A]] {
      override def alwaysSuccess: Boolean = true
    }

  val result = check[Result[Int]]

  val resultHandleConfigError = {
    val p1 = Properties.single("null value", forAll {
      val config = ConfigFactory.parseString("value = null")
      val result = ConfigReader[Result[Int]].read(config, "value")
      result.exists {
        case Result.Failure(ConfigError(ConfigError.NullValue(_, "value" :: Nil), _)) => true
        case _ => false
      }
    })
    val p2 = Properties.single("runtime exception", forAll {
      val config = Config.empty
      val e = new RuntimeException
      implicit val reader: ConfigReader[Int] = ConfigReader.fromTry((_, _) => throw e)
      val result = ConfigReader[Result[Int]].read(config, "value")
      result.contains(ConfigReader[Int].read(config, "value"))
    })
    val p3 = Properties.single("failure", forAll { s: String =>
      val config = Config.empty
      implicit val reader: ConfigReader[Int] = ConfigReader.failure(s)
      val result = ConfigReader[Result[Int]].read(config, "value")
      result.contains(ConfigReader[Int].read(config, "value"))
    })
    Properties.list(p1, p2, p3)
  }

}
