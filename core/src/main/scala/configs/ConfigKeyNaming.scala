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

import configs.ConfigUtil.splitWords
import java.util.Locale

sealed trait ConfigKeyNaming[A] {

  def apply(field: String): Seq[String]

  def applyFirst(field: String): String

  def andThen(f: String => Seq[String]): ConfigKeyNaming[A]

  def or(f: String => Seq[String]): ConfigKeyNaming[A]

  /**
   * Enable fail on parsing an object and there are superfluous keys in the config
   */
  def withFailOnSuperfluousKeys: ConfigKeyNaming[A]

  def failOnSuperfluousKeys: Boolean

}

object ConfigKeyNaming {

  def apply[A](f: String => String): ConfigKeyNaming[A] =
    apply(f.andThen(Seq(_)))

  def apply[A](f: String => Seq[String])(implicit dummy: DummyImplicit): ConfigKeyNaming[A] =
    new Impl(f, false).asInstanceOf[ConfigKeyNaming[A]]


  private final class Impl(f: String => Seq[String], val failOnSuperfluousKeys: Boolean) extends ConfigKeyNaming[Nothing] {
    self =>

    def apply(field: String): Seq[String] =
      f(field)

    def applyFirst(field: String): String =
      apply(field).head

    def andThen(f: String => Seq[String]): ConfigKeyNaming[Nothing] =
      new Impl(self.f.andThen(_.flatMap(f)), failOnSuperfluousKeys)

    def or(f: String => Seq[String]): ConfigKeyNaming[Nothing] =
      new Impl(field => (apply(field) ++ f(field)).distinct, failOnSuperfluousKeys)

    def withFailOnSuperfluousKeys: ConfigKeyNaming[Nothing] =
      new Impl(f, true)

  }


  implicit def defaultNaming[A]: ConfigKeyNaming[A] =
    hyphenSeparated[A]


  def identity[A]: ConfigKeyNaming[A] =
    _identity.asInstanceOf[ConfigKeyNaming[A]]

  private[this] val _identity: ConfigKeyNaming[Any] =
    ConfigKeyNaming((x: String) => x)


  def hyphenSeparated[A]: ConfigKeyNaming[A] =
    _hyphenSeparated.asInstanceOf[ConfigKeyNaming[A]]

  private[this] val _hyphenSeparated: ConfigKeyNaming[Any] =
    ConfigKeyNaming((x: String) => splitWords(x).mkString("-").toLowerCase(Locale.ROOT))


  def snakeCase[A]: ConfigKeyNaming[A] =
    _snakeCase.asInstanceOf[ConfigKeyNaming[A]]

  private[this] val _snakeCase: ConfigKeyNaming[Any] =
    ConfigKeyNaming((x: String) => splitWords(x).mkString("_").toLowerCase(Locale.ROOT))


  def lowerCamelCase[A]: ConfigKeyNaming[A] =
    _lowerCamelCase.asInstanceOf[ConfigKeyNaming[A]]

  private[this] val _lowerCamelCase: ConfigKeyNaming[Any] =
    ConfigKeyNaming { x: String =>
      splitWords(x) match {
        case Nil => ""
        case h :: t => (h.toLowerCase(Locale.ROOT) :: t.map(_.capitalize)).mkString
      }
    }


  def upperCamelCase[A]: ConfigKeyNaming[A] =
    _upperCamelCase.asInstanceOf[ConfigKeyNaming[A]]

  private[this] val _upperCamelCase: ConfigKeyNaming[Any] =
    ConfigKeyNaming((x: String) => splitWords(x).map(_.capitalize).mkString)

}
