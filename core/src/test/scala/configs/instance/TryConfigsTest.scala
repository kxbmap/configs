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

import com.typesafe.config.{ConfigException, ConfigFactory}
import configs.Configs
import configs.util._
import scala.util.{Failure, Success, Try}
import scalaprops.Property._
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Equal
import scalaz.std.anyVal._
import scalaz.std.option._
import scalaz.std.string._

@deprecated("", "0.4.1")
object TryConfigsTest extends Scalaprops {

  val `try` = check[Try[Int]]

  val handleError = {
    val p1 = forAll {
      val config = ConfigFactory.parseString("value = null")
      val result = Configs[Try[Int]].get(config, "value")
      result.exists {
        case Failure(e: ConfigException.Null) => true
        case _ => false
      }
    }
    val p2 = forAll {
      val config = ConfigFactory.empty()
      val e = new RuntimeException
      implicit val configs: Configs[Int] = Configs.fromTry((_, _) => throw e)
      val result = Configs[Try[Int]].get(config, "value")
      result.exists(_ == Failure(e))
    }
    val p3 = forAll { s: String =>
      val config = ConfigFactory.empty()
      implicit val configs: Configs[Int] = Configs.failure(s)
      val result = Configs[Try[Int]].get(config, "value")
      result.exists {
        case Failure(e: ConfigException) => e.getMessage == s
        case _ => false
      }
    }
    Properties.list(
      p1.toProperties("null value"),
      p2.toProperties("runtime exception"),
      p3.toProperties("message as ConfigException")
    )
  }

  implicit def tryGen[A: Gen]: Gen[Try[A]] =
    Gen.option[A].map {
      case Some(v) => Success(v)
      case None    => Failure(new RuntimeException("dummy"))
    }

  implicit def tryEqual[A: Equal]: Equal[Try[A]] =
    Equal[Option[A]].contramap(_.toOption)

  implicit def tryToConfigValue[A: ToConfigValue]: ToConfigValue[Try[A]] =
    ToConfigValue[Option[A]].contramap(_.toOption)

}
