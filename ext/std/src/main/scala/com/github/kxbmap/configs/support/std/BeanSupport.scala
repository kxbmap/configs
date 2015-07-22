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

import com.typesafe.config.ConfigException
import scala.collection.convert.WrapAsScala._


trait BeanSupport {

  object Beans {

    def apply[T](f: => T): Configs[T] = _.entrySet().foldLeft(f) { (o, e) =>
      val k = e.getKey
      val v = e.getValue
      val n = s"set${k.capitalize}"
      val u = v.unwrapped()
      // Assume only one matching setter
      o.getClass.getMethods.find { m =>
        m.getName == n && m.getParameterTypes.length == 1
      } match {
        case Some(m) =>
          try
            m.invoke(o, u)
          catch {
            case e: IllegalArgumentException =>
              throw new ConfigException.WrongType(v.origin(),
                s"Bean ${o.getClass.getName} property '$k' cannot be assigned type ${u.getClass.getName}");
          }
        case None    =>
          throw new ConfigException.BadPath(v.origin(),
            s"Bean ${o.getClass.getName} does not have property '$k'")
      }
      o
    }

    def apply[T: Manifest]: Configs[T] = Beans {
      implicitly[Manifest[T]].runtimeClass.asInstanceOf[Class[T]].
        getConstructor().newInstance()
    }

  }

}
