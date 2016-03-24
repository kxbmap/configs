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

package configs.testutil.instance

import java.io.File
import java.nio.file.{Path, Paths}
import scalaprops.Gen
import scalaz.Equal

object io {

  implicit lazy val pathGen: Gen[Path] =
    Gen.nonEmptyList(Gen.nonEmptyString(Gen.alphaChar)).map(p => Paths.get(p.head, p.tail.toList: _*))
  
  implicit lazy val pathEqual: Equal[Path] =
    Equal.equalA[Path]

  implicit lazy val fileGen: Gen[File] =
    pathGen.map(_.toFile)

  implicit lazy val fileEqual: Equal[File] =
    Equal.equalA[File]

}
