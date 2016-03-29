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
import configs.testutil.instance.string._
import scala.collection.JavaConversions._
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{Gen, Properties, Scalaprops}

object ConfigsTest extends Scalaprops {

  val get = forAllG(pathStringGen, Gen[Int]) { (p, v) =>
    val config = ConfigFactory.parseString(s"$p = $v")
    val configs: Configs[Int] = Configs.fromTry(_.getInt(_))
    configs.get(config, p).exists(_ == v)
  }

  val extractConfig = forAll { (p: String, v: Int) =>
    val config = ConfigFactory.parseString(s"${q(p)} = $v")
    val configs: Configs[Map[String, Int]] = Configs.fromTry {
      _.getConfig(_).root().mapValues(_.unwrapped().asInstanceOf[Int]).toMap
    }
    configs.extract(config).exists(_ == Map(p -> v))
  }

  val extractConfigValue = forAll { v: Int =>
    val cv = ConfigValue.from(v)
    val configs: Configs[Int] = Configs.fromTry(_.getInt(_))
    configs.extractValue(cv).exists(_ == v)
  }

  val map = forAll { (v: Int, f: Int => String) =>
    val config = ConfigFactory.parseString(s"v = $v")
    val configs: Configs[Int] = Configs.fromTry(_.getInt(_))
    configs.map(f).get(config, "v").exists(_ == f(v))
  }

  val flatMap = {
    case class Foo(a: Int, b: String, c: Boolean)
    val configs = for {
      a <- Configs.get[Int]("a")
      b <- Configs.get[String]("b")
      c <- Configs.get[Boolean]("c")
    } yield Foo(a, b, c)

    forAll { (a: Int, b: String, c: Boolean) =>
      val config = ConfigFactory.parseString(
        s"""a = $a
           |b = ${q(b)}
           |c = $c
           |""".stripMargin)
      configs.extract(config).exists(_ == Foo(a, b, c))
    }
  }

  val orElse = {
    val config = Config.empty
    val fail: Configs[Int] = Configs.failure("failure")
    val p1 = forAll { (v1: Int, v2: Int) =>
      val succ1: Configs[Int] = Configs.successful(v1)
      val succ2: Configs[Int] = Configs.successful(v2)
      succ1.orElse(succ2).get(config, "dummy").exists(_ == v1)
    }
    val p2 = forAll { (v: Int) =>
      val succ: Configs[Int] = Configs.successful(v)
      succ.orElse(fail).get(config, "dummy").exists(_ == v)
    }
    val p3 = forAll { (v: Int) =>
      val succ: Configs[Int] = Configs.successful(v)
      fail.orElse(succ).get(config, "dummy").exists(_ == v)
    }
    val p4 = forAll {
      fail.orElse(fail).get(config, "dummy").failed.exists(
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
    val configs = Configs.fromConfigTry(_.getInt(p))
    configs.extract(config).failed.exists {
      case ConfigError(_: ConfigError.NullValue, t) if t.isEmpty => true
    }
  }

  val handleException = forAll {
    val re = new RuntimeException()
    val configs = Configs.fromTry((_, _) => throw re)
    configs.extract(Config.empty).failed.exists {
      case ConfigError(ConfigError.Exceptional(e, _), t) if t.isEmpty => e eq re
      case _ => false
    }
  }

  val withPath = {
    val int: Configs[Int] = (c, p) => Result.Try(c.getInt(p))
    def config(p: String) = ConfigFactory.parseString(s"$p = not a number")
    val p1 = forAllG(pathStringGen) { p =>
      val wp = int.withPath
      val result = wp.get(config(p), p)
      result.failed.exists(_.head.paths == List(p))
    }
    val p2 = forAllG(pathStringGen) { p =>
      val wp = int.withPath.withPath
      val result = wp.get(config(p), p)
      result.failed.exists(_.head.paths == List(p))
    }
    Properties.list(
      p1.toProperties("once"),
      p2.toProperties("twice")
    )
  }

}
