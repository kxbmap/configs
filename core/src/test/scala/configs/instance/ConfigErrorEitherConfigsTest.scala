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
import configs.util._
import configs.{ConfigError, ConfigErrorImplicits, Configs}
import scalaprops.Property._
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.anyVal._
import scalaz.std.string._
import scalaz.{Equal, \/}

object ConfigErrorEitherConfigsTest extends Scalaprops with ConfigErrorImplicits {

  val either = check[Either[ConfigError, Int]]

  val handleError = {
    val p1 = forAll {
      val config = ConfigFactory.parseString("value = null")
      val result = Configs[Either[ConfigError, Int]].get(config, "value")
      result.exists {
        case Left(ConfigError(_: ConfigError.NullValue, _)) => true
        case _ => false
      }
    }
    val p2 = forAll {
      val config = ConfigFactory.empty()
      val e = new RuntimeException
      implicit val configs: Configs[Int] = Configs.Try((_, _) => throw e)
      val result = Configs[Either[ConfigError, Int]].get(config, "value")
      result.exists {
        case Left(ConfigError(ConfigError.Exceptional(`e`, _), _)) => true
        case _ => false
      }
    }
    val p3 = forAll { s: String =>
      val config = ConfigFactory.empty()
      implicit val configs: Configs[Int] = Configs.failure(s)
      val result = Configs[Either[ConfigError, Int]].get(config, "value")
      result.exists {
        case Left(ConfigError(ConfigError.Generic(m, _), _)) => m == s
        case _ => false
      }
    }
    Properties.list(
      p1.toProperties("null value"),
      p2.toProperties("runtime exception"),
      p3.toProperties("failure")
    )
  }

  implicit def eitherGen[A: Gen]: Gen[Either[ConfigError, A]] =
    Gen[A].map(Right(_))

  implicit def eitherEqual[A: Equal]: Equal[Either[ConfigError, A]] =
    Equal[ConfigError \/ A].contramap(\/.fromEither)

  implicit def eitherToConfigValue[A: ToConfigValue]: ToConfigValue[Either[ConfigError, A]] =
    ToConfigValue[Option[A]].contramap(_.right.toOption)

}
