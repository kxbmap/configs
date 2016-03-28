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
import configs.ConfigUtil.{quoteString => q}
import configs.testutil.fun._
import configs.testutil.instance.anyVal._
import configs.testutil.instance.option._
import configs.testutil.instance.string._
import configs.{Config, Configs}
import java.{util => ju}
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}

object OptionalTypesTest extends Scalaprops {

  val option = check[Option[Int]]

  val option2 = check[Option[Option[Int]]]

  type O3[A] = Option[Option[Option[A]]]

  implicit def option3CheckParam[A]: CheckParam[O3[A]] =
    new CheckParam[O3[A]] {
      override def exceptEncodeDecode(a: O3[A]): Boolean = a.contains(None)
    }

  val option3 = check[O3[Int]]


  val missing = forAll { p: String =>
    Configs[Option[Int]].get(Config.empty, q(p)).exists(_.isEmpty)
  }

  val nestedOption = {
    val OO = Configs[Option[Option[Int]]]
    val p1 = forAll { p: String =>
      OO.get(Config.empty, q(p)).exists(_.isEmpty)
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
