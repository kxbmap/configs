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
import configs.util._
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.anyVal._
import scalaz.std.string._
import scalaz.std.tuple._
import scalaz.syntax.equal._
import scalaz.{Apply, Equal}


object DeriveConfigsTest extends Scalaprops {

  def checkDerived[A: Gen : Configs : ToConfigValue : Equal] =
    forAll { a: A =>
      val actual = Configs[A].extract(a.toConfigValue)
      val result = actual.exists(_ === a)
      if (!result) {
        println(s"\nactual: $actual, expected value: $a")
      }
      result
    }


  case class CC0()

  object CC0 {
    implicit lazy val gen: Gen[CC0] =
      Gen.elements(CC0())

    implicit lazy val equal: Equal[CC0] =
      Equal.equalA[CC0]

    implicit lazy val tcv: ToConfigValue[CC0] =
      _ => ConfigFactory.empty().root()
  }

  val caseClass0 = checkDerived[CC0]


  case class CC1(a1: Int)

  object CC1 {
    implicit lazy val gen: Gen[CC1] =
      Gen[Int].map(CC1.apply)

    implicit lazy val equal: Equal[CC1] =
      Equal.equalA[CC1]

    implicit lazy val tcv: ToConfigValue[CC1] =
      ToConfigValue.fromMap {
        case CC1(a1) => Map("a1" -> a1.toConfigValue)
      }
  }

  val caseClass1 = checkDerived[CC1]


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

