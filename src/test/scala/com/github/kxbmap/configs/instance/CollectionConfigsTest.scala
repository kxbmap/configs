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

import com.github.kxbmap.configs.util._
import com.github.kxbmap.configs.{ConfigProp, Configs}
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Apply
import scalaz.std.string._

object CollectionConfigsTest extends Scalaprops with ConfigProp {

  val collections = Properties.list(
    check[List[Foo]].mapId("list " + _),
    check[Vector[Foo]].mapId("vector " + _),
    check[Stream[Foo]].mapId("stream " + _),
    check[Array[Foo]].mapId("array " + _)
  )


  case class Foo(a: String, b: Int)

  object Foo {

    implicit val fooConfigs: Configs[Foo] = Configs.onPath(c => Foo(c.getString("a"), c.getInt("b")))

    implicit val fooGen: Gen[Foo] = Apply[Gen].apply2(Gen[String], Gen[Int])(Foo(_, _))

    implicit val fooConfigVal: ConfigVal[Foo] = ConfigVal.fromMap(f => Map(
      "a" -> f.a.cv,
      "b" -> f.b.cv
    ))
  }

}
