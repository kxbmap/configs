/*
 * Copyright 2013 Tsukasa Kitachi
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

import com.typesafe.config.{Config, ConfigUtil}
import java.util.concurrent.TimeUnit
import scala.annotation.implicitNotFound
import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import scala.reflect.{ClassTag, classTag}
import scala.util.Try
import scala.util.control.Exception.Catcher


@implicitNotFound("No implicit Configs defined for ${T}.")
trait Configs[T] {
  def extract(config: Config): T

  def map[U](f: T => U): Configs[U] = Configs.configs { f compose extract }
}

object Configs extends ConfigsInstances {
  @inline def apply[T: Configs]: Configs[T] = implicitly[Configs[T]]

  def configs[T](f: Config => T): Configs[T] = new Configs[T] {
    def extract(config: Config): T = f(config)
  }

  def atPath[T](f: (Config, String) => T): AtPath[T] = configs { c => f(c, _) }
}

trait ConfigsInstances {

  import Configs._

  implicit val intAtPath:         AtPath[Int]           = atPath { _.getInt(_) }
  implicit val intListAtPath:     AtPath[List[Int]]     = atPath { _.getIntList(_).map(_.toInt).toList }
  implicit val longAtPath:        AtPath[Long]          = atPath { _.getLong(_) }
  implicit val longListAtPath:    AtPath[List[Long]]    = atPath { _.getLongList(_).map(_.toLong).toList }
  implicit val doubleAtPath:      AtPath[Double]        = atPath { _.getDouble(_) }
  implicit val doubleListAtPath:  AtPath[List[Double]]  = atPath { _.getDoubleList(_).map(_.toDouble).toList }
  implicit val booleanAtPath:     AtPath[Boolean]       = atPath { _.getBoolean(_) }
  implicit val booleanListAtPath: AtPath[List[Boolean]] = atPath { _.getBooleanList(_).map(_.booleanValue()).toList }
  implicit val stringAtPath:      AtPath[String]        = atPath { _.getString(_) }
  implicit val stringListAtPath:  AtPath[List[String]]  = atPath { _.getStringList(_).toList }

  implicit val configsIdentity: Configs[Config] = configs { identity }

  def mapConfigs[K, T: AtPath](f: String => K): Configs[Map[K, T]] = configs { c =>
    c.root().keysIterator.map { k => f(k) -> c.get[T](ConfigUtil.quoteString(k)) }.toMap
  }

  implicit def stringMapConfigs[T: AtPath]: Configs[Map[String, T]] = mapConfigs[String, T] { identity }
  implicit def symbolMapConfigs[T: AtPath]: Configs[Map[Symbol, T]] = mapConfigs[Symbol, T] { Symbol.apply }

  implicit val symbolAtPath:      AtPath[Symbol]        = AtPath by Symbol.apply
  implicit val symbolListAtPath:  AtPath[List[Symbol]]  = AtPath listBy Symbol.apply

  implicit def configsAtPath[T: Configs]: AtPath[T] = atPath {
    _.getConfig(_).extract[T]
  }
  implicit def configsListAtPath[T: Configs]: AtPath[List[T]] = atPath {
    _.getConfigList(_).map(_.extract[T]).toList
  }

  implicit def optionConfigs[T: Configs](implicit cc: CatchCond = CatchCond.missing): Configs[Option[T]] = configs { c =>
    try Some(c.extract[T]) catch optCatcher
  }
  implicit def optionAtPath[T: AtPath](implicit cc: CatchCond = CatchCond.missing): AtPath[Option[T]] = atPath { (c, p) =>
    try Some(c.get[T](p)) catch optCatcher
  }
  private def optCatcher(implicit cc: CatchCond): Catcher[Option[Nothing]] = {
    case e if cc(e) => None
  }

  implicit def eitherConfigs[E <: Throwable: ClassTag, T: Configs]: Configs[Either[E, T]] = configs { c =>
    try Right(c.extract[T]) catch eitherCatcher
  }
  implicit def eitherAtPath[E <: Throwable: ClassTag, T: AtPath]: AtPath[Either[E, T]] = atPath { (c, p) =>
    try Right(c.get[T](p)) catch eitherCatcher
  }
  private def eitherCatcher[E <: Throwable: ClassTag]: Catcher[Either[E, Nothing]] = {
    case e if classTag[E].runtimeClass isAssignableFrom e.getClass => Left(e.asInstanceOf[E])
  }

  implicit def tryConfigs[T: Configs]: Configs[Try[T]] = configs {
    Try apply _.extract[T]
  }
  implicit def tryAtPath[T: AtPath]: AtPath[Try[T]] = atPath {
    Try apply _.get[T](_)
  }

  implicit val durationAtPath: AtPath[Duration] = atPath {
    Duration fromNanos _.getDuration(_, TimeUnit.NANOSECONDS)
  }
  implicit val durationListAtPath: AtPath[List[Duration]] = atPath {
    _.getDurationList(_, TimeUnit.NANOSECONDS).map(Duration.fromNanos(_)).toList
  }

}
