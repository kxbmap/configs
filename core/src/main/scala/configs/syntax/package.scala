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

package configs

import scala.collection.convert.decorateAsScala._

package object syntax {

  implicit class ConfigOps(private val self: Config) extends AnyVal {

    def extract[A](implicit A: Configs[A]): Result[A] =
      A.extract(self)

    def get[A](path: String)(implicit A: Configs[A]): Result[A] =
      A.get(self, path)

    def getOrElse[A](path: String, default: => A)(implicit A: Configs[Option[A]]): Result[A] =
      get(path)(A).map(_.getOrElse(default))

    def ++(that: Config): Config =
      that.withFallback(self)

  }

  implicit class ConfigListOps(private val self: ConfigList) extends AnyVal {

    def :+[A](value: A)(implicit A: ToConfig[A]): ConfigList =
      ConfigList.from(self.asScala :+ value)

    def +:[A](value: A)(implicit A: ToConfig[A]): ConfigList =
      ConfigList.from(value +: self.asScala)

    def ++[A](values: Seq[A])(implicit A: ToConfig[A]): ConfigList =
      ConfigList.from(self.asScala ++ values.map(A.toValue))

    def ++(list: ConfigList): ConfigList =
      ConfigList.from(self.asScala ++ list.asScala)

  }

  implicit class ConfigObjectOps(private val self: ConfigObject) extends AnyVal {

    def +[A, B](kv: (A, B))(implicit A: FromString[A], B: ToConfig[B]): ConfigObject =
      self.withValue(A.show(kv._1), B.toValue(kv._2))

    def -[A](key: A)(implicit A: FromString[A]): ConfigObject =
      self.withoutKey(A.show(key))

    def ++[A: FromString, B: ToConfig](kvs: Seq[(A, B)]): ConfigObject =
      kvs.foldLeft(self)(_ + _)

    def ++[A: FromString, B: ToConfig](kvs: Map[A, B]): ConfigObject =
      kvs.foldLeft(self)(_ + _)

    def ++(obj: ConfigObject): ConfigObject =
      ++(obj.asScala.toMap)

  }

  implicit lazy val configMemorySizeOrdering: Ordering[ConfigMemorySize] =
    Ordering.by(_.toBytes)

  implicit class ConfigMemorySizeOps(private val self: ConfigMemorySize) extends AnyVal {

    def value: Long = self.toBytes

    def +(rhs: ConfigMemorySize): ConfigMemorySize =
      ConfigMemorySize(self.toBytes + rhs.toBytes)

    def -(rhs: ConfigMemorySize): ConfigMemorySize =
      ConfigMemorySize(self.toBytes - rhs.toBytes)

    def *(rhs: Int): ConfigMemorySize =
      ConfigMemorySize(self.toBytes * rhs)

    def *(rhs: Long): ConfigMemorySize =
      ConfigMemorySize(self.toBytes * rhs)

    def *(rhs: Double): ConfigMemorySize =
      ConfigMemorySize((self.toBytes * rhs).toLong)

    def /(rhs: Int): ConfigMemorySize =
      ConfigMemorySize(self.toBytes / rhs)

    def /(rhs: Long): ConfigMemorySize =
      ConfigMemorySize(self.toBytes / rhs)

    def /(rhs: Double): ConfigMemorySize =
      ConfigMemorySize((self.toBytes / rhs).toLong)

    def /(rhs: ConfigMemorySize): Double =
      self.toBytes.toDouble / rhs.toBytes

    def <<(rhs: Int): ConfigMemorySize =
      ConfigMemorySize(self.toBytes << rhs)

    def <<(rhs: Long): ConfigMemorySize =
      ConfigMemorySize(self.toBytes << rhs)

    def >>(rhs: Int): ConfigMemorySize =
      ConfigMemorySize(self.toBytes >> rhs)

    def >>(rhs: Long): ConfigMemorySize =
      ConfigMemorySize(self.toBytes >> rhs)

    def >>>(rhs: Int): ConfigMemorySize =
      ConfigMemorySize(self.toBytes >>> rhs)

    def >>>(rhs: Long): ConfigMemorySize =
      ConfigMemorySize(self.toBytes >>> rhs)

  }

  implicit class ResultOps[A](private val self: Result[A]) extends AnyVal {

    def ~[X](x: Result[X]): ResultBuilder.Builder2[A, X] =
      new ResultBuilder.Builder2(self, x)

  }

}
