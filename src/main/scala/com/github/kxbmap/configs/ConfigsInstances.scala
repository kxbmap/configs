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

import com.typesafe.config.{Config, ConfigException, ConfigMemorySize, ConfigUtil}
import java.io.File
import java.net.{InetAddress, InetSocketAddress}
import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeUnit
import java.{lang => jl, time => jt, util => ju}
import scala.collection.JavaConversions._
import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.{ClassTag, classTag}
import scala.util.Try

private[configs] abstract class ConfigsInstances {

  implicit def materializeConfigs[A]: Configs[A] = macro macros.ConfigsMacro.materialize[A]


  implicit def cbfConfigs[F[_], A: Configs](implicit cbf: CanBuildFrom[Nothing, A, F[A]]): Configs[F[A]] =
    (c, p) => c.getList(p).map(Configs[A].extract).to[F]


  implicit def optionConfigs[A: Configs]: Configs[Option[A]] =
    (c, p) => if (c.hasPath(p) && !c.getIsNull(p)) Some(Configs[A].get(c, p)) else None

  implicit def tryConfigs[A: Configs]: Configs[Try[A]] =
    (c, p) => Try(Configs[A].get(c, p))

  implicit def eitherConfigs[E <: Throwable : ClassTag, A: Configs]: Configs[Either[E, A]] =
    (c, p) =>
      try
        Right(Configs[A].get(c, p))
      catch {
        case e if classTag[E].runtimeClass.isAssignableFrom(e.getClass) =>
          Left(e.asInstanceOf[E])
      }


  implicit lazy val intConfigs: Configs[Int] =
    _.getInt(_)

  implicit lazy val javaIntegerConfigs: Configs[jl.Integer] =
    (c, p) => Int.box(c.getInt(p))

  implicit lazy val javaIntegerListConfigs: Configs[ju.List[jl.Integer]] =
    _.getIntList(_)

  implicit def intCBFConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Int, F[Int]]): Configs[F[Int]] =
    _.getIntList(_).map(_.toInt)(collection.breakOut)


  implicit lazy val longConfigs: Configs[Long] =
    _.getLong(_)

  implicit lazy val javaLongConfigs: Configs[jl.Long] =
    (c, p) => Long.box(c.getLong(p))

  implicit lazy val javaLongListsConfigs: Configs[ju.List[jl.Long]] =
    _.getLongList(_)

  implicit def longCBFConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Long, F[Long]]): Configs[F[Long]] =
    _.getLongList(_).map(_.toLong)(collection.breakOut)


  implicit lazy val doubleConfigs: Configs[Double] =
    _.getDouble(_)

  implicit lazy val javaDoubleConfigs: Configs[jl.Double] =
    (c, p) => Double.box(c.getDouble(p))

  implicit lazy val javaDoubleListConfigs: Configs[ju.List[jl.Double]] =
    _.getDoubleList(_)

  implicit def doubleCBFConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Double, F[Double]]): Configs[F[Double]] =
    _.getDoubleList(_).map(_.toDouble)(collection.breakOut)


  implicit lazy val booleanConfigs: Configs[Boolean] =
    _.getBoolean(_)

  implicit lazy val javaBooleanConfigs: Configs[jl.Boolean] =
    (c, p) => Boolean.box(c.getBoolean(p))

  implicit lazy val javaBooleanListConfigs: Configs[ju.List[jl.Boolean]] =
    _.getBooleanList(_)

  implicit def booleanCBFConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Boolean, F[Boolean]]): Configs[F[Boolean]] =
    _.getBooleanList(_).map(_.booleanValue())(collection.breakOut)


  implicit lazy val stringConfigs: Configs[String] =
    _.getString(_)

  implicit lazy val stringListConfigs: Configs[ju.List[String]] =
    _.getStringList(_)

  implicit def stringCBFConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, String, F[String]]): Configs[F[String]] =
    _.getStringList(_).to[F]


  implicit lazy val javaDurationConfigs: Configs[jt.Duration] =
    _.getDuration(_)

  implicit lazy val javaDurationListConfigs: Configs[ju.List[jt.Duration]] =
    _.getDurationList(_)

  implicit def javaDurationCBFConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, jt.Duration, F[jt.Duration]]): Configs[F[jt.Duration]] =
    _.getDurationList(_).to[F]


  implicit lazy val finiteDurationConfigs: Configs[FiniteDuration] =
    (c, p) => Duration.fromNanos(c.getDuration(p, TimeUnit.NANOSECONDS))

  implicit lazy val durationConfigs: Configs[Duration] =
    finiteDurationConfigs.asInstanceOf[Configs[Duration]]


  implicit lazy val configConfigs: Configs[Config] =
    _.getConfig(_)

  implicit lazy val configListConfigs: Configs[ju.List[Config]] =
    _.getConfigList(_).asInstanceOf[ju.List[Config]]

  implicit def configCBFConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Config, F[Config]]): Configs[F[Config]] =
    _.getConfigList(_).map(c => c: Config)(collection.breakOut)


  implicit lazy val configMemorySizeConfigs: Configs[ConfigMemorySize] =
    _.getMemorySize(_)

  implicit lazy val configMemorySizeListConfigs: Configs[ju.List[ConfigMemorySize]] =
    _.getMemorySizeList(_)

  implicit def configMemorySizeCBFConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, ConfigMemorySize, F[ConfigMemorySize]]): Configs[F[ConfigMemorySize]] =
    _.getMemorySizeList(_).to[F]


  implicit def stringMapConfigs[A: Configs]: Configs[Map[String, A]] =
    Configs.onPath { c =>
      val A = Configs[A]
      c.root().keysIterator.map(k => k -> A.get(c, ConfigUtil.quoteString(k))).toMap
    }

  implicit def symbolMapConfigs[A: Configs]: Configs[Map[Symbol, A]] =
    Configs[Map[String, A]].map(_.map {
      case (k, v) => Symbol(k) -> v
    })


  implicit def javaEnumConfigs[A <: java.lang.Enum[A] : ClassTag]: Configs[A] = {
    val arr = classTag[A].runtimeClass.getEnumConstants.asInstanceOf[Array[A]]
    (c, p) => {
      val v = c.getString(p)
      arr.find(_.name() == v).getOrElse {
        throw new ConfigException.BadValue(c.origin(), p, s"$v must be one of ${arr.mkString(", ")}")
      }
    }
  }


  implicit lazy val symbolConfigs: Configs[Symbol] =
    Configs[String].map(Symbol.apply)

  implicit lazy val pathConfigs: Configs[Path] =
    Configs[String].map(Paths.get(_))

  implicit lazy val fileConfigs: Configs[File] =
    Configs[String].map(new File(_))

  implicit lazy val inetAddressConfigs: Configs[InetAddress] =
    Configs[String].map(InetAddress.getByName)

  implicit lazy val inetSocketAddressConfigs: Configs[InetSocketAddress] =
    Configs.onPath { c =>
      val port = c.getInt("port")
      Configs[Option[String]].get(c, "hostname").fold {
        new InetSocketAddress(Configs[InetAddress].get(c, "addr"), port)
      } {
        hostname => new InetSocketAddress(hostname, port)
      }
    }

}
