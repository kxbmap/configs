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

import scala.collection.mutable
import scala.reflect.macros.blackbox

class NConfigsMacro(val c: blackbox.Context) extends MacroUtil {

  import c.universe._

  def materializeConfigs[A: WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[A]
    val instance = tpe.typeSymbol match {
      case typeSym if typeSym.isClass =>
        val classSym = typeSym.asClass
        if (classSym.isAbstract) {
          if (classSym.isSealed)
            sealedClassConfigs(tpe, classSym)
          else
            abort(s"$tpe is abstract but not sealed")
        } else {
          if (classSym.isCaseClass)
            caseClassConfigs(tpe, classSym.companion.asModule)
          else
            plainClassConfigs(tpe)
        }

      case _ => abort(s"$tpe is not class")
    }
    val self = freshName("s")
    q"""
      implicit lazy val $self: ${tConfigs(tpe)} = $instance
      $self
     """
  }


  private class State {
    private val buf = mutable.Buffer[(Type, TermName, Tree)]()
    val config: TermName = freshName("c")

    def configParam: Tree = q"$config: $tConfig"

    def vals: Seq[Tree] =
      buf.map {
        case (_, n, t) => q"val $n = $t"
      }

    def configs(tpe: Type): TermName =
      buf.find(_._1 =:= tpe).map(_._2).getOrElse {
        val c = freshName("c")
        buf += ((tpe, c, q"$Configs[$tpe]"))
        c
      }
  }

  private case class Param(sym: Symbol, method: Method, pos: Int) {
    val name = nameOf(sym)
    val term = sym.asTerm
    val tpe = sym.info

    def toResult(implicit st: State): Tree =
      q"${st.configs(pType)}.get(${st.config}, $name)"

    def pType: Type =
      if (term.isParamWithDefault) tOption(tpe) else tpe

    def toArg(a: TermName): Tree =
      defaultMethod.fold[Tree](q"$a")(m => q"$a.getOrElse(${m.applyTrees(Nil)})")

    def defaultMethod: Option[Method] =
      if (term.isParamWithDefault) method.defaultMethod(pos) else None
  }

  private sealed abstract class Method {
    def method: MethodSymbol

    def companion: Option[ModuleSymbol]

    def applyTrees(args: Seq[Seq[Tree]]): Tree

    def applyTerms(args: Seq[Seq[TermName]]): Tree =
      applyTrees(args.map(_.map(t => q"$t")))

    def paramLists: List[List[Param]] =
      zipWithParamPos(method.paramLists).map(_.map {
        case (s, p) => Param(s, this, p)
      })

    def defaultMethod(pos: Int): Option[Method] =
      defaultMethods.get(pos)

    private lazy val defaultMethods: Map[Int, Method] =
      companion match {
        case None => Map.empty
        case Some(cmp) =>
          val prefix = s"${method.name.encodedName}$$default$$"
          cmp.info.decls.collect {
            case m: MethodSymbol if encodedNameOf(m).startsWith(prefix) =>
              encodedNameOf(m).drop(prefix.length).toInt -> ModuleMethod(cmp, m)
          }(collection.breakOut)
      }
  }

  private case class ModuleMethod(module: ModuleSymbol, method: MethodSymbol) extends Method {

    lazy val companion: Option[ModuleSymbol] =
      Some(module)

    def applyTrees(args: Seq[Seq[Tree]]): Tree =
      q"$module.$method(...$args)"
  }

  private case class Constructor(tpe: Type, method: MethodSymbol) extends Method {

    lazy val companion: Option[ModuleSymbol] =
      tpe.typeSymbol.companion match {
        case NoSymbol => None
        case s => Some(s.asModule)
      }

    def applyTrees(args: Seq[Seq[Tree]]): Tree =
      q"new $tpe(...$args)"
  }


