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

  //  val configCollection = ???

  val intCollection = Properties.list(
    check[List[Int]].toProperties("list"),
    check[Vector[Int]].toProperties("vector"),
    check[Stream[Int]].toProperties("stream"),
    check[Array[Int]].toProperties("array")
  )

  val longCollection = Properties.list(
    check[List[Long]].toProperties("list"),
    check[Vector[Long]].toProperties("vector"),
    check[Stream[Long]].toProperties("stream"),
    check[Array[Long]].toProperties("array")
  )

  val doubleCollection = Properties.list(
    check[List[Double]].toProperties("list"),
    check[Vector[Double]].toProperties("vector"),
    check[Stream[Double]].toProperties("stream"),
    check[Array[Double]].toProperties("array")
  )

  val booleanCollection = Properties.list(
    check[List[Boolean]].toProperties("list"),
    check[Vector[Boolean]].toProperties("vector"),
    check[Stream[Boolean]].toProperties("stream"),
    check[Array[Boolean]].toProperties("array")
  )

  val stringCollection = Properties.list(
    check[List[String]].toProperties("list"),
    check[Vector[String]].toProperties("vector"),
    check[Stream[String]].toProperties("stream"),
    check[Array[String]].toProperties("array")
  )

  val javaDurationCollection = Properties.list(
    check[List[jt.Duration]].toProperties("list"),
    check[Vector[jt.Duration]].toProperties("vector"),
    check[Stream[jt.Duration]].toProperties("stream"),
    check[Array[jt.Duration]].toProperties("array")
  )

  val configMemorySizeCollection = Properties.list(
    check[List[ConfigMemorySize]].toProperties("list"),
    check[Vector[ConfigMemorySize]].toProperties("vector"),
    check[Stream[ConfigMemorySize]].toProperties("stream"),
    check[Array[ConfigMemorySize]].toProperties("array")
  )

}
