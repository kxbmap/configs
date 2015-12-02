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

import com.typesafe.config.ConfigException
import configs.util._
import scala.util.control.NoStackTrace
import scalaprops.{Cogen, CogenState, Gen, Scalaprops, scalazlaws}
import scalaz.{Equal, IList, NonEmptyList, Semigroup}

object ConfigErrorTest extends Scalaprops with ConfigErrorImplicits {

  val laws = scalazlaws.semigroup.all[ConfigError]

}

trait ConfigErrorImplicits {

  implicit lazy val configErrorInstances: Semigroup[ConfigError] =
    _ + _

  implicit lazy val configErrorEqual: Equal[ConfigError] =
    Equal.equalA[ConfigError]


  private case class M(m: String) extends ConfigException.Missing(m) with NoStackTrace

  private case class C(m: String) extends ConfigException(m) with NoStackTrace

  private case class G(m: String) extends RuntimeException(m) with NoStackTrace

  implicit lazy val singleConfigErrorGen: Gen[ConfigError.Single] =
    Gen.oneOf(
      Gen[String].map(M).map(ConfigError.Missing),
      Gen[String].map(C).map(ConfigError.Config),
      Gen[String].map(G).map(ConfigError.Generic)
    )

  implicit lazy val configErrorGen: Gen[ConfigError] =
    Gen.frequency(
      80 -> Gen[ConfigError.Single].map(e => e),
      20 -> Gen[NonEmptyList[ConfigError.Single]].mapSize(_ / 2).map(xs => ConfigError.Compose(xs.head, xs.tail.toList))
    )

  implicit lazy val configErrorCogen: Cogen[ConfigError] =
    new Cogen[ConfigError] {
      def cogen[B](a: ConfigError, g: CogenState[B]): CogenState[B] =
        a match {
          case e@ConfigError.Missing(_)  => Cogen[String].cogen(s"m:${e.messages}", g)
          case e@ConfigError.Config(_)   => Cogen[String].cogen(s"c:${e.messages}", g)
          case e@ConfigError.Generic(_)  => Cogen[String].cogen(s"g:${e.messages}", g)
          case ConfigError.Compose(h, t) => Cogen[IList[ConfigError]].cogen(IList(h +: t: _*), g)
        }
    }

}
