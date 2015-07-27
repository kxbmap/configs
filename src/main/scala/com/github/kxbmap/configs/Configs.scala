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

import com.typesafe.config.{Config, ConfigException, ConfigMemorySize, ConfigValue}
import java.io.File
import java.net.{InetAddress, InetSocketAddress, UnknownHostException}
import java.nio.file.{Path, Paths}
import java.time.{Duration => JDuration}
import java.util.concurrent.TimeUnit
import scala.annotation.implicitNotFound
import scala.collection.JavaConversions._
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.{ClassTag, classTag}
import scala.util.Try


@implicitNotFound("No implicit Configs defined for ${T}.")
trait Configs[T] {

  def get(config: Config, path: String): T

  def extract(config: Config): T = get(config.atPath(Configs.DummyPath), Configs.DummyPath)

  def extract(value: ConfigValue): T = get(value.atPath(Configs.DummyPath), Configs.DummyPath)

  def map[U](f: T => U): Configs[U] = (c, p) => f(get(c, p))

  def orElse[U >: T](other: Configs[U]): Configs[U] = (c, p) =>
    try
      get(c, p)
    catch {
      case _: ConfigException =>
        other.get(c, p)
    }
}

object Configs extends ConfigsInstances {

  private final val DummyPath = "configs-extract-path"

  @inline
  def apply[T](implicit T: Configs[T]): Configs[T] = T

  def from[T](f: (Config, String) => T): Configs[T] = f(_, _)

  def onPath[T](f: Config => T): Configs[T] = (c, p) => f(c.getConfig(p))

  def of[T]: Configs[T] = macro macros.ConfigsMacro.materialize[T]

  def bean[T]: Configs[T] = macro macros.BeanConfigsMacro.materializeT[T]

  def bean[T](newInstance: T): Configs[T] = macro macros.BeanConfigsMacro.materializeI[T]


  @deprecated("Use Configs.onPath", "0.3.0")
  def configs[T](f: Config => T): Configs[T] = onPath(f)

  @deprecated("Use Configs.from", "0.3.0")
  def atPath[T](f: (Config, String) => T): Configs[T] = from(f)

}

trait ConfigsInstances {

  implicit def materializeConfigs[T]: Configs[T] = macro macros.ConfigsMacro.materialize[T]


  implicit def collectionConfigs[C[_], T: Configs](implicit cbf: CBF[C, T]): Configs[C[T]] = (c, p) =>
    c.getList(p).map(Configs[T].extract).to[C]


  implicit def stringMapConfigs[T: Configs]: Configs[Map[String, T]] = Configs.onPath { c =>
    c.root().keysIterator.map(k => k -> c.get[T](k)).toMap
  }

  implicit def symbolMapConfigs[T: Configs]: Configs[Map[Symbol, T]] = Configs.onPath { c =>
    c.root().keysIterator.map(k => Symbol(k) -> c.get[T](k)).toMap
  }


  implicit lazy val configConfigs: Configs[Config] = _.getConfig(_)

  implicit def configsConfigs[C[_]](implicit cbf: CBF[C, Config]): Configs[C[Config]] =
    _.getConfigList(_).map(c => c: Config)(collection.breakOut)


  implicit lazy val intConfigs: Configs[Int] = _.getInt(_)

  implicit def intsConfigs[C[_]](implicit cbf: CBF[C, Int]): Configs[C[Int]] =
    _.getIntList(_).map(_.toInt)(collection.breakOut)


  implicit lazy val longConfigs: Configs[Long] = _.getLong(_)

  implicit def longsConfigs[C[_]](implicit cbf: CBF[C, Long]): Configs[C[Long]] =
    _.getLongList(_).map(_.toLong)(collection.breakOut)


  implicit lazy val doubleConfigs: Configs[Double] = _.getDouble(_)

  implicit def doublesConfigs[C[_]](implicit cbf: CBF[C, Double]): Configs[C[Double]] =
    _.getDoubleList(_).map(_.toDouble)(collection.breakOut)


  implicit lazy val booleanConfigs: Configs[Boolean] = _.getBoolean(_)

  implicit def booleansConfigs[C[_]](implicit cbf: CBF[C, Boolean]): Configs[C[Boolean]] =
    _.getBooleanList(_).map(_.booleanValue())(collection.breakOut)


  implicit lazy val stringConfigs: Configs[String] = _.getString(_)

  implicit def stringsConfigs[C[_]](implicit cbf: CBF[C, String]): Configs[C[String]] = _.getStringList(_).to[C]


  implicit lazy val symbolConfigs: Configs[Symbol] = Configs[String].map(Symbol.apply)


  implicit def optionConfigs[T: Configs]: Configs[Option[T]] = (c, p) =>
    if (c.hasPathOrNull(p) && !c.getIsNull(p)) Some(c.get[T](p)) else None


  implicit def eitherConfigs[E <: Throwable : ClassTag, T: Configs]: Configs[Either[E, T]] = (c, p) =>
    try Right(c.get[T](p)) catch {
      case e if classTag[E].runtimeClass.isAssignableFrom(e.getClass) =>
        Left(e.asInstanceOf[E])
    }


  implicit def tryConfigs[T: Configs]: Configs[Try[T]] = (c, p) => Try(c.get[T](p))


  implicit lazy val finiteDurationConfigs: Configs[FiniteDuration] = (c, p) =>
    Duration.fromNanos(c.getDuration(p, TimeUnit.NANOSECONDS))

  implicit lazy val durationConfigs: Configs[Duration] = finiteDurationConfigs.asInstanceOf[Configs[Duration]]


  implicit lazy val javaTimeDurationConfigs: Configs[JDuration] = _.getDuration(_)

  implicit def javaTimeDurationsConfigs[C[_]](implicit cbf: CBF[C, JDuration]): Configs[C[JDuration]] =
    _.getDurationList(_).to[C]


  implicit lazy val configMemorySizeConfigs: Configs[ConfigMemorySize] = _.getMemorySize(_)

  implicit def configMemorySizesConfigs[C[_]](implicit cbf: CBF[C, ConfigMemorySize]): Configs[C[ConfigMemorySize]] =
    _.getMemorySizeList(_).to[C]


  implicit lazy val fileConfigs: Configs[File] = Configs[String].map(new File(_))

  implicit lazy val pathConfigs: Configs[Path] = Configs[String].map(Paths.get(_))


  implicit lazy val inetAddressConfigs: Configs[InetAddress] = (c, p) =>
    try
      InetAddress.getByName(c.get[String](p))
    catch {
      case e: UnknownHostException =>
        throw new ConfigException.BadValue(c.origin(), p, e.getMessage, e)
    }


  implicit lazy val inetSocketAddressConfigs: Configs[InetSocketAddress] = Configs.onPath { c =>
    val port = c.getInt("port")
    c.opt[String]("hostname").fold {
      new InetSocketAddress(c.get[InetAddress]("addr"), port)
    } {
      hostname => new InetSocketAddress(hostname, port)
    }
  }

}
