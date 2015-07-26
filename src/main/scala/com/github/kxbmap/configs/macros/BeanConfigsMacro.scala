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

package com.github.kxbmap.configs.macros

import com.github.kxbmap.configs.Configs
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.macros.blackbox

class BeanConfigsMacro(val c: blackbox.Context) extends Helper {

  import c.universe._

  def materializeT[T: WeakTypeTag]: Expr[Configs[T]] = {
    val tpe = abortIfAbstract(weakTypeOf[T])
    val hasNoArgCtor = tpe.decls.exists { s =>
      s.isConstructor && s.isPublic && {
        val pss = s.asMethod.paramLists
        pss.lengthCompare(1) <= 0 && pss.forall(_.isEmpty)
      }
    }
    if (!hasNoArgCtor) {
      abort(s"$tpe must have public no-arg constructor")
    }
    materializeImpl[T](tpe, q"new $tpe()")
  }

  def materializeI[T: WeakTypeTag](newInstance: Expr[T]): Expr[Configs[T]] = {
    val tpe = weakTypeOf[T]
    materializeImpl[T](tpe, newInstance.tree)
  }

  private def materializeImpl[T: WeakTypeTag](tpe: Type, newInstance: Tree): Expr[Configs[T]] = {
    val cns = new mutable.HashMap[Type, TermName]()
    val ns = new mutable.HashSet[String]()
    val vs = new ArrayBuffer[Tree]()
    val config = TermName("config")
    val obj = TermName("obj")
    val sets = tpe.members.sorted.collect {
      case m: MethodSymbol
        if m.isPublic && name(m).length > 3 && name(m).startsWith("set") &&
          m.paramLists.lengthCompare(1) == 0 && m.paramLists.head.lengthCompare(1) == 0 =>
        val t = m.paramLists.head.head.info
        val ot = optionType(t)
        val n = {
          val s = name(m).drop(3)
          s.head.toLower +: s.tail
        }
        val hn = toLowerHyphenCase(n)
        val on = cns.getOrElse(ot, {
          val on = freshName("c")
          val oi = c.inferImplicitValue(atPathType(ot), silent = false, withMacrosDisabled = true)
          vs += q"val $on = $oi"
          on
        })
        val nn = Seq(n, hn).distinct
        val e = nn.map(n => q"$on.extract($config)($n)").reduceLeft((l, r) => q"$l.orElse($r)")
        ns ++= nn
        q"$e.foreach($obj.$m(_))"
    }
    c.Expr[Configs[T]](
      q"""
      new ${configsType(tpe)} {
        import scala.collection.JavaConversions._
        val ns = Set(..$ns)
        ..$vs
        def extract($config: $configType): $tpe = {
          val ks = $config.root().keySet().toSet
          if (!ks.forall(ns.contains)) {
            val bean = ${fullName(tpe)}
            val ps = ks.diff(ns).mkString(",")
            throw new $badPathType($config.origin(), "Bean " + bean + " does not have properties: " + ps)
          }
          val $obj = $newInstance
          ..$sets
          $obj
        }
      }
      """)
  }

}
