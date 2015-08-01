/*
 * Copyright 2013-2015 Tsukasa Kitachi
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

package com.github.kxbmap.configs

import com.typesafe.config._
import scala.collection.JavaConverters._
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.string._

object ConfigsTest extends Scalaprops {

  implicit val stringGen: Gen[String] = Gen.asciiString

  val get = forAll { (p: String, v: Int) =>
    val q = ConfigUtil.quoteString(p)
    val config = ConfigFactory.parseString(s"$q = $v")
    val configs: Configs[Int] = (_, _) => v
    configs.get(config, q) == v
  }

  val extractC = forAll { (p: String, v: Int) =>
    val q = ConfigUtil.quoteString(p)
    val config = ConfigFactory.parseString(s"$q = $v")
    val configs: Configs[Map[String, Int]] = _.getConfig(_).root().asScala.mapValues(_.unwrapped().asInstanceOf[Int]).toMap
    configs.extract(config) == Map(p -> v)
  }

  val extractV = forAll { v: Int =>
    val cv = ConfigValueFactory.fromAnyRef(v)
    val configs: Configs[Int] = _.getInt(_)
    configs.extract(cv) == v
  }

  val map = {
    val identity = forAll { (p: String, v: Int) =>
      val q = ConfigUtil.quoteString(p)
      val config = ConfigFactory.parseString(s"$q = $v")
      val configs: Configs[Int] = _.getInt(_)
      configs.map(a => a).get(config, q) == configs.get(config, q)
    }
    val composite = forAll { (p: String, v: Int, f: Int => String, g: String => Long) =>
      val q = ConfigUtil.quoteString(p)
      val config = ConfigFactory.parseString(s"$q = $v")
      val configs: Configs[Int] = _.getInt(_)
      configs.map(f).map(g).get(config, q) == (g compose f)(configs.get(config, q))
    }
    Properties.list(
      identity.toProperties("identity"),
      composite.toProperties("composite")
    )
  }

  val orElse = {
    val config = ConfigFactory.empty()
    val ce: Configs[Int] = (_, _) => throw new ConfigException.Generic("CE")
    val re: Configs[Int] = (_, _) => throw new RuntimeException("RE")

    val p1 = forAll { (v: Int) =>
      val cv: Configs[Int] = (_, _) => v
      cv.orElse(ce).get(config, "dummy") == v
    }
    val p2 = forAll { (v: Int) =>
      val cv: Configs[Int] = (_, _) => v
      ce.orElse(cv).get(config, "dummy") == v
    }
    val p3 = forAll {
      try {
        ce.orElse(ce).get(config, "dummy")
        false
      } catch {
        case e: ConfigException => e.getMessage == "CE"
      }
    }
    val p4 = forAll {
      val cv: Configs[Int] = (_, _) => 42
      try {
        re.orElse(cv).get(config, "dummy")
        false
      } catch {
        case e: RuntimeException => e.getMessage == "RE"
      }
    }
    Properties.list(
      p1.toProperties("value orElse CE"),
      p2.toProperties("CE orElse value"),
      p3.toProperties("CE orElse CE"),
      p4.toProperties("RE orElse value")
    )
  }

}
