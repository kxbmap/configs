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

import com.github.kxbmap.configs.{CValue, ConfigProp, Configs}
import scala.collection.JavaConverters._
import scalaprops.{Gen, Scalaprops}

object CollectionConfigsTest extends Scalaprops with ConfigProp {

  val collection = checkCollectionsOf[Foo].product(checkCollectionsOf[Bar])

  case class Foo(a: String, b: Int)

  object Foo {

    implicit val fooConfigs: Configs[Foo] = Configs.onPath(c => Foo(c.getString("a"), c.getInt("b")))

    implicit val fooGen: Gen[Foo] = for {
      a <- Gen[String]
      b <- Gen[Int]
    } yield Foo(a, b)

    implicit val fooCValue: CValue[Foo] = f => Map[String, Any]("a" -> f.a, "b" -> f.b).asJava
  }

  case class Bar(c: String, d: Int)

  object Bar {

    implicit val barConfigs: Configs[Bar] = Configs.onPath(c => Bar(c.getString("c"), c.getInt("d")))

    implicit val barGen: Gen[Bar] = for {
      c <- Gen[String]
      d <- Gen[Int]
    } yield Bar(c, d)

    implicit val barCValue: CValue[Bar] = f => Map[String, Any]("c" -> f.c, "d" -> f.d).asJava
  }

}
