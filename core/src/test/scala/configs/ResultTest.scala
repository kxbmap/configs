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

import configs.testutil.instance.anyVal._
import configs.testutil.instance.result._
import configs.testutil.instance.string._
import scalaprops.Property._
import scalaprops.{Properties, Scalaprops, scalazlaws}
import scalaz.std.list._
import scalaz.{Applicative, MonadError, Traverse}

object ResultTest extends Scalaprops {

//  val monadErrorLaw = {
//    import ResultInstance.monadError
//    scalazlaws.monadError.all[Result, ConfigError]
//  }

  val applicativeLaw = {
    import ResultInstance.applicative
    scalazlaws.applicative.all[Result]
  }

  val traverse = {
    import ResultInstance.applicative
    Properties.list(
      Properties.single("list", forAll { (xs: List[Int], f: Int => Result[Long]) =>
        Result.traverse(xs)(f) == Traverse[List].traverse(xs)(f)
      })
    )
  }

  val sequence = {
    import ResultInstance.applicative
    Properties.list(
      Properties.single("list", forAll { xs: List[Result[Int]] =>
        Result.sequence(xs) == Traverse[List].sequence(xs)
      })
    )
  }

  val toFromEither =
    forAll { a: Result[Int] =>
      Result.fromEither(a.toEither) == a
    }

}

object ResultInstance {

  implicit val monadError: MonadError[Result, ConfigError] =
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

  implicit val applicative: Applicative[Result] =
    new Applicative[Result] {
      def point[A](a: => A): Result[A] =
        Result.successful(a)

      def ap[A, B](fa: => Result[A])(f: => Result[A => B]): Result[B] =
        fa.ap(f)
    }

}
