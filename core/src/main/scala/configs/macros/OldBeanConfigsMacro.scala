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

import scala.reflect.macros.blackbox

class OldBeanConfigsMacro(val c: blackbox.Context) extends MacroUtil {

  import c.universe._

  def materializeA[A: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[A]
    if (tpe.typeSymbol.isAbstract) abort(s"$tpe must be concrete class")
    val hasNoArgCtor = tpe.decls.exists {
      case m: MethodSymbol => m.isConstructor && m.isPublic && m.paramLists.length <= 1 && m.paramLists.forall(_.isEmpty)
      case _               => false
    }
    if (!hasNoArgCtor) {
      abort(s"$tpe must have public no-arg constructor")
    }
    materializeImpl[A](q"new $tpe()")
  }

  def materializeI[A: WeakTypeTag](newInstance: Tree): Tree = {
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
    val sets = setters.map {
      case (name, method, paramType) =>
        val cn = TermName("c")
        val nn = Seq(name, toLowerHyphenCase(name)).distinct
        val opt = nn.map(n => q"$cn.get($config, $n)").reduceLeft((l, r) => q"$l.orElse($r)")
        q"""
        val $cn = $Configs[${tOption(paramType)}]
        $opt.foreach($obj.$method)
        """
    }
    q"""
    $Configs.onPath { $config: $tConfig =>
      val $obj: $targetType = $newInstance
      ..$sets
      $obj
    }
    """
  }

}
