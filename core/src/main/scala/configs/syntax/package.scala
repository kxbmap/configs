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

import java.lang.Long.{highestOneBit, numberOfTrailingZeros}
import scala.collection.convert.decorateAsScala._

package object syntax {

  implicit class EnrichConfig(private val self: Config) extends AnyVal {

    def extract[A](implicit A: Configs[A]): Result[A] =
      A.extract(self)

    def get[A](path: String)(implicit A: Configs[A]): Result[A] =
      A.get(self, path)

    def getOrElse[A](path: String, default: => A)(implicit A: Configs[Option[A]]): Result[A] =
      get(path)(A).map(_.getOrElse(default))

    def ++(that: Config): Config =
      that.withFallback(self)

  }

  implicit class EnrichConfigList(private val self: ConfigList) extends AnyVal {

    def :+[A](value: A)(implicit A: ToConfig[A]): ConfigList =
      ConfigList.from(self.asScala :+ A.toValue(value))

    def +:[A](value: A)(implicit A: ToConfig[A]): ConfigList =
      ConfigList.from(A.toValue(value) +: self.asScala)

    def ++[A](values: Seq[A])(implicit A: ToConfig[A]): ConfigList =
      ConfigList.from(self.asScala ++ values.map(A.toValue))

    def ++(list: ConfigList): ConfigList =
      ConfigList.from(self.asScala ++ list.asScala)

  }

  implicit class EnrichConfigObject(private val self: ConfigObject) extends AnyVal {

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

  implicit class EnrichConfigMemorySize(private val self: ConfigMemorySize) extends AnyVal {

    def value: Long = self.toBytes

    def +(rhs: ConfigMemorySize): ConfigMemorySize =
      (self.toBytes, rhs.toBytes) match {
        case (0, _) => rhs
        case (_, 0) => self
        case (a, b) => ConfigMemorySize(Math.addExact(a, b))
      }

    def -(rhs: ConfigMemorySize): ConfigMemorySize =
      rhs.toBytes match {
        case 0 => self
        case n => ConfigMemorySize(Math.subtractExact(self.toBytes, n))
      }

    def *(rhs: Int): ConfigMemorySize =
      (self.toBytes, rhs) match {
        case (_, 1) => self
        case (0, _) | (_, 0) => ConfigMemorySize.Zero
        case (_, b) if b < 0 => throw new IllegalArgumentException(s"multiply by negative number: $b")
        case (1, b) => ConfigMemorySize(b)
        case (a, b) => ConfigMemorySize(Math.multiplyExact(a, b))
      }

    def *(rhs: Long): ConfigMemorySize =
      (self.toBytes, rhs) match {
        case (_, 1) => self
        case (0, _) | (_, 0) => ConfigMemorySize.Zero
        case (_, b) if b < 0L => throw new IllegalArgumentException(s"multiply by negative number: $b")
        case (1, b) => ConfigMemorySize(b)
        case (a, b) => ConfigMemorySize(Math.multiplyExact(a, b))
      }

    def *(rhs: Double): ConfigMemorySize =
      (self.toBytes, rhs) match {
        case (_, 1.0d) => self
        case (_, b) if b.isNaN || b.isInfinity => throw new IllegalArgumentException(s"multiply by $b")
        case (0, _) => ConfigMemorySize.Zero
        case (_, b) if Math.signum(b) == -1.0 => throw new IllegalArgumentException(s"multiply by negative number: $b")
        case (a, b) =>
          val r = a * b
          if (r > Long.MaxValue) throw new ArithmeticException("long overflow")
          ConfigMemorySize(r.toLong)
      }

    def /(rhs: Int): ConfigMemorySize =
      (self.toBytes, rhs) match {
        case (_, 1) => self
        case (0, b) if b != 0 => ConfigMemorySize.Zero
        case (_, b) if b < 0 => throw new IllegalArgumentException(s"divide by negative number: $b")
        case (a, b) if a < b => ConfigMemorySize.Zero
        case (a, b) => ConfigMemorySize(a / b)
      }

    def /(rhs: Long): ConfigMemorySize =
      (self.toBytes, rhs) match {
        case (_, 1) => self
        case (0, b) if b != 0 => ConfigMemorySize.Zero
        case (_, b) if b < 0 => throw new IllegalArgumentException(s"divide by negative number: $b")
        case (a, b) if a < b => ConfigMemorySize.Zero
        case (a, b) => ConfigMemorySize(a / b)
      }

    def /(rhs: Double): ConfigMemorySize =
      (self.toBytes, rhs) match {
        case (_, 1.0d) => self
        case (_, b) if b.isNaN || b.isInfinity => throw new IllegalArgumentException(s"divide by $b")
        case (0, b) if b != 0.0 => ConfigMemorySize.Zero
        case (_, b) if java.lang.Double.compare(b, 0.0) == -1 =>
          throw new IllegalArgumentException(s"divide by negative number: $b")
        case (a, b) =>
          val r = a / b
          if (r > Long.MaxValue) throw new ArithmeticException("long overflow")
          ConfigMemorySize(r.toLong)
      }

    def /(rhs: ConfigMemorySize): Double =
      self.toBytes.toDouble / rhs.toBytes

    def <<(rhs: Int): ConfigMemorySize =
      (self.toBytes, rhs) match {
        case (0, _) | (_, 0) => self
        case (a, b) if (b & 0x3f) + numberOfTrailingZeros(highestOneBit(a)) > 62 =>
          throw new ArithmeticException("long overflow")
        case (a, b) => ConfigMemorySize(a << b)
      }

    def <<(rhs: Long): ConfigMemorySize =
      (self.toBytes, rhs) match {
        case (0, _) | (_, 0) => self
        case (a, b) if (b & 0x3f) + numberOfTrailingZeros(highestOneBit(a)) > 62 =>
          throw new ArithmeticException("long overflow")
        case (a, b) => ConfigMemorySize(a << b)
      }

    def >>(rhs: Int): ConfigMemorySize =
      ConfigMemorySize(self.toBytes >> rhs)

    def >>(rhs: Long): ConfigMemorySize =
      ConfigMemorySize(self.toBytes >> rhs)

    def >>>(rhs: Int): ConfigMemorySize =
      ConfigMemorySize(self.toBytes >>> rhs)

    def >>>(rhs: Long): ConfigMemorySize =
      ConfigMemorySize(self.toBytes >>> rhs)

  }

  implicit class EnrichResult[A](private val self: Result[A]) extends AnyVal {

    def ~[X](x: Result[X]): ResultBuilder.Builder2[A, X] =
      new ResultBuilder.Builder2(self, x)

  }

}
