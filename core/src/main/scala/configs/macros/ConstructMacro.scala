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

class ConstructMacro(val c: blackbox.Context) {

  import c.universe._

  private val Impl = q"_root_.configs.syntax.construct.Impl"

  private def transform(body: Tree): Tree = {
    val transformer = new Transformer {
      override def transform(tree: Tree): Tree = tree match {
        case q"configs.syntax.construct.ConstructSyntax[$ta]($a).:=[$tb]($b)($ia, $ib)" =>
          c.typecheck(q"$Impl.assign[$ta, $tb]($a, $b)($ia, $ib)")
        case _ => super.transform(tree)
      }
    }
    transformer.transform(body)
  }

  def configObject(body: Tree): Tree =
    q"$Impl.configObject(${transform(body)})"

  def configObjectWithComments(comments: Tree*)(body: Tree): Tree =
    q"$Impl.configObjectWithComments(..$comments)(${transform(body)})"

}
