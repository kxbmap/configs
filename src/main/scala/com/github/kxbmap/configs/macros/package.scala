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

package com.github.kxbmap.configs

import java.util.Locale
import java.util.regex.Pattern
import scala.annotation.tailrec

package object macros {

  private[this] val sep = Pattern.compile("[_-]+")

  private[macros] def toLowerHyphenCase(s: String): String = sep.split(s) match {
    case ps if ps.length > 1 =>
      ps.mkString("-").toLowerCase(Locale.ENGLISH)

    case _ =>
      def append(sb: StringBuilder, s: String): StringBuilder =
        if (sb.isEmpty) sb.append(s)
        else sb.append('-').append(s)

      @tailrec
      def format(s: String, sb: StringBuilder = new StringBuilder()): String =
        if (s.length == 0) sb.result().toLowerCase(Locale.ENGLISH)
        else {
          val (us, rest) = s.span(_.isUpper)
          us.length match {
            case 0 =>
              val (ls, next) = rest.span(!_.isUpper)
              format(next, append(sb, ls))
            case 1 =>
              val (ls, next) = rest.span(!_.isUpper)
              format(next, append(sb, us + ls))
            case _ =>
              format(us.last + rest, append(sb, us.init))
          }
        }
      format(s)
  }

}