  private def sealedClassConfigs(tpe: Type, classSym: ClassSymbol): Tree = {
    val knownSubclasses = {
      def go(cs: ClassSymbol): List[ClassSymbol] =
        if (cs.isSealed) {
          val subs = cs.knownDirectSubclasses.toList.map(_.asClass).flatMap(go)
          if (cs.isAbstract) subs else cs :: subs
        }
        else if (!cs.isAbstract) List(cs)
        else Nil
      go(classSym)
    }
    if (knownSubclasses.isEmpty)
      abort(s"$tpe has no known sub classes")

    val parts = knownSubclasses.map { cs =>
      val name = nameOf(cs)
      val inst =
        if (cs.isModuleClass)
          q"$Configs.fromConfig[$tpe](_ => $Result.successful(${cs.module}))"
        else if (cs.isCaseClass)
          caseClassConfigs(cs.toType, cs.companion.asModule, tpe)
        else
          plainClassConfigs(cs.toType, tpe)
      val module =
        if (cs.isModuleClass) Some(cs.module) else None
      (cq"$name => $inst", module.map(m => cq"$name => $m"))
    }

    val typeKey = "type"
    val typeInst =
      q"""
        $Configs.get[$tString]($typeKey).flatMap[$tpe] {
          case ..${parts.map(_._1)}
          case s => $Configs.failure("unknown type: " + s)
        }
       """
    if (parts.forall(_._2.isEmpty)) typeInst
    else {
      val moduleInst =
        q"""
          $Configs[$tString].map[$tpe] {
            case ..${parts.flatMap(_._2)}
            case s => throw new $ConfigException.Generic("unknown module: " + s)
          }
         """
      q"$moduleInst.orElse($typeInst)"
    }
  }

  private def caseClassConfigs(tpe: Type, module: ModuleSymbol): Tree =
    caseClassConfigs(tpe, module, tpe)

  private def caseClassConfigs(tpe: Type, module: ModuleSymbol, target: Type): Tree = {
    val applies = module.info.decls.sorted
      .collect {
        case m: MethodSymbol
          if m.isPublic && m.returnType =:= tpe && nameOf(m) == "apply" =>
          ModuleMethod(module, m)
      }
      // synthetic first
      .sortBy(!_.method.isSynthetic)

    if (applies.isEmpty)
      abort(s"$tpe has no available apply methods")
    else
      applies.map(methodConfigs(_, target)).reduceLeft((l, r) => q"$l.orElse($r)")
  }

  private def plainClassConfigs(tpe: Type): Tree =
    plainClassConfigs(tpe, tpe)

  private def plainClassConfigs(tpe: Type, target: Type): Tree = {
    val ctors = tpe.decls.sorted.collect {
      case m: MethodSymbol if m.isConstructor && m.isPublic =>
        Constructor(tpe, m)
    }
    if (ctors.isEmpty)
      abort(s"$tpe has no public constructor")
    else
      ctors.map(methodConfigs(_, target)).reduceLeft((l, r) => q"$l.orElse($r)")
  }

  private def methodConfigs(method: Method, target: Type): Tree = {
    implicit val st = new State()
    val paramLists = method.paramLists

    def noArgMethod: Tree = {
      val args = paramLists.map(_.map(_ => abort(s"bug or broken: $method")))
      q"""$Result.successful(${method.applyTrees(args)})"""
    }

    def oneArgMethod: Tree = {
      val a = freshName("a")
      val param = paramLists.flatten match {
        case p :: Nil => p
        case _ => abort(s"bug or broken: $method")
      }
      val args = paramLists.map(_.map(_.toArg(a)))
      q"""${param.toResult}.map(($a: ${param.pType}) => ${method.applyTrees(args)})"""
    }

    def smallMethod(n: Int): Tree = {
      val parts = paramLists.map(_.map { p =>
        val a = freshName("a")
        (p.toResult, q"$a: ${p.tpe}", a)
      }.unzip3)
      val results = parts.flatMap(_._1)
      val params = parts.flatMap(_._2)
      val args = parts.map(_._3)
      q"""${applyResultN(n)}(..$results)(..$params => ${method.applyTerms(args)})"""
    }

    def largeMethod(n: Int): Tree = {
      val results = paramLists.flatMap(_.map(p => (p.tpe, p.toResult)))
      val g = {
        val d = (n + MaxTupleN - 1) / MaxTupleN
        (n + d - 1) / d
      }
      val (rs, params, gArgs) =
        results.grouped(g).map { group =>
          val m = group.length
          val r = q"""${tupleResultN(m)}(..${group.map(_._2)})"""
          val t = freshName("t")
          val tt = tq"${tTupleN(m)}[..${group.map(_._1)}]"
          val as = (1 to m).map(i => q"$t.${TermName(s"_$i")}")
          (r, q"$t: $tt", as)
        }.toList.unzip3
      val args = fitShape(gArgs.flatten, paramLists)
      q"""${applyResultN(rs.length)}(..$rs)(..$params => ${method.applyTrees(args)})"""
    }

    val body = length(paramLists) match {
      case 0 => noArgMethod
      case 1 => oneArgMethod
      case n if n <= MaxApplyN => smallMethod(n)
      case n if n <= MaxTupleN * MaxApplyN => largeMethod(n)
      case _ => abort("too large param lists")
    }
    q"""
      $Configs.fromConfig[$target] { ${st.configParam} =>
        ..${st.vals}
        $body
      }
     """
  }

}
