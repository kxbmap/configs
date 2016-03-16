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

package configs.instance

import configs.util._
import configs.{Configs, Converter}
import java.{lang => jl, util => ju}
import scala.collection.immutable.TreeMap
import scala.collection.mutable
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.list._
import scalaz.std.stream._
import scalaz.std.string._
import scalaz.std.vector._
import scalaz.{Equal, Order}

object CollectionConfigsTest extends Scalaprops {

  val javaList = {
    implicit val c = Foo.fooConfigs
    check[ju.List[Foo]]
  }

  val javaIterable = {
    implicit val c = Foo.fooJListConfigs
    check[jl.Iterable[Foo]]
  }

  val javaCollection = {
    implicit val c = Foo.fooJListConfigs
    check[ju.Collection[Foo]]
  }

  val javaSet = {
    implicit val c = Foo.fooJListConfigs
    check[ju.Set[Foo]]
  }

  val javaMap = {
    implicit val c = Foo.fooConfigs
    Properties.list(
      check[ju.Map[String, Foo]]("string map"),
      check[ju.Map[Symbol, Foo]]("symbol map")
    )
  }

  val fromJList = {
    implicit val c = Foo.fooJListConfigs
    Properties.list(
      check[List[Foo]]("list"),
      check[Vector[Foo]]("vector"),
      check[Stream[Foo]]("stream"),
      check[Array[Foo]]("array"),
      check[Set[Foo]]("set")
    )
  }

  val fromJMap = {
    val string = {
      implicit val c = Foo.fooJMapConfigs[String]
      Properties.either(
        "string map",
        check[Map[String, Foo]]("map"),
        check[TreeMap[String, Foo]]("tree map"),
        check[mutable.Map[String, Foo]]("mutable map")
      )
    }
    val symbol = {
      implicit val c = Foo.fooJMapConfigs[Symbol]
      implicit val o = Order[Symbol].toScalaOrdering
      Properties.either(
        "symbol map",
        check[Map[Symbol, Foo]]("map"),
        check[TreeMap[Symbol, Foo]]("tree map"),
        check[mutable.Map[Symbol, Foo]]("mutable map")
      )
    }
    string.product(symbol)
  }


  case class Foo(value: Int)

  object Foo {

    implicit val gen: Gen[Foo] =
      Gen[Int].map(Foo.apply)

    implicit val equal: Equal[Foo] =
      Equal.equalA[Foo]

    implicit val tcv: ToConfigValue[Foo] =
      _.value.toConfigValue.atKey("v").root()

    val fooConfigs: Configs[Foo] =
      Configs.Try(c => Foo(c.getInt("v")))

    val fooJListConfigs: Configs[ju.List[Foo]] =
      Configs.javaListConfigs(fooConfigs)

    def fooJMapConfigs[A](implicit A: Converter[String, A]): Configs[ju.Map[A, Foo]] =
      Configs.javaMapConfigs(A, fooConfigs)

  }

}
