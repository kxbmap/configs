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

import configs.testutil.instance.config._
import configs.testutil.instance.string._
import configs.testutil.instance.symbol._
import configs.{ConfigObject, ConfigValue}
import scala.collection.JavaConverters._
import scalaprops.Property.forAll
import scalaprops.Scalaprops
import scalaz.Monoid
import scalaz.syntax.equal._

object RichConfigObjectTest extends Scalaprops {

  val + = forAll { (co: ConfigObject, k: Symbol, v: Int) =>
    val result = co + (k -> v)
    ConfigValue.fromAny(v).exists(_ === result.get(k.name))
  }

  val - = forAll { (co: ConfigObject, d: Symbol) =>
    val k = co.keySet().asScala.headOption.fold(d)(Symbol(_))
    (co.isEmpty || co.get(k.name) != null) && {
      val result = co - k
      result.get(k.name) == null
    }
  }

  val `++ Seq` =
    forAll { (co: ConfigObject, kvs: List[(Symbol, Int)], dupSize: Int) =>
      val dup = co.asScala.keys.take(dupSize).map(Symbol(_) -> 42).toSeq
      val result = co ++ kvs ++ dup
      (kvs ++ dup).groupBy(_._1).forall {
        case (k, vs) => ConfigValue.fromAny(vs.last._2).exists(_ === result.get(k.name))
        case _ => true
      }
    }

  val `++ Map` =
    forAll { (co: ConfigObject, kvs: Map[Symbol, Int], dupSize: Int) =>
      val dup = co.asScala.keys.take(dupSize).map(Symbol(_) -> 42).toMap
      val result = co ++ kvs ++ dup
      (kvs ++ dup).forall {
        case (k, v) => ConfigValue.fromAny(v).exists(_ === result.get(k.name))
      }
    }

  val `++ ConfigObject` = forAll { (co1: ConfigObject, co2: ConfigObject) =>
    val result = co1 ++ co2
    co2.asScala.forall {
      case (k, v) => result.get(k) === v
    }
  }

  val withComments = forAll { (co: ConfigObject, xs: List[String]) =>
    val wc: ConfigObject = co.withComments(xs)
    wc.origin().comments() == xs.asJava
  }

  implicit lazy val configObjectMonoid: Monoid[ConfigObject] =
    Monoid.instance(_ ++ _, ConfigObject.empty)

  val `++/empty monoid` = scalaprops.scalazlaws.monoid.all[ConfigObject]

}
