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

package configs.macros

import java.util.Locale

private[macros] trait Util {

  final val MaxApplySize = 22
  final val MaxTupleSize = 22
  final val MaxSize = MaxApplySize * MaxTupleSize

  def toLower(s: String): String =
    s.toLowerCase(Locale.ROOT)

  def toLowerHyphenCase(s: String): String =
    toLower(words(s).mkString("-"))

  def toLowerCamel(s: String): String = {
    val w :: ws = words(s)
    (toLower(w) :: ws).mkString
  }

  def words(s: String): List[String] = {
    @annotation.tailrec
    def loop(s: String, acc: List[String]): List[String] =
      if (s.isEmpty) acc.reverse
      else {
        val (upper, t) = s.span(_.isUpper)
        if (t.isEmpty) (upper :: acc).reverse
        else {
          val (digit, rest) = t.span(_.isDigit)
          if (upper.isEmpty) {
            if (rest.isEmpty) (digit :: acc).reverse
            else if (!digit.isEmpty) loop(rest, digit :: acc)
            else {
              val (xs, next) = rest.span(c => !c.isUpper && !c.isDigit)
              loop(next, xs :: acc)
            }
          } else {
            if (rest.isEmpty) (digit :: upper :: acc).reverse
            else if (!digit.isEmpty) loop(rest, digit :: upper :: acc)
            else if (upper.length == 1) {
              val (lower, next) = rest.span(_.isLower)
              loop(next, upper + lower :: acc)
            }
            else loop(upper.last +: rest, upper.init :: acc)
          }
        }
      }
    s.split("[_-]+").toList.flatMap(loop(_, Nil))
  }

  def grouping[A](xs: List[A]): List[List[A]] = {
    val n = xs.length
    val t = (n + MaxTupleSize - 1) / MaxTupleSize
    val g = (n + t - 1) / t
    xs.grouped(g).toList
  }

}
