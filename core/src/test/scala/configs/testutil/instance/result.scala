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

import configs.testutil.instance.error._
import configs.{ConfigError, ConfigWriter, Result}
import scalaprops.Gen
import scalaz.Equal

object result {

  implicit def resultGen[A: Gen]: Gen[Result[A]] =
    Gen.oneOf(
      Gen[A].map(Result.successful),
      Gen[ConfigError].map(Result.failure)
    )

  implicit def resultEqual[A: Equal]: Equal[Result[A]] =
    Equal.equal((r1, r2) =>
      (r1, r2) match {
        case (Result.Success(a1), Result.Success(a2)) => Equal[A].equal(a1, a2)
        case (Result.Failure(e1), Result.Failure(e2)) => Equal[ConfigError].equal(e1, e2)
        case _ => false
      })


  object success {

    implicit def successResultGen[A: Gen]: Gen[Result[A]] =
      Gen[A].map(Result.successful)

    implicit def successResultEqual[A: Equal]: Equal[Result[A]] =
      Equal.equalBy(_.value)

    implicit def successResultConfigWriter[A: ConfigWriter]: ConfigWriter[Result[A]] =
      ConfigWriter.by(_.value)

  }

}
