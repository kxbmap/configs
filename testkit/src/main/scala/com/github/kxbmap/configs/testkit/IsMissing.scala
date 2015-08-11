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

package com.github.kxbmap.configs.testkit

import com.typesafe.config.ConfigException
import scala.util.{Failure, Try}
import scalaz.Need

trait IsMissing[A] {
  def check(a: Need[A]): Boolean
}

object IsMissing {

  def apply[A](implicit A: IsMissing[A]): IsMissing[A] = A


  implicit def defaultIsMissing[A]: IsMissing[A] =
    default.asInstanceOf[IsMissing[A]]

  private[this] final val default: IsMissing[Any] = a =>
    intercept0(a.value) {
      case _: ConfigException.Missing => true
    }


  implicit def optionIsMissing[A]: IsMissing[Option[A]] =
    option.asInstanceOf[IsMissing[Option[A]]]

  private[this] final val option: IsMissing[Option[Any]] =
    _.value.isEmpty


  implicit def eitherIsMissing[A]: IsMissing[Either[Throwable, A]] =
    either.asInstanceOf[IsMissing[Either[Throwable, A]]]

  private[this] final val either: IsMissing[Either[Throwable, Any]] =
    _.value.left.exists(_.isInstanceOf[ConfigException.Missing])


  implicit def tryIsMissing[A]: IsMissing[Try[A]] =
    tryM.asInstanceOf[IsMissing[Try[A]]]

  private[this] final val tryM: IsMissing[Try[Any]] = _.value match {
    case Failure(_: ConfigException.Missing) => true
    case _                                   => false
  }

}
