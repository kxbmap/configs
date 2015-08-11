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
import com.github.kxbmap.configs.util.{ConfigVal, IsMissing, IsWrongType}
import com.typesafe.config.ConfigException
import scala.util.{Failure, Success, Try}
import scalaprops.{Gen, Scalaprops}
import scalaz.Equal

object TryConfigsTest extends Scalaprops with ConfigProp {

  val `try` = check[Try[java.time.Duration]]


  implicit def isMissing[A]: IsMissing[A] = _.value match {
    case Failure(_: ConfigException.Missing) => true
    case _                                   => false
  }

  implicit def isWrongType[A]: IsWrongType[A] = _.value match {
    case Failure(_: ConfigException.WrongType) => true
    case _                                     => false
  }


  implicit def tryGen[A: Gen]: Gen[Try[A]] =
    Gen.option[A].map {
      case Some(v) => Success(v)
      case None    => Failure(new RuntimeException("dummy"))
    }

  implicit def tryEqual[A: Equal]: Equal[Try[A]] =
    Equal[Option[A]].contramap(_.toOption)

  implicit def tryConfigVal[A: ConfigVal]: ConfigVal[Try[A]] =
    ConfigVal[Option[A]].contramap(_.toOption)

}
