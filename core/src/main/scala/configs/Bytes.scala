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

final case class Bytes(value: Long) extends Ordered[Bytes] {

  def compare(rhs: Bytes): Int = value.compare(rhs.value)

  def +(rhs: Bytes): Bytes = Bytes(value + rhs.value)

  def -(rhs: Bytes): Bytes = Bytes(value - rhs.value)

  def *(rhs: Int): Bytes = Bytes(value * rhs)

  def *(rhs: Long): Bytes = Bytes(value * rhs)

  def *(rhs: Double): Bytes = Bytes((value * rhs).toLong)

  def /(rhs: Int): Bytes = Bytes(value / rhs)

  def /(rhs: Long): Bytes = Bytes(value / rhs)

  def /(rhs: Double): Bytes = Bytes((value / rhs).toLong)

  def /(rhs: Bytes): Double = value.toDouble / rhs.value

  def unary_+ : Bytes = this

  def unary_- : Bytes = Bytes(-value)

  def <<(rhs: Int): Bytes = Bytes(value << rhs)

  def <<(rhs: Long): Bytes = Bytes(value << rhs)

  def >>(rhs: Int): Bytes = Bytes(value >> rhs)

  def >>(rhs: Long): Bytes = Bytes(value >> rhs)

  def >>>(rhs: Int): Bytes = Bytes(value >>> rhs)

  def >>>(rhs: Long): Bytes = Bytes(value >>> rhs)

}

object Bytes {

  final val Zero = Bytes(0)

  final val MinValue = Bytes(Long.MinValue)
  final val MaxValue = Bytes(Long.MaxValue)

  implicit lazy val bytesConfigs: Configs[Bytes] =
    Configs.fromTry(_.getBytes(_)).map(Bytes(_))

  implicit lazy val bytesToConfig: ToConfig[Bytes] =
    ToConfig.by(_.value)

  implicit lazy val bytesOrdering: Ordering[Bytes] =
    Ordering.by(_.value)

  implicit class BytesMultiplication(val factor: Double) extends AnyVal {
    def *(bytes: Bytes): Bytes = bytes * factor
  }

}
