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

import com.github.kxbmap.configs.syntax._
import com.typesafe.config.{Config, ConfigException, ConfigList, ConfigMemorySize, ConfigObject, ConfigUtil, ConfigValue}
import java.io.File
import java.net.{InetAddress, InetSocketAddress}
import java.nio.file.{Path, Paths}
import java.util.concurrent.TimeUnit
import java.util.{Locale, UUID}
import java.{lang => jl, time => jt, util => ju}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.{ClassTag, classTag}
import scala.util.Try

private[configs] abstract class AutoConfigs extends SimpleConfigs with MaterializeConfigs

private[configs] sealed trait MaterializeConfigs {

  implicit def materializeConfigs[A]: Configs[A] = macro macros.ConfigsMacro.materialize[A]

}

private[configs] abstract class SimpleConfigs {

  implicit def javaListConfigs[A: Configs]: Configs[ju.List[A]] =
    _.getList(_).map(Configs[A].extract).asJava

  implicit def javaMapConfigs[A: Configs]: Configs[ju.Map[String, A]] =
    Configs.onPath { c =>
      c.root().keysIterator.map(k => k -> Configs[A].get(c, ConfigUtil.quoteString(k))).toMap.asJava
    }

  implicit def javaSetConfigs[A](implicit C: Configs[ju.List[A]]): Configs[ju.Set[A]] =
    C.get(_, _).toSet.asJava

  implicit def javaSymbolMapConfigs[A](implicit C: Configs[ju.Map[String, A]]): Configs[ju.Map[Symbol, A]] =
    C.get(_, _).map(t => Symbol(t._1) -> t._2).asJava


  implicit def fromJListConfigs[F[_], A](implicit C: Configs[ju.List[A]], cbf: CanBuildFrom[Nothing, A, F[A]]): Configs[F[A]] =
    C.get(_, _).to[F]

  implicit def fromJMapConfigs[A, B](implicit C: Configs[ju.Map[A, B]]): Configs[Map[A, B]] =
    C.get(_, _).toMap


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

  implicit lazy val byteJListConfigs: Configs[ju.List[Byte]] =
    _.getIntList(_).map(_.byteValue()).asJava

  implicit lazy val javaByteConfigs: Configs[jl.Byte] =
    byteConfigs.asInstanceOf[Configs[jl.Byte]]

  implicit lazy val javaByteListConfigs: Configs[ju.List[jl.Byte]] =
    byteJListConfigs.asInstanceOf[Configs[ju.List[jl.Byte]]]


  implicit lazy val shortConfigs: Configs[Short] =
    _.getInt(_) |> (_.toShort)

  implicit lazy val shortJListConfigs: Configs[ju.List[Short]] =
    _.getIntList(_).map(_.shortValue()).asJava

  implicit lazy val javaShortConfigs: Configs[jl.Short] =
    shortConfigs.asInstanceOf[Configs[jl.Short]]

  implicit lazy val javaShortListConfigs: Configs[ju.List[jl.Short]] =
    shortJListConfigs.asInstanceOf[Configs[ju.List[jl.Short]]]


  implicit lazy val intConfigs: Configs[Int] =
    _.getInt(_)

  implicit lazy val intJListConfigs: Configs[ju.List[Int]] =
    javaIntegerListConfigs.asInstanceOf[Configs[ju.List[Int]]]

  implicit lazy val javaIntegerConfigs: Configs[jl.Integer] =
    intConfigs.asInstanceOf[Configs[jl.Integer]]

  implicit lazy val javaIntegerListConfigs: Configs[ju.List[jl.Integer]] =
    _.getIntList(_)


  implicit lazy val longConfigs: Configs[Long] =
    _.getLong(_)

  implicit lazy val longJListConfigs: Configs[ju.List[Long]] =
    javaLongListConfigs.asInstanceOf[Configs[ju.List[Long]]]

  implicit lazy val javaLongConfigs: Configs[jl.Long] =
    longConfigs.asInstanceOf[Configs[jl.Long]]

  implicit lazy val javaLongListConfigs: Configs[ju.List[jl.Long]] =
    _.getLongList(_)


  implicit lazy val floatConfigs: Configs[Float] =
    _.getDouble(_) |> (_.toFloat)

  implicit lazy val floatJListConfigs: Configs[ju.List[Float]] =
    _.getDoubleList(_).map(_.floatValue()).asJava

  implicit lazy val javaFloatConfigs: Configs[jl.Float] =
    floatConfigs.asInstanceOf[Configs[jl.Float]]

  implicit lazy val javaFloatListConfigs: Configs[ju.List[jl.Float]] =
    floatJListConfigs.asInstanceOf[Configs[ju.List[jl.Float]]]


  implicit lazy val doubleConfigs: Configs[Double] =
    _.getDouble(_)

  implicit lazy val doubleJListConfigs: Configs[ju.List[Double]] =
    javaDoubleListConfigs.asInstanceOf[Configs[ju.List[Double]]]

  implicit lazy val javaDoubleConfigs: Configs[jl.Double] =
    doubleConfigs.asInstanceOf[Configs[jl.Double]]

  implicit lazy val javaDoubleListConfigs: Configs[ju.List[jl.Double]] =
    _.getDoubleList(_)


  implicit lazy val booleanConfigs: Configs[Boolean] =
    _.getBoolean(_)

  implicit lazy val booleanJListConfigs: Configs[ju.List[Boolean]] =
    javaBooleanListConfigs.asInstanceOf[Configs[ju.List[Boolean]]]

  implicit lazy val javaBooleanConfigs: Configs[jl.Boolean] =
    booleanConfigs.asInstanceOf[Configs[jl.Boolean]]

  implicit lazy val javaBooleanListConfigs: Configs[ju.List[jl.Boolean]] =
    _.getBooleanList(_)


  implicit lazy val charConfigs: Configs[Char] =
    (c, p) => {
      val s = c.getString(p)
      if (s.length == 1) s(0)
      else throw new ConfigException.BadValue(c.origin(), p, s"single bmp char required rather than '$s'")
    }

  implicit lazy val charJListConfigs: Configs[ju.List[Char]] =
    (c, p) => ju.Arrays.asList(c.getString(p).toCharArray: _*)

  implicit lazy val javaCharConfigs: Configs[jl.Character] =
    charConfigs.asInstanceOf[Configs[jl.Character]]

  implicit lazy val javaCharListConfigs: Configs[ju.List[jl.Character]] =
    charJListConfigs.asInstanceOf[Configs[ju.List[jl.Character]]]


  implicit lazy val stringConfigs: Configs[String] =
    _.getString(_)

  implicit lazy val stringJListConfigs: Configs[ju.List[String]] =
    _.getStringList(_)


  implicit lazy val javaDurationConfigs: Configs[jt.Duration] =
    _.getDuration(_)

  implicit lazy val javaDurationListConfigs: Configs[ju.List[jt.Duration]] =
    _.getDurationList(_)


  implicit lazy val finiteDurationConfigs: Configs[FiniteDuration] =
    _.getDuration(_, TimeUnit.NANOSECONDS) |> Duration.fromNanos

  implicit lazy val finiteDurationJListConfigs: Configs[ju.List[FiniteDuration]] =
    _.getDurationList(_, TimeUnit.NANOSECONDS).map(Duration.fromNanos(_)).asJava


  implicit lazy val durationConfigs: Configs[Duration] =
    finiteDurationConfigs.orElse { (c, p) =>
      c.getString(p) match {
        case "infinity" | "+infinity" => Duration.Inf
        case "-infinity"              => Duration.MinusInf
        case "undefined"              => Duration.Undefined
        case s                        => throw new ConfigException.BadValue(c.origin(), p, s"Could not parse duration '$s'")
      }
    }


  implicit lazy val configConfigs: Configs[Config] =
    _.getConfig(_)

  implicit lazy val configJListConfigs: Configs[ju.List[Config]] =
    _.getConfigList(_).asInstanceOf[ju.List[Config]]


  implicit lazy val configValueConfigs: Configs[ConfigValue] =
    _.getValue(_)

  implicit lazy val configValueJListConfigs: Configs[ju.List[ConfigValue]] =
    _.getList(_)

  implicit lazy val configValueJMapConfigs: Configs[ju.Map[String, ConfigValue]] =
    _.getObject(_)


  implicit lazy val configListConfigs: Configs[ConfigList] =
    _.getList(_)


  implicit lazy val configObjectConfigs: Configs[ConfigObject] =
    _.getObject(_)

  implicit lazy val configObjectJListConfigs: Configs[ju.List[ConfigObject]] =
    _.getObjectList(_).asInstanceOf[ju.List[ConfigObject]]


  implicit lazy val configMemorySizeConfigs: Configs[ConfigMemorySize] =
    _.getMemorySize(_)

  implicit lazy val configMemorySizeJListConfigs: Configs[ju.List[ConfigMemorySize]] =
    _.getMemorySizeList(_)


  private def enumMap[A <: jl.Enum[A] : ClassTag]: Map[String, A] =
    classTag[A].runtimeClass
      .getEnumConstants.asInstanceOf[Array[A]]
      .map(a => a.name() -> a)(collection.breakOut)

  private def getEnum[A <: jl.Enum[A]](m: Map[String, A], s: String, c: Config, p: String): A =
    m.getOrElse(s, throw new ConfigException.BadValue(c.origin(), p, s"$s must be one of ${m.keys.mkString(", ")}"))

  implicit def javaEnumConfigs[A <: jl.Enum[A] : ClassTag]: Configs[A] = {
    val m = enumMap[A]
    (c, p) => getEnum(m, c.getString(p), c, p)
  }

  implicit def javaEnumJListConfigs[A <: jl.Enum[A] : ClassTag]: Configs[ju.List[A]] = {
    val m = enumMap[A]
    (c, p) => c.getStringList(p).map(getEnum(m, _, c, p))
  }


  implicit lazy val symbolConfigs: Configs[Symbol] =
    stringConfigs.map(Symbol.apply)

  implicit lazy val symbolJListConfigs: Configs[ju.List[Symbol]] =
    stringJListConfigs.map(_.map(Symbol.apply))


  implicit lazy val uuidConfigs: Configs[UUID] =
    stringConfigs.map(UUID.fromString)

  implicit lazy val uuidJListConfigs: Configs[ju.List[UUID]] =
    stringJListConfigs.map(_.map(UUID.fromString))


  private[this] lazy val availableLocales: Map[String, Locale] =
    Locale.getAvailableLocales.map(l => l.toString -> l)(collection.breakOut)

  private[this] def getLocale(s: String, c: Config, p: String): Locale =
    availableLocales.getOrElse(s, throw new ConfigException.BadValue(c.origin(), p, s"Locale '$s' is not available"))

  implicit lazy val localeConfigs: Configs[Locale] =
    (c, p) => getLocale(c.getString(p), c, p)

  implicit lazy val localeJListConfigs: Configs[ju.List[Locale]] =
    (c, p) => c.getStringList(p).map(getLocale(_, c, p))


  implicit lazy val pathConfigs: Configs[Path] =
    stringConfigs.map(Paths.get(_))

  implicit lazy val pathJListConfigs: Configs[ju.List[Path]] =
    stringJListConfigs.map(_.map(Paths.get(_)))


  implicit lazy val fileConfigs: Configs[File] =
    stringConfigs.map(new File(_))

  implicit lazy val fileJListConfigs: Configs[ju.List[File]] =
    stringJListConfigs.map(_.map(new File(_)))


  implicit lazy val inetAddressConfigs: Configs[InetAddress] =
    stringConfigs.map(InetAddress.getByName)

  implicit lazy val inetAddressJListConfigs: Configs[ju.List[InetAddress]] =
    stringJListConfigs.map(_.map(InetAddress.getByName))


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
