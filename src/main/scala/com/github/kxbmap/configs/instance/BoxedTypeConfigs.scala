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
import java.{lang => jl}

trait BoxedTypeConfigs {

  implicit lazy val javaIntegerConfigs: Configs[jl.Integer] = (c, p) => Int.box(c.getInt(p))

  implicit lazy val javaLongConfigs: Configs[jl.Long] = (c, p) => Long.box(c.getLong(p))

  implicit lazy val javaDoubleConfigs: Configs[jl.Double] = (c, p) => Double.box(c.getDouble(p))

  implicit lazy val javaBooleanConfigs: Configs[jl.Boolean] = (c, p) => Boolean.box(c.getBoolean(p))

}
