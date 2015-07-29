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

import com.github.kxbmap.configs.Configs
import com.typesafe.config.{Config, ConfigMemorySize}
import java.{time => jt}
import scala.collection.JavaConversions._
import scala.collection.generic.CanBuildFrom

trait BasicTypeCollectionConfigs {

  implicit def configCollectionConfigs[C[_]](implicit cbf: CanBuildFrom[Nothing, Config, C[Config]]): Configs[C[Config]] =
    _.getConfigList(_).map(c => c: Config)(collection.breakOut)


  implicit def intCollectionConfigs[C[_]](implicit cbf: CanBuildFrom[Nothing, Int, C[Int]]): Configs[C[Int]] =
    _.getIntList(_).map(_.toInt)(collection.breakOut)


  implicit def longCollectionConfigs[C[_]](implicit cbf: CanBuildFrom[Nothing, Long, C[Long]]): Configs[C[Long]] =
    _.getLongList(_).map(_.toLong)(collection.breakOut)


  implicit def doubleCollectionConfigs[C[_]](implicit cbf: CanBuildFrom[Nothing, Double, C[Double]]): Configs[C[Double]] =
    _.getDoubleList(_).map(_.toDouble)(collection.breakOut)


  implicit def booleanCollectionConfigs[C[_]](implicit cbf: CanBuildFrom[Nothing, Boolean, C[Boolean]]): Configs[C[Boolean]] =
    _.getBooleanList(_).map(_.booleanValue())(collection.breakOut)


  implicit def stringCollectionConfigs[C[_]](implicit cbf: CanBuildFrom[Nothing, String, C[String]]): Configs[C[String]] =
    _.getStringList(_).to[C]


  implicit def javaDurationCollectionConfigs[C[_]](implicit cbf: CanBuildFrom[Nothing, jt.Duration, C[jt.Duration]]): Configs[C[jt.Duration]] =
    _.getDurationList(_).to[C]


  implicit def configMemorySizeCollectionConfigs[C[_]](implicit cbf: CanBuildFrom[Nothing, ConfigMemorySize, C[ConfigMemorySize]]): Configs[C[ConfigMemorySize]] =
    _.getMemorySizeList(_).to[C]

}
