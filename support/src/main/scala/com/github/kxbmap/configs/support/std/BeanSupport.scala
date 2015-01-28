/*
 * Copyright 2015 Philip L. McMahon
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
package support.std

import scala.collection.JavaConversions._


trait BeanSupport {

  object Beans {

    def apply[T](f: => T): Configs[T] = Configs.configs {
      _.entrySet().foldLeft(f) { (o, e) =>
        val m = s"set${e.getKey.capitalize}"
        val v = e.getValue.unwrapped()
        o.getClass.getMethod(m, v.getClass).invoke(o, v)
        o
      }
    }

    def apply[T: Manifest]: Configs[T] = Beans {
      implicitly[Manifest[T]].runtimeClass.asInstanceOf[Class[T]].
        getConstructor().newInstance()
    }

  }

}
