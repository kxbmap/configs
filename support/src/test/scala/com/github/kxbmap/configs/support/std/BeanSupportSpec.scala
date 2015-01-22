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
package support.std

import com.typesafe.config.ConfigFactory
import org.scalatest.{FunSpec, Matchers}

class BeanSupportSpec extends FunSpec with Matchers with BeanSupport {

  class Inner extends Obj

  describe("generic bean support") {
    val c = ConfigFactory.parseString(
      """a.foo=x
        |b.bar=1
        |c.abc=xyz""".stripMargin)

    implicit val topConfigs = Beans[Top]
    implicit val innerConfigs = Beans { new Inner }

    it ("should be available to get a value with a no-arg constructor") {
      val o = c.get[Top]("a")
      o._foo shouldBe "x"
      o._bar shouldBe null // Omitted from config
    }

    it ("should return different instances with a no-arg constructor") {
      c.get[Top]("a") shouldNot be theSameInstanceAs c.get[Top]("a")
    }

    it ("should be available to get a value via factory function") {
      val o = c.get[Inner]("a")
      o._foo shouldBe "x"
      o._bar shouldBe null
    }

    it ("should return different instances with factory function") {
      c.get[Inner]("a") shouldNot be theSameInstanceAs c.get[Inner]("a")
    }

    it ("should throw an exception for incorrect property types") {
      intercept[NoSuchMethodException] { c.get[Inner]("b") }
    }

    it ("should throw an exception for unknown property names") {
      intercept[NoSuchMethodException] { c.get[Inner]("c") }
    }
  }

}

trait Obj {

  var _foo: String = _
  var _bar: String = _

  def setFoo(s: String): Unit = _foo = s
  def setBar(s: String): Unit = _bar = s

}

class Top extends Obj
