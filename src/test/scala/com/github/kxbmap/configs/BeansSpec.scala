/*
 * Copyright 2015 Philip L. McMahon
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
import java.util.{List => JList}
import org.scalatest.{FunSpec, Matchers}
import scala.beans.BeanProperty

class BeansSpec extends FunSpec with Matchers {

  import BeansSpec._

  class Inner extends Obj

  describe("generic bean support") {
    val c = ConfigFactory.parseString(
      """a.string=x
        |b.int=one
        |c.missing=prop
        |d.boolean=true
        |d.double=0.1
        |d.int=1
        |d.list=[foo, bar, baz]
        |d.long=2
        |d.string=x""".stripMargin)

    implicit val topConfigs = Beans[Top]
    implicit val innerConfigs = Beans(new Inner)

    it("should be available to get a value with a no-arg constructor") {
      val o = c.get[Top]("a")
      o.string shouldBe "x"
      o.list shouldBe null // Omitted from config
    }

    it("should return different instances with a no-arg constructor") {
      c.get[Top]("a") shouldNot be theSameInstanceAs c.get[Top]("a")
    }

    it("should be available to get a value via factory function") {
      val o = c.get[Inner]("a")
      o.string shouldBe "x"
      o.list shouldBe null
    }

    it("should return different instances with factory function") {
      c.get[Inner]("a") shouldNot be theSameInstanceAs c.get[Inner]("a")
    }

    it("should throw an exception for incorrect property types") {
      intercept[ConfigException.WrongType] {
        c.get[Inner]("b")
      }
    }

    it("should throw an exception for unknown property names") {
      intercept[ConfigException.BadPath] {
        c.get[Inner]("c")
      }
    }

    it("should support both primitive types and objects") {
      val o = c.get[Top]("d")
      o.boolean shouldBe true
      o.int shouldBe 1
      o.list should have length 3
      o.long shouldBe 2
      o.string shouldBe "x" // Omitted from config
    }

  }

}

object BeansSpec {

  trait Obj {
    @BeanProperty var boolean: Boolean = _
    @BeanProperty var double: Double = _
    @BeanProperty var int: Int = _
    @BeanProperty var list: JList[String] = _
    @BeanProperty var long: Long = _
    @BeanProperty var string: String = _
  }

  class Top extends Obj

}
