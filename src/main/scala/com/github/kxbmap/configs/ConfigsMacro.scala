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

import com.typesafe.config.Config
import java.util.Locale
import java.util.regex.Pattern
import scala.annotation.tailrec
import scala.reflect.macros.blackbox

class ConfigsMacro(val c: blackbox.Context) {

  import c.universe._

  lazy val configType = typeOf[Config]

  def configsType(arg: Type) = appliedType(typeOf[Configs[_]].typeConstructor, arg)

  def atPathType(arg: Type) = appliedType(typeOf[AtPath[_]].typeConstructor, arg)

  def optionType(arg: Type) = appliedType(typeOf[Option[_]].typeConstructor, arg)

  lazy val configsCompanion = symbolOf[Configs[_]].companion


  def materialize[T: WeakTypeTag]: Expr[Configs[T]] = {
    val tpe = weakTypeOf[T]
    val ctors = tpe.decls.collect {
      case m: MethodSymbol if m.isConstructor && m.isPublic => m
    }.toSeq.sortBy {
      m => (!m.isPrimaryConstructor, -m.paramLists.map(_.length).sum)
    }
    if (ctors.isEmpty) {
      c.abort(c.enclosingPosition, s"$tpe must have a public constructor")
    }
    val ts = ctors.flatMap(_.paramLists.flatMap(_.map(_.info))).distinct

    val instances: Map[Type, (TermName, TermName, Seq[Tree])] = ts.map { t =>
      val cn = TermName(c.freshName("c"))
      val ct = atPathType(t)
      val ci = c.inferImplicitValue(ct, silent = false)
      val on = TermName(c.freshName("o"))
      val ot = atPathType(optionType(t))
      val oi = q"$configsCompanion.optionAtPath[$t]($cn)"
      (t, (cn, on, Seq(q"val $cn: $ct = $ci", q"val $on: $ot = $oi")))
    }(collection.breakOut)

    val config = TermName("config")
    val cs = ctors.map { ctor =>
      val argLists = ctor.paramLists.map { params =>
        params.map { p =>
          val (cn, on, _) = instances(p.info)
          val key = p.name.decodedName.toString
          val hyphen = MacroImplUtil.toLowerHyphenCase(key)
          if (key == hyphen) {
            q"$cn.extract($config)($key)"
          } else {
            val ov = q"$on.extract($config)($key)"
            val cv = q"$cn.extract($config)($hyphen)"
            q"$ov.getOrElse($cv)"
          }
        }
      }
      q"""
      new ${configsType(tpe)} {
        def extract($config: $configType): $tpe = new $tpe(...$argLists)
      }
      """
    }

    c.Expr[Configs[T]](q"""
      {
        ..${instances.values.flatMap(_._3)}
        ${cs.reduceLeft((l, r) => q"$l orElse $r")}
      }
      """)
  }

}


object MacroImplUtil {

  private[this] val sep = Pattern.compile("[_-]+")

  def toLowerHyphenCase(s: String): String = sep.split(s) match {
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
