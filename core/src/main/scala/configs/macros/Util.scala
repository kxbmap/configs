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
        val (us, rs) = s.span(_.isUpper)
        if (rs.isEmpty) loop(rs, us :: acc)
        else us.length match {
          case 0 =>
            val (ls, rest) = rs.span(!_.isUpper)
            loop(rest, ls :: acc)
          case 1 =>
            val (ls, rest) = rs.span(!_.isUpper)
            loop(rest, us + ls :: acc)
          case _ =>
            loop(us.last +: rs, us.init :: acc)
        }
      }
    s.split("[_-]+").flatMap(loop(_, Nil)).toList
  }

  def grouping[A](xs: List[A]): List[List[A]] = {
    val n = xs.length
    val t = (n + MaxTupleSize - 1) / MaxTupleSize
    val g = (n + t - 1) / t
    xs.grouped(g).toList
  }

}
