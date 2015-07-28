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
import java.{lang => jl, time => jt, util => ju}

trait BasicTypeConfigs {

  implicit lazy val configConfigs: Configs[Config] = _.getConfig(_)

  implicit lazy val configListConfigs: Configs[ju.List[Config]] = _.getConfigList(_).asInstanceOf[ju.List[Config]]


  implicit lazy val intConfigs: Configs[Int] = _.getInt(_)

  implicit lazy val integerListConfigs: Configs[ju.List[jl.Integer]] = _.getIntList(_)


  implicit lazy val longConfigs: Configs[Long] = _.getLong(_)

  implicit lazy val longListsConfigs: Configs[ju.List[jl.Long]] = _.getLongList(_)


  implicit lazy val doubleConfigs: Configs[Double] = _.getDouble(_)

  implicit lazy val doubleListConfigs: Configs[ju.List[jl.Double]] = _.getDoubleList(_)


  implicit lazy val booleanConfigs: Configs[Boolean] = _.getBoolean(_)

  implicit lazy val booleanListConfigs: Configs[ju.List[jl.Boolean]] = _.getBooleanList(_)


  implicit lazy val stringConfigs: Configs[String] = _.getString(_)

  implicit lazy val stringListConfigs: Configs[ju.List[String]] = _.getStringList(_)


  implicit lazy val javaTimeDurationConfigs: Configs[jt.Duration] = _.getDuration(_)

  implicit lazy val javaTimeDurationListConfigs: Configs[ju.List[jt.Duration]] = _.getDurationList(_)


  implicit lazy val configMemorySizeConfigs: Configs[ConfigMemorySize] = _.getMemorySize(_)

  implicit lazy val configMemorySizeListConfigs: Configs[ju.List[ConfigMemorySize]] = _.getMemorySizeList(_)

}
