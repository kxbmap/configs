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
import scala.collection.JavaConverters._
import scalaprops.{Gen, Scalaprops}
import scalaz.Equal


object MapConfigsTest extends Scalaprops with ConfigProp {

  def checkMap[A: Configs : Gen : CValue : Equal] =
    check[Map[String, A]]("string key").product(check[Map[Symbol, A]]("symbol key"))

  val map = checkMap[java.time.Duration]


  implicit lazy val nonEmptyStringGen: Gen[String] =
    Gen.parameterised { (size, r) =>
      for {
        n <- Gen.chooseR(1, size, r)
        c <- Gen.sequenceNArray(n, Gen.asciiChar)
      } yield String.valueOf(c)
    }

  implicit def stringMapCValue[T: CValue]: CValue[Map[String, T]] =
    _.map(t => t._1 -> CValue[T].toConfigValue(t._2)).asJava


  implicit lazy val nonEmptySymbolGen = nonEmptyStringGen.map(Symbol.apply)

  implicit def symbolMapGen[T: Gen]: Gen[Map[Symbol, T]] =
    Gen.mapGen[String, T].map(_.map(t => Symbol(t._1) -> t._2))

  implicit def symbolMapCValue[T: CValue]: CValue[Map[Symbol, T]] =
    _.map(t => t._1.name -> CValue[T].toConfigValue(t._2)).asJava

}
