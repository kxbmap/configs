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
import com.typesafe.config.ConfigException
import scala.reflect.{ClassTag, classTag}

trait JavaEnumConfigs {

  implicit def javaEnumConfigs[A <: java.lang.Enum[A] : ClassTag]: Configs[A] = {
    val arr = classTag[A].runtimeClass.getEnumConstants.asInstanceOf[Array[A]]
    (c, p) => {
      val v = c.getString(p)
      arr.find(_.name() == v).getOrElse {
        throw new ConfigException.BadValue(c.origin(), p, s"$v must be one of ${arr.mkString(", ")}")
      }
    }
  }

}
