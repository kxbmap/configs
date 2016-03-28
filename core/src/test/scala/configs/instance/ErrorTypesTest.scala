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
import configs.{Config, ConfigError, Configs, Result}
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}

object ErrorTypesTest extends Scalaprops {

  implicit def resultCheckParam[A]: CheckParam[Result[A]] =
    new CheckParam[Result[A]] {
      override def checkPushPath: Boolean = false
    }

  val result = check[Result[Int]]

  val resultHandleConfigError = {
    val p1 = Properties.single("null value", forAll {
      val config = ConfigFactory.parseString("value = null")
      val result = Configs[Result[Int]].get(config, "value")
      result.exists {
        case Result.Failure(ConfigError(_: ConfigError.NullValue, _)) => true
        case _ => false
      }
    })
    val p2 = Properties.single("runtime exception", forAll {
      val config = Config.empty
      val e = new RuntimeException
      implicit val configs: Configs[Int] = Configs.fromTry((_, _) => throw e)
      val result = Configs[Result[Int]].get(config, "value")
      result.exists(_ == Configs[Int].get(config, "value"))
    })
    val p3 = Properties.single("failure", forAll { s: String =>
      val config = Config.empty
      implicit val configs: Configs[Int] = Configs.failure(s)
      val result = Configs[Result[Int]].get(config, "value")
      result.exists(_ == Configs[Int].get(config, "value"))
    })
    Properties.list(p1, p2, p3)
  }

}
