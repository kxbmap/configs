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

import com.typesafe.config.{ConfigException, ConfigFactory}
import configs.util._
import java.{lang => jl, util => ju}
import scala.beans.BeanProperty
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.std.option._
import scalaz.std.string._
import scalaz.std.tuple._
import scalaz.{Apply, Equal, Need}

object BeanConfigsTest extends Scalaprops {

  val simple = check[SimpleBean]
  val nested = check[NestedBean]
  val recursive = check[RecursiveBean]
  val javaTypes = check[JavaTypes]

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

  val wrapError = intercept {
    val config = ConfigFactory.parseString("foo = 1")
    Configs[ThrowException].extract(config)
  } {
    case e: ConfigException.BadValue => e.getCause.getMessage == "should wrap"
  }

  val requireNonNull = intercept {
    val config = ConfigFactory.parseString(s"string = foo")
    Configs.bean(null: SimpleBean).extract(config)
  } {
    case e: ConfigException => e.getMessage.contains("newInstance")
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

    implicit val toConfigValue: ToConfigValue[SimpleBean] =
      ToConfigValue.fromMap(o => Map(
        "boolean" -> o.boolean.toConfigValue,
        "double" -> o.double.toConfigValue,
        "int" -> o.int.toConfigValue,
        "list" -> Option(o.list).toConfigValue,
        "long" -> o.long.toConfigValue,
        "string" -> Option(o.string).toConfigValue
      ))

    implicit val gen: Gen[SimpleBean] =
      Apply[Gen].apply6(
        Gen[Option[Boolean]],
        Gen[Option[Double]],
        Gen[Option[Int]],
        Gen[Option[List[String]]],
        Gen[Option[Long]],
        Gen[Option[String]]
      ) { (b, d, i, ls, l, s) =>
        val o = new SimpleBean()
        b.foreach(o.setBoolean)
        d.foreach(o.setDouble)
        i.foreach(o.setInt)
        ls.foreach(o.setList)
        l.foreach(o.setLong)
        s.foreach(o.setString)
        o
      }

    implicit val equal: Equal[SimpleBean] =
      Equal.equalBy(o => (o.boolean, o.double, o.int, Option(o.list), o.long, Option(o.string)))

  }


  class NestedBean {
    @BeanProperty var value: Int = _
    @BeanProperty var simple: SimpleBean = _
  }

  object NestedBean {

    implicit val configs: Configs[NestedBean] = Configs.bean[NestedBean]

    implicit val toConfigValue: ToConfigValue[NestedBean] =
      ToConfigValue.fromMap(o => Map(
        "value" -> o.value.toConfigValue,
        "simple" -> Option(o.simple).toConfigValue
      ))

    implicit val gen: Gen[NestedBean] =
      Apply[Gen].apply2(Gen[Option[Int]], Gen[Option[SimpleBean]]) { (v, s) =>
        val o = new NestedBean()
        v.foreach(o.setValue)
        s.foreach(o.setSimple)
        o
      }

    implicit val equal: Equal[NestedBean] =
      Equal.equalBy(o => (o.value, Option(o.simple)))

  }


  class RecursiveBean {
    @BeanProperty var value: Int = _
    @BeanProperty var next: RecursiveBean = _
  }

  object RecursiveBean {

    implicit val configs: Configs[RecursiveBean] = Configs.bean[RecursiveBean]

    implicit val toConfigValue: ToConfigValue[RecursiveBean] =
      ToConfigValue.fromMap(o => Map(
        "value" -> o.value.toConfigValue,
        "next" -> Option(o.next).toConfigValue
      ))

    implicit val gen: Gen[RecursiveBean] =
      Apply[Gen].apply2(
        Gen[Option[Int]],
        Gen.oneOfLazy(Need(Gen[Option[RecursiveBean]]))
      ) { (v, n) =>
        val o = new RecursiveBean()
        v.foreach(o.setValue)
        n.foreach(o.setNext)
        o
      }

    implicit val equal: Equal[RecursiveBean] =
      Equal.equal { (a, b) =>
        a.value == b.value && Equal[Option[RecursiveBean]].equal(Option(a.next), Option(b.next))
      }

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

    implicit val toConfigValue: ToConfigValue[JavaTypes] =
      ToConfigValue.fromMap(o => Map(
        "boolean" -> Option(o.boolean).toConfigValue,
        "double" -> Option(o.double).toConfigValue,
        "int" -> Option(o.int).toConfigValue,
        "long" -> Option(o.long).toConfigValue,
        "list" -> Option(o.list).toConfigValue
      ))

    implicit val gen: Gen[JavaTypes] =
      Apply[Gen].apply5(
        Gen[Option[jl.Boolean]],
        Gen[Option[jl.Double]],
        Gen[Option[jl.Integer]],
        Gen[Option[jl.Long]],
        Gen[Option[ju.List[String]]]
      ) { (b, d, i, l, ss) =>
        val o = new JavaTypes()
        b.foreach(o.setBoolean)
        d.foreach(o.setDouble)
        i.foreach(o.setInt)
        l.foreach(o.setLong)
        ss.foreach(o.setList)
        o
      }

    implicit val equal: Equal[JavaTypes] =
      Equal.equalBy(o => (Option(o.boolean), Option(o.double), Option(o.int), Option(o.long), Option(o.list)))

  }

  class ThrowException {
    def setFoo(a: Int): Unit = {
      throw new RuntimeException("should wrap")
    }
  }

  object ThrowException {
    implicit val configs: Configs[ThrowException] = Configs.bean[ThrowException]
  }

}
