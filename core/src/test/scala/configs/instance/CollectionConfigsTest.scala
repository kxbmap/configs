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

import configs.Configs
import configs.util._
import java.{lang => jl, util => ju}
import scala.collection.immutable.TreeMap
import scala.collection.mutable
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Equal
import scalaz.std.list._
import scalaz.std.stream._
import scalaz.std.string._
import scalaz.std.vector._

object CollectionConfigsTest extends Scalaprops {

  val javaList = check[ju.List[Foo]]

  val javaIterable = check[jl.Iterable[Foo]]

  val javaCollection = check[ju.Collection[Foo]]

  val javaSet = check[ju.Set[Foo]]

  val javaMap = Properties.list(
    check[ju.Map[String, Foo]]("string map"),
    check[ju.Map[Symbol, Foo]]("symbol map")
  )

  val fromJList = Properties.list(
    check[List[Foo]]("list"),
    check[Vector[Foo]]("vector"),
    check[Stream[Foo]]("stream"),
    check[Array[Foo]]("array"),
    check[Set[Foo]]("set")
  )

  val fromJMap = Properties.list(
    check[Map[String, Foo]]("map"),
    check[TreeMap[String, Foo]]("tree map"),
    check[mutable.Map[String, Foo]]("mutable map")
  )


  case class Foo(value: Int)

  object Foo {

    implicit val gen: Gen[Foo] =
      Gen[Int].map(Foo.apply)

    implicit val equal: Equal[Foo] =
      Equal.equalA[Foo]

    implicit val tcv: ToConfigValue[Foo] =
      _.value.toConfigValue.atKey("v").root()

    implicit val configs: Configs[Foo] =
      Configs.Try(c => Foo(c.getInt("v")))

  }

}
