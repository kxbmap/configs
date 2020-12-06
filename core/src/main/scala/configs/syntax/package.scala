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

import scala.jdk.CollectionConverters._

package object syntax {

  implicit class RichConfig(private val self: Config) extends AnyVal {

    def extract[A](implicit A: ConfigReader[A]): Result[A] =
      A.extract(self)

    def get[A](path: String)(implicit A: ConfigReader[A]): Result[A] =
      A.read(self, path)

    def getOrElse[A: ConfigReader](path: String, default: => A): Result[A] =
      get[Option[A]](path).map(_.getOrElse(default))

    def ++(that: Config): Config =
      that.withFallback(self)

  }

  implicit class RichConfigWriterInstance[A](private val self: A) extends AnyVal {

    def toConfigValue(implicit A: ConfigWriter[A]): ConfigValue =
      A.write(self)

  }

  implicit class RichConfigValue(private val self: ConfigValue) extends AnyVal {

    def extract[A](implicit A: ConfigReader[A]): Result[A] =
      A.extractValue(self)

    def withComments(comments: collection.Seq[String]): ConfigValue =
      self.withOrigin(self.origin().withComments(comments.asJava))

  }

  implicit class RichConfigList(private val self: ConfigList) extends AnyVal {

    def :+[A](value: A)(implicit A: ConfigWriter[A]): ConfigList =
      ConfigList.fromSeq(self.asScala :+ A.write(value)).value

    def +:[A](value: A)(implicit A: ConfigWriter[A]): ConfigList =
      ConfigList.fromSeq(A.write(value) +: self.asScala).value

    def ++[A](values: collection.Seq[A])(implicit A: ConfigWriter[A]): ConfigList =
      ConfigList.fromSeq(self.asScala ++ values.map(A.write)).value

    def ++(list: ConfigList): ConfigList =
      ConfigList.fromSeq(self.asScala ++ list.asScala).value

    def withComments(comments: collection.Seq[String]): ConfigList =
      self.withOrigin(self.origin().withComments(comments.asJava))

  }

  implicit class RichConfigObject(private val self: ConfigObject) extends AnyVal {

    def +[A, B](kv: (A, B))(implicit A: StringConverter[A], B: ConfigWriter[B]): ConfigObject =
      self.withValue(A.toString(kv._1), B.write(kv._2))

    def -[A](key: A)(implicit A: StringConverter[A]): ConfigObject =
      self.withoutKey(A.toString(key))

    def ++[A: StringConverter, B: ConfigWriter](kvs: collection.Seq[(A, B)]): ConfigObject =
      kvs.foldLeft(self)(_ + _)

    def ++[A: StringConverter, B: ConfigWriter](kvs: Map[A, B]): ConfigObject =
      kvs.foldLeft(self)(_ + _)

    def ++(obj: ConfigObject): ConfigObject =
      obj.asScala.foldLeft(self)(_ + _)

    def withComments(comments: collection.Seq[String]): ConfigObject =
      self.withOrigin(self.origin().withComments(comments.asJava))

  }


  implicit class RichConfigMemorySize(private val self: ConfigMemorySize) extends AnyVal {

    def value: BigInt = self.toBytesBigInteger

    def +(rhs: ConfigMemorySize): ConfigMemorySize = ConfigMemorySize(value + rhs.value)

    def -(rhs: ConfigMemorySize): ConfigMemorySize = ConfigMemorySize(value - rhs.value)

    def *(rhs: BigInt): ConfigMemorySize = ConfigMemorySize(value * rhs)

    def /(rhs: BigInt): ConfigMemorySize =
      if (self != ConfigMemorySize.Zero && rhs < BigInt(0))
        throw new IllegalArgumentException(s"divide by negative number: $rhs")
      else
        ConfigMemorySize(value / rhs)

    def <<(rhs: Int): ConfigMemorySize = ConfigMemorySize(value << rhs)

    def >>(rhs: Int): ConfigMemorySize = ConfigMemorySize(value >> rhs)

  }

  implicit class RichResult[A](private val self: Result[A]) extends AnyVal {

    def ~[X](x: Result[X]): ResultBuilder.Builder2[A, X] =
      new ResultBuilder.Builder2(self, x)

  }

}
