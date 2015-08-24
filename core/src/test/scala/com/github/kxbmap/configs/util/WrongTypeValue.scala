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

package com.github.kxbmap.configs.util

import com.typesafe.config.{ConfigList, ConfigValue}
import java.{lang => jl, util => ju}
import scalaprops.Gen

trait WrongTypeValue[A] {
  def gen: Option[Gen[ConfigValue]]
}

object WrongTypeValue {

  def apply[A](implicit A: WrongTypeValue[A]): WrongTypeValue[A] = A

  def of[A](g: Gen[ConfigValue]): WrongTypeValue[A] = new WrongTypeValue[A] {
    val gen: Option[Gen[ConfigValue]] = Some(g)
  }


  private[this] final val stringConfigValueGen: Gen[ConfigValue] =
    Gen[ConfigValue :@ String].as[ConfigValue]

  private[this] final val emptyListConfigValueGen: Gen[ConfigValue] =
    Gen.value(ju.Collections.emptyList[ConfigValue]().toConfigValue)


  private[this] final val default: WrongTypeValue[Any] =
    WrongTypeValue.of(emptyListConfigValueGen)

  implicit def defaultWrongTypeValue[A]: WrongTypeValue[A] =
    default.asInstanceOf[WrongTypeValue[A]]

  implicit val configValueWrongTypeValue: WrongTypeValue[ConfigValue] = new WrongTypeValue[ConfigValue] {
    val gen: Option[Gen[ConfigValue]] = None
  }

  implicit val configListWrongTypeValue: WrongTypeValue[ConfigList] =
    WrongTypeValue.of(stringConfigValueGen)

  private[this] def collectionWrongTypeValue[F[_], A: WrongTypeValue]: WrongTypeValue[F[A]] =
    WrongTypeValue.of {
      Gen.oneOf(
        stringConfigValueGen,
        WrongTypeValue[A].gen.map(genNonEmptyConfigList).map(_.as[ConfigValue]).toSeq: _*
      )
    }

  implicit def javaCollectionWrongTypeValue[F[_], A: WrongTypeValue](implicit ev: F[A] <:< ju.Collection[A]): WrongTypeValue[F[A]] =
    collectionWrongTypeValue[F, A]

  implicit def traversableWrongTypeValue[F[_], A: WrongTypeValue](implicit ev: F[A] => Traversable[A]): WrongTypeValue[F[A]] =
    collectionWrongTypeValue[F, A]

  implicit def optionWrongTypeValue[A: WrongTypeValue]: WrongTypeValue[Option[A]] = new WrongTypeValue[Option[A]] {
    val gen: Option[Gen[ConfigValue]] = WrongTypeValue[A].gen
  }


  implicit val intWrongTypeValue: WrongTypeValue[Int] =
    WrongTypeValue.of {
      Gen.oneOf(
        emptyListConfigValueGen,
        Seq(
          Gen.value(Int.MaxValue + 1L),
          Gen.value(Long.MaxValue),
          Gen.value(Int.MinValue - 1L),
          Gen.value(Long.MinValue),
          Gen.chooseLong(Int.MaxValue + 1L, Long.MaxValue),
          Gen.chooseLong(Long.MinValue, Int.MinValue - 1L)
        ).map(_.map(_.toConfigValue)): _*
      )
    }

  implicit val javaIntegerWongTypeValue: WrongTypeValue[jl.Integer] =
    intWrongTypeValue.asInstanceOf[WrongTypeValue[jl.Integer]]


  implicit val charWongTypeValue: WrongTypeValue[Char] =
    WrongTypeValue.of {
      import jl.{Character => C}
      def tos(cp: Int): String = new String(C.toChars(cp))
      Gen.oneOf(
        emptyListConfigValueGen,
        Seq(
          Gen.value(C.MAX_VALUE + 1).map(tos),
          Gen.choose(C.MAX_VALUE + 1, C.MAX_CODE_POINT).map(tos),
          Gen.value(""),
          Gen.genString(Gen[Char], 2)
        ).map(_.map(_.toConfigValue)): _*
      )
    }

  implicit val javaCharacterWongTypeValue: WrongTypeValue[jl.Character] =
    charWongTypeValue.asInstanceOf[WrongTypeValue[jl.Character]]

  implicit val charJListWrongTypeValue: WrongTypeValue[ju.List[Char]] =
    defaultWrongTypeValue[ju.List[Char]]

  implicit val characterJListWrongTypeValue: WrongTypeValue[ju.List[jl.Character]] =
    defaultWrongTypeValue[ju.List[jl.Character]]

  implicit def charTraversableWrongTypeValue[F[_]](implicit ev: F[Char] <:< Traversable[Char]): WrongTypeValue[F[Char]] =
    defaultWrongTypeValue[F[Char]]

}
