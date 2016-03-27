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

import com.typesafe.config.{Config, ConfigList, ConfigObject, ConfigValue, ConfigValueFactory}
import configs.{ConfigKeyValue, FromString, ToConfig}
import scala.collection.convert.decorateAsJava._

object create {

  def configValue[A](a: A)(implicit A: ToConfig[A]): ConfigValue =
    A.toValue(a)

  def configList[A](as: A*)(implicit A: ToConfig[A]): ConfigList =
    ConfigValueFactory.fromIterable(as.map(A.toValue).asJava)

  def configObject(kvs: ConfigKeyValue*): ConfigObject =
    ConfigValueFactory.fromMap(kvs.map(_.tuple).toMap.asJava)

  def config(kvs: ConfigKeyValue*): Config =
    configObject(kvs: _*).toConfig

  implicit def tupleToConfigKeyValue[A, B](kv: (A, B))(implicit A: FromString[A], B: ToConfig[B]): ConfigKeyValue =
    ConfigKeyValue(A.show(kv._1), B.toValue(kv._2))

}
