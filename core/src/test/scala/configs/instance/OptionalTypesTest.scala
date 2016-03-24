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
import configs.Configs
import configs.testutil.fun._
import configs.testutil.instance.anyVal._
import configs.testutil.instance.option._
import configs.testutil.instance.string._
import java.{util => ju}
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}

object OptionalTypesTest extends Scalaprops {

  val option = check[Option[Int]]

  val option2 = check[Option[Option[Int]]]

  val option3 = checkExcept[Option[Option[Option[Int]]]](_.contains(None))

  val missing = forAll { p: String =>
    val config = ConfigFactory.empty()
    Configs[Option[Int]].get(config, q(p)).exists(_.isEmpty)
  }

  val nestedOption = {
    val OO = Configs[Option[Option[Int]]]
    val p1 = forAll { p: String =>
      val config = ConfigFactory.empty()
      OO.get(config, q(p)).exists(_.isEmpty)
    }
    val p2 = forAll { p: String =>
      val config = ConfigFactory.parseString(s"${q(p)} = null")
      OO.get(config, q(p)).exists(_.contains(None))
    }
    Properties.list(
      p1.toProperties("missing"),
      p2.toProperties("null")
    )
  }

  val optional = check[ju.Optional[Int]]

  val optionalInt = check[ju.OptionalInt]

  val optionalLong = check[ju.OptionalLong]

  val optionalDouble = check[ju.OptionalDouble]

}
