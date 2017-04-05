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

package configs.syntax

import com.typesafe.config.ConfigFactory
import configs.testutil.instance.config._
import configs.testutil.instance.string._
import configs.{Config, ConfigObject, ConfigReader, ConfigValue, ConfigWriter, Result}
import scala.collection.JavaConverters._
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}
import scalaz.Monoid
import scalaz.syntax.equal._

object RichConfigTest extends Scalaprops {

  val extract = forAll { m: Map[String, Int] =>
    ConfigObject.fromMap(m).map(_.toConfig).flatMap(_.extract[Map[String, Int]]).exists(_ == m)
  }

  val get = forAll { n: Int =>
    val config = ConfigFactory.parseString(s"path = $n")
    config.get[Int]("path") == Result.successful(n)
  }

  val getOrElse = {
    implicit val unused: ConfigReader[Option[Int]] = ConfigReader.successful(Some(42))

    val p1 = Properties.single("get", forAll { (n: Int, d: Int) =>
      val config = ConfigWriter[Int].write(n).atPath("path")
      config.getOrElse("path", d) == Result.successful(n)
    })
    val p2 = Properties.single("null", forAll { d: Int =>
      val config = ConfigValue.Null.atPath("path")
      config.getOrElse("path", d) == Result.successful(d)
    })
    val p3 = Properties.single("missing", forAll { d: Int =>
      Config.empty.getOrElse("path", d) == Result.successful(d)
    })
    Properties.list(p1, p2, p3)
  }

  val ++ = forAll { (c1: Config, c2: Config) =>
    val result = c1 ++ c2
    c2.entrySet().asScala.forall { e =>
      result.getValue(e.getKey) === e.getValue
    }
  }

  implicit lazy val configMonoid: Monoid[Config] =
    Monoid.instance(_ ++ _, Config.empty)

  val `++/empty monoid` = scalaprops.scalazlaws.monoid.all[Config]

}
