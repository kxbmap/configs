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

import com.typesafe.config.{ConfigUtil, ConfigValue}
import scalaprops.Property.forAll
import scalaprops.{Gen, Property}

package object util {

  val q = ConfigUtil.quoteString _


  implicit class UtilOps[A](private val self: A) {

    def configValue(implicit A: ConfigVal[A]): ConfigValue = A.configValue(self)

    def cv(implicit A: ConfigVal[A]): ConfigValue = configValue
  }


  private[util] def intercept0(block: => Unit)(cond: PartialFunction[Throwable, Boolean]): Boolean =
    try {
      block
      false
    } catch cond

  def intercept[A](block: => A)(cond: PartialFunction[Throwable, Boolean]): Property =
    forAll {
      intercept0(block)(cond)
    }

  def intercept[R, A1: Gen](block: A1 => R)(cond: PartialFunction[Throwable, Boolean]): Property =
    forAll { a1: A1 =>
      intercept0(block(a1))(cond)
    }

  def intercept[R, A1: Gen, A2: Gen](block: (A1, A2) => R)(cond: PartialFunction[Throwable, Boolean]): Property =
    forAll { (a1: A1, a2: A2) =>
      intercept0(block(a1, a2))(cond)
    }

}
