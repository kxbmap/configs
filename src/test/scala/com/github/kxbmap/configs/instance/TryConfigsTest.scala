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
import scala.util.{Failure, Success, Try}
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.Equal
import scalaz.std.anyVal._
import scalaz.std.string._

object TryConfigsTest extends Scalaprops with ConfigProp {

  def checkTry[T: Configs : Gen : CValue : Equal] =
    Properties.list(
      check[Try[T]].toProperties("try"),
      checkCollectionsOf[Try[T]],
      checkMissing[Try[T]](isMissing).toProperties("missing")
    )

  val tryInt = checkTry[Int]

  val tryFoo = checkTry[Foo]


  def isMissing[T](e: Try[T]): Boolean = e match {
    case Failure(_: ConfigException.Missing) => true
    case _                                   => false
  }

  implicit def tryGen[T: Gen]: Gen[Try[T]] =
    Gen.option[T].map {
      case Some(v) => Success(v)
      case None    => Failure(new RuntimeException("dummy"))
    }

  implicit def tryEqual[T: Equal]: Equal[Try[T]] = (a1, a2) =>
    (a1, a2) match {
      case (Success(r1), Success(r2)) => Equal[T].equal(r1, r2)
      case (Failure(_), Failure(_))   => true
      case _                          => false
    }

  implicit def tryCValue[T: CValue]: CValue[Try[T]] =
    _.map(CValue[T].toConfigValue).getOrElse(null)


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
