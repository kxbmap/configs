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
import scala.collection.JavaConverters._
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{Gen, Properties, Scalaprops}

object ConfigReaderTest extends Scalaprops {

  val get = forAllG(pathStringGen, Gen[Int]) { (p, v) =>
    val config = ConfigFactory.parseString(s"$p = $v")
    val reader: ConfigReader[Int] = ConfigReader.fromTry(_.getInt(_))
    reader.read(config, p).contains(v)
  }

  val extract = forAll { (p: String, v: Int) =>
    val config = ConfigFactory.parseString(s"${q(p)} = $v")
    val reader: ConfigReader[Map[String, Int]] = ConfigReader.fromTry {
      _.getConfig(_).root().asScala.mapValues(_.unwrapped().asInstanceOf[Int]).toMap
    }
    reader.extract(config).contains(Map(p -> v))
  }

  val extractValue = forAll { v: Int =>
    val cv = ConfigValue.fromAny(v)
    val reader: ConfigReader[Int] = ConfigReader.fromTry(_.getInt(_))
    cv.flatMap(reader.extractValue(_)).contains(v)
  }


  private def extractor(v: Int): ConfigReader[Int] =
    new ConfigReader[Int] {
      protected def readImpl(config: Config, path: String): Result[Int] =
        Result.failure(ConfigError("failure"))
      override def extract(config: Config, key: String): Result[Int] =
        Result.successful(v)
      override def extractValue(value: ConfigValue, key: String): Result[Int] =
        Result.successful(v)
    }

  val map = Properties.list(
    Properties.single(
      "map",
      forAll { (v: Int, f: Int => String) =>
        val config = ConfigFactory.parseString(s"v = $v")
        val reader: ConfigReader[Int] = ConfigReader.fromTry(_.getInt(_))
        reader.map(f).read(config, "v").contains(f(v))
      }),
    Properties.single(
      "respect original impl",
      forAll { (v: Int, f: Int => String) =>
        val reader = extractor(v)
        reader.map(f).extract(Config.empty).contains(f(v)) &&
          reader.map(f).extractValue(ConfigValue.Null).contains(f(v))
      }))

  val rmap = Properties.list(
    Properties.single(
      "rmap",
      forAll { (v: Int, f: Int => Result[String]) =>
        val config = ConfigFactory.parseString(s"v = $v")
        val reader: ConfigReader[Int] = ConfigReader.fromTry(_.getInt(_))
        reader.rmap(f).read(config, "v") == f(v).pushPath("v")
      }),
    Properties.single(
      "respect original impl",
      forAll { (v: Int, f: Int => Result[String]) =>
        val reader = extractor(v)
        reader.rmap(f).extract(Config.empty) == f(v).pushPath("extract") &&
          reader.rmap(f).extractValue(ConfigValue.Null) == f(v).pushPath("extract")
      }))

  val flatMap = Properties.list(
    Properties.single(
      "flatMap", {
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
      }),
    Properties.single(
      "respect original impl",
      forAll { (v: Int, f0: Int => Result[String]) =>
        val reader = extractor(v)
        val f = f0.andThen(ConfigReader.withResult)
        reader.flatMap(f).extract(Config.empty) == f0(v).pushPath("extract") &&
          reader.flatMap(f).extractValue(ConfigValue.Null) == f0(v).pushPath("extract")
      }))

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
    val p5 = forAll { (v: Int) =>
      val reader = extractor(v)
      reader.orElse(fail).extract(Config.empty).contains(v) &&
        reader.orElse(fail).extractValue(ConfigValue.Null).contains(v)
    }
    Properties.list(
      p1.toProperties("succ orElse succ"),
      p2.toProperties("succ orElse fail"),
      p3.toProperties("fail orElse succ"),
      p4.toProperties("fail orElse fail"),
      p5.toProperties("respect original impl")
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
