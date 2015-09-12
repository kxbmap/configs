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

import com.typesafe.config.{Config, ConfigException, ConfigList, ConfigMemorySize, ConfigObject, ConfigUtil, ConfigValue}
import java.util.concurrent.TimeUnit
import java.{lang => jl, math => jm, time => jt, util => ju}
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration.{Duration, FiniteDuration}
import scala.reflect.{ClassTag, classTag}
import scala.util.Try

private[configs] sealed abstract class MacroConfigsInstances {

  implicit def materializeConfigs[A]: Configs[A] = macro macros.ConfigsMacro.materialize[A]

}

private[configs] sealed abstract class ConfigsInstances0 extends MacroConfigsInstances {

  implicit def javaListConfigs[A: Configs]: Configs[ju.List[A]] =
    _.getList(_).map(Configs[A].extract).asJava

}

private[configs] abstract class ConfigsInstances extends ConfigsInstances0 {

  implicit def javaIterableConfigs[A](implicit C: Configs[ju.List[A]]): Configs[jl.Iterable[A]] =
    C.asInstanceOf[Configs[jl.Iterable[A]]]

  implicit def javaCollectionConfigs[A](implicit C: Configs[ju.List[A]]): Configs[ju.Collection[A]] =
    C.asInstanceOf[Configs[ju.Collection[A]]]

  implicit def javaSetConfigs[A](implicit C: Configs[ju.List[A]]): Configs[ju.Set[A]] =
    C.get(_, _).toSet.asJava

  implicit def javaMapConfigs[A, B](implicit A: Converter[String, A], B: Configs[B]): Configs[ju.Map[A, B]] =
    Configs.onPath { c =>
      c.root().keysIterator.map(k => A.convert(k) -> B.get(c, ConfigUtil.quoteString(k))).toMap.asJava
    }


  implicit def fromJListConfigs[F[_], A](implicit C: Configs[ju.List[A]], cbf: CanBuildFrom[Nothing, A, F[A]]): Configs[F[A]] =
    C.get(_, _).to[F]

  implicit def fromJMapConfigs[M[_, _], A, B](implicit C: Configs[ju.Map[A, B]], cbf: CanBuildFrom[Nothing, (A, B), M[A, B]]): Configs[M[A, B]] =
    C.get(_, _).to[({type F[_] = M[A, B]})#F]


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


  def fromConverter[A, B](implicit A: Configs[A], C: Converter[A, B]): Configs[B] =
    A.map(C.convert)

  def fromConverterJList[A, B](implicit A: Configs[ju.List[A]], C: Converter[A, B]): Configs[ju.List[B]] =
    A.map(_.map(C.convert))

  implicit def fromStringConfigs[A: Converter.FromString]: Configs[A] =
    fromConverter[String, A]

  implicit def fromStringJListConfigs[A: Converter.FromString]: Configs[ju.List[A]] =
    fromConverterJList[String, A]


  private[this] def toByte(n: Number, c: Config, p: String): Byte = {
    val l = n.longValue()
    if (l.isValidByte) l.toByte
    else throw new ConfigException.WrongType(c.origin(), p, "byte (8-bit integer)", s"out-of-range value $l")
  }

  implicit lazy val byteConfigs: Configs[Byte] =
    (c, p) => toByte(c.getNumber(p), c, p)

  implicit lazy val byteJListConfigs: Configs[ju.List[Byte]] =
    (c, p) => c.getNumberList(p).map(toByte(_, c, p))

  implicit lazy val javaByteConfigs: Configs[jl.Byte] =
    byteConfigs.asInstanceOf[Configs[jl.Byte]]

  implicit lazy val javaByteListConfigs: Configs[ju.List[jl.Byte]] =
    byteJListConfigs.asInstanceOf[Configs[ju.List[jl.Byte]]]


  private[this] def toShort(n: Number, c: Config, p: String): Short = {
    val l = n.longValue()
    if (l.isValidShort) l.toShort
    else throw new ConfigException.WrongType(c.origin(), p, "short (16-bit integer)", s"out-of-range value $l")
  }

  implicit lazy val shortConfigs: Configs[Short] =
    (c, p) => toShort(c.getNumber(p), c, p)

  implicit lazy val shortJListConfigs: Configs[ju.List[Short]] =
    (c, p) => c.getNumberList(p).map(toShort(_, c, p))

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


  private[this] def parseLong(s: String, c: Config, p: String): Long = {
    val n =
      try BigDecimal(s).toBigInt() catch {
        case e: NumberFormatException =>
          throw new ConfigException.WrongType(c.origin(), p, "long (64-bit integer)", s"STRING value $s", e)
      }
    if (n.isValidLong) n.longValue()
    else throw new ConfigException.WrongType(c.origin(), p, "long (64-bit integer)", s"out-of-range value $n")
  }

  implicit lazy val longConfigs: Configs[Long] =
    (c, p) => parseLong(c.getString(p), c, p)

  implicit lazy val longJListConfigs: Configs[ju.List[Long]] =
    (c, p) => c.getStringList(p).map(parseLong(_, c, p))

  implicit lazy val javaLongConfigs: Configs[jl.Long] =
    longConfigs.asInstanceOf[Configs[jl.Long]]

  implicit lazy val javaLongListConfigs: Configs[ju.List[jl.Long]] =
    longJListConfigs.asInstanceOf[Configs[ju.List[jl.Long]]]


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


  implicit lazy val bigIntConfigs: Configs[BigInt] =
    bigDecimalConfigs.map(_.toBigInt())

  implicit lazy val bigIntJListConfigs: Configs[ju.List[BigInt]] =
    bigDecimalJListConfigs.map(_.map(_.toBigInt()))

  implicit lazy val bigIntegerConfigs: Configs[jm.BigInteger] =
    javaBigDecimalConfigs.map(_.toBigInteger)

  implicit lazy val bigIntegerJListConfigs: Configs[ju.List[jm.BigInteger]] =
    javaBigDecimalListConfigs.map(_.map(_.toBigInteger))


  implicit lazy val bigDecimalConfigs: Configs[BigDecimal] =
    stringConfigs.map(BigDecimal.apply)

  implicit lazy val bigDecimalJListConfigs: Configs[ju.List[BigDecimal]] =
    stringJListConfigs.map(_.map(BigDecimal.apply))

  implicit lazy val javaBigDecimalConfigs: Configs[jm.BigDecimal] =
    stringConfigs.map(new jm.BigDecimal(_))

  implicit lazy val javaBigDecimalListConfigs: Configs[ju.List[jm.BigDecimal]] =
    stringJListConfigs.map(_.map(new jm.BigDecimal(_)))


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
      else throw new ConfigException.WrongType(c.origin(), p, "single BMP char", s"STRING value '$s'")
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


  implicit lazy val configConfigs: Configs[Config] = new Configs[Config] {
    def get(config: Config, path: String): Config = config.getConfig(path)

    override def extract(config: Config): Config = config
  }

  implicit lazy val configJListConfigs: Configs[ju.List[Config]] =
    _.getConfigList(_).asInstanceOf[ju.List[Config]]


  implicit lazy val configValueConfigs: Configs[ConfigValue] = new Configs[ConfigValue] {
    def get(config: Config, path: String): ConfigValue = config.getValue(path)

    override def extract(config: Config): ConfigValue = config.root()

    override def extract(value: ConfigValue): ConfigValue = value
  }

  implicit lazy val configValueJListConfigs: Configs[ju.List[ConfigValue]] =
    _.getList(_)

  implicit lazy val configValueJMapConfigs: Configs[ju.Map[String, ConfigValue]] =
    _.getObject(_)

  implicit def configValueJMapKeyConfigs[A](implicit A: Converter[String, A]): Configs[ju.Map[A, ConfigValue]] =
    Configs[ConfigObject].map(_.map(t => A.convert(t._1) -> t._2))


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


  implicit lazy val javaPropertiesConfigs: Configs[ju.Properties] =
    Configs[ju.Map[String, String]].map { m =>
      val p = new ju.Properties()
      p.putAll(m)
      p
    }

}
