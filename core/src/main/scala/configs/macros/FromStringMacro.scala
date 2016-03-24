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

class FromStringMacro(val c: blackbox.Context) {

  import c.universe._

  def enumValueFromString[A <: Enumeration : WeakTypeTag]: Tree = {
    val A = weakTypeOf[A].termSymbol.asModule
    q"""
      _root_.configs.FromString.fromOption[$A.Value](
        s => $A.values.find(_.toString == s),
        s => _root_.configs.ConfigError(
          s"$$s is not a valid value for $${${A.fullName}} (valid values: $${$A.values.mkString(", ")})"),
        _.toString)
     """
  }

}
