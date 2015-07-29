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

import com.typesafe.config.{ConfigFactory, ConfigMemorySize, ConfigUtil}
import java.{lang => jl, time => jt, util => ju}
import scala.collection.JavaConverters._
import scala.reflect.{ClassTag, classTag}
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties}
import scalaz.Equal
import scalaz.std.list._
import scalaz.std.stream._
import scalaz.std.string._
import scalaz.std.vector._

trait ConfigProp {

  def check[T: Configs : Gen : CValue : Equal] = forAll { value: T =>
    val p = "dummy-path"
    val c = CValue[T].toConfigValue(value).atPath(p)
    Equal[T].equal(Configs[T].get(c, p), value)
  }

  def checkCollectionsOf[T: Configs : Gen : Equal : CValue : ClassTag] = {
    val props = Seq(
      "list" -> check[List[T]],
      "vector" -> check[Vector[T]],
      "stream" -> check[Stream[T]],
      "array" -> check[Array[T]]
    ).map {
      case (id, p) => (id, p.mapSize(_ / 3 + 1))
    }
    Properties.properties("collection of " + classTag[T].runtimeClass.getSimpleName)(props: _*)
  }

  def checkMissing[T: Configs](f: T => Boolean) = forAll { path: String =>
    val c = ConfigFactory.empty()
    val p = ConfigUtil.quoteString(path)
    f(Configs[T].get(c, p))
  }


  implicit def generalEqual[T]: Equal[T] =
    Equal.equalA[T]

  implicit def arrayEqual[T: Equal]: Equal[Array[T]] =
    Equal.equalBy(_.toList)


  implicit def javaListGen[T: Gen]: Gen[ju.List[T]] =
    Gen.list[T].map(_.asJava)


  implicit lazy val stringGen: Gen[String] =
    Gen.asciiString

  implicit lazy val doubleGen: Gen[Double] =
    Gen.genFiniteDouble


  implicit lazy val javaDoubleGen: Gen[jl.Double] =
    doubleGen.map(Double.box)


  implicit lazy val javaDurationGen: Gen[jt.Duration] =
    Gen.chooseLong(0, Long.MaxValue).map(jt.Duration.ofNanos)

  implicit lazy val javaDurationCValue: CValue[jt.Duration] =
    d => s"${d.toNanos}ns"


  implicit lazy val configMemorySizeGen: Gen[ConfigMemorySize] =
    Gen.chooseLong(0, Long.MaxValue).map(ConfigMemorySize.ofBytes)

}
