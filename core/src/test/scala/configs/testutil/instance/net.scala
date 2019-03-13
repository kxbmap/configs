/*
 * Copyright 2013-2016 Tsukasa Kitachi
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

package configs.testutil.instance

import java.net.{InetAddress, URI}
import scalaprops.Gen
import scalaprops.ScalapropsScalaz._
import scalaz.{Apply, Equal}

object net {

  implicit lazy val inetAddressGen: Gen[InetAddress] = {
    val p = Gen.choose(0, 255)
    Apply[Gen].apply4(p, p, p, p)((a, b, c, d) => s"$a.$b.$c.$d").map(InetAddress.getByName)
  }

  implicit lazy val inetAddressEqual: Equal[InetAddress] =
    Equal.equalA[InetAddress]

  implicit lazy val uriGen: Gen[URI] = {
    val str = Gen.nonEmptyString(Gen.alphaChar)
    val opt = Gen.option(str)
    Apply[Gen].apply3(opt, str, opt) {
      (scheme, ssp, fragment) => new URI(scheme.orNull, ssp, fragment.orNull)
    }
  }

  implicit lazy val uriEqual: Equal[URI] =
    Equal.equalA[URI]

}
