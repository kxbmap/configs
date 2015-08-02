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
import java.{lang => jl, util => ju}
import scala.beans.BeanProperty
import scala.collection.JavaConversions._
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}
import scalaz.std.string._

object BeanConfigsTest extends Scalaprops with ConfigProp {

  val simple = forAll { (s: String) =>
    val config = ConfigFactory.parseString(s"string = ${q(s)}")
    val o = Configs[SimpleBean].extract(config)
    o.string == s && o.list == null
  }

  val nested = forAll { (n: Int, b: Boolean, l: Long) =>
    val config = ConfigFactory.parseString(
      s"""value = $n
         |simple = {
         |  boolean = $b
         |  long = $l
         |}
         |""".stripMargin)
    val o = Configs[NestedBean].extract(config)
    o.value == n && o.simple.boolean == b && o.simple.long == l
  }

  val recursive = forAll { (n: Int, m: Int) =>
    val config = ConfigFactory.parseString(
      s"""value = $n
         |next = {
         |  value = $m
         |}
         |""".stripMargin)
    val o = Configs[RecursiveBean].extract(config)
    o.value == n && o.next.value == m && o.next.next == null
  }

  val differentInstance = forAll {
    val config = ConfigFactory.empty()
    val o1 = Configs[SimpleBean].extract(config)
    val o2 = Configs[SimpleBean].extract(config)
    o1 ne o2
  }

  val factory = {
    val C = Configs.bean {
      val b = new SimpleBean()
      b.string = "foo"
      b
    }
    val p1 = forAll { (n: Int) =>
      val config = ConfigFactory.parseString(s"int = $n")
      val o = C.extract(config)
      o.string == "foo" && o.int == n
    }
    val p2 = forAll {
      val config = ConfigFactory.empty()
      val o1 = C.extract(config)
      val o2 = C.extract(config)
      (o1 ne o2) && o1.string == "foo" && o2.string == "foo"
    }
    Properties.list(
      p1.toProperties("get"),
      p2.toProperties("different instance")
    )
  }

  val wrongType = forAll {
    val config = ConfigFactory.parseString(s"int = one")
    try {
      Configs[SimpleBean].extract(config)
      false
    } catch {
      case e: ConfigException.WrongType => e.getMessage.contains("int")
    }
  }

  val badPath = forAll {
    val config = ConfigFactory.parseString(s"bad-path = prop")
    try {
      Configs[SimpleBean].extract(config)
      false
    } catch {
      case e: ConfigException.BadPath => e.getMessage.contains("bad-path")
    }
  }


  val javaTypes = forAll { (b: Boolean, d: Double, n: Int, l: Long, ss: ju.List[String]) =>
    val config = ConfigFactory.parseString(
      s"""boolean = $b
         |double = $d
         |int = $n
         |long = $l
         |list = ${ss.map(q).mkString("[", ",", "]")}
         |""".stripMargin)
    val o = Configs[JavaTypes].extract(config)
    o.boolean == b && o.double == d && o.int == n && o.long == l && o.list == ss
  }


  class SimpleBean {
    @BeanProperty var boolean: Boolean = _
    @BeanProperty var double: Double = _
    @BeanProperty var int: Int = _
    @BeanProperty var list: List[String] = _
    @BeanProperty var long: Long = _
    @BeanProperty var string: String = _
  }

  object SimpleBean {
    implicit val configs: Configs[SimpleBean] = Configs.bean[SimpleBean]
  }

  class NestedBean {
    @BeanProperty var value: Int = _
    @BeanProperty var simple: SimpleBean = _
  }

  object NestedBean {
    implicit val configs: Configs[NestedBean] = Configs.bean[NestedBean]
  }

  class RecursiveBean {
    @BeanProperty var value: Int = _
    @BeanProperty var next: RecursiveBean = _
  }

  object RecursiveBean {
    implicit val configs: Configs[RecursiveBean] = Configs.bean[RecursiveBean]
  }


  class JavaTypes {
    @BeanProperty var boolean: jl.Boolean = _
    @BeanProperty var double: jl.Double = _
    @BeanProperty var int: jl.Integer = _
    @BeanProperty var long: jl.Long = _
    @BeanProperty var list: ju.List[String] = _
  }

  object JavaTypes {
    implicit val configs: Configs[JavaTypes] = Configs.bean[JavaTypes]
  }

}
