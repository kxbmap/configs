package com.github.kxbmap.configs
package ext.std

import java.net.{InetSocketAddress, InetAddress}

trait JavaNetConfigs {

  /**
   * AtPath for `InetAddress`
   */
  implicit val inetAddressAtPath: AtPath[InetAddress] =
    AtPath mapBy InetAddress.getByName

  /**
   * AtPath for `List[InetAddress]`
   */
  implicit val inetAddressListAtPath: AtPath[List[InetAddress]] =
    AtPath mapListBy InetAddress.getByName

  /**
   * Configs for `InetSocketAddress`
   */
  implicit val inetSocketAddressConfigs: Configs[InetSocketAddress] =
    Configs { c =>
      new InetSocketAddress(c.get[InetAddress]("host"), c.getInt("port"))
    }

}
