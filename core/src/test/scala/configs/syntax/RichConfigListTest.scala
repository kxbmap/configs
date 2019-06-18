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

package configs.syntax

import configs.internal.CollectionConverters._
import configs.testutil.instance.config._
import configs.testutil.instance.result._
import configs.testutil.instance.string._
import configs.{ConfigList, ConfigValue}
import scalaprops.Property.forAll
import scalaprops.Scalaprops
import scalaz.Monoid
import scalaz.syntax.equal._

object RichConfigListTest extends Scalaprops {

  val :+ = forAll { (list: ConfigList, v: Int) =>
    val result = list :+ v
    ConfigValue.fromAny(v).exists(_ === result.get(result.size() - 1)) &&
      ConfigList.fromSeq(result.asScala.init).exists(_ === list)
  }

  val +: = forAll { (list: ConfigList, v: Int) =>
    val result = v +: list
    ConfigValue.fromAny(v).exists(_ === result.get(0)) &&
      ConfigList.fromSeq(result.asScala.tail).exists(_ === list)
  }

  val `++ Seq` = forAll { (xs: ConfigList, ys: List[Int]) =>
    val result = xs ++ ys
    ConfigList.fromSeq(result.asScala.take(xs.size())).exists(_ === xs) &&
      ConfigList.fromSeq(result.asScala.drop(xs.size())) === ConfigList.fromSeq(ys)
  }

  val `++ ConfigList` = forAll { (xs: ConfigList, ys: ConfigList) =>
    val result = xs ++ ys
    ConfigList.fromSeq(result.asScala.take(xs.size())).exists(_ === xs) &&
      ConfigList.fromSeq(result.asScala.drop(xs.size())).exists(_ === ys)
  }

  val `++/empty monoid` = {
    implicit val m: Monoid[ConfigList] = Monoid.instance(_ ++ _, ConfigList.empty)
    scalaprops.scalazlaws.monoid.all[ConfigList]
  }

  val withComments = forAll { (cl: ConfigList, xs: List[String]) =>
    val wc: ConfigList = cl.withComments(xs)
    wc.origin().comments() == xs.asJava
  }

}
