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

import scala.jdk.CollectionConverters._
import scalaprops.Property.forAll
import scalaprops.ScalapropsScalaz._
import scalaprops.{Gen, Scalaprops}
import scalaz.syntax.apply._
import collection.JavaConverters._

object ConfigKeyNamingTest extends Scalaprops {

  case class User(name: String, emailAddress: String, siteURL: String)

  implicit val userGen: Gen[User] = {
    val s = Gen.nonEmptyString(Gen.alphaChar)
    (s |@| s |@| s)(User.apply)
  }


  val identity = {
    implicit val naming: ConfigKeyNaming[User] = ConfigKeyNaming.identity

    forAll { user: User =>
      val result = ConfigWriter[User].write(user).asInstanceOf[ConfigObject]
      result.keySet().asScala == Set("emailAddress", "name", "siteURL")
    }
  }

  val hyphenSeparated = {
    implicit val naming: ConfigKeyNaming[User] = ConfigKeyNaming.hyphenSeparated

    forAll { user: User =>
      val result = ConfigWriter[User].write(user).asInstanceOf[ConfigObject]
      result.keySet().asScala == Set("email-address", "name", "site-url")
    }
  }

  val snakeCase = {
    implicit val naming: ConfigKeyNaming[User] = ConfigKeyNaming.snakeCase

    forAll { user: User =>
      val result = ConfigWriter[User].write(user).asInstanceOf[ConfigObject]
      result.keySet().asScala == Set("email_address", "name", "site_url")
    }
  }

  val lowerCamelCase = {
    implicit val naming: ConfigKeyNaming[User] = ConfigKeyNaming.lowerCamelCase

    forAll { user: User =>
      val result = ConfigWriter[User].write(user).asInstanceOf[ConfigObject]
      result.keySet().asScala == Set("emailAddress", "name", "siteURL")
    }
  }

  val upperCamelCase = {
    implicit val naming: ConfigKeyNaming[User] = ConfigKeyNaming.upperCamelCase

    forAll { user: User =>
      val result = ConfigWriter[User].write(user).asInstanceOf[ConfigObject]
      result.keySet().asScala == Set("EmailAddress", "Name", "SiteURL")
    }
  }

  val andThen = {
    implicit val naming: ConfigKeyNaming[User] =
      ConfigKeyNaming.hyphenSeparated.andThen(x => Seq("user-" + x))

    forAll { user: User =>
      val result = ConfigWriter[User].write(user).asInstanceOf[ConfigObject]
      result.keySet().asScala == Set("user-email-address", "user-name", "user-site-url")
    }
  }

}
