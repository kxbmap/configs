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
import java.net.InetAddress
import java.{util => ju}
import scalaprops.{Gen, Scalaprops}
import scalaz.{Apply, Equal}

object JavaNetConfigsTest extends Scalaprops {

  val inetAddress = check[InetAddress]

  val inetAddressJList = {
    implicit val h = hideConfigs[InetAddress]
    check[ju.List[InetAddress]]
  }


  implicit lazy val inetAddressGen: Gen[InetAddress] =
    inetAddressStringGen.map(InetAddress.getByName)

  implicit lazy val inetAddressEqual: Equal[InetAddress] =
    Equal.equalA[InetAddress]

  implicit lazy val inetAddressStringGen: Gen[String] = {
    val part = Gen.choose(0, 255)
    Apply[Gen].apply4(part, part, part, part)((a, b, c, d) => s"$a.$b.$c.$d")
  }

  implicit lazy val inetAddressToConfigValue: ToConfigValue[InetAddress] =
    ToConfigValue[String].contramap(_.getHostAddress)

}
