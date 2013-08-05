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


case class Bytes(value: Long) extends Ordered[Bytes] {
  def compare(that: Bytes): Int = value compare that.value

  def +(other: Bytes): Bytes = Bytes(value + other.value)
  def -(other: Bytes): Bytes = Bytes(value - other.value)
  def *(factor: Double): Bytes = Bytes((value * factor).toLong)
  def /(divisor: Double): Bytes = Bytes((value / divisor).toLong)
  def /(other: Bytes): Double = value / other.value.toDouble
  def unary_+ : Bytes = this
  def unary_- : Bytes = Bytes(-value)
}


object Bytes {

  implicit val bytesAtPath: AtPath[Bytes] = Configs.atPath {
    Bytes apply _.getBytes(_)
  }
  implicit val bytesListAtPath: AtPath[List[Bytes]] = Configs.atPath {
    import scala.collection.JavaConversions._
    _.getBytesList(_).map(Bytes(_)).toList
  }

  implicit val bytesOrdering: Ordering[Bytes] = Ordering.by(_.value)

  final implicit class BytesMultiplication(val factor: Double) extends AnyVal {
    def *(bytes: Bytes): Bytes = bytes * factor
  }

}
