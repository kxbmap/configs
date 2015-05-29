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

package com.github.kxbmap.configs

import com.typesafe.config.{Config, ConfigMemorySize}
import java.time.{Duration => JDuration}
import java.util.concurrent.TimeUnit
import scala.annotation.implicitNotFound
import scala.collection.JavaConversions._
import scala.concurrent.duration.Duration
import scala.reflect.{ClassTag, classTag}
import scala.util.Try


@implicitNotFound("No implicit Configs defined for ${T}.")
trait Configs[T] {
  def extract(config: Config): T

  def map[U](f: T => U): Configs[U] = Configs.configs(f.compose(extract))
}

object Configs extends ConfigsInstances {
  @inline def apply[T](implicit C: Configs[T]): Configs[T] = C

  def configs[T](f: Config => T): Configs[T] = new Configs[T] {
    def extract(config: Config): T = f(config)
  }

  def atPath[T](f: (Config, String) => T): AtPath[T] = configs(c => f(c, _))
}

trait ConfigsInstances {

  import Configs._

  implicit val configConfigs: Configs[Config] = configs(identity)


  implicit val intAtPath: AtPath[Int] = atPath(_.getInt(_))

  implicit def intsAtPath[C[_]](implicit cbf: CBF[C, Int]): AtPath[C[Int]] = atPath {
    _.getIntList(_).map(_.toInt).to[C]
  }

  implicit val longAtPath: AtPath[Long] = atPath(_.getLong(_))

  implicit def longsAtPath[C[_]](implicit cbf: CBF[C, Long]): AtPath[C[Long]] = atPath {
    _.getLongList(_).map(_.toLong).to[C]
  }

  implicit val doubleAtPath: AtPath[Double] = atPath(_.getDouble(_))

  implicit def doublesAtPath[C[_]](implicit cbf: CBF[C, Double]): AtPath[C[Double]] = atPath {
    _.getDoubleList(_).map(_.toDouble).to[C]
  }

  implicit val booleanAtPath: AtPath[Boolean] = atPath(_.getBoolean(_))

  implicit def booleansAtPath[C[_]](implicit cbf: CBF[C, Boolean]): AtPath[C[Boolean]] = atPath {
    _.getBooleanList(_).map(_.booleanValue()).to[C]
  }

  implicit val stringAtPath: AtPath[String] = atPath(_.getString(_))

  implicit def stringsAtPath[C[_]](implicit cbf: CBF[C, String]): AtPath[C[String]] = atPath {
    _.getStringList(_).to[C]
  }

  def mapConfigs[K, T: AtPath](f: String => K): Configs[Map[K, T]] = configs { c =>
    c.root().keysIterator.map { k => f(k) -> c.get[T](k) }.toMap
  }

  implicit def stringMapConfigs[T: AtPath]: Configs[Map[String, T]] = mapConfigs(identity)

  implicit def symbolMapConfigs[T: AtPath]: Configs[Map[Symbol, T]] = mapConfigs(Symbol.apply)

  implicit val symbolAtPath: AtPath[Symbol] = AtPath.by(Symbol.apply)

  implicit def symbolsAtPath[C[_]](implicit cbf: CBF[C, Symbol]): AtPath[C[Symbol]] = AtPath.listBy(Symbol.apply)

  implicit def configsAtPath[T: Configs]: AtPath[T] = atPath {
    _.getConfig(_).extract[T]
  }

  implicit def configsCollectionAtPath[C[_], T: Configs](implicit cbf: CBF[C, T]): AtPath[C[T]] = atPath {
    _.getConfigList(_).map(_.extract[T]).to[C]
  }

  implicit def optionConfigs[T: Configs]: Configs[Option[T]] = configs { c =>
    Some(c.extract[T])
  }

  implicit def optionAtPath[T: AtPath]: AtPath[Option[T]] = atPath { (c, p) =>
    if (c.hasPathOrNull(p) && !c.getIsNull(p)) Some(c.get[T](p)) else None
  }

  implicit def eitherConfigs[E <: Throwable : ClassTag, T: Configs]: Configs[Either[E, T]] = configs { c =>
    eitherFrom(c.extract[T])
  }

  implicit def eitherAtPath[E <: Throwable : ClassTag, T: AtPath]: AtPath[Either[E, T]] = atPath { (c, p) =>
    eitherFrom(c.get[T](p))
  }

  private def eitherFrom[E <: Throwable : ClassTag, T](value: => T): Either[E, T] =
    try Right(value) catch {
      case e if classTag[E].runtimeClass.isAssignableFrom(e.getClass) =>
        Left(e.asInstanceOf[E])
    }

  implicit def tryConfigs[T: Configs]: Configs[Try[T]] = configs { c =>
    Try(c.extract[T])
  }

  implicit def tryAtPath[T: AtPath]: AtPath[Try[T]] = atPath { (c, p) =>
    Try(c.get[T](p))
  }

  implicit val durationAtPath: AtPath[Duration] = atPath { (c, p) =>
    Duration.fromNanos(c.getDuration(p, TimeUnit.NANOSECONDS))
  }

  implicit def durationsAtPath[C[_]](implicit cbf: CBF[C, Duration]): AtPath[C[Duration]] = atPath {
    _.getDurationList(_, TimeUnit.NANOSECONDS).map(Duration.fromNanos(_): Duration).to[C]
  }

  implicit val javaTimeDurationAtPath: AtPath[JDuration] = atPath {
    _.getDuration(_)
  }

  implicit def javaTimeDurationsAtPath[C[_]](implicit cbf: CBF[C, JDuration]): AtPath[C[JDuration]] = atPath {
    _.getDurationList(_).to[C]
  }

  implicit val configMemorySizeAtPath: AtPath[ConfigMemorySize] = atPath {
    _.getMemorySize(_)
  }

  implicit def configMemorySizesAtPath[C[_]](implicit cbf: CBF[C, ConfigMemorySize]): AtPath[C[ConfigMemorySize]] = atPath {
    _.getMemorySizeList(_).to[C]
  }

}
