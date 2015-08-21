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

import com.github.kxbmap.configs.simple._
import com.github.kxbmap.configs.util._
import java.util.{Locale, UUID}
import java.{util => ju}
import scalaprops.{Gen, Scalaprops}
import scalaz.Equal

object JavaUtilConfigsTest extends Scalaprops {

  val uuid = check[UUID]

  val uuidJList = {
    implicit val h = hideConfigs[UUID]
    check[ju.List[UUID]]
  }

  val locale = check[Locale]

  val localeJList = {
    implicit val h = hideConfigs[Locale]
    check[ju.List[Locale]]
  }


  implicit lazy val uuidGen: Gen[UUID] =
    Gen[Array[Byte]].map(UUID.nameUUIDFromBytes)

  implicit lazy val uuidEqual: Equal[UUID] =
    Equal.equalA[UUID]

  implicit lazy val uuidConfigVal: ConfigVal[UUID] =
    ConfigVal[String].contramap(_.toString)


  implicit lazy val localeGen: Gen[Locale] = {
    val ls = Locale.getAvailableLocales
    Gen.elements(ls.head, ls.tail: _*)
  }

  implicit lazy val localeEqual: Equal[Locale] =
    Equal.equalA[Locale]

  implicit lazy val localeConfigVal: ConfigVal[Locale] =
    ConfigVal[String].contramap(_.toString)

}
