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

class ToConfigMacro(val c: blackbox.Context) extends MacroUtil with Util {

  import c.universe._

  def derive[A: WeakTypeTag]: Tree = {
    asCaseClass[A].map(caseClassInstance).getOrElse(abort("not a case class"))
  }

  val ToConfig = q"_root_.configs.ToConfig"
  val ImmSeq = q"_root_.scala.collection.immutable.Seq"

  case class CaseClass(tpe: Type, accessors: List[CaseAccessor])

  case class CaseAccessor(method: MethodSymbol, result: Type, key: String)

  def isCaseClass(sym: Symbol): Boolean =
    sym.isClass && {
      val cs = sym.asClass
      cs.isCaseClass && !cs.isAbstract
    }

  def asCaseClass[A: WeakTypeTag]: Option[CaseClass] =
    PartialFunction.condOpt(symbolOf[A]) {
      case sym if isCaseClass(sym) =>
        val tpe = weakTypeOf[A]
        val accessors = tpe.decls.sorted.collect {
          case m: MethodSymbol if m.isCaseAccessor =>
            m.infoIn(tpe) match {
              case NullaryMethodType(result) =>
                val key = toLowerHyphenCase(nameOf(m))
                CaseAccessor(m, result, key)
              case _ => abort("bug or broken")
            }
        }
        CaseClass(tpe, accessors)
    }

  def caseClassInstance(cc: CaseClass): Tree = {
    val v = freshName("v")
    val kvs = cc.accessors.map { a =>
      q"(${a.key}, $ToConfig[${a.result}].toValueOption($v.${a.method}))"
    }
    val seq = q"$ImmSeq[($tString, ${tOption(tConfigValue)})](..$kvs)"
    q"""
      $ToConfig.from { $v: ${cc.tpe} =>
        $seq.foldLeft(_root_.configs.ConfigObject.empty) {
          case (c, (k, v)) => v.foldLeft(c)(_.withValue(k, _))
        }
      }
     """
  }

}
