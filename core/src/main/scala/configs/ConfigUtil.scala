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

package configs

import com.typesafe.config.{ConfigUtil => TypesafeConfigUtil}
import scala.collection.convert.decorateAsJava._
import scala.collection.convert.decorateAsScala._

object ConfigUtil {

  def quoteString(s: String): String =
    TypesafeConfigUtil.quoteString(s)

  def joinPath(element: String, elements: String*): String =
    TypesafeConfigUtil.joinPath(element +: elements: _*)

  def joinPath(elements: Seq[String]): String =
    TypesafeConfigUtil.joinPath(elements.asJava)

  def splitPath(path: String): List[String] =
    TypesafeConfigUtil.splitPath(path).asScala.toList

}
