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

package configs.syntax

import configs.{Config, ConfigKeyValue, ConfigList, ConfigObject, ConfigValue, FromString, ToConfig}
import scala.collection.breakOut

object create {

  def configValue[A](a: A)(implicit A: ToConfig[A]): ConfigValue =
    A.toValue(a)

  def configList[A](as: A*)(implicit A: ToConfig[A]): ConfigList =
    ConfigList.from(as.map(A.toValue))

  def configObject(kvs: ConfigKeyValue*): ConfigObject =
    ConfigObject.from(kvs.map(_.tuple)(breakOut))

  def config(kvs: ConfigKeyValue*): Config =
    configObject(kvs: _*).toConfig

  implicit def tupleToConfigKeyValue[A, B](kv: (A, B))(implicit A: FromString[A], B: ToConfig[B]): ConfigKeyValue =
    ConfigKeyValue(A.show(kv._1), B.toValue(kv._2))

}
