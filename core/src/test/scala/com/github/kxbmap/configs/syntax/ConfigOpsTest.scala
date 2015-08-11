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

package com.github.kxbmap.configs.syntax

import com.github.kxbmap.configs.ConfigProp
import com.github.kxbmap.configs.testkit._
import com.typesafe.config.ConfigValueFactory
import scala.collection.JavaConverters._
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object ConfigOpsTest extends Scalaprops with ConfigProp {

  val extract = forAll { m: Map[String, java.lang.Integer] =>
    val config = ConfigValueFactory.fromMap(m.asJava).toConfig
    config.extract[Map[String, java.lang.Integer]] == m
  }

  val get = forAll { n: Int =>
    val p = "path"
    val config = n.cv.atKey(p)
    config.get[Int](p) == n
  }

  val getOpt = forAll { n: Option[Int] =>
    val p = "path"
    val config = n.cv.atKey(p)
    config.getOpt[Int](p) == n
  }

  val getOrElse = forAll { (n: Option[Int], m: Int) =>
    val p = "path"
    val config = n.cv.atKey(p)
    config.getOrElse[Int](p, m) == n.getOrElse(m)
  }

}
