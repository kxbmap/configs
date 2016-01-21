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

import scalaprops.Property._
import scalaprops.{Gen, Scalaprops, scalazlaws}
import scalaz.std.anyVal._
import scalaz.{Applicative, Equal, MonadError}

object ResultTest extends Scalaprops with ResultImplicits {

  val monadErrorLaw = {
    implicit val instance: MonadError[Result, ConfigError] =
      new MonadError[Result, ConfigError] {
        def point[A](a: => A): Result[A] =
          Result.successful(a)

        def bind[A, B](fa: Result[A])(f: A => Result[B]): Result[B] =
          fa.flatMap(f)

        def raiseError[A](e: ConfigError): Result[A] =
          Result.Failure(e)

        def handleError[A](fa: Result[A])(f: ConfigError => Result[A]): Result[A] =
          fa.handleWith { case e => f(e) }
      }

    scalazlaws.monadError.all[Result, ConfigError]
  }

  val applicativeLaw = {
    implicit val instance: Applicative[Result] =
      new Applicative[Result] {
        def point[A](a: => A): Result[A] =
          Result.successful(a)

        def ap[A, B](fa: => Result[A])(f: => Result[A => B]): Result[B] =
          fa.ap(f)
      }

    scalazlaws.applicative.all[Result]
  }

  val toFromEither =
    forAll { a: Result[Int] =>
      Result.fromEither(a.toEither) == a
    }

}

trait ResultImplicits extends ConfigErrorImplicits {

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
        case _                                        => false
      })

}
