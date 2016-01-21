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
import scalaz.std.vector._
import scalaz.{Applicative, Equal, MonadError, NonEmptyList}

object AttemptTest extends Scalaprops with AttemptImplicits {

  val monadErrorLaw = {
    implicit val configErrorsGen: Gen[Vector[ConfigError]] =
      Gen[NonEmptyList[ConfigError]].map(x => x.list.toVector).mapSize(_ / 3)

    implicit val instance: MonadError[Attempt, Vector[ConfigError]] =
      new MonadError[Attempt, Vector[ConfigError]] {
        def point[A](a: => A): Attempt[A] =
          Attempt.successful(a)

        def bind[A, B](fa: Attempt[A])(f: A => Attempt[B]): Attempt[B] =
          fa.flatMap(f)

        def raiseError[A](e: Vector[ConfigError]): Attempt[A] =
          Attempt.Failure(e)

        def handleError[A](fa: Attempt[A])(f: Vector[ConfigError] => Attempt[A]): Attempt[A] =
          fa.handleWith { case e => f(e) }
      }

    scalazlaws.monadError.all[Attempt, Vector[ConfigError]]
  }

  val applicativeLaw = {
    implicit val instance: Applicative[Attempt] =
      new Applicative[Attempt] {
        def point[A](a: => A): Attempt[A] =
          Attempt.successful(a)

        def ap[A, B](fa: => Attempt[A])(f: => Attempt[A => B]): Attempt[B] =
          fa.ap(f)
      }

    scalazlaws.applicative.all[Attempt]
  }

  val toFromEither =
    forAll { a: Attempt[Int] =>
      Attempt.fromEither(a.toEither) == a
    }

}

trait AttemptImplicits extends ConfigErrorImplicits {

  implicit def attemptGen[A: Gen]: Gen[Attempt[A]] =
    Gen.oneOf(
      Gen[A].map(Attempt.successful),
      Gen[ConfigError].map(Attempt.failure(_))
    )

  implicit def attemptEqual[A: Equal]: Equal[Attempt[A]] =
    Equal.equal((r1, r2) =>
      (r1, r2) match {
        case (Attempt.Success(a1), Attempt.Success(a2)) => Equal[A].equal(a1, a2)
        case (Attempt.Failure(e1), Attempt.Failure(e2)) => Equal[Vector[ConfigError]].equal(e1, e2)
        case _                                          => false
      })

}
