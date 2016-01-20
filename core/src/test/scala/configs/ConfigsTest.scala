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

import com.typesafe.config.{ConfigException, ConfigFactory, ConfigValueFactory}
import configs.util._
import scala.collection.JavaConversions._
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.string._

object ConfigsTest extends Scalaprops {

  val get = forAll { (p: String, v: Int) =>
    val config = ConfigFactory.parseString(s"${q(p)} = $v")
    val configs: Configs[Int] = Configs.fromTry(_.getInt(_))
    configs.get(config, q(p)).exists(_ == v)
  }

  val extractC = forAll { (p: String, v: Int) =>
    val config = ConfigFactory.parseString(s"${q(p)} = $v")
    val configs: Configs[Map[String, Int]] = Configs.fromTry {
      _.getConfig(_).root().mapValues(_.unwrapped().asInstanceOf[Int]).toMap
    }
    configs.extract(config).exists(_ == Map(p -> v))
  }

  val extractV = forAll { v: Int =>
    val cv = ConfigValueFactory.fromAnyRef(v)
    val configs: Configs[Int] = Configs.fromTry(_.getInt(_))
    configs.extract(cv).exists(_ == v)
  }

  val map = {
    val identity = forAll { (p: String, v: Int) =>
      val config = ConfigFactory.parseString(s"${q(p)} = $v")
      val configs: Configs[Int] = Configs.fromTry(_.getInt(_))
      configs.map(a => a).get(config, q(p)) == configs.get(config, q(p))
    }
    val composite = forAll { (p: String, v: Int, f: Int => String, g: String => Long) =>
      val config = ConfigFactory.parseString(s"${q(p)} = $v")
      val configs: Configs[Int] = Configs.fromTry(_.getInt(_))
      configs.map(f).map(g).get(config, q(p)) == configs.get(config, q(p)).map(g compose f)
    }
    Properties.list(
      identity.toProperties("identity"),
      composite.toProperties("composite")
    )
  }

  val flatMap = {
    val c0: Configs[String] = Configs.fromTry(_.getConfig(_).getString("type"))
    val c: Configs[Any] = c0.flatMap {
      case "int"    => Configs.fromTry(_.getConfig(_).getInt("value"))
      case "string" => Configs.fromTry(_.getConfig(_).getString("value"))
      case "bool"   => Configs.fromTry(_.getConfig(_).getBoolean("value"))
      case s        => throw new RuntimeException(s)
    }
    val g = Gen.oneOf[(String, Any)](
      Gen[Int].map("int" -> _),
      Gen[String].map("string" -> _),
      Gen[Boolean].map("bool" -> _)
    )
    forAllG(g) {
      case (t, v) =>
        val qv = v match {
          case s: String => q(s)
          case _         => v
        }
        val config = ConfigFactory.parseString(
          s"""type = $t
             |value = $qv
             |""".stripMargin)

        c.extract(config).exists(_ == v)
    }
  }

  val orElse = {
    val config = ConfigFactory.empty()
    val ce: Configs[Int] = Configs.fromTry((_, _) => throw new ConfigException.Generic("CE"))

    val p1 = forAll { (v: Int) =>
      val cv: Configs[Int] = Configs.fromTry((_, _) => v)
      cv.orElse(ce).get(config, "dummy").exists(_ == v)
    }
    val p2 = forAll { (v: Int) =>
      val cv: Configs[Int] = Configs.fromTry((_, _) => v)
      ce.orElse(cv).get(config, "dummy").exists(_ == v)
    }
    val p3 = forAll {
      ce.orElse(ce).get(config, "dummy").fold(
        e => e.messages == Seq("CE"),
        _ => false
      )
    }
    Properties.list(
      p1.toProperties("value orElse CE"),
      p2.toProperties("CE orElse value"),
      p3.toProperties("CE orElse CE")
    )
  }

}
