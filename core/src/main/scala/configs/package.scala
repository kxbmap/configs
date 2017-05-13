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

package object configs {

  type Config = com.typesafe.config.Config

  type ConfigValue = com.typesafe.config.ConfigValue

  type ConfigList = com.typesafe.config.ConfigList

  type ConfigObject = com.typesafe.config.ConfigObject

  type ConfigMemorySize = com.typesafe.config.ConfigMemorySize

  type ConfigOrigin = com.typesafe.config.ConfigOrigin

  type ConfigParseOptions = com.typesafe.config.ConfigParseOptions

  type ConfigResolveOptions = com.typesafe.config.ConfigResolveOptions

  type ConfigRenderOptions = com.typesafe.config.ConfigRenderOptions


  @deprecated("use ConfigReader instead", "0.5.0")
  type Configs[A] = ConfigReader[A]

  @deprecated("use ConfigReader instead", "0.5.0")
  val Configs = ConfigReader

  @deprecated("use StringConverter instead", "0.5.0")
  type FromString[A] = StringConverter[A]

  @deprecated("use StringConverter instead", "0.5.0")
  val FromString = StringConverter

}
