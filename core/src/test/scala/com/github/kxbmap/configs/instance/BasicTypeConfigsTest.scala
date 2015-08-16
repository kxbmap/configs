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

package com.github.kxbmap.configs.instance

import com.github.kxbmap.configs.ConfigProp
import java.{lang => jl, util => ju}
import scalaprops.Scalaprops
import scalaz.std.string._

object BasicTypeConfigsTest extends Scalaprops with ConfigProp {

  val byte = check[Byte]
  val byteJList = {
    implicit val h = hideConfigs[Byte]
    check[ju.List[Byte]]
  }
  val javaByte = check[jl.Byte]
  val javaByteList = {
    implicit val h = hideConfigs[jl.Byte]
    check[ju.List[jl.Byte]]
  }

  val short = check[Short]
  val shortJList = {
    implicit val h = hideConfigs[Short]
    check[ju.List[Short]]
  }
  val javaShort = check[jl.Short]
  val javaShortList = {
    implicit val h = hideConfigs[jl.Short]
    check[ju.List[jl.Short]]
  }

  val int = check[Int]
  val intJList = {
    implicit val h = hideConfigs[Int]
    check[ju.List[Int]]
  }
  val javaInteger = check[jl.Integer]
  val javaIntegerList = {
    implicit val h = hideConfigs[jl.Integer]
    check[ju.List[jl.Integer]]
  }

  val long = check[Long]
  val longJList = {
    implicit val h = hideConfigs[Long]
    check[ju.List[Long]]
  }
  val javaLong = check[jl.Long]
  val javaLongList = {
    implicit val h = hideConfigs[jl.Long]
    check[ju.List[jl.Long]]
  }

  val float = check[Float]
  val floatJList = {
    implicit val h = hideConfigs[Float]
    check[ju.List[Float]]
  }
  val javaFloat = check[jl.Float]
  val javaFloatList = {
    implicit val h = hideConfigs[jl.Float]
    check[ju.List[jl.Float]]
  }

  val double = check[Double]
  val doubleJList = {
    implicit val h = hideConfigs[Double]
    check[ju.List[Double]]
  }
  val javaDouble = check[jl.Double]
  val javaDoubleList = {
    implicit val h = hideConfigs[jl.Double]
    check[ju.List[jl.Double]]
  }

  val boolean = check[Boolean]
  val booleanJList = {
    implicit val h = hideConfigs[Boolean]
    check[ju.List[Boolean]]
  }
  val javaBoolean = check[jl.Boolean]
  val javaBooleanList = {
    implicit val h = hideConfigs[jl.Boolean]
    check[ju.List[jl.Boolean]]
  }

  val char = check[Char]
  val charJList = {
    implicit val h = hideConfigs[Char]
    check[ju.List[Char]]
  }
  val javaCharacter = check[jl.Character]
  val javaCharacterList = {
    implicit val h = hideConfigs[jl.Character]
    check[ju.List[jl.Character]]
  }

  val string = check[String]
  val stringJList = {
    implicit val h = hideConfigs[String]
    check[ju.List[String]]
  }

}
