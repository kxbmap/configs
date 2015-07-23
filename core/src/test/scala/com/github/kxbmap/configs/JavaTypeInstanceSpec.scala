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

import com.typesafe.config.{ConfigException, ConfigFactory}
import java.io.File
import java.net.{InetSocketAddress, InetAddress}
import java.nio.file.{Paths, Path}

class JavaTypeInstanceSpec extends UnitSpec {

  describe("java.io.File support") {

    val c = ConfigFactory.parseString(
      """a="path/to/file"
        |b= ["a", "b/c"]""".stripMargin)

    it("should be available to get a value") {
      assert(c.get[File]("a") == new File("path/to/file"))
    }

    it("should be available to get values as list") {
      assert(c.get[List[File]]("b") === List(new File("a"), new File("b/c")))
    }

    it("should be available to get values as vector") {
      assert(c.get[Vector[File]]("b") === Vector(new File("a"), new File("b/c")))
    }
  }


  describe("java.nio.file.Path support") {

    val c = ConfigFactory.parseString(
      """a="path/to/file"
        |b= ["a", "b/c"]""".stripMargin)

    it("should be available to get a value") {
      assert(c.get[Path]("a") == Paths.get("path", "to", "file"))
    }

    it("should be available to get values as list") {
      assert(c.get[List[Path]]("b") === List(Paths.get("a"), Paths.get("b", "c")))
    }

    it("should be available to get values as vector") {
      assert(c.get[Vector[Path]]("b") === Vector(Paths.get("a"), Paths.get("b", "c")))
    }
  }


  describe("java.net.InetAddress support") {
    describe("(valid address)") {
      val c = ConfigFactory.parseString(
        """a = "192.168.0.1"
          |b = ["::1", "127.0.0.1"]
          |""".stripMargin)

      it("should be available to get a value") {
        assert(c.get[InetAddress]("a") == InetAddress.getByName("192.168.0.1"))
      }

      it("should be available to get values as list") {
        assert(c.get[List[InetAddress]]("b") === List(InetAddress.getByName("::1"), InetAddress.getByName("127.0.0.1")))
      }

      it("should be available to get values as vector") {
        assert(c.get[Vector[InetAddress]]("b") === Vector(InetAddress.getByName("::1"), InetAddress.getByName("127.0.0.1")))
      }
    }

    describe("(unknown host)") {
      val c = ConfigFactory.parseString(
        """a = "some-unknown-host"
          |b = ["::1", "some-unknown-host"]
          |""".stripMargin)

      it("should throw a ConfigException.BadValue") {
        intercept[ConfigException.BadValue] {
          c.get[InetAddress]("a")
        }
      }

      it("should throw a ConfigException.BadValue (List)") {
        intercept[ConfigException.BadValue] {
          c.get[List[InetAddress]]("b")
        }
      }
    }
  }

  describe("java.net.InetSocketAddress support") {
    describe("(valid address)") {
      describe("(addr)") {
        it("should be available to extract a value") {
          val c = ConfigFactory.parseString(
            """addr = "192.168.0.1"
              |port = 8080
              |""".stripMargin)

          assert(c.extract[InetSocketAddress] == new InetSocketAddress(InetAddress.getByName("192.168.0.1"), 8080))
        }
      }

      describe("(host)") {
        val c = ConfigFactory.parseString(
          """hostname = "192.168.0.1"
            |port = 8080
            |""".stripMargin)

        it("should be available to extract a value") {
          assert(c.extract[InetSocketAddress] == new InetSocketAddress(InetAddress.getByName("192.168.0.1"), 8080))
        }
      }
    }

    describe("(unknown host)") {
      describe("(addr)") {
        it("should throw a ConfigException.BadValue") {
          val c = ConfigFactory.parseString(
            """addr = "some-unknown-host"
              |port = 8080
              |""".stripMargin)

          intercept[ConfigException.BadValue] {
            c.extract[InetSocketAddress]
          }
        }
      }

      describe("(host)") {
        val c = ConfigFactory.parseString(
          """hostname = "some-unknown-host"
            |port = 8080
            |""".stripMargin)

        it("should be available to extract a value") {
          assert(c.extract[InetSocketAddress] == new InetSocketAddress("some-unknown-host", 8080))
        }
      }
    }
  }

}
