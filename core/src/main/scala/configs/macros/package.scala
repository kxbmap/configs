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

package configs

import java.util.Locale
import java.util.regex.Pattern
import scala.annotation.tailrec

package object macros {

  private[this] val sep = Pattern.compile("[_-]+")

  private[macros] def toLowerHyphenCase(s: String): String = sep.split(s) match {
    case ps if ps.length > 1 =>
      ps.mkString("-").toLowerCase(Locale.ROOT)

    case _ =>
      @tailrec
      def format(s: String, sb: StringBuilder = new StringBuilder()): String =
        if (s.length == 0) sb.result().toLowerCase(Locale.ROOT)
        else {
          def append(s: String) = (if (sb.isEmpty) sb else sb.append('-')).append(s)
          val (us, rs) = s.span(_.isUpper)
          if (rs.isEmpty) format(rs, append(us))
          else us.length match {
            case 0 =>
              val (ls, rest) = rs.span(!_.isUpper)
              format(rest, append(ls))
            case 1 =>
              val (ls, rest) = rs.span(!_.isUpper)
              format(rest, append(us + ls))
            case _ =>
              format(us.last + rs, append(us.init))
          }
        }
      format(s)
  }

}
