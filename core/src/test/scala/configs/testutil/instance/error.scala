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

package configs.testutil.instance

import com.typesafe.config.ConfigException
import configs.testutil.instance.string._
import configs.{Config, ConfigError}
import scala.util.control.NoStackTrace
import scalaprops.{Cogen, CogenState, Gen}
import scalaprops.ScalapropsScalaz._
import scalaz.{Equal, NonEmptyList, Semigroup}

object error {

  implicit lazy val configErrorSemigroup: Semigroup[ConfigError] =
    new Semigroup[ConfigError] {
      def append(e1: ConfigError, e2: => ConfigError): ConfigError = e1 + e2
    }

  implicit lazy val configErrorEqual: Equal[ConfigError] =
    Equal.equalA[ConfigError]


  private case class N(n: Int) extends ConfigException.Null(Config.empty.origin(), "p", null) with NoStackTrace

  private case class E(n: Int) extends RuntimeException(n.toString) with NoStackTrace

  implicit lazy val configErrorEntryGen: Gen[ConfigError.Entry] =
    Gen.oneOf(
      Gen[Int].map(N).map(ConfigError.NullValue(_)),
      Gen[Int].map(E).map(ConfigError.Exceptional(_)),
      Gen[String].map(ConfigError.Generic(_))
    )

  implicit lazy val configErrorEntryCogen: Cogen[ConfigError.Entry] =
    new Cogen[ConfigError.Entry] {
      def cogen[B](a: ConfigError.Entry, g: CogenState[B]): CogenState[B] =
        a match {
          case ConfigError.NullValue(N(n), _) => Cogen[Int].cogen(n * 2, g)
          case ConfigError.Exceptional(E(n), _) => Cogen[Int].cogen(n * 2 + 1, g)
          case ConfigError.Generic(m, _) => Cogen[String].cogen(m, g)
          case _ => sys.error("bug or broken")
        }
    }

  implicit lazy val configErrorGen: Gen[ConfigError] =
    Gen[NonEmptyList[ConfigError.Entry]].map(xs => ConfigError(xs.head, xs.tail.toVector))

  implicit lazy val configErrorCogen: Cogen[ConfigError] =
    Cogen[NonEmptyList[ConfigError.Entry]].contramap(e => NonEmptyList(e.head, e.tail: _*))

}
