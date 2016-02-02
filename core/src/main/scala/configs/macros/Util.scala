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
import scala.annotation.tailrec

private[macros] trait Util {

  def toLowerHyphenCase(s: String): String =
    words(s).mkString("-").toLowerCase(Locale.ROOT)

  def words(s: String): List[String] = {
    @tailrec
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

  val MaxApplyN = 22
  val MaxTupleN = 22

  def grouping[A](xs: List[A]): List[List[A]] = {
    val n = xs.length
    val t = (n + MaxTupleN - 1) / MaxTupleN
    val g = (n + t - 1) / t
    xs.grouped(g).toList
  }

  def length(xss: Seq[Seq[_]]): Int =
    xss.foldLeft(0)(_ + _.length)

  def zipWithPos[A](xss: List[List[A]]): List[List[(A, Int)]] =
    xss.zip(xss.scanLeft(1)(_ + _.length)).map {
      case (xs, s) => xs.iterator.zip(Iterator.from(s)).toList
    }

  def fitShape[A](xs: List[A], shape: List[List[_]]): List[List[A]] = {
    @tailrec
    def loop(xs: List[A], shape: List[List[_]], acc: List[List[A]]): List[List[A]] =
      shape match {
        case Nil => acc.reverse
        case s :: ss =>
          val (h, t) = xs.splitAt(s.length)
          loop(t, ss, h :: acc)
      }
    loop(xs, shape, Nil)
  }

  def fitZip[A, B](xs: List[A], yss: List[List[B]]): List[List[(A, B)]] = {
    val xss = fitShape(xs, yss)
    xss.zip(yss).map(t => t._1.zip(t._2))
  }

  def validateHyphenName(name: String, hyphen: String, names: Seq[String], hyphens: Seq[String]): Option[String] =
    if (name != hyphen && !names.contains(hyphen) && hyphens.count(_ == hyphen) == 1)
      Some(hyphen)
    else
      None

}
