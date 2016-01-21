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

package configs.util

import com.typesafe.config.ConfigException
import configs.Result
import scala.util.{Failure, Try}
import scalaz.Need

trait IsWrongType[A] {
  def check(a: Need[Result[A]]): Boolean
}

object IsWrongType {

  def apply[A](implicit A: IsWrongType[A]): IsWrongType[A] = A


  implicit def defaultIsWrongType[A]: IsWrongType[A] =
    default.asInstanceOf[IsWrongType[A]]

  private[this] final val default: IsWrongType[Any] =
    _.value.fold(_.head.toConfigException.isInstanceOf[ConfigException.WrongType], _ => false)


  implicit def eitherIsWrongType[A]: IsWrongType[Either[Throwable, A]] =
    either.asInstanceOf[IsWrongType[Either[Throwable, A]]]

  private[this] final val either: IsWrongType[Either[Throwable, Any]] =
    _.value.exists(_.left.exists(_.isInstanceOf[ConfigException.WrongType]))


  implicit def tryIsWrongType[A]: IsWrongType[Try[A]] =
    tryW.asInstanceOf[IsWrongType[Try[A]]]

  private[this] final val tryW: IsWrongType[Try[Any]] =
    _.value.exists {
      case Failure(_: ConfigException.WrongType) => true
      case _                                     => false
    }

}
