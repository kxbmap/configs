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
import com.github.kxbmap.configs.util._
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.list._
import scalaz.std.stream._
import scalaz.std.string._
import scalaz.std.vector._

object SymbolConfigsTest extends Scalaprops with ConfigProp {

  val symbol = check[Symbol]

  val symbolCollections = Properties.list(
    check[List[Symbol]].mapId("list " + _),
    check[Vector[Symbol]].mapId("vector " + _),
    check[Stream[Symbol]].mapId("stream " + _),
    check[Array[Symbol]].mapId("array " + _)
  )

  implicit lazy val symbolGen: Gen[Symbol] = Gen[String].map(Symbol.apply)

  implicit lazy val symbolConfigVal: ConfigVal[Symbol] = ConfigVal[String].contramap(_.name)

}
