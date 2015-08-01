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

import com.github.kxbmap.configs.ConfigProp
import com.github.kxbmap.configs.util.{CValue, IsMissing, IsWrongType}
import com.typesafe.config.ConfigException
import scalaprops.{Gen, Scalaprops}
import scalaz.Equal

object EitherConfigsTest extends Scalaprops with ConfigProp {

  val either = check[Either[Throwable, java.time.Duration]]


  implicit def isMissing[T]: IsMissing[Either[Throwable, T]] =
    _.value.left.exists(_.isInstanceOf[ConfigException.Missing])

  implicit def isWrongType[T]: IsWrongType[Either[Throwable, T]] =
    _.value.left.exists(_.isInstanceOf[ConfigException.WrongType])


  implicit def eitherGen[T: Gen]: Gen[Either[Throwable, T]] =
    Gen.option[T].map(_.toRight(new RuntimeException("dummy")))

  implicit def eitherEqual[T: Equal]: Equal[Either[Throwable, T]] = (a1, a2) =>
    (a1, a2) match {
      case (Right(r1), Right(r2)) => Equal[T].equal(r1, r2)
      case (Left(_), Left(_))     => true
      case _                      => false
    }

  implicit def eitherCValue[T: CValue]: CValue[Either[Throwable, T]] =
    _.right.toOption.map(CValue[T].toConfigValue).orNull

}
