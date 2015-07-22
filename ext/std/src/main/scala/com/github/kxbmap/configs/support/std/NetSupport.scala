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
package support.std

import com.typesafe.config.ConfigException
import java.net.{InetAddress, InetSocketAddress, UnknownHostException}

trait NetSupport {

  /**
   * AtPath for `InetAddress`
   */
  implicit val inetAddressAtPath: AtPath[InetAddress] = c => p =>
    try
      InetAddress.getByName(c.getString(p))
    catch {
      case e: UnknownHostException =>
        throw new ConfigException.BadValue(c.origin(), p, e.getMessage, e)
    }

  /**
   * AtPath for `List[InetAddress]`
   */
  implicit def inetAddressesAtPath[C[_]](implicit cbf: CBF[C, InetAddress]): AtPath[C[InetAddress]] = c => p =>
    try
      c.get[List[String]](p).map(InetAddress.getByName)(collection.breakOut)
    catch {
      case e: UnknownHostException =>
        throw new ConfigException.BadValue(c.origin(), p, e.getMessage, e)
    }

  /**
   * Configs for `InetSocketAddress`
   */
  implicit val inetSocketAddressConfigs: Configs[InetSocketAddress] = c => {
    val port = c.getInt("port")
    c.opt[String]("hostname").fold {
      new InetSocketAddress(c.get[InetAddress]("addr"), port)
    } {
      hostname => new InetSocketAddress(hostname, port)
    }
  }

}
