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

import configs.ConfigUtil
import java.{lang => jl}
import scala.annotation.tailrec
import scalaprops.Gen
import scalaz.{Monoid, Order}

object string {

  implicit lazy val stringOrder: Order[String] =
    scalaz.std.string.stringInstance

  implicit lazy val stringMonoid: Monoid[String] =
    scalaz.std.string.stringInstance

  lazy val unicodeStringGen: Gen[String] = {
    import jl.{Character => C}
    val g = {
      val cps = (C.MIN_CODE_POINT to C.MAX_CODE_POINT).filter { cp =>
        (cp > C.MAX_SURROGATE || cp < C.MIN_SURROGATE) && C.isDefined(cp)
      }
      Gen.elements(cps.head, cps.tail: _*)
    }
    Gen.sized { size =>
      Gen.sequenceNArray(size, g).map { cps =>
        @tailrec
        def toString(i: Int, arr: Array[Char], pos: Int): String =
          if (i >= cps.length) new String(arr)
          else {
            val n = C.toChars(cps(i), arr, pos)
            toString(i + 1, arr, pos + n)
          }
        toString(0, new Array(cps.foldLeft(0)(_ + C.charCount(_))), 0)
      }
    }
  }

  lazy val notInUnquoteChar: Gen[Char] = {
    val s = "$\"{}[]:=,+#`^?!@*&\\".toSeq
    Gen.elements(s.head, s.tail: _*)
  }

  implicit lazy val stringGen: Gen[String] =
    Gen.frequency(
      50 -> unicodeStringGen,
      25 -> Gen.asciiString,
      20 -> Gen.alphaNumString,
      2 -> Gen.genString(notInUnquoteChar),
      3 -> Gen.elements(
        "//", "foo//", "//foo", "foo//bar", // comment
        ".", ".foo", "foo.", "foo.bar", // period
        "\n", "\r", "\r\n", "foo\nbar", "foo\rbar", // CRLF
        "\u0020", "\u0020foo", "foo\u0020", "foo\u0020bar", // SPACE
        "\u00a0", "\u00a0foo", "foo\u00a0", "foo\u00a0bar", // NO-BREAK SPACE
        "\ufeff", "\ufefffoo", "foo\ufeff", "foo\ufeffbar" // BOM
      )
    )

  lazy val pathStringGen: Gen[String] =
    Gen.nonEmptyList(stringGen).map { ks =>
      ConfigUtil.joinPath(ks.list.toList)
    }

  implicit lazy val charArrayGen: Gen[Array[Char]] =
    stringGen.map(_.toCharArray)

  implicit lazy val charListGen: Gen[List[Char]] =
    stringGen.map(_.toList)

  implicit lazy val javaCharListGen: Gen[List[jl.Character]] =
    charListGen.asInstanceOf[Gen[List[jl.Character]]]

}
