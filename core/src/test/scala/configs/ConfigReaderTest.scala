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
import configs.ConfigUtil.{quoteString => q}
import configs.testutil.instance.result._
import configs.testutil.instance.string._
import scala.collection.JavaConversions._
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{Gen, Properties, Scalaprops}

object ConfigReaderTest extends Scalaprops {

  val get = forAllG(pathStringGen, Gen[Int]) { (p, v) =>
    val config = ConfigFactory.parseString(s"$p = $v")
    val reader: ConfigReader[Int] = ConfigReader.fromTry(_.getInt(_))
    reader.read(config, p).contains(v)
  }

  val extractConfig = forAll { (p: String, v: Int) =>
    val config = ConfigFactory.parseString(s"${q(p)} = $v")
    val reader: ConfigReader[Map[String, Int]] = ConfigReader.fromTry {
      _.getConfig(_).root().mapValues(_.unwrapped().asInstanceOf[Int]).toMap
    }
    reader.extract(config).contains(Map(p -> v))
  }

  val extractConfigValue = forAll { v: Int =>
    val cv = ConfigValue.from(v)
    val reader: ConfigReader[Int] = ConfigReader.fromTry(_.getInt(_))
    reader.extractValue(cv).contains(v)
  }

  val map = forAll { (v: Int, f: Int => String) =>
    val config = ConfigFactory.parseString(s"v = $v")
    val reader: ConfigReader[Int] = ConfigReader.fromTry(_.getInt(_))
    reader.map(f).read(config, "v").contains(f(v))
  }

  val rmap = forAll { (v: Int, f: Int => Result[String]) =>
    val config = ConfigFactory.parseString(s"v = $v")
    val reader: ConfigReader[Int] = ConfigReader.fromTry(_.getInt(_))
    reader.rmap(f).read(config, "v") == f(v).pushPath("v")
  }

  val flatMap = {
    case class Foo(a: Int, b: String, c: Boolean)
    val reader = for {
      a <- ConfigReader.get[Int]("a")
      b <- ConfigReader.get[String]("b")
      c <- ConfigReader.get[Boolean]("c")
    } yield Foo(a, b, c)

    forAll { (a: Int, b: String, c: Boolean) =>
      val config = ConfigFactory.parseString(
        s"""a = $a
           |b = ${q(b)}
           |c = $c
           |""".stripMargin)
      reader.extract(config).contains(Foo(a, b, c))
    }
  }

  val orElse = {
    val config = Config.empty
    val fail: ConfigReader[Int] = ConfigReader.failure("failure")
    val p1 = forAll { (v1: Int, v2: Int) =>
      val succ1: ConfigReader[Int] = ConfigReader.successful(v1)
      val succ2: ConfigReader[Int] = ConfigReader.successful(v2)
      succ1.orElse(succ2).read(config, "dummy").contains(v1)
    }
    val p2 = forAll { (v: Int) =>
      val succ: ConfigReader[Int] = ConfigReader.successful(v)
      succ.orElse(fail).read(config, "dummy").contains(v)
    }
    val p3 = forAll { (v: Int) =>
      val succ: ConfigReader[Int] = ConfigReader.successful(v)
      fail.orElse(succ).read(config, "dummy").contains(v)
    }
    val p4 = forAll {
      fail.orElse(fail).read(config, "dummy").failed.exists(
        _.messages == Seq("[dummy] failure")
      )
    }
    Properties.list(
      p1.toProperties("succ orElse succ"),
      p2.toProperties("succ orElse fail"),
      p3.toProperties("fail orElse succ"),
      p4.toProperties("fail orElse fail")
    )
  }

  val handleNullValue = forAllG(pathStringGen) { p =>
    val config = ConfigFactory.parseString(s"$p = null")
    val reader = ConfigReader.fromConfigTry(_.getInt(p))
    reader.extract(config).failed.exists {
      case ConfigError(_: ConfigError.NullValue, t) if t.isEmpty => true
      case _ => false
    }
  }

  val handleException = forAll {
    val re = new RuntimeException()
    val reader = ConfigReader.fromTry((_, _) => throw re)
    reader.extract(Config.empty).failed.exists {
      case ConfigError(ConfigError.Exceptional(e, _), t) if t.isEmpty => e eq re
      case _ => false
    }
  }

}
