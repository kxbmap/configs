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

import com.github.kxbmap.configs._
import com.typesafe.config.ConfigMemorySize
import java.{lang => jl, time => jt, util => ju}
import scalaprops.Scalaprops
import scalaz.std.anyVal._

object BasicTypeConfigsTest extends Scalaprops with ConfigProp {

  //  val config = ???
  //  val configList = ???

  val int = check[Int]
  val javaIntegerList = check[ju.List[jl.Integer]]

  val long = check[Long]
  val javaLongList = check[ju.List[jl.Long]]

  val double = check[Double]
  val javaDoubleList = check[ju.List[jl.Double]]

  val boolean = check[Boolean]
  val javaBooleanList = check[ju.List[jl.Boolean]]

  val string = check[String]
  val stringList = check[ju.List[String]]

  val javaDuration = check[jt.Duration]
  val javaDurationList = check[ju.List[jt.Duration]]

  val configMemorySize = check[ConfigMemorySize]
  val configMemorySizeList = check[ju.List[ConfigMemorySize]]

}
