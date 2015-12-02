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

import com.typesafe.config.ConfigList
import configs.util._
import scala.collection.JavaConversions._
import scala.collection.generic.CanBuildFrom
import scalaprops.Property.forAll
import scalaprops.{Properties, Scalaprops}
import scalaz.std.string._

object MapFTest extends Scalaprops {

  implicit def ints[F[_]](implicit cbf: CanBuildFrom[Nothing, Int, F[Int]]): Configs[F[Int]] =
    Configs.from(_.getIntList(_).map(_.intValue())(collection.breakOut))

  case class Wrap(n: Int)

  def wrapInt[F[_]](implicit mapF: Configs.MapF[F, Int, Wrap]): Configs[F[Wrap]] =
    mapF(Wrap)

  val mapF = {
    val list = forAll { (cl: ConfigList :@ Int) =>
      wrapInt[List].extract(cl).exists(_ == cl.map(_.unwrapped().asInstanceOf[Int] |> Wrap).toList)
    }
    val set = forAll { (cl: ConfigList :@ Int) =>
      wrapInt[Set].extract(cl).exists(_ == cl.map(_.unwrapped().asInstanceOf[Int] |> Wrap).toSet)
    }
    Properties.list(
      list.toProperties("List"),
      set.toProperties("Set")
    )
  }

}
