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
import scala.collection.JavaConverters._

package object syntax {

  implicit class EnrichConfig(private val self: Config) extends AnyVal {

    def extract[A](implicit A: Configs[A]): Result[A] =
      A.extract(self)

    def get[A](path: String)(implicit A: Configs[A]): Result[A] =
      A.get(self, path)

    def getOrElse[A: Configs](path: String, default: => A): Result[A] =
      get[Option[A]](path).map(_.getOrElse(default))

    def getWithOrigin[A: Configs](path: String): Result[(A, ConfigOrigin)] =
      get[(A, ConfigOrigin)](path)

    def ++(that: Config): Config =
      that.withFallback(self)

  }

  implicit class EnrichConfigList(private val self: ConfigList) extends AnyVal {

    def :+[A](value: A)(implicit A: ConfigWriter[A]): ConfigList =
      ConfigList.from(self.asScala :+ A.write(value))

    def +:[A](value: A)(implicit A: ConfigWriter[A]): ConfigList =
      ConfigList.from(A.write(value) +: self.asScala)

    def ++[A](values: Seq[A])(implicit A: ConfigWriter[A]): ConfigList =
      ConfigList.from(self.asScala ++ values.map(A.write))

    def ++(list: ConfigList): ConfigList =
      ConfigList.from(self.asScala ++ list.asScala)

  }

  implicit class EnrichConfigObject(private val self: ConfigObject) extends AnyVal {

    def +[A, B](kv: (A, B))(implicit A: StringConverter[A], B: ConfigWriter[B]): ConfigObject =
      self.withValue(A.to(kv._1), B.write(kv._2))

    def -[A](key: A)(implicit A: StringConverter[A]): ConfigObject =
      self.withoutKey(A.to(key))

    def ++[A: StringConverter, B: ConfigWriter](kvs: Seq[(A, B)]): ConfigObject =
      kvs.foldLeft(self)(_ + _)

    def ++[A: StringConverter, B: ConfigWriter](kvs: Map[A, B]): ConfigObject =
      kvs.foldLeft(self)(_ + _)

    def ++(obj: ConfigObject): ConfigObject =
      obj.asScala.foldLeft(self)(_ + _)

  }


  implicit lazy val memorySizeOrdering: Ordering[MemorySize] =
    Ordering.by(_.toBytes)

  implicit class EnrichMemorySize(private val self: MemorySize) extends AnyVal {

    def value: Long = self.toBytes

    def +(rhs: MemorySize): MemorySize =
      (self.toBytes, rhs.toBytes) match {
        case (0, _) => rhs
        case (_, 0) => self
        case (a, b) => MemorySize(Math.addExact(a, b))
      }

    def -(rhs: MemorySize): MemorySize =
      rhs.toBytes match {
        case 0 => self
        case n => MemorySize(Math.subtractExact(self.toBytes, n))
      }

    def *(rhs: Int): MemorySize =
      (self.toBytes, rhs) match {
        case (_, 1) => self
        case (0, _) | (_, 0) => MemorySize.Zero
        case (_, b) if b < 0 => throw new IllegalArgumentException(s"multiply by negative number: $b")
        case (1, b) => MemorySize(b)
        case (a, b) => MemorySize(Math.multiplyExact(a, b))
      }

    def *(rhs: Long): MemorySize =
      (self.toBytes, rhs) match {
        case (_, 1) => self
        case (0, _) | (_, 0) => MemorySize.Zero
        case (_, b) if b < 0L => throw new IllegalArgumentException(s"multiply by negative number: $b")
        case (1, b) => MemorySize(b)
        case (a, b) => MemorySize(Math.multiplyExact(a, b))
      }

    def *(rhs: Double): MemorySize =
      (self.toBytes, rhs) match {
        case (_, 1.0d) => self
        case (_, b) if b.isNaN || b.isInfinity => throw new IllegalArgumentException(s"multiply by $b")
        case (0, _) => MemorySize.Zero
        case (_, b) if Math.signum(b) == -1.0 => throw new IllegalArgumentException(s"multiply by negative number: $b")
        case (a, b) =>
          val r = a * b
          if (r > Long.MaxValue) throw new ArithmeticException("long overflow")
          MemorySize(r.toLong)
      }

    def /(rhs: Int): MemorySize =
      (self.toBytes, rhs) match {
        case (_, 1) => self
        case (0, b) if b != 0 => MemorySize.Zero
        case (_, b) if b < 0 => throw new IllegalArgumentException(s"divide by negative number: $b")
        case (a, b) if a < b => MemorySize.Zero
        case (a, b) => MemorySize(a / b)
      }

    def /(rhs: Long): MemorySize =
      (self.toBytes, rhs) match {
        case (_, 1) => self
        case (0, b) if b != 0 => MemorySize.Zero
        case (_, b) if b < 0 => throw new IllegalArgumentException(s"divide by negative number: $b")
        case (a, b) if a < b => MemorySize.Zero
        case (a, b) => MemorySize(a / b)
      }

    def /(rhs: Double): MemorySize =
      (self.toBytes, rhs) match {
        case (_, 1.0d) => self
        case (_, b) if b.isNaN || b.isInfinity => throw new IllegalArgumentException(s"divide by $b")
        case (0, b) if b != 0.0 => MemorySize.Zero
        case (_, b) if java.lang.Double.compare(b, 0.0) == -1 =>
          throw new IllegalArgumentException(s"divide by negative number: $b")
        case (a, b) =>
          val r = a / b
          if (r > Long.MaxValue) throw new ArithmeticException("long overflow")
          MemorySize(r.toLong)
      }

    def /(rhs: MemorySize): Double =
      self.toBytes.toDouble / rhs.toBytes

    def <<(rhs: Int): MemorySize =
      (self.toBytes, rhs) match {
        case (0, _) | (_, 0) => self
        case (a, b) if (b & 0x3f) + numberOfTrailingZeros(highestOneBit(a)) > 62 =>
          throw new ArithmeticException("long overflow")
        case (a, b) => MemorySize(a << b)
      }

    def <<(rhs: Long): MemorySize =
      (self.toBytes, rhs) match {
        case (0, _) | (_, 0) => self
        case (a, b) if (b & 0x3f) + numberOfTrailingZeros(highestOneBit(a)) > 62 =>
          throw new ArithmeticException("long overflow")
        case (a, b) => MemorySize(a << b)
      }

    def >>(rhs: Int): MemorySize =
      MemorySize(self.toBytes >> rhs)

    def >>(rhs: Long): MemorySize =
      MemorySize(self.toBytes >> rhs)

    def >>>(rhs: Int): MemorySize =
      MemorySize(self.toBytes >>> rhs)

    def >>>(rhs: Long): MemorySize =
      MemorySize(self.toBytes >>> rhs)

    def asBytes: Bytes = Bytes(self.toBytes)

  }

  implicit class EnrichResult[A](private val self: Result[A]) extends AnyVal {

    def ~[X](x: Result[X]): ResultBuilder.Builder2[A, X] =
      new ResultBuilder.Builder2(self, x)

  }

  implicit class EnrichStringConverter[A](self: A)(implicit A: StringConverter[A]) {

    def :=[B](value: B)(implicit B: ConfigWriter[B]): ConfigKeyValue =
      ConfigKeyValue(A.to(self), B.write(value))

  }

}
