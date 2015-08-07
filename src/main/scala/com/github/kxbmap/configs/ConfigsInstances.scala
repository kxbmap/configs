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
import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.{ClassTag, classTag}
import scala.util.Try

private[configs] abstract class ConfigsInstances {

  implicit def materializeConfigs[A]: Configs[A] = macro macros.ConfigsMacro.materialize[A]


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


  implicit lazy val byteConfigs: Configs[Byte] =
    _.getInt(_) |> (_.toByte)

  implicit lazy val javaByteConfigs: Configs[jl.Byte] =
    _.getInt(_) |> (_.toByte |> Byte.box)

  implicit lazy val javaByteListConfigs: Configs[ju.List[jl.Byte]] =
    _.getIntList(_).map(_.toByte |> Byte.box).asJava

  implicit def bytesConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Byte, F[Byte]]): Configs[F[Byte]] =
    _.getIntList(_).map(_.toByte)(collection.breakOut)


  implicit lazy val shortConfigs: Configs[Short] =
    _.getInt(_) |> (_.toShort)

  implicit lazy val javaShortConfigs: Configs[jl.Short] =
    _.getInt(_) |> (_.toShort |> Short.box)

  implicit lazy val javaShortListConfigs: Configs[ju.List[jl.Short]] =
    _.getIntList(_).map(_.toShort |> Short.box).asJava

  implicit def shortsConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Short, F[Short]]): Configs[F[Short]] =
    _.getIntList(_).map(_.toShort)(collection.breakOut)


  implicit lazy val intConfigs: Configs[Int] =
    _.getInt(_)

  implicit lazy val javaIntegerConfigs: Configs[jl.Integer] =
    _.getInt(_) |> Int.box

  implicit lazy val javaIntegerListConfigs: Configs[ju.List[jl.Integer]] =
    _.getIntList(_)

  implicit def intsConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Int, F[Int]]): Configs[F[Int]] =
    _.getIntList(_).map(_.toInt)(collection.breakOut)


  implicit lazy val longConfigs: Configs[Long] =
    _.getLong(_)

  implicit lazy val javaLongConfigs: Configs[jl.Long] =
    _.getLong(_) |> Long.box

  implicit lazy val javaLongListConfigs: Configs[ju.List[jl.Long]] =
    _.getLongList(_)

  implicit def longsConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Long, F[Long]]): Configs[F[Long]] =
    _.getLongList(_).map(_.toLong)(collection.breakOut)


  implicit lazy val floatConfigs: Configs[Float] =
    _.getDouble(_) |> (_.toFloat)

  implicit lazy val javaFloatConfigs: Configs[jl.Float] =
    _.getDouble(_) |> (_.toFloat |> Float.box)

  implicit lazy val javaFloatListConfigs: Configs[ju.List[jl.Float]] =
    _.getDoubleList(_).map(_.toFloat |> Float.box).asJava

  implicit def floatsConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Float, F[Float]]): Configs[F[Float]] =
    _.getDoubleList(_).map(_.toFloat)(collection.breakOut)


  implicit lazy val doubleConfigs: Configs[Double] =
    _.getDouble(_)

  implicit lazy val javaDoubleConfigs: Configs[jl.Double] =
    _.getDouble(_) |> Double.box

  implicit lazy val javaDoubleListConfigs: Configs[ju.List[jl.Double]] =
    _.getDoubleList(_)

  implicit def doublesConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Double, F[Double]]): Configs[F[Double]] =
    _.getDoubleList(_).map(_.toDouble)(collection.breakOut)


  implicit lazy val booleanConfigs: Configs[Boolean] =
    _.getBoolean(_)

  implicit lazy val javaBooleanConfigs: Configs[jl.Boolean] =
    _.getBoolean(_) |> Boolean.box

  implicit lazy val javaBooleanListConfigs: Configs[ju.List[jl.Boolean]] =
    _.getBooleanList(_)

  implicit def booleansConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Boolean, F[Boolean]]): Configs[F[Boolean]] =
    _.getBooleanList(_).map(_.booleanValue())(collection.breakOut)


  implicit lazy val charConfigs: Configs[Char] =
    _.getInt(_) |> (_.toChar)

  implicit lazy val javaCharConfigs: Configs[jl.Character] =
    _.getInt(_) |> (_.toChar |> Char.box)

  implicit lazy val javaCharListConfigs: Configs[ju.List[jl.Character]] =
    _.getIntList(_).map(_.toChar |> Char.box).asJava

  implicit def charsConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Char, F[Char]]): Configs[F[Char]] =
    _.getIntList(_).map(_.toChar)(collection.breakOut)


  implicit lazy val stringConfigs: Configs[String] =
    _.getString(_)

  implicit lazy val stringListConfigs: Configs[ju.List[String]] =
    _.getStringList(_)

  implicit def stringsConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, String, F[String]]): Configs[F[String]] =
    _.getStringList(_).to[F]


  implicit lazy val javaDurationConfigs: Configs[jt.Duration] =
    _.getDuration(_)

  implicit lazy val javaDurationListConfigs: Configs[ju.List[jt.Duration]] =
    _.getDurationList(_)

  implicit def javaDurationsConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, jt.Duration, F[jt.Duration]]): Configs[F[jt.Duration]] =
    _.getDurationList(_).to[F]


  implicit lazy val finiteDurationConfigs: Configs[FiniteDuration] =
    _.getDuration(_, TimeUnit.NANOSECONDS) |> Duration.fromNanos

  implicit lazy val durationConfigs: Configs[Duration] =
    finiteDurationConfigs.asInstanceOf[Configs[Duration]]

  implicit def durationsConfigs[F[_], A >: FiniteDuration <: Duration](implicit cbf: CanBuildFrom[Nothing, A, F[A]]): Configs[F[A]] =
    _.getDurationList(_, TimeUnit.NANOSECONDS).map(Duration.fromNanos(_))(collection.breakOut)


  implicit lazy val configConfigs: Configs[Config] =
    _.getConfig(_)

  implicit lazy val configListConfigs: Configs[ju.List[Config]] =
    _.getConfigList(_).asInstanceOf[ju.List[Config]]

  implicit def configsConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, Config, F[Config]]): Configs[F[Config]] =
    _.getConfigList(_).map(c => c: Config)(collection.breakOut)


  implicit lazy val configMemorySizeConfigs: Configs[ConfigMemorySize] =
    _.getMemorySize(_)

  implicit lazy val configMemorySizeListConfigs: Configs[ju.List[ConfigMemorySize]] =
    _.getMemorySizeList(_)

  implicit def configMemorySizesConfigs[F[_]](implicit cbf: CanBuildFrom[Nothing, ConfigMemorySize, F[ConfigMemorySize]]): Configs[F[ConfigMemorySize]] =
    _.getMemorySizeList(_).to[F]


  implicit def collectionConfigs[F[_], A: Configs](implicit cbf: CanBuildFrom[Nothing, A, F[A]]): Configs[F[A]] =
    _.getList(_).map(Configs[A].extract).to[F]


  implicit def stringMapConfigs[A: Configs]: Configs[Map[String, A]] =
    Configs.onPath { c =>
      val A = Configs[A]
      c.root().keysIterator.map(k => k -> A.get(c, ConfigUtil.quoteString(k))).toMap
    }

  implicit def symbolMapConfigs[A: Configs]: Configs[Map[Symbol, A]] =
    Configs[Map[String, A]].map(_.map {
      case (k, v) => Symbol(k) -> v
    })


  implicit def javaListConfigs[A: Configs]: Configs[ju.List[A]] =
    Configs[List[A]].map(_.asJava)

  implicit def javaMapConfigs[A: Configs]: Configs[ju.Map[String, A]] =
    Configs[Map[String, A]].map(_.asJava)

  implicit def javaSetConfigs[A: Configs]: Configs[ju.Set[A]] =
    Configs[Set[A]].map(_.asJava)


  implicit def javaEnumConfigs[A <: java.lang.Enum[A] : ClassTag]: Configs[A] = {
    val arr = ju.Objects.requireNonNull(classTag[A].runtimeClass.getEnumConstants.asInstanceOf[Array[A]])
    (c, p) => {
      val v = c.getString(p)
      arr.find(_.name() == v).getOrElse {
        throw new ConfigException.BadValue(c.origin(), p, s"$v must be one of ${arr.mkString(", ")}")
      }
    }
  }


  implicit lazy val symbolConfigs: Configs[Symbol] =
    Configs[String].map(Symbol.apply)

  implicit def symbolsConfigs[F[_]](implicit mapF: Configs.MapF[F, String, Symbol]): Configs[F[Symbol]] =
    mapF(Symbol.apply)


  implicit lazy val pathConfigs: Configs[Path] =
    Configs[String].map(Paths.get(_))

  implicit def pathsConfigs[F[_]](implicit mapF: Configs.MapF[F, String, Path]): Configs[F[Path]] =
    mapF(Paths.get(_))


  implicit lazy val fileConfigs: Configs[File] =
    Configs[String].map(new File(_))

  implicit def filesConfigs[F[_]](implicit mapF: Configs.MapF[F, String, File]): Configs[F[File]] =
    mapF(new File(_))


  implicit lazy val inetAddressConfigs: Configs[InetAddress] =
    Configs[String].map(InetAddress.getByName)

  implicit def inetAddressesConfigs[F[_]](implicit mapF: Configs.MapF[F, String, InetAddress]): Configs[F[InetAddress]] =
    mapF(InetAddress.getByName)


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
