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

import com.typesafe.config.{Config, ConfigValueFactory}
import configs.util._
import scala.collection.convert.decorateAsJava._
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}
import scalaz.std.string._

object ConfigOpsTest extends Scalaprops {

  val throws = {
    import syntax.throws._
    Properties.list(
      extract(_.extract[Map[String, java.lang.Integer]] == _),
      get(_.get[Int](_) == _),
      getOpt(_.getOpt[Int](_) == _),
      getOrElse(_.getOrElse[Int](_, _) == _)
    )
  }

  val accumulate = {
    import syntax.accumulate._
    Properties.list(
      extract(_.extract[Map[String, java.lang.Integer]] == Result.successful(_)),
      get(_.get[Int](_) == Result.successful(_)),
      getOpt(_.getOpt[Int](_) == Result.successful(_)),
      getOrElse(_.getOrElse[Int](_, _) == Result.successful(_))
    )
  }

  private def extract(check: (Config, Map[String, java.lang.Integer]) => Boolean) =
    forAll { m: Map[String, java.lang.Integer] =>
      val config = ConfigValueFactory.fromMap(m.asJava).toConfig
      check(config, m)
    }.toProperties("extract")

  private def get(check: (Config, String, Int) => Boolean) =
    forAll { n: Int =>
      val p = "path"
      val config = n.toConfigValue.atKey(p)
      check(config, p, n)
    }.toProperties("get")

  private def getOpt(check: (Config, String, Option[Int]) => Boolean) =
    forAll { n: Option[Int] =>
      val p = "path"
      val config = n.toConfigValue.atKey(p)
      check(config, p, n)
    }.toProperties("getOpt")

  private def getOrElse(check: (Config, String, Int, Int) => Boolean) =
    forAll { (n: Option[Int], m: Int) =>
      val p = "path"
      val config = n.toConfigValue.atKey(p)
      check(config, p, m, n.getOrElse(m))
    }.toProperties("getOrElse")

}
