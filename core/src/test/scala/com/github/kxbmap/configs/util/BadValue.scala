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

import com.typesafe.config.ConfigValue
import java.{lang => jl}
import scala.util.Try
import scalaprops.Gen

trait BadValue[A] {
  def gen: Option[Gen[ConfigValue]]
}

object BadValue {

  def apply[A](implicit A: BadValue[A]): BadValue[A] = A

  def from[A](g: Gen[ConfigValue]): BadValue[A] = new BadValue[A] {
    val gen: Option[Gen[ConfigValue]] = Some(g)
  }

  implicit def defaultBadValue[A]: BadValue[A] = default.asInstanceOf[BadValue[A]]

  private[this] final val default: BadValue[Any] = new BadValue[Any] {
    val gen: Option[Gen[ConfigValue]] = None
  }


  implicit def optionBadValue[A: BadValue]: BadValue[Option[A]] = new BadValue[Option[A]] {
    val gen: Option[Gen[ConfigValue]] = BadValue[A].gen
  }

  implicit def eitherBadValue[A: BadValue]: BadValue[Either[Throwable, A]] = new BadValue[Either[Throwable, A]] {
    val gen: Option[Gen[ConfigValue]] = BadValue[A].gen
  }

  implicit def tryBadValue[A: BadValue]: BadValue[Try[A]] = new BadValue[Try[A]] {
    val gen: Option[Gen[ConfigValue]] = BadValue[A].gen
  }

  implicit def javaIterableBadValue[F[_], A: BadValue](implicit ev: F[A] <:< jl.Iterable[A]): BadValue[F[A]] = new BadValue[F[A]] {
    val gen: Option[Gen[ConfigValue]] = BadValue[A].gen.map {
      genNonEmptyConfigList(_).as[ConfigValue]
    }
  }

}
