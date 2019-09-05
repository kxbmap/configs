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

package configs.testutil.instance

import configs.testutil.instance.string._
import java.{lang => jl, util => ju}
import scala.collection.compat._
import scala.jdk.CollectionConverters._
import scalaprops.Gen
import scalaz.{Equal, Order, std}

object collection {

  implicit def listEqual[A: Equal]: Equal[List[A]] =
    std.list.listEqual[A]

  implicit def vectorEqual[A: Equal]: Equal[Vector[A]] =
    std.vector.vectorEqual[A]

  implicit def arrayEqual[A: Equal]: Equal[Array[A]] =
    listEqual[A].contramap(_.toList)


  implicit def javaListGen[F[X] >: ju.List[X], A: Gen]: Gen[F[A]] =
    Gen.list[A].map(_.asJava)

  implicit def javaIterableEqual[F[X] <: jl.Iterable[X], A: Equal]: Equal[F[A]] =
    listEqual[A].contramap(_.asScala.toList)


  implicit def mapGen[M[_, _], A: Gen, B: Gen](implicit F: Factory[(A, B), M[A, B]]): Gen[M[A, B]] =
    Gen.mapGen[A, B].map(F.fromSpecific)

  implicit def mapEqual[M[X, Y] <: scala.collection.Map[X, Y], A: Order, B: Equal]: Equal[M[A, B]] =
    std.map.mapEqual[A, B].contramap(m => m.toMap)

  implicit def javaMapGen[A: Gen, B: Gen]: Gen[ju.Map[A, B]] =
    Gen.mapGen[A, B].map(_.asJava)

  implicit def javaMapEqual[M[X, Y] <: ju.Map[X, Y], A: Order, B: Equal]: Equal[M[A, B]] =
    std.map.mapEqual[A, B].contramap(_.asScala.toMap)


  implicit def setEqual[S[X] <: scala.collection.Set[X], A: Order]: Equal[S[A]] =
    std.set.setOrder[A].contramap(_.toSet)

  implicit def javaSetGen[A: Gen]: Gen[ju.Set[A]] =
    Gen.setGen[A].map(_.asJava)

  implicit def javaSetEqual[A: Order]: Equal[ju.Set[A]] =
    setEqual[scala.collection.Set, A].contramap(_.asScala)


  implicit lazy val javaPropertiesGen: Gen[ju.Properties] =
    Gen[ju.Map[String, String]].map { m =>
      val p = new ju.Properties()
      (p: java.util.Hashtable[AnyRef, AnyRef]).putAll(m)
      p
    }

  implicit lazy val javaPropertiesEqual: Equal[ju.Properties] =
    Equal.equalBy(_.asScala)

}
