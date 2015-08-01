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

package com.github.kxbmap.configs.util

import com.typesafe.config.ConfigUtil
import java.{util => ju}
import scala.collection.JavaConverters._

trait ConfigString[A] {

  def configString(a: A): String

  def contramap[B](f: B => A): ConfigString[B] = b => configString(f(b))
}

object ConfigString {

  def apply[A](implicit A: ConfigString[A]): ConfigString[A] = A


  private val q = ConfigUtil.quoteString _

  implicit val stringConfigString: ConfigString[String] = q(_)

  implicit def traversableConfigString[F[_], A: ConfigString](implicit ev: F[A] <:< Traversable[A]): ConfigString[F[A]] =
    _.map(_.configString).mkString("[", ",", "]")

  implicit def mapConfigString[A: ConfigString]: ConfigString[Map[String, A]] =
    _.map(t => s"${q(t._1)} = ${t._2.configString}").mkString("{", ",", "}")

  implicit def javaListConfigString[A: ConfigString]: ConfigString[ju.List[A]] =
    _.asScala.configString

}
