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
import com.github.kxbmap.configs.util._
import java.net.{InetAddress, InetSocketAddress}
import scalaprops.{Gen, Scalaprops}
import scalaz.Apply

object JavaNetConfigsTest extends Scalaprops with ConfigProp {

  val inetAddress = check[InetAddress]

  implicit lazy val inetAddressGen: Gen[InetAddress] =
    inetAddressStringGen.map(InetAddress.getByName)

  implicit lazy val inetAddressStringGen: Gen[String] = {
    val part = Gen.choose(0, 255)
    Apply[Gen].apply4(part, part, part, part)((a, b, c, d) => s"$a.$b.$c.$d")
  }

  implicit lazy val inetAddressConfigVal: ConfigVal[InetAddress] = _.getHostAddress


  val inetSocketAddress = {
    val addr = {
      implicit val gen: Gen[InetSocketAddress] =
        Apply[Gen].apply2(inetAddressGen, Gen.choose(0, Short.MaxValue))(new InetSocketAddress(_, _))

      implicit val cv: ConfigVal[InetSocketAddress] = ConfigVal.asMap(a => Map(
        "addr" -> a.getAddress,
        "port" -> a.getPort
      ))

      check[InetSocketAddress]("addr")
    }
    val hostname = {
      implicit val gen: Gen[InetSocketAddress] =
        Apply[Gen].apply2(Gen.value("localhost"), Gen.choose(0, Short.MaxValue))(new InetSocketAddress(_, _))

      implicit val cv: ConfigVal[InetSocketAddress] = ConfigVal.asMap(a => Map(
        "hostname" -> a.getHostName,
        "port" -> a.getPort
      ))

      check[InetSocketAddress]("hostname")
    }
    addr.product(hostname)
  }

}
