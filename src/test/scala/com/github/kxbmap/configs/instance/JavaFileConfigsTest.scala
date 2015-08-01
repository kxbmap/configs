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
import com.github.kxbmap.configs.util.CValue
import java.io.File
import java.nio.file.{Path, Paths}
import scalaprops.{Gen, Scalaprops}

object JavaFileConfigsTest extends Scalaprops with ConfigProp {

  val path = check[Path]
  val file = check[File]


  implicit lazy val pathGen: Gen[Path] =
    Gen.nonEmptyList[String](Gen.alphaString).map(ss => Paths.get(ss.head, ss.tail: _*))

  implicit lazy val pathCValue: CValue[Path] = _.toString


  implicit lazy val fileGen: Gen[File] = pathGen.map(_.toFile)

  implicit lazy val fileCValue: CValue[File] = _.toString

}
