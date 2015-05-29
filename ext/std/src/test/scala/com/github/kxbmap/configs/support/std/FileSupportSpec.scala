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

package com.github.kxbmap.configs.support.std

import com.github.kxbmap.configs._
import com.typesafe.config.ConfigFactory
import java.io.File

class FileSupportSpec extends UnitSpec with FileSupport {

  describe("java.io.File support") {
    val c = ConfigFactory.parseString(
      """a="path/to/file"
        |b= ["a", "b/c"]""".stripMargin)

    it("should be available to get a value") {
      assert(c.get[File]("a") == (new File("path/to/file"): File))
    }

    it("should be available to get values as list") {
      assert(c.get[List[File]]("b") === (List(new File("a"), new File("b/c")): List[File]))
    }

    it("should be available to get values as vector") {
      assert(c.get[Vector[File]]("b") === (Vector(new File("a"), new File("b/c")): Vector[File]))
    }
  }
}
