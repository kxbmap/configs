/*
 * Copyright 2013 Tsukasa Kitachi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.kxbmap.configs
package std

import com.typesafe.config.ConfigFactory
import java.io.File
import java.nio.file.{Path, Paths}
import org.scalatest.{Matchers, FunSpec}

class JavaIOSupportSpec extends FunSpec with Matchers {

  val support = new JavaIOSupport {}

  import support._

  describe("java.io.File support") {
    val c = ConfigFactory.parseString(
      """a="path/to/file"
        |b= ["a", "b/c"]""".stripMargin)

    it ("should be available to get a value") {
      c.get[File]("a") shouldBe new File("path/to/file")
    }

    it ("should be available to get values as list") {
      c.get[List[File]]("b") shouldBe List(new File("a"), new File("b/c"))
    }
  }

  describe("java.nio.file.Path support") {
    val c = ConfigFactory.parseString(
      """a="path/to/file"
        |b= ["a", "b/c"]""".stripMargin)

    it ("should be available to get a value") {
      c.get[Path]("a") shouldBe Paths.get("path", "to", "file")
    }

    it ("should be available to get values as list") {
      c.get[List[Path]]("b") shouldBe List(Paths.get("a"), Paths.get("b", "c"))
    }
  }
}
