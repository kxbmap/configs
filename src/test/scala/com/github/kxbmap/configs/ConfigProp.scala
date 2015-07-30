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

package com.github.kxbmap.configs

import com.typesafe.config.{ConfigException, ConfigFactory, ConfigMemorySize, ConfigValueFactory}
import java.{lang => jl, time => jt, util => ju}
import scala.collection.JavaConverters._
import scalaprops.Or.Empty
import scalaprops.Property.forAll
import scalaprops.{:-:, Gen, Properties}
import scalaz.std.anyVal._
import scalaz.std.list._
import scalaz.std.string._
import scalaz.{Equal, Need, Order}

trait ConfigProp {

  def check[A: Configs : Gen : Equal : CValue : IsMissing : IsWrongType : WrongTypeValue]: Properties[Unit :-: String :-: Empty] =
    checkId(())

  def check[A: Configs : Gen : Equal : CValue : IsMissing : IsWrongType : WrongTypeValue](id: String): Properties[String :-: String :-: Empty] =
    checkId(id)

  private def checkId[A: Order, B: Configs : Gen : Equal : CValue : IsMissing : IsWrongType : WrongTypeValue](id: A) =
    Properties.either(
      id,
      checkGet[B].toProperties("get"),
      checkMissing[B].toProperties("missing"),
      checkWrongType[B].toProperties("wrong type")
    )


  def checkGet[T: Configs : Gen : Equal : CValue] = forAll { value: T =>
    val p = "dummy-path"
    val c = CValue[T].toConfigValue(value).atPath(p)
    Equal[T].equal(Configs[T].get(c, p), value)
  }

  def checkMissing[A: Configs : IsMissing] = forAll {
    val p = "missing"
    val c = ConfigFactory.empty()
    IsMissing[A].check(Need(Configs[A].get(c, p)))
  }

  def checkWrongType[A: Configs : IsWrongType : WrongTypeValue] = forAll {
    val p = "dummy-path"
    val c = ConfigValueFactory.fromAnyRef(WrongTypeValue[A].value).atPath(p)
    IsWrongType[A].check(Need(Configs[A].get(c, p)))
  }


  implicit def generalEqual[T]: Equal[T] =
    Equal.equalA[T]

  implicit def arrayEqual[T: Equal]: Equal[Array[T]] =
    Equal.equalBy(_.toList)


  implicit def javaListGen[T: Gen]: Gen[ju.List[T]] =
    Gen.list[T].map(_.asJava)


  implicit lazy val stringGen: Gen[String] =
    Gen.asciiString

  implicit lazy val doubleGen: Gen[Double] =
    Gen.genFiniteDouble


  implicit lazy val javaDoubleGen: Gen[jl.Double] =
    doubleGen.map(Double.box)


  implicit lazy val javaDurationGen: Gen[jt.Duration] =
    Gen.chooseLong(0, Long.MaxValue).map(jt.Duration.ofNanos)

  implicit lazy val javaDurationCValue: CValue[jt.Duration] =
    d => s"${d.toNanos}ns"


  implicit lazy val configMemorySizeGen: Gen[ConfigMemorySize] =
    Gen.chooseLong(0, Long.MaxValue).map(ConfigMemorySize.ofBytes)

}


trait IsMissing[A] {
  def check(a: Need[A]): Boolean
}

object IsMissing {

  def apply[A](implicit A: IsMissing[A]): IsMissing[A] = A

  implicit def defaultIsMissing[A]: IsMissing[A] = a =>
    try {
      a.value
      false
    } catch {
      case _: ConfigException.Missing => true
    }
}

trait IsWrongType[A] {
  def check(a: Need[A]): Boolean
}

object IsWrongType {

  def apply[A](implicit A: IsWrongType[A]): IsWrongType[A] = A

  implicit def defaultIsWrong[A]: IsWrongType[A] = a =>
    try {
      a.value
      false
    } catch {
      case _: ConfigException.WrongType => true
    }
}

trait WrongTypeValue[A] {
  def value: Any
}

object WrongTypeValue {

  def apply[A](implicit A: WrongTypeValue[A]): WrongTypeValue[A] = A

  private[this] final val string: WrongTypeValue[Any] = new WrongTypeValue[Any] {
    val value: Any = "wrong type value"
  }

  private[this] final val list: WrongTypeValue[Any] = new WrongTypeValue[Any] {
    val value: Any = ju.Collections.emptyList()
  }

  implicit def defaultWrongTypeValue[A]: WrongTypeValue[A] = list.asInstanceOf[WrongTypeValue[A]]

  implicit def javaListWrongTypeValue[A]: WrongTypeValue[ju.List[A]] = string.asInstanceOf[WrongTypeValue[ju.List[A]]]

  implicit def seqWrongTypeValue[F[_] <: Seq[_], A]: WrongTypeValue[F[A]] = string.asInstanceOf[WrongTypeValue[F[A]]]

  implicit def arrayWrongTypeValue[A]: WrongTypeValue[Array[A]] = string.asInstanceOf[WrongTypeValue[Array[A]]]

}
