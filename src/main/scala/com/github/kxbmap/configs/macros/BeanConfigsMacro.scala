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

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.reflect.macros.blackbox

class BeanConfigsMacro(val c: blackbox.Context) extends Helper {

  import c.universe._

  def materializeA[A: WeakTypeTag]: Tree = {
    val tpe = abortIfAbstract(weakTypeOf[A])
    val hasNoArgCtor = tpe.decls.exists { s =>
      s.isConstructor && s.isPublic && {
        val pss = s.asMethod.paramLists
        pss.lengthCompare(1) <= 0 && pss.forall(_.isEmpty)
      }
    }
    if (!hasNoArgCtor) {
      abort(s"$tpe must have public no-arg constructor")
    }
    materializeImpl[A](q"new $tpe()")
  }

  def materializeI[A: WeakTypeTag](newInstance: Tree): Tree = {
    materializeImpl[A](c.untypecheck(newInstance))
  }

  private def materializeImpl[A: WeakTypeTag](newInstance: Tree): Tree = {
    val targetType = weakTypeOf[A]
    val setters = targetType.members.sorted.collect {
      case m: MethodSymbol
        if m.isPublic && nameOf(m).length > 3 && nameOf(m).startsWith("set") &&
          m.paramLists.lengthCompare(1) == 0 && m.paramLists.head.lengthCompare(1) == 0 =>
        val name = {
          val s = nameOf(m).drop(3)
          s.head.toLower +: s.tail
        }
        (name, m, m.paramLists.head.head.info)
    }
    if (setters.isEmpty) {
      warning(s"${fullNameOf(targetType)} has no setters")
    }
    val config = TermName("config")
    val obj = TermName("obj")
    val names = TermName("names")
    val cns = new mutable.ArrayBuffer[(Type, TermName)]()
    val ns = new mutable.HashSet[String]()
    val vs = new ArrayBuffer[Tree]()
    val sets = setters.map {
      case (name, method, paramType) =>
        val hyphen = toLowerHyphenCase(name)
        val ot = optionType(paramType)
        val on = tpeGetOrElseAppend(cns, ot, {
          val on = freshName("c")
          val oi = c.inferImplicitValue(configsType(ot), silent = false)
          vs += q"val $on = $oi"
          on
        })
        val nn = Seq(name, hyphen).distinct
        val e = nn.map(n => q"$on.get($config, $n)").reduceLeft((l, r) => q"$l.orElse($r)")
        ns ++= nn
        q"$e.foreach($obj.$method(_))"
    }
    q"""
    val $names = Set(..$ns)
    ..$vs
    $configsCompanion.onPath { $config: $configType =>
      ${checkKeys(targetType, config, names)}
      val $obj = $newInstance
      ..$sets
      $obj
    }
    """
  }

  private def checkKeys(bean: Type, config: TermName, names: TermName): Tree =
    q"""
    import scala.collection.JavaConversions._
    val ks = $config.root().keySet().toSet
    if (!ks.forall($names.contains)) {
      val ps = ks.diff($names).mkString(",")
      throw new $badPathType($config.origin(), s"Bean $${${fullNameOf(bean)}} does not have properties: $$ps")
    }
    """

}
