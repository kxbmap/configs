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

package com.github.kxbmap.configs.instance

import com.github.kxbmap.configs.Configs
import com.github.kxbmap.configs.simple._
import com.github.kxbmap.configs.util._
import java.{util => ju}
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
    implicit val c = configs.fooConfigs
    check[ju.List[Foo]]
  }

  val javaSet = {
    implicit val c = configs.fooJListConfigs
    check[ju.Set[Foo]]
  }

  val javaCollection = {
    implicit val c = configs.fooJListConfigs
    check[ju.Collection[Foo]]
  }

  val javaMap = {
    implicit val c = configs.fooConfigs
    check[ju.Map[String, Foo]]
  }

  val javaSymbolMap = {
    implicit val c = configs.fooJMapConfigs
    check[ju.Map[Symbol, Foo]]
  }

  val fromJList = {
    implicit val c = configs.fooJListConfigs
    Properties.list(
      check[List[Foo]].mapId("list " + _),
      check[Vector[Foo]].mapId("vector " + _),
      check[Stream[Foo]].mapId("stream " + _),
      check[Array[Foo]].mapId("array " + _),
      check[Set[Foo]].mapId("set " + _)
    )
  }

  val fromJMap = {
    implicit val c = configs.fooJMapConfigs
    implicit val o = Order[Symbol].toScalaOrdering
    Properties.list(
      check[Map[String, Foo]].mapId("string map " + _),
      check[Map[Symbol, Foo]].mapId("symbol map " + _),
      check[TreeMap[String, Foo]].mapId("string tree map " + _),
      check[TreeMap[Symbol, Foo]].mapId("symbol tree map " + _),
      check[mutable.Map[String, Foo]].mapId("string mutable map " + _),
      check[mutable.Map[Symbol, Foo]].mapId("symbol mutable map " + _)
    )
  }


  case class Foo(value: Int)

  implicit lazy val fooGen: Gen[Foo] = Gen[Int].map(Foo.apply)

  implicit lazy val fooEqual: Equal[Foo] = Equal.equalA[Foo]

  implicit lazy val fooToConfigValue: ToConfigValue[Foo] = _.value.toConfigValue.atKey("v").root()

  object configs {

    val fooConfigs: Configs[Foo] = Configs.onPath(c => Foo(c.getInt("v")))

    val fooJListConfigs: Configs[ju.List[Foo]] = javaListConfigs(fooConfigs)

    val fooJMapConfigs: Configs[ju.Map[String, Foo]] = javaMapConfigs(fooConfigs)

  }

}
