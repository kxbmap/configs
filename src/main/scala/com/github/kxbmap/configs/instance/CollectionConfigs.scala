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

trait CollectionConfigs {

  private type CBF[To[_], Elem] = CanBuildFrom[Nothing, Elem, To[Elem]]


  implicit def collectionConfigs[C[_], T: Configs](implicit cbf: CBF[C, T]): Configs[C[T]] = (c, p) =>
    c.getList(p).map(Configs[T].extract).to[C]


  implicit def configCollectionConfigs[C[_]](implicit cbf: CBF[C, Config]): Configs[C[Config]] =
    _.getConfigList(_).map(c => c: Config)(collection.breakOut)


  implicit def intCollectionConfigs[C[_]](implicit cbf: CBF[C, Int]): Configs[C[Int]] =
    _.getIntList(_).map(_.toInt)(collection.breakOut)


  implicit def longCollectionConfigs[C[_]](implicit cbf: CBF[C, Long]): Configs[C[Long]] =
    _.getLongList(_).map(_.toLong)(collection.breakOut)


  implicit def doubleCollectionConfigs[C[_]](implicit cbf: CBF[C, Double]): Configs[C[Double]] =
    _.getDoubleList(_).map(_.toDouble)(collection.breakOut)


  implicit def booleanCollectionConfigs[C[_]](implicit cbf: CBF[C, Boolean]): Configs[C[Boolean]] =
    _.getBooleanList(_).map(_.booleanValue())(collection.breakOut)


  implicit def stringCollectionConfigs[C[_]](implicit cbf: CBF[C, String]): Configs[C[String]] =
    _.getStringList(_).to[C]


  implicit def javaTimeDurationCollectionConfigs[C[_]](implicit cbf: CBF[C, jt.Duration]): Configs[C[jt.Duration]] =
    _.getDurationList(_).to[C]


  implicit def configMemorySizeCollectionConfigs[C[_]](implicit cbf: CBF[C, ConfigMemorySize]): Configs[C[ConfigMemorySize]] =
    _.getMemorySizeList(_).to[C]

}
