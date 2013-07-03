/*
 * Copyright 2013 Tsukasa Kitachi
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
package std

import java.net.{InetSocketAddress, InetAddress}

trait JavaNetSupport {

  /**
   * AtPath for `InetAddress`
   */
  implicit val inetAddressAtPath: AtPath[InetAddress] =
    AtPath by InetAddress.getByName

  /**
   * AtPath for `List[InetAddress]`
   */
  implicit val inetAddressListAtPath: AtPath[List[InetAddress]] =
    AtPath listBy InetAddress.getByName

  /**
   * Configs for `InetSocketAddress`
   */
  implicit val inetSocketAddressConfigs: Configs[InetSocketAddress] =
    Configs.configs { c =>
      new InetSocketAddress(c.get[InetAddress]("host"), c.getInt("port"))
    }

}

object JavaNetSupport extends JavaNetSupport
