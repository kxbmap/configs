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

class FromStringMacro(val c: blackbox.Context) extends MacroUtil {

  import c.universe._

  lazy val FromString = q"_root_.configs.FromString"
  lazy val ConfigError = q"_root_.configs.ConfigError"

  def enumValueFromString[A: WeakTypeTag]: Tree = {
    val A = weakTypeOf[A].termSymbol.asModule
    q"""
      $FromString.from[$A.Value] { s: String =>
        $A.values.find(_.toString == s).fold(
          $Result.failure[$A.Value]($ConfigError(
            s"$$s is not a valid value of $${${A.fullName}} (valid values: $${$A.values.mkString(", ")})"
          )))($Result.successful)
      }
     """
  }

}