    implicit lazy val tcv: ToConfigValue[CC22] =
      ToConfigValue.fromMap(
        _.productIterator.zipWithIndex.map {
          case (a, i) => s"a${i + 1}" -> a.asInstanceOf[Int].toConfigValue
        }.toMap)
  }

  val caseClass22 = checkDerived[CC22]


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

    implicit lazy val tcv: ToConfigValue[CC23] =
      ToConfigValue.fromMap(
        _.productIterator.zipWithIndex.map {
          case (a, i) => s"a${i + 1}" -> a.asInstanceOf[Int].toConfigValue
        }.toMap)
  }

  val caseClass23 = checkDerived[CC23]


  case class SubApply(a1: Int, a2: Int)

  object SubApply {

    def apply(s1: String, s2: String): SubApply =
      new SubApply(s1.toInt, s2.toInt)

    implicit lazy val gen: Gen[SubApply] =
      Apply[Gen].apply2(Gen[Int], Gen[Int])(SubApply.apply)

    implicit lazy val equal: Equal[SubApply] =
      Equal.equalA[SubApply]

    implicit lazy val tcv: ToConfigValue[SubApply] =
      ToConfigValue.fromMap(cc =>
        Map(
          "s1" -> cc.a1.toString.toConfigValue,
          "s2" -> cc.a2.toString.toConfigValue
        ))
  }

  val subApply = {
    val p1 = checkDerived[SubApply]
    val p2 =
      forAll { (a1: Int, a2: Int, s1: Int, s2: Int) =>
        val c = ConfigFactory.parseString(
          s"""a1 = $a1
             |a2 = $a2
             |s1 = $s1
             |s2 = $s2
           """.stripMargin)
        Configs[SubApply].extract(c).exists(_ === SubApply(a1, a2))
      }
    Properties.list(
      p1.toProperties("sub apply"),
      p2.toProperties("synthetic first")
    )
  }


  class PlainClass(val a1: Int, val a2: Int, val a3: Int) {
    def this(a1: Int, a2: Int) =
      this(a1, a2, 42)
  }

  object PlainClass {
    implicit lazy val gen: Gen[PlainClass] =
      Apply[Gen].apply3(Gen[Int], Gen[Int], Gen[Int])(new PlainClass(_, _, _))

    implicit lazy val equal: Equal[PlainClass] =
      Equal.equalBy(p => (p.a1, p.a2, p.a3))

    implicit lazy val tcv: ToConfigValue[PlainClass] =
      ToConfigValue.fromMap(p =>
        Map(
          "a1" -> p.a1.toConfigValue,
          "a2" -> p.a2.toConfigValue,
          "a3" -> p.a3.toConfigValue
        ))
  }

  val plainClass = {
    val p1 = checkDerived[PlainClass]
    val p2 =
      forAll { (a1: Int, a2: Int) =>
        val c = ConfigFactory.parseString(
          s"""a1 = $a1
             |a2 = $a2
           """.stripMargin)
        Configs[PlainClass].extract(c).exists(_ === new PlainClass(a1, a2, 42))
      }
    Properties.list(
      p1.toProperties("plain class"),
      p2.toProperties("sub constructor")
    )
  }


  sealed trait Sealed

  case class SealedCase(a1: Int) extends Sealed

  class SealedPlain(val a1: Int) extends Sealed

  case object SealedCaseObject extends Sealed

  object SealedObject extends Sealed

  sealed abstract class SealedSealed extends Sealed

  case class SealedSealedClass(a1: Int) extends SealedSealed

  sealed class SealedConcrete(val a1: Int) extends Sealed

  class NoArgClass extends Sealed

  object Sealed {
    // SI-7046
    implicit lazy val configs: Configs[Sealed] =
      Configs.derive[Sealed]

    implicit lazy val gen: Gen[Sealed] =
      Gen.oneOf(
        Gen[Int].map(SealedCase),
        Gen[Int].map(new SealedPlain(_)),
        Gen[Int].map(SealedSealedClass),
        Gen[Int].map(new SealedConcrete(_)),
        Gen.elements(SealedCaseObject, SealedObject, new NoArgClass())
      )

    implicit lazy val equal: Equal[Sealed] =
      Equal.equalBy {
        case SealedCase(a1) => ("CA", a1)
        case p: SealedPlain => ("PL", p.a1)
        case SealedCaseObject => ("CO", 0)
        case SealedObject => ("SO", 0)
        case SealedSealedClass(a1) => ("SS", a1)
        case s: SealedConcrete => ("SC", s.a1)
        case _: NoArgClass => ("NA", 0)
      }

    implicit lazy val tcv: ToConfigValue[Sealed] =
      ToConfigValue.fromMap { s =>
        val m = Map("type" -> s.getClass.getSimpleName.stripSuffix("$").toConfigValue)
        s match {
          case SealedCase(a1) => m + ("a1" -> a1.toConfigValue)
          case p: SealedPlain => m + ("a1" -> p.a1.toConfigValue)
          case SealedCaseObject => m
          case SealedObject => m
          case SealedSealedClass(a1) => m + ("a1" -> a1.toConfigValue)
          case s: SealedConcrete => m + ("a1" -> s.a1.toConfigValue)
          case _: NoArgClass => m
        }
      }
  }

  val sealedClass = {
    val p1 = checkDerived[Sealed]
    val p2 = {
      implicit val moduleAsStringTCV: ToConfigValue[Sealed] = {
        case SealedCaseObject => "SealedCaseObject".toConfigValue
        case SealedObject => "SealedObject".toConfigValue
        case s => Sealed.tcv.toConfigValue(s)
      }
      checkDerived[Sealed]
    }
    Properties.list(
      p1.toProperties("sealed class"),
      p2.toProperties("module as string")
    )
  }


  case class Default1(a1: Int = 1)

  object Default1 {
    implicit lazy val equal: Equal[Default1] =
      Equal.equalA[Default1]
  }

  val default1 = {
    val p1 = {
      implicit val gen: Gen[Default1] =
        Gen[Int].map(Default1.apply)
      implicit val tcv: ToConfigValue[Default1] =
        ToConfigValue.fromMap {
          case Default1(a1) => Map("a1" -> a1.toConfigValue)
        }
      checkDerived[Default1]
    }
    val p2 = {
      implicit val gen: Gen[Default1] =
        Gen.elements(Default1())
      implicit val tcv: ToConfigValue[Default1] =
        _ => ConfigFactory.empty().root()
      checkDerived[Default1]
    }
    Properties.list(
      p1.toProperties("w/o default"),
      p2.toProperties("w/ default")
    )
  }


  class Default22(
      val a1: Int, val a2: Int, val a3: Int, val a4: Int, val a5: Int, val a6: Int, val a7: Int,
      val a8: Int = 8,
      val a9: Int = 9,
      val a10: Int = 10)(
      val a11: Int,
      val a12: Int = a1 + a8,
      val a13: Int = 13,
      val a14: Int,
      val a15: Int = a10 + 15)(
      val a16: Int = 16,
      val a17: Int,
      val a18: Int = 18 + a2,
      val a19: Int,
      val a20: Int,
      val a21: Int = a3 + a13 + 21,
      val a22: Int = 22 + a9 + a14) {

    override def toString: String = {
      val as1 = (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
      val as2 = (a11, a12, a13, a14, a15)
      val as3 = (a16, a17, a18, a19, a20, a21, a22)
      s"${getClass.getSimpleName}$as1$as2$as3"
    }
  }

  object Default22 {
    implicit lazy val equal: Equal[Default22] =
      Equal.equalBy(d => (
        (d.a1, d.a2, d.a3, d.a4, d.a5, d.a6, d.a7, d.a8),
        (d.a9, d.a10, d.a11, d.a12, d.a13, d.a14, d.a15, d.a16),
        (d.a17, d.a18, d.a19, d.a20, d.a21, d.a22)))
  }

  val default22 = {
    val p1 = {
      implicit val gen: Gen[Default22] =
        Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)].map {
          case (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13, a14, a15, a16, a17, a18, a19, a20, a21, a22) =>
            new Default22(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)(a11, a12, a13, a14, a15)(a16, a17, a18, a19, a20, a21, a22)
          }
      implicit val tcv: ToConfigValue[Default22] =
        ToConfigValue.fromMap(d => Map(
          "a1" -> d.a1.toConfigValue, "a2" -> d.a2.toConfigValue, "a3" -> d.a3.toConfigValue,
          "a4" -> d.a4.toConfigValue, "a5" -> d.a5.toConfigValue, "a6" -> d.a6.toConfigValue,
          "a7" -> d.a7.toConfigValue, "a8" -> d.a8.toConfigValue, "a9" -> d.a9.toConfigValue,
          "a10" -> d.a10.toConfigValue,
          "a11" -> d.a11.toConfigValue, "a12" -> d.a12.toConfigValue, "a13" -> d.a13.toConfigValue,
          "a14" -> d.a14.toConfigValue, "a15" -> d.a15.toConfigValue,
          "a16" -> d.a16.toConfigValue, "a17" -> d.a17.toConfigValue, "a18" -> d.a18.toConfigValue,
          "a19" -> d.a19.toConfigValue, "a20" -> d.a20.toConfigValue, "a21" -> d.a21.toConfigValue,
          "a22" -> d.a22.toConfigValue
        ))
      checkDerived[Default22]
    }
    val p2 = {
      implicit val gen: Gen[Default22] =
        Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)].map {
          case (a1, a2, a3, a4, a5, a6, a7, a11, a14, a17, a19, a20) =>
            new Default22(a1, a2, a3, a4, a5, a6, a7)(a11 = a11, a14 = a14)(a17 = a17, a19 = a19, a20 = a20)
        }
      implicit val tcv: ToConfigValue[Default22] =
        ToConfigValue.fromMap(d => Map(
          "a1" -> d.a1.toConfigValue, "a2" -> d.a2.toConfigValue, "a3" -> d.a3.toConfigValue,
          "a4" -> d.a4.toConfigValue, "a5" -> d.a5.toConfigValue, "a6" -> d.a6.toConfigValue,
          "a7" -> d.a7.toConfigValue,
          "a11" -> d.a11.toConfigValue, "a14" -> d.a14.toConfigValue,
          "a17" -> d.a17.toConfigValue, "a19" -> d.a19.toConfigValue, "a20" -> d.a20.toConfigValue
        ))
      checkDerived[Default22]
    }
    Properties.list(
      p1.toProperties("w/o defaults"),
      p2.toProperties("w/ defaults")
    )
  }


  class Default23(
      val a1: Int, val a2: Int, val a3: Int, val a4: Int, val a5: Int, val a6: Int, val a7: Int,
      val a8: Int = 8,
      val a9: Int = 9,
      val a10: Int = 10)(
      val a11: Int,
      val a12: Int = a1 + a8,
      val a13: Int = 13,
      val a14: Int,
      val a15: Int = a10 + 15)(
      val a16: Int = 16,
      val a17: Int,
      val a18: Int = 18 + a2,
      val a19: Int,
      val a20: Int,
      val a21: Int = a3 + a13 + 21,
      val a22: Int = 22 + a9 + a14)(
      val a23: Int = a1 + a2 + a3 + a4 + a5 + a6 + a7 + a8 + a9 + a10 + a11 + a12 + a13 + a14 + a15 + a16 + a17 + a18 + a19 + a20 + a21 + a22) {

    override def toString: String = {
      val as1 = (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)
      val as2 = (a11, a12, a13, a14, a15)
      val as3 = (a16, a17, a18, a19, a20, a21, a22)
      s"${getClass.getSimpleName}$as1$as2$as3($a23)"
    }
  }

  object Default23 {
    implicit lazy val equal: Equal[Default23] =
      Equal.equalBy(d => (
        (d.a1, d.a2, d.a3, d.a4, d.a5, d.a6, d.a7, d.a8),
        (d.a9, d.a10, d.a11, d.a12, d.a13, d.a14, d.a15, d.a16),
        (d.a17, d.a18, d.a19, d.a20, d.a21, d.a22, d.a23)))
  }

  val default23 = {
    val p1 = {
      implicit val gen: Gen[Default23] =
        Apply[Gen].apply2(
          Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)],
          Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)]
        ) {
          case ((a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12), (a13, a14, a15, a16, a17, a18, a19, a20, a21, a22, a23)) =>
            new Default23(a1, a2, a3, a4, a5, a6, a7, a8, a9, a10)(a11, a12, a13, a14, a15)(a16, a17, a18, a19, a20, a21, a22)(a23)
        }
      implicit val tcv: ToConfigValue[Default23] =
        ToConfigValue.fromMap(d => Map(
          "a1" -> d.a1.toConfigValue, "a2" -> d.a2.toConfigValue, "a3" -> d.a3.toConfigValue,
          "a4" -> d.a4.toConfigValue, "a5" -> d.a5.toConfigValue, "a6" -> d.a6.toConfigValue,
          "a7" -> d.a7.toConfigValue, "a8" -> d.a8.toConfigValue, "a9" -> d.a9.toConfigValue,
          "a10" -> d.a10.toConfigValue,
          "a11" -> d.a11.toConfigValue, "a12" -> d.a12.toConfigValue, "a13" -> d.a13.toConfigValue,
          "a14" -> d.a14.toConfigValue, "a15" -> d.a15.toConfigValue,
          "a16" -> d.a16.toConfigValue, "a17" -> d.a17.toConfigValue, "a18" -> d.a18.toConfigValue,
          "a19" -> d.a19.toConfigValue, "a20" -> d.a20.toConfigValue, "a21" -> d.a21.toConfigValue,
          "a22" -> d.a22.toConfigValue,
          "a23" -> d.a23.toConfigValue
        ))
      checkDerived[Default23]
    }
    val p2 = {
      implicit val gen: Gen[Default23] =
        Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int)].map {
          case (a1, a2, a3, a4, a5, a6, a7, a11, a14, a17, a19, a20) =>
            new Default23(a1, a2, a3, a4, a5, a6, a7)(a11 = a11, a14 = a14)(a17 = a17, a19 = a19, a20 = a20)()
        }
      implicit val tcv: ToConfigValue[Default23] =
        ToConfigValue.fromMap(d => Map(
          "a1" -> d.a1.toConfigValue, "a2" -> d.a2.toConfigValue, "a3" -> d.a3.toConfigValue,
          "a4" -> d.a4.toConfigValue, "a5" -> d.a5.toConfigValue, "a6" -> d.a6.toConfigValue,
          "a7" -> d.a7.toConfigValue,
          "a11" -> d.a11.toConfigValue, "a14" -> d.a14.toConfigValue,
          "a17" -> d.a17.toConfigValue, "a19" -> d.a19.toConfigValue, "a20" -> d.a20.toConfigValue
        ))
      checkDerived[Default23]
    }
    Properties.list(
      p1.toProperties("w/o defaults"),
      p2.toProperties("w/ defaults")
    )
  }


  //  case class CC254(
  //      a1: Int, a2: Int, a3: Int, a4: Int, a5: Int, a6: Int, a7: Int, a8: Int, a9: Int, a10: Int,
  //      a11: Int, a12: Int, a13: Int, a14: Int, a15: Int, a16: Int, a17: Int, a18: Int, a19: Int, a20: Int,
  //      a21: Int, a22: Int, a23: Int, a24: Int, a25: Int, a26: Int, a27: Int, a28: Int, a29: Int, a30: Int,
  //      a31: Int, a32: Int, a33: Int, a34: Int, a35: Int, a36: Int, a37: Int, a38: Int, a39: Int, a40: Int,
  //      a41: Int, a42: Int, a43: Int, a44: Int, a45: Int, a46: Int, a47: Int, a48: Int, a49: Int, a50: Int,
  //      a51: Int, a52: Int, a53: Int, a54: Int, a55: Int, a56: Int, a57: Int, a58: Int, a59: Int, a60: Int,
  //      a61: Int, a62: Int, a63: Int, a64: Int, a65: Int, a66: Int, a67: Int, a68: Int, a69: Int, a70: Int,
  //      a71: Int, a72: Int, a73: Int, a74: Int, a75: Int, a76: Int, a77: Int, a78: Int, a79: Int, a80: Int,
  //      a81: Int, a82: Int, a83: Int, a84: Int, a85: Int, a86: Int, a87: Int, a88: Int, a89: Int, a90: Int,
  //      a91: Int, a92: Int, a93: Int, a94: Int, a95: Int, a96: Int, a97: Int, a98: Int, a99: Int, a100: Int,
  //      a101: Int, a102: Int, a103: Int, a104: Int, a105: Int, a106: Int, a107: Int, a108: Int, a109: Int, a110: Int,
  //      a111: Int, a112: Int, a113: Int, a114: Int, a115: Int, a116: Int, a117: Int, a118: Int, a119: Int, a120: Int,
  //      a121: Int, a122: Int, a123: Int, a124: Int, a125: Int, a126: Int, a127: Int, a128: Int, a129: Int, a130: Int,
  //      a131: Int, a132: Int, a133: Int, a134: Int, a135: Int, a136: Int, a137: Int, a138: Int, a139: Int, a140: Int,
  //      a141: Int, a142: Int, a143: Int, a144: Int, a145: Int, a146: Int, a147: Int, a148: Int, a149: Int, a150: Int,
  //      a151: Int, a152: Int, a153: Int, a154: Int, a155: Int, a156: Int, a157: Int, a158: Int, a159: Int, a160: Int/*,
  //      a161: Int, a162: Int, a163: Int, a164: Int, a165: Int, a166: Int, a167: Int, a168: Int, a169: Int, a170: Int,
  //      a171: Int, a172: Int, a173: Int, a174: Int, a175: Int, a176: Int, a177: Int, a178: Int, a179: Int, a180: Int,
  //      a181: Int, a182: Int, a183: Int, a184: Int, a185: Int, a186: Int, a187: Int, a188: Int, a189: Int, a190: Int,
  //      a191: Int, a192: Int, a193: Int, a194: Int, a195: Int, a196: Int, a197: Int, a198: Int, a199: Int, a200: Int,
  //      a201: Int, a202: Int, a203: Int, a204: Int, a205: Int, a206: Int, a207: Int, a208: Int, a209: Int, a210: Int,
  //      a211: Int, a212: Int, a213: Int, a214: Int, a215: Int, a216: Int, a217: Int, a218: Int, a219: Int, a220: Int,
  //      a221: Int, a222: Int, a223: Int, a224: Int, a225: Int, a226: Int, a227: Int, a228: Int, a229: Int, a230: Int,
  //      a231: Int, a232: Int, a233: Int, a234: Int, a235: Int, a236: Int, a237: Int, a238: Int, a239: Int, a240: Int,
  //      a241: Int, a242: Int, a243: Int, a244: Int, a245: Int, a246: Int, a247: Int, a248: Int, a249: Int, a250: Int,
  //      a251: Int, a252: Int, a253: Int, a254: Int*/)
  //
  //  val caseClass254 =
  //    forAll {
  //      val config = ConfigFactory.parseString(
  //        (1 to 254).map(n => s"a$n = $n").mkString("\n"))
  //      val cc = CC254(
  //        1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
  //        21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40,
  //        41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60,
  //        61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80,
  //        81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100,
  //        101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114, 115, 116, 117, 118, 119, 120,
  //        121, 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139, 140,
  //        141, 142, 143, 144, 145, 146, 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160/*,
  //        161, 162, 163, 164, 165, 166, 167, 168, 169, 170, 171, 172, 173, 174, 175, 176, 177, 178, 179, 180,
  //        181, 182, 183, 184, 185, 186, 187, 188, 189, 190, 191, 192, 193, 194, 195, 196, 197, 198, 199, 200,
  //        201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214, 215, 216, 217, 218, 219, 220,
  //        221, 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239, 240,
  //        241, 242, 243, 244, 245, 246, 247, 248, 249, 250, 251, 252, 253, 254*/)
  //      Configs[CC254].extract(config).exists(_ == cc)
  //    }

}
