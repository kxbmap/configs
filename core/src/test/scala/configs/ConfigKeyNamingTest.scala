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

package configs

import com.typesafe.config.ConfigRenderOptions
import scalaprops.Property.forAll
import scalaprops.{Gen, Scalaprops}
import scalaz.syntax.apply._

object ConfigKeyNamingTest extends Scalaprops {

  case class User(name: String, emailAddress: String, siteURL: String)

  implicit val userGen: Gen[User] = {
    val s = Gen.nonEmptyString(Gen.alphaChar)
    (s |@| s |@| s)(User.apply)
  }

  def render[A: ConfigWriter](a: A): String =
    ConfigWriter[A].write(a).render(
      ConfigRenderOptions.defaults().setJson(false).setOriginComments(false))

  val identity = {
    implicit val naming: ConfigKeyNaming[User] = ConfigKeyNaming.identity

    forAll { user: User =>
      val expected =
        s"""emailAddress=${user.emailAddress}
           |name=${user.name}
           |siteURL=${user.siteURL}
           |""".stripMargin

      render(user) == expected
    }
  }

  val hyphenSeparated = {
    implicit val naming: ConfigKeyNaming[User] = ConfigKeyNaming.hyphenSeparated

    forAll { user: User =>
      val expected =
        s"""email-address=${user.emailAddress}
           |name=${user.name}
           |site-url=${user.siteURL}
           |""".stripMargin

      render(user) == expected
    }
  }

  val snakeCase = {
    implicit val naming: ConfigKeyNaming[User] = ConfigKeyNaming.snakeCase

    forAll { user: User =>
      val expected =
        s""""email_address"=${user.emailAddress}
           |name=${user.name}
           |"site_url"=${user.siteURL}
           |""".stripMargin

      render(user) == expected
    }
  }

  val lowerCamelCase = {
    implicit val naming: ConfigKeyNaming[User] = ConfigKeyNaming.lowerCamelCase

    forAll { user: User =>
      val expected =
        s"""emailAddress=${user.emailAddress}
           |name=${user.name}
           |siteURL=${user.siteURL}
           |""".stripMargin

      render(user) == expected
    }
  }

  val upperCamelCase = {
    implicit val naming: ConfigKeyNaming[User] = ConfigKeyNaming.upperCamelCase

    forAll { user: User =>
      val expected =
        s"""EmailAddress=${user.emailAddress}
           |Name=${user.name}
           |SiteURL=${user.siteURL}
           |""".stripMargin

      render(user) == expected
    }
  }

  val andThen = {
    implicit val naming: ConfigKeyNaming[User] =
      ConfigKeyNaming.hyphenSeparated.andThen("user-" + _)

    forAll { user: User =>
      val expected =
        s"""user-email-address=${user.emailAddress}
           |user-name=${user.name}
           |user-site-url=${user.siteURL}
           |""".stripMargin

      render(user) == expected
    }
  }

}
