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

import com.typesafe.config.{Config, ConfigException, ConfigValue}
import scala.collection.generic.CanBuildFrom
import scala.util.control.NonFatal


trait Configs[A] {

  def get(config: Config, path: String): A

  def extract(config: Config): A = get(config.atKey(Configs.ExtractKey), Configs.ExtractKey)

  def extract(value: ConfigValue): A = get(value.atKey(Configs.ExtractKey), Configs.ExtractKey)

  def map[B](f: A => B): Configs[B] = Configs.from(get(_, _) |> f)

  def mapF[F[_], B, C](f: B => C)(implicit ev1: A =:= F[B], ev2: F[B] => Traversable[B], cbf: CanBuildFrom[Nothing, C, F[C]]): Configs[F[C]] =
    map(ev1(_).map(f)(collection.breakOut))

  def flatMap[B](f: A => Configs[B]): Configs[B] = Configs.from((c, p) => f(get(c, p)).get(c, p))

  def orElse[B >: A](other: Configs[B]): Configs[B] = (c, p) =>
    try
      get(c, p)
    catch {
      case suppress: ConfigException =>
        try
          other.get(c, p)
        catch {
          case NonFatal(e) =>
            e.addSuppressed(suppress)
            throw e
        }
    }
}

object Configs extends ConfigsInstances {

  private final val ExtractKey = "configs-extract"

  @inline
  def apply[A](implicit A: Configs[A]): Configs[A] = A


  def of[A]: Configs[A] =
    macro macros.ConfigsMacro.materialize[A]

  def bean[A]: Configs[A] =
    macro macros.BeanConfigsMacro.materializeA[A]

  def bean[A](newInstance: => A): Configs[A] =
    macro macros.BeanConfigsMacro.materializeI[A]


  def from[A](f: (Config, String) => A): Configs[A] = (c, p) =>
    try
      f(c, p)
    catch {
      case e: ConfigException => throw e
      case NonFatal(e)        => throw new ConfigException.BadValue(c.origin(), p, e.getMessage, e)
    }

  def onPath[A](f: Config => A): Configs[A] = from(_.getConfig(_) |> f)


  final class MapF[F[_], A, B] private(c: Configs[F[A]])(implicit ev: F[A] => Traversable[A], cbf: CanBuildFrom[Nothing, B, F[B]]) {
    def apply(f: A => B): Configs[F[B]] = c.mapF(f)
  }

  object MapF {
    implicit def mkMapF[F[_], A, B](implicit c: Configs[F[A]], ev: F[A] => Traversable[A], cbf: CanBuildFrom[Nothing, B, F[B]]): MapF[F, A, B] =
      new MapF(c)
  }

}
