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
import scala.jdk.CollectionConverters._

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

  def getRootKeys(config: Config): List[String] = config.root.keySet.asScala.toList

  def getSuperfluousKeys(configKeys: List[String], paramKeys: List[String]): List[(String, List[String])] = {
    configKeys.diff(paramKeys)
      .map( key => (key, getSimilarKeys( key, paramKeys )))
  }

  val maxSimilarityDistance = 6 // limit levenshtein distance calculation to a maximum of 6 character mutations
  def getSimilarKeys(key: String, params: List[String]): List[String] = {
    import org.apache.commons.text.similarity.LevenshteinDistance
    val similarityDistanceLimit = math.min(key.length/2+1,maxSimilarityDistance)
    val similarityCalculator = new LevenshteinDistance(similarityDistanceLimit)
    params.map( p => (p,similarityCalculator.apply(p, key)))
      .filter( _._2 >= 0) // ignore if not similar (result is -1 in this case)
      .sortBy(_._2)
      .map(_._1)
  }
}
