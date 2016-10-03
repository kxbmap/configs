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

import configs.{ConfigList, ConfigObject, ConfigValue, ConfigWriter, StringConverter}
import scala.util.DynamicVariable

object construct {

  private[this] val entries = new DynamicVariable[Map[String, ConfigValue]](null)

  private def build(body: => Unit, f: Map[String, ConfigValue] => ConfigObject): ConfigObject =
    entries.withValue(Map.empty) {
      body
      f(entries.value)
    }

  def %(body: => Unit): ConfigObject =
    build(body, ConfigObject.from)

  def %#(comments: String*)(body: => Unit): ConfigObject =
    build(body, ConfigObject.from(_).withComments(comments))


  def \[A: ConfigWriter](elements: A*): ConfigList =
    ConfigList.from(elements.map(_.toConfigValue))

  def \#[A: ConfigWriter](comments: String*)(elements: A*): ConfigList =
    \(elements: _*).withComments(comments)


  implicit class ConstructSyntax[A](private val a: A) extends AnyVal {

    def :=[B](b: B)(implicit A: StringConverter[A], B: ConfigWriter[B]): Unit = {
      entries.value match {
        case null => throw new IllegalStateException("outside of % block")
        case kvs => entries.value = B.append(kvs, A.to(a), b)
      }
    }

    def <#(comments: String*)(implicit A: ConfigWriter[A]): ConfigValue =
      A.write(a).withComments(comments)

  }

}
