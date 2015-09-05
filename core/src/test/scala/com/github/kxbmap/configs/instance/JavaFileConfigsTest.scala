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

import com.github.kxbmap.configs.util._
import java.io.File
import java.nio.file.{Path, Paths}
import java.{util => ju}
import scalaprops.{Gen, Scalaprops}
import scalaz.Equal

object JavaFileConfigsTest extends Scalaprops {

  val path = check[Path]

  val pathJList = {
    implicit val h = hideConfigs[Path]
    check[ju.List[Path]]
  }

  val file = check[File]

  val fileJList = {
    implicit val h = hideConfigs[File]
    check[ju.List[File]]
  }


  implicit lazy val pathGen: Gen[Path] =
    Gen.nonEmptyList(Gen.alphaNumString).map(ss => Paths.get(ss.head, ss.tail: _*))

  implicit lazy val pathEqual: Equal[Path] =
    Equal.equalA[Path]


  implicit lazy val fileGen: Gen[File] =
    pathGen.map(_.toFile)

  implicit lazy val fileEqual: Equal[File] =
    Equal.equalA[File]

}
