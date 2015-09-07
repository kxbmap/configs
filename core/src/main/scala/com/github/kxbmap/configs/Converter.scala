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

trait Converter[A, B] {

  def convert(a: A): B

  def map[C](f: B => C): Converter[A, C] =
    convert(_) |> f

}

object Converter {

  def apply[A, B](implicit C: Converter[A, B]): Converter[A, B] = C


  type FromString[A] = Converter[String, A]

  object FromString {
    def apply[A](implicit A: FromString[A]): FromString[A] = A
  }


  private[this] val _identity: Converter[Any, Any] = identity

  implicit def identityConverter[A]: Converter[A, A] =
    _identity.asInstanceOf[Converter[A, A]]


  implicit val symbolFromString: FromString[Symbol] =
    Symbol.apply

}
