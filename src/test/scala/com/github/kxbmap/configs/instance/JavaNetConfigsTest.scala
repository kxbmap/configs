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

import com.github.kxbmap.configs.{CValue, ConfigProp}
import java.net.{InetAddress, InetSocketAddress}
import scala.collection.JavaConverters._
import scalaprops.{Gen, Scalaprops}

object JavaNetConfigsTest extends Scalaprops with ConfigProp {

  val inetAddress = check[InetAddress]

  implicit lazy val inetAddressGen: Gen[InetAddress] =
    inetAddressStringGen.map(InetAddress.getByName)

  implicit lazy val inetAddressStringGen: Gen[String] = {
    val partGen = Gen.choose(0, 255)
    for {
      a <- partGen
      b <- partGen
      c <- partGen
      d <- partGen
    } yield s"$a.$b.$c.$d"
  }

  implicit lazy val inetAddressCValue: CValue[InetAddress] = a => a.getHostAddress


  val inetSocketAddress = {
    val addr = {
      implicit val gen: Gen[InetSocketAddress] = for {
        a <- inetAddressGen
        p <- Gen.choose(0, Short.MaxValue)
      } yield new InetSocketAddress(a, p)

      implicit val cv: CValue[InetSocketAddress] = a => Map[String, Any](
        "addr" -> a.getAddress.getHostAddress,
        "port" -> a.getPort
      ).asJava

      check[InetSocketAddress]("addr")
    }
    val hostname = {
      implicit val gen: Gen[InetSocketAddress] = for {
        h <- Gen.value("localhost")
        p <- Gen.choose(0, Short.MaxValue)
      } yield new InetSocketAddress(h, p)

      implicit val cv: CValue[InetSocketAddress] = a => Map[String, Any](
        "hostname" -> a.getHostName,
        "port" -> a.getPort
      ).asJava

      check[InetSocketAddress]("hostname")
    }
    addr.product(hostname)
  }

}
