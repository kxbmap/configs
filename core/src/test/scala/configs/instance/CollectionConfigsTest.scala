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

import configs.Configs
import configs.util._
import java.{lang => jl, util => ju}
import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.collection.immutable.TreeMap
import scala.collection.mutable
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.list._
import scalaz.std.map._
import scalaz.std.stream._
import scalaz.std.string._
import scalaz.std.vector._
import scalaz.{Equal, Order}

object CollectionConfigsTest extends Scalaprops {

  val javaList = check[ju.List[Foo]]

  val javaIterable = check[jl.Iterable[Foo]]

  val javaCollection = check[ju.Collection[Foo]]

  val javaSet = check[ju.Set[Foo]]

  val javaMap = Properties.list(
    check[ju.Map[String, Foo]]("string map"),
    check[ju.Map[Symbol, Foo]]("symbol map")
  )

  val fromJList = Properties.list(
    check[List[Foo]]("list"),
    check[Vector[Foo]]("vector"),
    check[Stream[Foo]]("stream"),
    check[Array[Foo]]("array"),
    check[Set[Foo]]("set")
  )

  val fromJMap = Properties.list(
    check[Map[String, Foo]]("map"),
    check[TreeMap[String, Foo]]("tree map"),
    check[mutable.Map[String, Foo]]("mutable map")
  )


  case class Foo(value: Int)

  object Foo {

    implicit val gen: Gen[Foo] =
      Gen[Int].map(Foo.apply)

    implicit val equal: Equal[Foo] =
      Equal.equalA[Foo]

    implicit val tcv: ToConfigValue[Foo] =
      _.value.toConfigValue.atKey("v").root()

    implicit val configs: Configs[Foo] =
      Configs.fromConfigTry(c => Foo(c.getInt("v")))

  }


  implicit lazy val symbolOrder: Order[Symbol] =
    Order[String].contramap(_.name)

  implicit def cbfMapGen[M[_, _], A: Gen, B: Gen](implicit g: Gen[Map[A, B]], cbf: CanBuildFrom[Nothing, (A, B), M[A, B]]): Gen[M[A, B]] =
    g.map(_.to[({type F[_] = M[A, B]})#F])

  implicit def javaStringMapGen[A: Gen]: Gen[ju.Map[String, A]] =
    Gen[Map[String, A]].map(_.asJava)

  implicit def javaSymbolMapGen[A: Gen]: Gen[ju.Map[Symbol, A]] =
    Gen[Map[String, A]].map(_.map(t => Symbol(t._1) -> t._2).asJava)

  implicit def javaSetGen[A: Gen]: Gen[ju.Set[A]] =
    Gen[Set[A]].map(_.asJava)

  implicit def javaIterableGen[A: Gen]: Gen[jl.Iterable[A]] =
    Gen[ju.List[A]].as[jl.Iterable[A]]

  implicit def javaCollectionGen[A: Gen]: Gen[ju.Collection[A]] =
    Gen[ju.List[A]].as[ju.Collection[A]]

  implicit def arrayEqual[A: Equal]: Equal[Array[A]] =
    Equal.equalBy(_.toList)

  implicit def mapSubEqual[M[_, _], A: Order, B: Equal](implicit ev: M[A, B] <:< collection.Map[A, B]): Equal[M[A, B]] =
    Equal.equalBy(_.toMap)

  implicit def setEqual[A: Equal]: Equal[Set[A]] =
    Equal.equalA[Set[A]]

  implicit def javaSetEqual[A: Equal]: Equal[ju.Set[A]] =
    Equal.equalBy(_.asScala.toSet)

}
