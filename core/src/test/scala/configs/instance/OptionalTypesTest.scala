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
import configs.testutil.instance.option._
import configs.testutil.instance.string._
import configs.testutil.instance.config._
import configs.{Config, ConfigReader, ConfigValue, ConfigWriter}
import java.{util => ju}
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{Properties, Scalaprops}

object OptionalTypesTest extends Scalaprops {

  val option = check[Option[Int]] x
    Properties.single(
      "append None",
      forAll { (m: Map[String, ConfigValue], p: String) =>
        ConfigWriter[Option[Int]].append(m, p, None) == m
      })

  val option2 = check[Option[Option[Int]]]

  type O3[A] = Option[Option[Option[A]]]

  implicit def option3CheckParam[A]: CheckParam[O3[A]] =
    new CheckParam[O3[A]] {
      override def exceptRoundtrip(a: O3[A]): Boolean = a.contains(None)
    }

  val option3 = check[O3[Int]]


  val missing = forAllG(pathStringGen) { p =>
    ConfigReader[Option[Int]].read(Config.empty, p).exists(_.isEmpty)
  }

  val nestedOption = {
    val OO = ConfigReader[Option[Option[Int]]]
    val p1 = forAllG(pathStringGen) { p =>
      OO.read(Config.empty, p).exists(_.isEmpty)
    }
    val p2 = forAllG(pathStringGen) { p =>
      val config = ConfigFactory.parseString(s"$p = null")
      OO.read(config, p).exists(_.contains(None))
    }
    Properties.list(
      p1.toProperties("missing"),
      p2.toProperties("null")
    )
  }

  val optional = check[ju.Optional[Int]] x
    check[ju.Optional[ju.Optional[Int]]]("nested") x
    Properties.single(
      "append empty",
      forAll { (m: Map[String, ConfigValue], p: String) =>
        ConfigWriter[ju.Optional[Int]].append(m, p, ju.Optional.empty()) == m
      })

  val optionalInt = check[ju.OptionalInt] x
    Properties.single(
      "append empty",
      forAll { (m: Map[String, ConfigValue], p: String) =>
        ConfigWriter[ju.OptionalInt].append(m, p, ju.OptionalInt.empty()) == m
      })

  val optionalLong = check[ju.OptionalLong] x
    Properties.single(
      "append empty",
      forAll { (m: Map[String, ConfigValue], p: String) =>
        ConfigWriter[ju.OptionalLong].append(m, p, ju.OptionalLong.empty()) == m
      })

  val optionalDouble = check[ju.OptionalDouble] x
    Properties.single(
      "append empty",
      forAll { (m: Map[String, ConfigValue], p: String) =>
        ConfigWriter[ju.OptionalDouble].append(m, p, ju.OptionalDouble.empty()) == m
      })

}
