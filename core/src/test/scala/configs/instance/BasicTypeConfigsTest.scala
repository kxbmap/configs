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

package configs.instance

import configs.util._
import java.{lang => jl}
import scalaprops.{Gen, Scalaprops}
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.std.string._

object BasicTypeConfigsTest extends Scalaprops {

  val byte = check[Byte]

  val javaByte = check[jl.Byte]

  val short = check[Short]

  val javaShort = check[jl.Short]

  val int = check[Int]

  val javaInteger = check[jl.Integer]

  val long = check[Long]

  val javaLong = check[jl.Long]

  val float = check[Float]

  val javaFloat = check[jl.Float]

  val double = check[Double]

  val javaDouble = check[jl.Double]

  val boolean = check[Boolean]

  val javaBoolean = check[jl.Boolean]

  val character = check[Char]

  val characterList = {
    implicit val gen: Gen[List[Char]] = Gen[String].map(_.toList)
    implicit val tcv: ToConfigValue[List[Char]] =
      ToConfigValue[String].contramap(cs => new String(cs.toArray))
    check[List[Char]]
  }

  val javaCharacter = check[jl.Character]

  val javaCharacterList = {
    implicit val gen: Gen[List[jl.Character]] =
      Gen[String].map(_.map(jl.Character.valueOf)(collection.breakOut))
    implicit val tcv: ToConfigValue[List[jl.Character]] =
      ToConfigValue[String].contramap(cs => new String(cs.map(_.charValue())(collection.breakOut)))
    check[List[jl.Character]]
  }

  val string = check[String]

}
