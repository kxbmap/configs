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

package configs.instance

import com.typesafe.config.ConfigFactory
import configs.Configs
import configs.util._
import java.{util => ju}
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Equal
import scalaz.std.anyVal.{doubleInstance => _, _}
import scalaz.std.option._
import scalaz.std.string._

object OptionConfigsTest extends Scalaprops {

  val option = check[Option[Int]]

  val missing = forAll { p: String =>
    val config = ConfigFactory.empty()
    Configs[Option[Int]].get(config, q(p)).exists(_.isEmpty)
  }

  val nestedOption = {
    val OO = Configs[Option[Option[Int]]]
    val p1 = forAll { p: String =>
      val config = ConfigFactory.empty()
      OO.get(config, q(p)).exists(_.isEmpty)
    }
    val p2 = forAll { p: String =>
      val config = ConfigFactory.parseString(s"${q(p)} = null")
      OO.get(config, q(p)).exists(_.contains(None))
    }
    Properties.list(
      p1.toProperties("missing"),
      p2.toProperties("null")
    )
  }

  val optional = {
    def asJava[A] = (_: Option[A]).fold(ju.Optional.empty[A])(ju.Optional.of)
    def asScala[A] = (o: ju.Optional[A]) => if (o.isPresent) Some(o.get) else None
    implicit def gen[A: Gen]: Gen[ju.Optional[A]] =
      Gen[Option[A]].map(asJava)
    implicit def equal[A: Equal]: Equal[ju.Optional[A]] =
      Equal[Option[A]].contramap(asScala)
    implicit def tcv[A: ToConfigValue]: ToConfigValue[ju.Optional[A]] =
      ToConfigValue[Option[A]].contramap(asScala)
    check[ju.Optional[Int]]
  }

  val optionalInt = {
    val asJava = (_: Option[Int]).fold(ju.OptionalInt.empty)(ju.OptionalInt.of)
    val asScala = (o: ju.OptionalInt) => if (o.isPresent) Some(o.getAsInt) else None
    implicit val gen: Gen[ju.OptionalInt] =
      Gen[Option[Int]].map(asJava)
    implicit val equal: Equal[ju.OptionalInt] =
      Equal[Option[Int]].contramap(asScala)
    implicit val tcv: ToConfigValue[ju.OptionalInt] =
      ToConfigValue[Option[Int]].contramap(asScala)
    check[ju.OptionalInt]
  }

  val optionalLong = {
    val asJava = (_: Option[Long]).fold(ju.OptionalLong.empty)(ju.OptionalLong.of)
    val asScala = (o: ju.OptionalLong) => if (o.isPresent) Some(o.getAsLong) else None
    implicit val gen: Gen[ju.OptionalLong] =
      Gen[Option[Long]].map(asJava)
    implicit val equal: Equal[ju.OptionalLong] =
      Equal[Option[Long]].contramap(asScala)
    implicit val tcv: ToConfigValue[ju.OptionalLong] =
      ToConfigValue[Option[Long]].contramap(asScala)
    check[ju.OptionalLong]
  }

  val optionalDouble = {
    val asJava = (_: Option[Double]).fold(ju.OptionalDouble.empty)(ju.OptionalDouble.of)
    val asScala = (o: ju.OptionalDouble) => if (o.isPresent) Some(o.getAsDouble) else None
    implicit val gen: Gen[ju.OptionalDouble] =
      Gen[Option[Double]].map(asJava)
    implicit val doubleEqual: Equal[Double] =
      (a, b) => a.isNaN && b.isNaN || a == b
    implicit val equal: Equal[ju.OptionalDouble] =
      Equal[Option[Double]].contramap(asScala)
    implicit val tcv: ToConfigValue[ju.OptionalDouble] =
      ToConfigValue[Option[Double]].contramap(asScala)
    check[ju.OptionalDouble]
  }

}
