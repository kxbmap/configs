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

import com.typesafe.config.ConfigFactory
import configs.testutil.fun._
import configs.testutil.instance.anyVal._
import configs.testutil.instance.string._
import configs.testutil.instance.tuple._
import scalaprops.Property.forAll
import scalaprops.{Gen, Lazy, Properties, Scalaprops}
import scalaprops.ScalapropsScalaz._
import scalaz.{Apply, Equal}

// Note:
// the following case class needs to be declared outside and before DeriveTest object, because otherwise the generation
// of default value functions in scala 2.11 might occur after macro expansion
case class OptionDefault(opt: Option[Int] = Some(42))
object OptionDefault {
  implicit lazy val equal: Equal[OptionDefault] =
    Equal.equalA[OptionDefault]
}

object DeriveTest extends Scalaprops {

  case class CC0()

  object CC0 {
    implicit lazy val gen: Gen[CC0] =
      Gen.value(CC0())

    implicit lazy val equal: Equal[CC0] =
      Equal.equalA[CC0]
  }

  val caseClass0 = check[CC0]


  case class CC1(a1: Int)

  object CC1 {
    implicit lazy val gen: Gen[CC1] =
      Gen[Int].map(CC1.apply)

    implicit lazy val equal: Equal[CC1] =
      Equal.equalA[CC1]
  }

  val caseClass1 = check[CC1]


  case class CC22(
      a1: Int, a2: Int, a3: Int, a4: Int, a5: Int, a6: Int, a7: Int, a8: Int, a9: Int, a10: Int,
      a11: Int, a12: Int, a13: Int, a14: Int, a15: Int, a16: Int, a17: Int, a18: Int, a19: Int, a20: Int,
      a21: Int, a22: Int)

  object CC22 {
    implicit lazy val gen: Gen[CC22] =
      Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)]
        .map((CC22.apply _).tupled)

    implicit lazy val equal: Equal[CC22] =
      Equal.equalA[CC22]
  }

  val caseClass22 = check[CC22]


  case class CC23(
      a1: Int, a2: Int, a3: Int, a4: Int, a5: Int, a6: Int, a7: Int, a8: Int, a9: Int, a10: Int,
      a11: Int, a12: Int, a13: Int, a14: Int, a15: Int, a16: Int, a17: Int, a18: Int, a19: Int, a20: Int,
      a21: Int, a22: Int, a23: Int)

  object CC23 {
    implicit lazy val gen: Gen[CC23] =
      Apply[Gen].apply2(
        Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)],
        Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)]
      )((t1, t2) => CC23(
        t1._1, t1._2, t1._3, t1._4, t1._5, t1._6, t1._7, t1._8, t1._9, t1._10, t1._11, t1._12,
        t2._1, t2._2, t2._3, t2._4, t2._5, t2._6, t2._7, t2._8, t2._9, t2._10, t2._11))

    implicit lazy val equal: Equal[CC23] =
      Equal.equalA[CC23]
  }

  val caseClass23 = check[CC23]


  sealed trait Sealed

  case class SealedCase(a1: Int) extends Sealed

  case object SealedCaseObject extends Sealed

  sealed abstract class SealedSealed extends Sealed

  case class SealedSealedClass(a1: Int) extends SealedSealed

  object Sealed {
    // SI-7046
    implicit lazy val reader: ConfigReader[Sealed] = ConfigReader.derive[Sealed]
    implicit lazy val writer: ConfigWriter[Sealed] = ConfigWriter.derive[Sealed]

    implicit lazy val gen: Gen[Sealed] =
      Gen.oneOf(
        Gen[Int].map(SealedCase.apply),
        Gen[Int].map(SealedSealedClass.apply),
        Gen.value(SealedCaseObject)
      )

    implicit lazy val equal: Equal[Sealed] =
      Equal.equalBy {
        case SealedCase(a1) => ("CA", a1)
        case SealedCaseObject => ("CO", 0)
        case SealedSealedClass(a1) => ("SS", a1)
      }
  }

  val sealedClass = check[Sealed]


  case class Default1(a1: Int = 1)

  object Default1 {
    implicit lazy val equal: Equal[Default1] =
      Equal.equalA[Default1]
  }

  val default1 = {
    val p1 = {
      implicit val gen: Gen[Default1] = Gen[Int].map(Default1.apply)
      check[Default1]("w/o default")
    }
    val p2 = {
      implicit val gen: Gen[Default1] = Gen.value(Default1())
      implicit val writer: ConfigWriter[Default1] = _ => ConfigObject.empty
      check[Default1]("w/ default")
    }
    p1 x p2
  }

  val optionDefault = {
    val p1 =
      Properties.single("missing", forAll {
        ConfigReader[OptionDefault].extract(Config.empty).contains(OptionDefault(Some(42)))
      })
    val p2 =
      Properties.single("null", forAll {
        val config = ConfigFactory.parseString("opt = null")
        ConfigReader[OptionDefault].extract(config).contains(OptionDefault(None))
      })
    p1 x p2
  }


  sealed trait Recursive

  case class RCons(head: Int, tail: Recursive) extends Recursive

  case object RNil extends Recursive

  object Recursive {
    implicit lazy val reader: ConfigReader[Recursive] = ConfigReader.derive[Recursive]
    implicit lazy val writer: ConfigWriter[Recursive] = ConfigWriter.derive[Recursive]

    implicit lazy val gen: Gen[Recursive] =
      Gen.oneOfLazy(
        Lazy(Apply[Gen].apply2(Gen[Int], Gen[Recursive])(RCons.apply)),
        Lazy(Gen.value(RNil))
      )

    implicit lazy val equal: Equal[Recursive] =
      Equal.equalA[Recursive]
  }

  val recursive = check[Recursive]


  case class RecursiveOpt(value: Option[RecursiveOpt])

  object RecursiveOpt {
    implicit lazy val reader: ConfigReader[RecursiveOpt] =
      ConfigReader.derive[RecursiveOpt]

    implicit lazy val writer: ConfigWriter[RecursiveOpt] =
      ConfigWriter.derive[RecursiveOpt]

    implicit lazy val gen: Gen[RecursiveOpt] =
      Gen.oneOfLazy(
        Lazy(Gen[Option[RecursiveOpt]].map(RecursiveOpt.apply))
      )

    implicit lazy val equal: Equal[RecursiveOpt] =
      Equal.equalA[RecursiveOpt]
  }

  val recursiveOption = check[RecursiveOpt]


  case class HyphenSeparated(normalParam: Int, paramWithDefault: Int = 1)

  object HyphenSeparated {
    implicit val equal: Equal[HyphenSeparated] =
      Equal.equalA[HyphenSeparated]

    implicit val gen: Gen[HyphenSeparated] =
      Gen[(Int, Int)].map {
        case (a, b) => HyphenSeparated(a, b)
      }
  }

  val hyphenSeparatedPath = check[HyphenSeparated]


  val localCaseClass = {
    case class LocalCC(a1: Int)
    implicit val gen: Gen[LocalCC] = Gen[Int].map(LocalCC.apply)
    implicit val equal: Equal[LocalCC] = Equal.equalA[LocalCC]
    check[LocalCC]
  }


  class ValueClass(val value: Int) extends AnyVal

  object ValueClass {
    implicit val equal: Equal[ValueClass] =
      Equal.equalBy(_.value)

    implicit val gen: Gen[ValueClass] =
      Gen[Int].map(new ValueClass(_))
  }

  val valueClass = check[ValueClass]

}
