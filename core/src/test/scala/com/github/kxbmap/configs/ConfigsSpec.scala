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

import com.typesafe.config.{Config, ConfigException, ConfigFactory, ConfigMemorySize}
import scala.concurrent.duration._
import scala.util.control.ControlThrowable
import scala.util.{Success, Try}

class ConfigsSpec extends UnitSpec {

  import ConfigFactory.parseString

  describe("Configs") {

    it("should be a instance of Functor") {
      val c = parseString("value = 42")
      val cs = Configs.configs(c => c.getInt("value"))

      assert(cs.map(identity).extract(c) === identity(cs).extract(c))

      val f = (_: Int) * 2
      val g = (_: Int).toLong
      assert(cs.map(g compose f).extract(c) === cs.map(f).map(g).extract(c))
    }

    describe("conversions") {
      implicit val cs: Configs[A] = Configs.configs(_ => A)

      val c = parseString(
        """a = {}
          |b = [{}, {}]
          |""".stripMargin)

      it("should be available for AtPath[A]") {
        assert(c.get[A]("a") === A)
      }

      it("should be available for AtPath[List[A]]") {
        assert(c.get[List[A]]("b") === List(A, A))
      }
    }

    describe("instances") {

      describe("for Int") {
        val c = parseString(
          """a = 42
            |b = [42, 100]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[Int]("a") === 42)
        }

        it("should be available to get values as list") {
          assert(c.get[List[Int]]("b") === List(42, 100))
        }
      }

      describe("for Long") {
        val c = parseString(
          """a = 42
            |b = [42, 100]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[Long]("a") === 42L)
        }
        it("should be available to get values as list") {
          assert(c.get[List[Long]]("b") === List(42L, 100L))
        }
      }

      describe("for Double") {
        val c = parseString(
          """a = 42.195
            |b = [2.3, 42]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[Double]("a") === 42.195)
        }
        it("should be available to get values as list") {
          assert(c.get[List[Double]]("b") === List(2.3, 42d))
        }
      }

      describe("for Boolean") {
        val c = parseString(
          """a = true
            |b = [on, off]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[Boolean]("a") === true)
        }
        it("should be available to get values as list") {
          assert(c.get[List[Boolean]]("b") === List(true, false))
        }
      }

      describe("for String") {
        val c = parseString(
          """a = foo
            |b = [Hello, World]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[String]("a") === "foo")
        }
        it("should be available to get values as list") {
          assert(c.get[List[String]]("b") === List("Hello", "World"))
        }
      }

      describe("for Config") {
        val c = parseString("a = foo")

        it("should be extract value") {
          assert(c.extract[Config] === c)
        }
      }

      describe("for Map[String, T]") {
        val c = parseString(
          """a = 1
            |b = 2
            |""".stripMargin)

        it("should be extract value") {
          assert(c.extract[Map[String, Int]] === Map("a" -> 1, "b" -> 2))
        }
      }

      describe("for Map[Symbol, T]") {
        val c = parseString(
          """a = 1
            |b = 2
            |""".stripMargin)

        it("should be extract value") {
          assert(c.extract[Map[Symbol, Int]] === Map('a -> 1, 'b -> 2))
        }
      }

      describe("for Symbol") {
        val c = parseString(
          """a = foo
            |b = [Hello, World]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[Symbol]("a") === 'foo)
        }
        it("should be available to get values as list") {
          assert(c.get[List[Symbol]]("b") === List('Hello, 'World))
        }
      }

      describe("for Duration") {
        val c = parseString(
          """a = 10days
            |b = [1ms, 42h]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[Duration]("a") === 10.days)
        }
        it("should be available to get values as list") {
          assert(c.get[List[Duration]]("b") === List(1.milli, 42.hours))
        }
      }

      describe("for FiniteDuration") {
        val c = parseString(
          """a = 10days
            |b = [1ms, 42h]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[FiniteDuration]("a") === 10.days)
        }
        it("should be available to get values as list") {
          assert(c.get[List[FiniteDuration]]("b") === List(1.milli, 42.hours))
        }
      }

      describe("for java.time.Duration") {
        val c = parseString(
          """a = 10days
            |b = [1ms, 42h]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[java.time.Duration]("a") === java.time.Duration.ofDays(10))
        }
        it("should be available to get values as list") {
          assert(c.get[List[java.time.Duration]]("b") === List(java.time.Duration.ofMillis(1), java.time.Duration.ofHours(42)))
        }
      }

      describe("for ConfigMemorySize") {
        val c = parseString(
          """a = 42M
            |b = [1KiB, 1024b]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[ConfigMemorySize]("a") === ConfigMemorySize.ofBytes(42 * 1024 * 1024))
        }
        it("should be available to get values as list") {
          assert(c.get[List[ConfigMemorySize]]("b") === List(ConfigMemorySize.ofBytes(1024), ConfigMemorySize.ofBytes(1024)))
        }
      }

      describe("for Either") {
        val c = parseString(
          """a = foo
            |b = [Hello, World]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[Either[Throwable, String]]("a") === Right("foo"))
        }

        it("should be available to get values as list") {
          assert(c.get[Either[Throwable, List[String]]]("b") === Right(List("Hello", "World")))
        }

        implicit val cs = Configs.atPath[A]((_, _) => throw FatalError)

        describe("with Throwable") {
          it("should catch all error") {
            assert(c.get[Either[Throwable, A]]("a") === Left(FatalError))
          }
        }

        describe("with FatalError") {
          it("should catch FatalError") {
            assert(c.get[Either[FatalError, A]]("a") === Left(FatalError))
          }

          it("should not catch others") {
            intercept[ConfigException.WrongType] {
              c.get[Either[FatalError, Int]]("a")
            }
          }
        }
      }

      describe("for Try") {
        val c = parseString(
          """a = foo
            |b = [Hello, World]
            |""".stripMargin)

        it("should be available to get a value") {
          assert(c.get[Try[String]]("a") === Success("foo"))
        }

        it("should be available to get values as list") {
          assert(c.get[Try[List[String]]]("b") === Success(List("Hello", "World")))
        }

        it("should catch non fatal error") {
          assert(c.get[Try[Int]]("a").isFailure)
        }

        implicit val cs = Configs.atPath[A]((_, _) => throw new FatalError())

        it("should not catch fatal error") {
          intercept[FatalError] {
            c.get[Try[A]]("a")
          }
        }
      }
    }
  }


  sealed trait A

  case object A extends A

  class FatalError extends ControlThrowable

  object FatalError extends FatalError

}
