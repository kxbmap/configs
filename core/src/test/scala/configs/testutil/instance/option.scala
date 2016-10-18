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

import configs.testutil.instance.anyVal._
import java.{util => ju}
import scalaprops.Gen
import scalaz.{Equal, std}

object option {

  implicit def optionEqual[A: Equal]: Equal[Option[A]] =
    std.option.optionEqual[A]

  implicit def javaOptionalGen[A: Gen]: Gen[ju.Optional[A]] =
    Gen[Option[A]].map(_.fold(ju.Optional.empty[A])(ju.Optional.of))

  implicit def javaOptionalEqual[A: Equal]: Equal[ju.Optional[A]] =
    Equal[Option[A]].contramap(o => if (o.isPresent) Some(o.get) else None)

  implicit lazy val javaOptionalIntGen: Gen[ju.OptionalInt] =
    Gen[Option[Int]].map(_.fold(ju.OptionalInt.empty)(ju.OptionalInt.of))

  implicit lazy val javaOptionalIntEqual: Equal[ju.OptionalInt] =
    Equal[Option[Int]].contramap(o => if (o.isPresent) Some(o.getAsInt) else None)

  implicit lazy val javaOptionalLongGen: Gen[ju.OptionalLong] =
    Gen[Option[Long]].map(_.fold(ju.OptionalLong.empty)(ju.OptionalLong.of))

  implicit lazy val javaOptionalLongEqual: Equal[ju.OptionalLong] =
    Equal[Option[Long]].contramap(o => if (o.isPresent) Some(o.getAsLong) else None)

  implicit lazy val javaOptionalDoubleGen: Gen[ju.OptionalDouble] =
    Gen[Option[Double]].map(_.fold(ju.OptionalDouble.empty)(ju.OptionalDouble.of))

  implicit lazy val javaOptionalDoubleEqual: Equal[ju.OptionalDouble] =
    Equal[Option[Double]].contramap(o => if (o.isPresent) Some(o.getAsDouble) else None)

}
