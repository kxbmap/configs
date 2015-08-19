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

package com.github.kxbmap.configs

import scala.collection.JavaConversions._
import scala.collection.generic.CanBuildFrom
import scalaprops.Property.forAllG
import scalaprops.{Properties, Scalaprops}
import scalaz.std.string._

object MapFTest extends Scalaprops {

  implicit def ints[F[_]](implicit cbf: CanBuildFrom[Nothing, Int, F[Int]]): Configs[F[Int]] =
    _.getIntList(_).map(_.intValue())(collection.breakOut)

  def bigInts[F[_]](implicit mapF: Configs.MapF[F, Int, BigInt]): Configs[F[BigInt]] =
    mapF(BigInt(_))

  val mapF = {
    val g = testkit.genConfigList(testkit.genConfigValue[Int])
    val list = forAllG(g) { cl =>
      bigInts[List].extract(cl) == cl.map(_.unwrapped().asInstanceOf[Int] |> BigInt.apply).toList
    }
    val set = forAllG(g) { cl =>
      bigInts[Set].extract(cl) == cl.map(_.unwrapped().asInstanceOf[Int] |> BigInt.apply).toSet
    }
    Properties.list(
      list.toProperties("List"),
      set.toProperties("Set")
    )
  }

}
