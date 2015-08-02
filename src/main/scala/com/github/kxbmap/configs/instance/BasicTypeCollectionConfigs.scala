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

  implicit def configCollectionConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Config, F[Config]]): Configs[F[Config]] =
    _.getConfigList(_).map(c => c: Config)(collection.breakOut)


  implicit def intCollectionConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Int, F[Int]]): Configs[F[Int]] =
    _.getIntList(_).map(_.toInt)(collection.breakOut)


  implicit def longCollectionConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Long, F[Long]]): Configs[F[Long]] =
    _.getLongList(_).map(_.toLong)(collection.breakOut)


  implicit def doubleCollectionConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Double, F[Double]]): Configs[F[Double]] =
    _.getDoubleList(_).map(_.toDouble)(collection.breakOut)


  implicit def booleanCollectionConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Boolean, F[Boolean]]): Configs[F[Boolean]] =
    _.getBooleanList(_).map(_.booleanValue())(collection.breakOut)


  implicit def stringCollectionConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, String, F[String]]): Configs[F[String]] =
    _.getStringList(_).to[F]


  implicit def javaDurationCollectionConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, jt.Duration, F[jt.Duration]]): Configs[F[jt.Duration]] =
    _.getDurationList(_).to[F]


  implicit def configMemorySizeCollectionConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, ConfigMemorySize, F[ConfigMemorySize]]): Configs[F[ConfigMemorySize]] =
    _.getMemorySizeList(_).to[F]

}
