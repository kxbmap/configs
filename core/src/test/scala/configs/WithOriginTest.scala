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

package configs

import com.typesafe.config.ConfigFactory
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object WithOriginTest extends Scalaprops {

  val value = forAll { (n: Int) =>
    val config = ConfigFactory.parseString(s"a = $n")
    val result = ConfigReader[WithOrigin[Int]].read(config, "a")
    result == Result.successful(WithOrigin(n, ConfigOrigin.simple("String").withLineNumber(1)))
  }

  val `null` = forAll {
    val config = ConfigFactory.parseString("a = null")
    val result = ConfigReader[WithOrigin[Option[Int]]].read(config, "a")
    result == Result.successful(WithOrigin(None, ConfigOrigin.simple("String").withLineNumber(1)))
  }

  val missing = forAll {
    val config = ConfigFactory.empty()
    val result = ConfigReader[WithOrigin[Option[Int]]].read(config, "a")
    result == Result.successful(WithOrigin(None, ConfigOrigin.simple("empty config")))
  }

}
