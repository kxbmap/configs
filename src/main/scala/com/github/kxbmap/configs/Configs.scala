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

import com.typesafe.config.Config
import scala.collection.JavaConversions._
import scala.reflect.{ClassTag, classTag}
import scala.util.Try
import scala.util.control.Exception._
import shapeless.TypeOperators._


trait Configs[T] {
  def get(config: Config): T
}

object Configs {
  @inline def of[T: Configs]: Configs[T] = implicitly[Configs[T]]

  def apply[T](f: Config => T): Configs[T] = new Configs[T] {
    def get(config: Config): T = f(config)
  }


  implicit val intAtPath:         AtPath[Int]           = AtPath { _.getInt(_) }
  implicit val intListAtPath:     AtPath[List[Int]]     = AtPath { _.getIntList(_).map(_.toInt).toList }
  implicit val longAtPath:        AtPath[Long]          = AtPath { _.getLong(_) }
  implicit val longListAtPath:    AtPath[List[Long]]    = AtPath { _.getLongList(_).map(_.toLong).toList }
  implicit val doubleAtPath:      AtPath[Double]        = AtPath { _.getDouble(_) }
  implicit val doubleListAtPath:  AtPath[List[Double]]  = AtPath { _.getDoubleList(_).map(_.toDouble).toList }
  implicit val booleanAtPath:     AtPath[Boolean]       = AtPath { _.getBoolean(_) }
  implicit val booleanListAtPath: AtPath[List[Boolean]] = AtPath { _.getBooleanList(_).map(_.booleanValue()).toList }
  implicit val stringAtPath:      AtPath[String]        = AtPath { _.getString(_) }
  implicit val stringListAtPath:  AtPath[List[String]]  = AtPath { _.getStringList(_).toList }

  implicit val configsIdentity: Configs[Config] = Configs { identity }

  def mapConfigs[K, T: AtPath](f: String => K): Configs[Map[K, T]] = Configs { c =>
    c.root().keysIterator.map { k => f(k) -> c.get[T](k) }.toMap
  }

  implicit def stringMapConfigs[T: AtPath]: Configs[Map[String, T]] = mapConfigs[String, T] { identity }
  implicit def symbolMapConfigs[T: AtPath]: Configs[Map[Symbol, T]] = mapConfigs[Symbol, T] { Symbol.apply }

  implicit val symbolAtPath:      AtPath[Symbol]        = AtPath { (c, p) => Symbol(c.getString(p)) }
  implicit val symbolListAtPath:  AtPath[List[Symbol]]  = AtPath { _.get[List[String]](_).map(Symbol.apply) }

  implicit def configsAtPath[T: Configs]: AtPath[T] = AtPath {
    _.getConfig(_).extract[T]
  }
  implicit def configsListAtPath[T: Configs]: AtPath[List[T]] = AtPath {
    _.getConfigList(_).map(_.extract[T]).toList
  }

  implicit def optionConfigs[T: Configs]: Configs[Option[T]] = Configs {
    nonFatalCatch opt _.extract[T]
  }
  implicit def optionAtPath[T: AtPath]: AtPath[Option[T]] = AtPath {
    nonFatalCatch opt _.get[T](_)
  }

  implicit def eitherConfigs[E <: Throwable: ClassTag, T: Configs]: Configs[Either[E, T]] = Configs { c =>
    (catching(classTag[E].runtimeClass) either c.extract[T]).left.map(_.asInstanceOf[E])
  }
  implicit def eitherAtPath[E <: Throwable: ClassTag, T: AtPath]: AtPath[Either[E, T]]  = AtPath { (c, p) =>
    (catching(classTag[E].runtimeClass) either c.get[T](p)).left.map(_.asInstanceOf[E])
  }

  implicit def throwableEitherConfigs[T: Configs]: Configs[Either[Throwable, T]] = Configs {
    nonFatalCatch either _.extract[T]
  }
  implicit def throwableEitherAtPath[T: AtPath]: AtPath[Either[Throwable, T]] = AtPath {
    nonFatalCatch either _.get[T](_)
  }

  implicit def tryConfigs[T: Configs]: Configs[Try[T]] = Configs {
    Try apply _.extract[T]
  }
  implicit def tryAtPath[T: AtPath]: AtPath[Try[T]] = AtPath {
    Try apply _.get[T](_)
  }

  implicit val bytesAtPath: AtPath[Long @@ Bytes] = AtPath { (c, p) =>
    Bytes(c.getBytes(p).toLong)
  }
  implicit val bytesListAtPath: AtPath[List[Long @@ Bytes]] = AtPath {
    _.getBytesList(_).map { bs => Bytes(bs.toLong) }.toList
  }

  implicit val millisecondsAtPath: AtPath[Long @@ Milliseconds] = AtPath { (c, p) =>
    Milliseconds(c.getMilliseconds(p).toLong)
  }
  implicit val millisecondsListAtPath: AtPath[List[Long @@ Milliseconds]] = AtPath {
    _.getMillisecondsList(_).map { ms => Milliseconds(ms.toLong) }.toList
  }

  implicit val nanosecondsAtPath: AtPath[Long @@ Nanoseconds] = AtPath { (c, p) =>
    Nanoseconds(c.getNanoseconds(p).toLong)
  }
  implicit val nanosecondsListAtPath: AtPath[List[Long @@ Nanoseconds]] = AtPath {
    _.getNanosecondsList(_).map { ns => Nanoseconds(ns.toLong) }.toList
  }

}
