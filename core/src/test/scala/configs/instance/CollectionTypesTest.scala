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

import configs.testutil.fun._
import configs.testutil.instance.anyVal._
import configs.testutil.instance.collection._
import configs.testutil.instance.string._
import configs.testutil.instance.symbol._
import java.{lang => jl, util => ju}
import scala.collection.immutable.TreeMap
import scala.collection.mutable
import scalaprops.{Properties, Scalaprops}

object CollectionTypesTest extends Scalaprops {

  val javaList = check[ju.List[Int]]

  val javaIterable = check[jl.Iterable[Int]]

  val javaCollection = check[ju.Collection[Int]]

  val javaSet = check[ju.Set[Int]]

  val javaMap = Properties.list(
    check[ju.Map[String, Int]]("string map"),
    check[ju.Map[Symbol, Int]]("symbol map")
  )

  val fromJList = Properties.list(
    check[List[Int]]("list"),
    check[Vector[Int]]("vector"),
    check[Stream[Int]]("stream"),
    check[Array[Int]]("array"),
    check[Set[Int]]("set")
  )

  val fromJMap = Properties.list(
    check[Map[String, Int]]("map"),
    check[TreeMap[String, Int]]("tree map"),
    check[mutable.Map[String, Int]]("mutable map")
  )

  val javaProperties = check[ju.Properties]

}
