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
import scala.reflect.ClassTag
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Equal
import scalaz.std.string._

object CollectionConfigsTest extends Scalaprops with ConfigProp {

  def checkC[A: Configs : Gen : Equal : CValue : ClassTag] = Properties.list(
    check[List[A]].mapId("list " + _),
    check[Vector[A]].mapId("vector " + _),
    check[Stream[A]].mapId("stream " + _),
    check[Array[A]].mapId("array " + _)
  )

  val collections = checkC[Foo]


  case class Foo(a: String, b: Int)

  object Foo {

    implicit val fooConfigs: Configs[Foo] = Configs.onPath(c => Foo(c.getString("a"), c.getInt("b")))

    implicit val fooGen: Gen[Foo] = for {
      a <- Gen[String]
      b <- Gen[Int]
    } yield Foo(a, b)

    implicit val fooCValue: CValue[Foo] = f => Map[String, Any]("a" -> f.a, "b" -> f.b).asJava
  }

}
