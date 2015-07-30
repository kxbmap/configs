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

import com.github.kxbmap.configs.ConfigProp
import com.typesafe.config.ConfigMemorySize
import java.{time => jt}
import scalaprops.{Properties, Scalaprops}
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.std.stream._
import scalaz.std.string._
import scalaz.std.vector._

object BasicTypeCollectionConfigsTest extends Scalaprops with ConfigProp {

  //  val configCollections = ???

  val intCollections = Properties.list(
    check[List[Int]].mapId("list " + _),
    check[Vector[Int]].mapId("vector " + _),
    check[Stream[Int]].mapId("stream " + _),
    check[Array[Int]].mapId("array " + _)
  )

  val longCollections = Properties.list(
    check[List[Long]].mapId("list " + _),
    check[Vector[Long]].mapId("vector " + _),
    check[Stream[Long]].mapId("stream " + _),
    check[Array[Long]].mapId("array " + _)
  )

  val doubleCollections = Properties.list(
    check[List[Double]].mapId("list " + _),
    check[Vector[Double]].mapId("vector " + _),
    check[Stream[Double]].mapId("stream " + _),
    check[Array[Double]].mapId("array " + _)
  )

  val booleanCollections = Properties.list(
    check[List[Boolean]].mapId("list " + _),
    check[Vector[Boolean]].mapId("vector " + _),
    check[Stream[Boolean]].mapId("stream " + _),
    check[Array[Boolean]].mapId("array " + _)
  )

  val stringCollections = Properties.list(
    check[List[String]].mapId("list " + _),
    check[Vector[String]].mapId("vector " + _),
    check[Stream[String]].mapId("stream " + _),
    check[Array[String]].mapId("array " + _)
  )

  val javaDurationCollections = Properties.list(
    check[List[jt.Duration]].mapId("list " + _),
    check[Vector[jt.Duration]].mapId("vector " + _),
    check[Stream[jt.Duration]].mapId("stream " + _),
    check[Array[jt.Duration]].mapId("array " + _)
  )

  val configMemorySizeCollections = Properties.list(
    check[List[ConfigMemorySize]].mapId("list " + _),
    check[Vector[ConfigMemorySize]].mapId("vector " + _),
    check[Stream[ConfigMemorySize]].mapId("stream " + _),
    check[Array[ConfigMemorySize]].mapId("array " + _)
  )

}
