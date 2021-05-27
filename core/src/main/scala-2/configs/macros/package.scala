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

package object macros {

  final val MaxApplySize = 22
  final val MaxTupleSize = 22
  final val MaxSize = MaxApplySize * MaxTupleSize

  final val TypeKey = "type"

  def grouping[A](xs: List[A]): List[List[A]] = {
    val n = xs.length
    val t = (n + MaxTupleSize - 1) / MaxTupleSize
    val g = (n + t - 1) / t
    xs.grouped(g).toList
  }

}
