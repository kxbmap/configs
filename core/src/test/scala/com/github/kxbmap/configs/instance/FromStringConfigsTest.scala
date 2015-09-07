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

import com.github.kxbmap.configs.Converter
import com.github.kxbmap.configs.util._
import java.{util => ju}
import scalaprops.{Gen, Scalaprops}
import scalaz.Equal

object FromStringConfigsTest extends Scalaprops {

  val fromString = check[Foo]

  val fromStringJList = {
    implicit val h = hideConfigs[Foo]
    check[ju.List[Foo]]
  }

  case class Foo(value: String) {
    require(value.forall(_.isLower))
  }

  implicit lazy val fooFromString: Converter[String, Foo] =
    Foo.apply


  implicit lazy val fooGen: Gen[Foo] =
    Gen.alphaLowerString.map(Foo)

  implicit lazy val fooEqual: Equal[Foo] =
    Equal.equalA[Foo]

  implicit lazy val fooToConfigValue: ToConfigValue[Foo] =
    ToConfigValue[String].contramap(_.value)

  implicit lazy val fooBadValue: BadValue[Foo] =
    BadValue.from(Gen.nonEmptyString(Gen.alphaUpperChar).map(_.toConfigValue))

}
