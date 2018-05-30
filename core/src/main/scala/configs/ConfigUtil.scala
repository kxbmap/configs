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

package configs

import com.typesafe.config.{ConfigUtil => Impl}
import scala.collection.JavaConverters._

object ConfigUtil {

  def quoteString(s: String): String =
    Impl.quoteString(s)

  def joinPath(element: String, elements: String*): String =
    Impl.joinPath(element +: elements: _*)

  def joinPath(elements: collection.Seq[String]): String =
    Impl.joinPath(elements.asJava)

  def splitPath(path: String): List[String] =
    Impl.splitPath(path).asScala.toList


  def splitWords(s: String): List[String] = {
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

}
