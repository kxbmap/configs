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

import java.{lang => jl}
import scala.annotation.tailrec
import scalaprops.Gen
import scalaz.Order

object string {

  implicit lazy val stringOrder: Order[String] =
    scalaz.std.string.stringInstance

  implicit lazy val stringGen: Gen[String] = {
    import jl.{Character => C}
    val g = {
      val cp = (C.MIN_CODE_POINT to C.MAX_CODE_POINT).filter(C.isDefined)
      Gen.elements(cp.head, cp.tail: _*)
    }
    Gen.sized { size =>
      Gen.sequenceNArray(size, g).map { cps =>
        @tailrec
        def toChars(i: Int, j: Int, arr: Array[Char]): Array[Char] =
          if (i < cps.length) {
            val cs = C.toChars(cps(i))
            System.arraycopy(cs, 0, arr, j, cs.length)
            toChars(i + 1, j + cs.length, arr)
          } else {
            require(j == arr.length)
            arr
          }
        val cc = cps.foldLeft(0)(_ + C.charCount(_))
        new String(toChars(0, 0, new Array(cc)))
      }
    }
  }

  implicit lazy val charArrayGen: Gen[Array[Char]] =
    stringGen.map(_.toCharArray)

  implicit lazy val charListGen: Gen[List[Char]] =
    stringGen.map(_.toList)

  implicit lazy val javaCharListGen: Gen[List[jl.Character]] =
    charListGen.asInstanceOf[Gen[List[jl.Character]]]

}
