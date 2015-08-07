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
import java.{util => ju}
import scala.collection.JavaConverters._
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Apply
import scalaz.std.string._

object CollectionConfigsTest extends Scalaprops with ConfigProp {

  val collections = Properties.list(
    check[List[Foo]].mapId("list " + _),
    check[Vector[Foo]].mapId("vector " + _),
    check[Stream[Foo]].mapId("stream " + _),
    check[Array[Foo]].mapId("array " + _),
    check[Map[String, Foo]].mapId("string map " + _),
    check[Map[Symbol, Foo]].mapId("symbol map " + _),
    check[Set[Foo]].mapId("set " + _)
  )

  val javaCollections = Properties.list(
    check[ju.List[Foo]].mapId("list " + _),
    check[ju.Map[String, Foo]].mapId("map " + _),
    check[ju.Set[Foo]].mapId("set " + _)
  )


  implicit def symbolMapGen[A: Gen]: Gen[Map[Symbol, A]] =
    Gen[Map[String, A]].map(_.map(t => Symbol(t._1) -> t._2))

  implicit def symbolMapConfigVal[A: ConfigVal]: ConfigVal[Map[Symbol, A]] =
    ConfigVal[Map[String, A]].contramap(_.map(t => t._1.name -> t._2))

  implicit def javaMapGen[A: Gen]: Gen[ju.Map[String, A]] =
    Gen[Map[String, A]].map(_.asJava)

  implicit def javaMapConfigVal[A: ConfigVal]: ConfigVal[ju.Map[String, A]] =
    ConfigVal.fromMap(_.asScala.mapValues(_.cv).toMap)

  implicit def javaSetGen[A: Gen]: Gen[ju.Set[A]] =
    Gen[Set[A]].map(_.asJava)

  implicit def javaSetConfigVal[A: ConfigVal]: ConfigVal[ju.Set[A]] =
    ConfigVal[List[A]].contramap(_.asScala.toList)

  implicit def javaSetWrongTypeValue[A]: WrongTypeValue[ju.Set[A]] =
    WrongTypeValue.string[ju.Set[A]]


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
