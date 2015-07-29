/*
 * Copyright 2013-2015 Tsukasa Kitachi
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

package com.github.kxbmap.configs.instance

import com.github.kxbmap.configs.{CValue, ConfigProp, Configs}
import com.typesafe.config.ConfigException
import scala.collection.JavaConverters._
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Equal
import scalaz.std.anyVal._
import scalaz.std.string._

object EitherConfigsTest extends Scalaprops with ConfigProp {

  def checkEither[T: Configs : Gen : CValue : Equal] =
    Properties.list(
      check[Either[Throwable, T]].toProperties("either"),
      checkCollectionsOf[Either[Throwable, T]],
      checkMissing[Either[Throwable, T]](isMissing).toProperties("missing")
    )

  val eitherInt = checkEither[Int]

  val eitherFoo = checkEither[Foo]


  def isMissing[T](e: Either[Throwable, T]): Boolean = e.left.exists(_.isInstanceOf[ConfigException.Missing])

  implicit def eitherGen[T: Gen]: Gen[Either[Throwable, T]] =
    Gen.option[T].map(_.toRight(new RuntimeException("dummy")))

  implicit def eitherEqual[T: Equal]: Equal[Either[Throwable, T]] = (a1, a2) =>
    (a1, a2) match {
      case (Right(r1), Right(r2)) => Equal[T].equal(r1, r2)
      case (Left(_), Left(_))     => true
      case _                      => false
    }

  implicit def eitherCValue[T: CValue]: CValue[Either[Throwable, T]] =
    _.right.toOption.map(CValue[T].toConfigValue).orNull


  case class Foo(a: String, b: Int)

  object Foo {

    implicit val fooConfigs: Configs[Foo] = Configs.onPath(c => Foo(c.getString("a"), c.getInt("b")))

    implicit val fooGen: Gen[Foo] = for {
      a <- Gen[String]
      b <- Gen[Int]
    } yield Foo(a, b)

    implicit val fooCValue: CValue[Foo] = f => Map[String, Any]("a" -> f.a, "b" -> f.b).asJava
  }

}
