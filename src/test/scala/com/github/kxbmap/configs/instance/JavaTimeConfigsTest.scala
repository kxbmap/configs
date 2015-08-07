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
import java.{time => jt, util => ju}
import scalaprops.{Properties, Scalaprops}
import scalaz.std.list._
import scalaz.std.stream._
import scalaz.std.string._
import scalaz.std.vector._

object JavaTimeConfigsTest extends Scalaprops with ConfigProp {

  val javaDuration = check[jt.Duration]
  val javaDurationList = check[ju.List[jt.Duration]]
  val javaDurationCollections = Properties.list(
    check[List[jt.Duration]].mapId("list " + _),
    check[Vector[jt.Duration]].mapId("vector " + _),
    check[Stream[jt.Duration]].mapId("stream " + _),
    check[Array[jt.Duration]].mapId("array " + _)
  )

}
