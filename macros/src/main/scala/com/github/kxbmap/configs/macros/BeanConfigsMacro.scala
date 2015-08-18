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

import scala.reflect.macros.blackbox

class BeanConfigsMacro(val c: blackbox.Context) extends Helper {

  import c.universe._

  def materializeA[A: WeakTypeTag](dummy: Tree): Tree = {
    val tpe = abortIfAbstract(weakTypeOf[A])
    val hasNoArgCtor = tpe.decls.exists {
      case m: MethodSymbol => m.isConstructor && m.isPublic && m.paramLists.length <= 1 && m.paramLists.forall(_.isEmpty)
      case _               => false
    }
    if (!hasNoArgCtor) {
      abort(s"$tpe must have public no-arg constructor")
    }
    materializeImpl[A](q"new $tpe()")
  }

  def materializeI[A: WeakTypeTag](newInstance: Tree)(dummy: Tree): Tree = {
    val nonNull =
      q"""
      val obj: ${weakTypeOf[A]} = ${c.untypecheck(newInstance)}
      require(obj != null, "newInstance requires non null value")
      obj
      """
    materializeImpl[A](nonNull)
  }

  private def materializeImpl[A: WeakTypeTag](newInstance: Tree): Tree = {
    val targetType = weakTypeOf[A]
    val setters = targetType.members.sorted.collect {
      case m: MethodSymbol
        if m.isPublic &&
          nameOf(m).length > 3 && nameOf(m).startsWith("set") &&
          m.paramLists.length == 1 && m.paramLists.head.length == 1 =>
        val n = nameOf(m).drop(3)
        (n.head.toLower +: n.tail, m, m.paramLists.head.head.info)
    }
    if (setters.isEmpty) {
      warning(s"${fullNameOf(targetType)} has no setters")
    }
    val config = TermName("config")
    val obj = TermName("obj")
    val names = TermName("names")
    val (ns, sets) = setters.map {
      case (name, method, paramType) =>
        val cn = TermName("c")
        val nn = Seq(name, toLowerHyphenCase(name)).distinct
        val opt = nn.map(n => q"$cn.get($config, $n)").reduceLeft((l, r) => q"$l.orElse($r)")
        val set =
          q"""
          val $cn = $configsCompanion[${optionType(paramType)}]
          $opt.foreach($obj.$method)
          """
        (nn, set)
    }.unzip
    q"""
    val $names: ${setType(typeOf[String])} = ${ns.flatten.toSet}
    $configsCompanion.onPath { $config: $configType =>
      ${checkKeys(targetType, config, names)}
      val $obj: $targetType = $newInstance
      ..$sets
      $obj
    }
    """
  }

  private def checkKeys(bean: Type, config: TermName, names: TermName): Tree =
    q"""
    import scala.collection.JavaConverters._
    val ks = $config.root().keySet().asScala
    if (!ks.forall($names.contains)) {
      val ps = ks.diff($names).mkString(",")
      throw new $badPathType($config.origin(), s"Bean $${${fullNameOf(bean)}} does not have properties: $$ps")
    }
    """

}
