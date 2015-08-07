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
import com.github.kxbmap.configs.util.ConfigVal
import java.util.UUID
import java.{util => ju}
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.list._
import scalaz.std.stream._
import scalaz.std.string._
import scalaz.std.vector._

object JavaUtilConfigsTest extends Scalaprops with ConfigProp {

  val uuid = check[UUID]
  val uuidJavaList = check[ju.List[UUID]]
  val uuidCollections = Properties.list(
    check[List[UUID]].mapId("list " + _),
    check[Vector[UUID]].mapId("vector " + _),
    check[Stream[UUID]].mapId("stream " + _),
    check[Array[UUID]].mapId("array " + _)
  )


  implicit lazy val uuidGen: Gen[UUID] =
    Gen[Array[Byte]].map(UUID.nameUUIDFromBytes)

  implicit lazy val uuidConfigVal: ConfigVal[UUID] =
    ConfigVal[String].contramap(_.toString)

}
