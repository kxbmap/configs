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

import com.typesafe.config.ConfigList
import java.{lang => jl, util => ju}

trait WrongTypeValue[A] {
  def value: Any
}

object WrongTypeValue {

  def apply[A](implicit A: WrongTypeValue[A]): WrongTypeValue[A] = A

  def string[A]: WrongTypeValue[A] = _string.asInstanceOf[WrongTypeValue[A]]

  private[this] final val _string: WrongTypeValue[Any] = new WrongTypeValue[Any] {
    val value: Any = "wrong type value"
  }

  def list[A]: WrongTypeValue[A] = _list.asInstanceOf[WrongTypeValue[A]]

  private[this] final val _list: WrongTypeValue[Any] = new WrongTypeValue[Any] {
    val value: Any = ju.Collections.emptyList()
  }

  implicit def defaultWrongTypeValue[A]: WrongTypeValue[A] = list[A]

  implicit val configListWrongTypeValue: WrongTypeValue[ConfigList] = string[ConfigList]

  implicit def javaListWrongTypeValue[A]: WrongTypeValue[ju.List[A]] = string[ju.List[A]]

  implicit def javaSetWrongTypeValue[A]: WrongTypeValue[ju.Set[A]] = string[ju.Set[A]]

  implicit def traversableWrongTypeValue[F[_] <: Traversable[_], A]: WrongTypeValue[F[A]] = string[F[A]]

  implicit def arrayWrongTypeValue[A]: WrongTypeValue[Array[A]] = string[Array[A]]

  implicit val charJListWrongTypeValue: WrongTypeValue[ju.List[Char]] = list[ju.List[Char]]

  implicit val characterJListWrongTypeValue: WrongTypeValue[ju.List[jl.Character]] = list[ju.List[jl.Character]]

  implicit def charTraversableWrongTypeValue[F[_] <: Traversable[_]]: WrongTypeValue[F[Char]] = list[F[Char]]

  implicit val charArrayWrongTypeValue: WrongTypeValue[Array[Char]] = list[Array[Char]]

}
