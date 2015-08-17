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

import com.github.kxbmap.configs.testkit._
import com.typesafe.config.{ConfigException, ConfigFactory}
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.string._
import scalaz.syntax.equal._
import scalaz.{Apply, Equal}


object AutoConfigsTest extends Scalaprops {

  import MaterializeConfigsTest._
  import com.github.kxbmap.configs.auto._

  val simple = checkSimple
  val nested = checkNested
  val recursive = checkRecursive
  val paramLists = checkParamLists
  val subCtors = checkSubCtors
  val multiApply = checkMultiApply
  val formatCase = checkFormatCase
  val defaults = checkDefaults
  val caseDefaults = checkCaseDefaults

  val implicitParam = checkImplicitParam { implicit n =>
    Configs[ImplicitParam]
  }

  val implicitParamWithDefault = checkImplicitWithDefault { implicit n =>
    Configs[ImplicitWithDefault]
  }

}


object SimpleConfigsTest extends Scalaprops {

  import MaterializeConfigsTest._
  import com.github.kxbmap.configs.simple._


  implicit lazy val simpleConfigs: Configs[SimpleSetting] = Configs.of[SimpleSetting]
  val simple = checkSimple

  implicit lazy val nestedConfigs: Configs[NestedSetting] = Configs.of[NestedSetting]
  val nested = checkNested

//  implicit lazy val recursiveConfigs: Configs[RecursiveSetting] = Configs.of[RecursiveSetting]
//  val recursive = checkRecursive

  implicit lazy val paramListsConfigs: Configs[ParamListsSetting] = Configs.of[ParamListsSetting]
  val paramLists = checkParamLists

  implicit lazy val subCtorsConfigs: Configs[SubCtorsSetting] = Configs.of[SubCtorsSetting]
  val subCtors = checkSubCtors

  implicit lazy val multiApplyConfigs: Configs[MultiApply] = Configs.of[MultiApply]
  val multiApply = checkMultiApply

  implicit lazy val formatCaseConfigs: Configs[FormatCaseSetting] = Configs.of[FormatCaseSetting]
  implicit lazy val duplicate1Configs: Configs[Duplicate1] = Configs.of[Duplicate1]
  implicit lazy val duplicate2Configs: Configs[Duplicate2] = Configs.of[Duplicate2]
  val formatCase = checkFormatCase

  implicit lazy val defaultsConfigs: Configs[Defaults] = Configs.of[Defaults]
  val defaults = checkDefaults

  implicit lazy val caseDefaultsConfigs: Configs[CaseDefaults] = Configs.of[CaseDefaults]
  val caseDefaults = checkCaseDefaults

  val implicitParam = checkImplicitParam { implicit n =>
    Configs.of[ImplicitParam]
  }

  implicit lazy val implicitWithDefaultConfigs: Configs[ImplicitWithDefault] = Configs.of[ImplicitWithDefault]
  val implicitWithDefault = checkImplicitWithDefault { implicit n =>
    Configs.of[ImplicitWithDefault]
  }


  ////
  val sealedTrait = checkMat[SealedTrait]

  sealed trait SealedTrait

  case class SealedChild1(n: Int) extends SealedTrait

  case object SealedChild2 extends SealedTrait

  class SealedChild3(val m: Int) extends SealedTrait

  object SealedChild4 extends SealedTrait

  sealed trait SealedNest extends SealedTrait

  case class SealedNestChild(nn: Int) extends SealedNest

  case class SealedChild5(value: Int) extends SealedTrait

  case class SealedChild6(value: Int) extends SealedTrait

  case object SealedChild7 extends SealedTrait


  implicit lazy val sealedTraitConfigs: Configs[SealedTrait] = Configs.of[SealedTrait]

  implicit lazy val sealedTraitEqual: Equal[SealedTrait] = (a, b) =>
    (a, b) match {
      case (c3a: SealedChild3, c3b: SealedChild3) => c3a.m == c3b.m
      case _                                      => a == b
    }

  implicit lazy val sealedTraitConfigVal: ConfigVal[SealedTrait] = {
    case SealedChild1(n)     => Map("n" -> n).cv
    case SealedChild2        => "SealedChild2".cv
    case s: SealedChild3     => Map("m" -> s.m).cv
    case SealedChild4        => "SealedChild4".cv
    case SealedNestChild(nn) => Map("nn" -> nn).cv
    case SealedChild5(value) =>
      Map[String, Any](
        "'type" -> "SealedChild5",
        "value" -> value
      ).cv
    case SealedChild6(value) =>
      Map[String, Any](
        "'type" -> "SealedChild6",
        "value" -> value
      ).cv
    case SealedChild7 =>
      Map[String, Any](
        "'type" -> "SealedChild7"
      ).cv
  }

  implicit lazy val sealedTraitGen: Gen[SealedTrait] =
    Gen.oneOf(
      Gen[Int].map(SealedChild1),
      Gen.value(SealedChild2),
      Gen[Int].map(new SealedChild3(_)),
      Gen.value(SealedChild4),
      Gen[Int].map(SealedNestChild),
      Gen[Int].map(SealedChild5),
      Gen[Int].map(SealedChild6),
      Gen.value(SealedChild7)
    )

}


object MaterializeConfigsTest extends ConfigProp {

  def checkMat[A: Gen : Configs : ConfigVal : Equal] = forAll { a: A =>
    Configs[A].extract(a.cv) === a
  }


  ////
  case class SimpleSetting(user: String, password: String)

  implicit lazy val simpleSettingConfigVal: ConfigVal[SimpleSetting] =
    ConfigVal.fromMap(s => Map(
      "user" -> s.user.cv,
      "password" -> s.password.cv
    ))

  implicit lazy val simpleSettingGen: Gen[SimpleSetting] =
    Apply[Gen].apply2(Gen[String], Gen[String])(SimpleSetting.apply)

  def checkSimple(implicit C: Configs[SimpleSetting]) = checkMat[SimpleSetting]


  ////
  case class NestedSetting(
    simple: SimpleSetting,
    simples: Seq[SimpleSetting],
    simpleMap: Map[String, SimpleSetting],
    optional: Option[SimpleSetting])

  implicit lazy val nestedSettingConfigVal: ConfigVal[NestedSetting] =
    ConfigVal.fromMap(s => Map(
      "simple" -> s.simple.cv,
      "simples" -> s.simples.cv,
      "simpleMap" -> s.simpleMap.cv,
      "optional" -> s.optional.cv
    ))

  implicit lazy val nestedSettingGen: Gen[NestedSetting] =
    Apply[Gen].apply4(
      Gen[SimpleSetting],
      Gen[List[SimpleSetting]],
      Gen[Map[String, SimpleSetting]],
      Gen[Option[SimpleSetting]]
    )(NestedSetting.apply)

  def checkNested(implicit C: Configs[NestedSetting]) = checkMat[NestedSetting]


  ////
  case class RecursiveSetting(value: Int, next: Option[RecursiveSetting])

  implicit lazy val recursiveSettingConfigVal: ConfigVal[RecursiveSetting] =
    ConfigVal.fromMap(s => Map(
      "value" -> s.value.cv,
      "next" -> s.next.cv
    ))

  implicit lazy val recursiveSettingGen: Gen[RecursiveSetting] =
    Apply[Gen].apply2(
      Gen[Int],
      Gen[Option[RecursiveSetting]]
    )(RecursiveSetting.apply)

  def checkRecursive(implicit C: Configs[RecursiveSetting]) = checkMat[RecursiveSetting]


  ////
  class ParamListsSetting(val firstName: String, val lastName: String)(val age: Int)

  implicit lazy val paramListsSettingConfigVal: ConfigVal[ParamListsSetting] =
    ConfigVal.fromMap(s => Map(
      "firstName" -> s.firstName.cv,
      "lastName" -> s.lastName.cv,
      "age" -> s.age.cv
    ))

  implicit lazy val paramListsSettingGen: Gen[ParamListsSetting] =
    Apply[Gen].apply3(Gen[String], Gen[String], Gen[Int])(new ParamListsSetting(_, _)(_))

  implicit lazy val paramListsSettingEqual: Equal[ParamListsSetting] =
    (s1, s2) => s1.firstName == s2.firstName && s1.lastName == s2.lastName && s1.age == s2.age

  def checkParamLists(implicit C: Configs[ParamListsSetting]) = checkMat[ParamListsSetting]


  ////
  class SubCtorsSetting(val name: String, val age: Int, val country: String) {

    def this(name: String, age: Int) = this(name, age, "JPN")

    def this(firstName: String, lastName: String, age: Int) = this(s"$firstName $lastName", age)

    def this(firstName: String, lastName: String) = this(firstName, lastName, 0)
  }

  implicit lazy val subCtorsSettingConfigVal: ConfigVal[SubCtorsSetting] =
    ConfigVal.fromMap(s => Map(
      "name" -> s.name.cv,
      "age" -> s.age.cv,
      "country" -> s.country.cv
    ))

  implicit lazy val subCtorsSettingGen: Gen[SubCtorsSetting] =
    Apply[Gen].apply3(Gen[String], Gen[Int], Gen[String])(new SubCtorsSetting(_, _, _))

  implicit lazy val subCtorsSettingEqual: Equal[SubCtorsSetting] =
    (s1, s2) => s1.name == s2.name && s1.age == s2.age && s1.country == s2.country

  def checkSubCtors(implicit C: Configs[SubCtorsSetting]) = {
    val subCtor1 = forAll { (first: String, last: String, age: Int) =>
      val config = ConfigFactory.parseString(s"firstName = ${q(first)}, lastName = ${q(last)}, age = $age")
      C.extract(config) === new SubCtorsSetting(first, last, age)
    }

    val subCtor2 = forAll { (first: String, last: String) =>
      val config = ConfigFactory.parseString(s"firstName = ${q(first)}, lastName = ${q(last)}")
      C.extract(config) === new SubCtorsSetting(first, last)
    }

    val primaryCtorFirst = forAll { (first: String, last: String, name: String, age: Int, country: String) =>
      val config = ConfigFactory.parseString(
        s"""firstName = ${q(first)}
           |lastName = ${q(last)}
           |name = ${q(name)}
           |age = $age
           |country = ${q(country)}
           |""".stripMargin)
      C.extract(config) === new SubCtorsSetting(name, age, country)
    }

    val ignorePrivateCtor = intercept { (name: String, age: Int) =>
      val config = ConfigFactory.parseString(s"firstName = ${q(name)}, age = $age")
      C.extract(config)
    } {
      case _: ConfigException => true
    }

    Properties.list(
      checkMat[SubCtorsSetting].toProperties("primary"),
      subCtor1.toProperties("sub decl order"),
      subCtor2.toProperties("sub next"),
      primaryCtorFirst.toProperties("primary first"),
      ignorePrivateCtor.toProperties("ignore private")
    )
  }


  ////
  case class MultiApply(a: Int, b: Int, c: Int)

  object MultiApply {

    def apply(a0: Int, b0: Int): MultiApply = MultiApply(a0, b0, 0)

    def apply(a0: Int): MultiApply = MultiApply(a0, 0, 0)
  }

  implicit lazy val multiApplyConfigVal: ConfigVal[MultiApply] =
    ConfigVal.fromMap(s => Map(
      "a" -> s.a.cv,
      "b" -> s.b.cv,
      "c" -> s.c.cv
    ))

  implicit lazy val multiApplyGen: Gen[MultiApply] =
    Apply[Gen].apply3(Gen[Int], Gen[Int], Gen[Int])(MultiApply(_, _, _))

  def checkMultiApply(implicit C: Configs[MultiApply]) = {
    val subApply1 = forAll { (a0: Int, b0: Int) =>
      val config = ConfigFactory.parseString(s"a0 = $a0, b0 = $b0")
      C.extract(config) === MultiApply(a0, b0)
    }

    val subApply2 = forAll { a0: Int =>
      val config = ConfigFactory.parseString(s"a0 = $a0")
      C.extract(config) === MultiApply(a0)
    }

    val syntheticApplyFirst = forAll { (a: Int, b: Int, c: Int, a0: Int, b0: Int) =>
      val config = ConfigFactory.parseString(
        s"""a = $a
           |b = $b
           |c = $c
           |a0 = $a0
           |b0 = $b0
           |""".stripMargin)
      C.extract(config) === new MultiApply(a, b, c)
    }

    Properties.list(
      checkMat[MultiApply].toProperties("synthetic"),
      subApply1.toProperties("sub decl order"),
      subApply2.toProperties("sub next"),
      syntheticApplyFirst.toProperties("synthetic first")
    )
  }


  ////
  case class FormatCaseSetting(
    lowerCamel: Int,
    UpperCamel: Int,
    lower_snake: Int,
    UPPER_SNAKE: Int,
    `lower-hyphen`: Int,
    UPPERThenCamel: Int)

  implicit lazy val formatCaseSettingConfigVal: ConfigVal[FormatCaseSetting] =
    ConfigVal.fromMap(s => Map(
      "lower-camel" -> s.lowerCamel.cv,
      "upper-camel" -> s.UpperCamel.cv,
      "lower-snake" -> s.lower_snake.cv,
      "upper-snake" -> s.UPPER_SNAKE.cv,
      "lower-hyphen" -> s.`lower-hyphen`.cv,
      "upper-then-camel" -> s.UPPERThenCamel.cv
    ))

  implicit lazy val formatCaseSettingGen: Gen[FormatCaseSetting] =
    Apply[Gen].apply6(Gen[Int], Gen[Int], Gen[Int], Gen[Int], Gen[Int], Gen[Int])(FormatCaseSetting.apply)

  case class Duplicate1(duplicateName: Int, DuplicateName: Int)

  case class Duplicate2(duplicateName: Int, `duplicate-name`: Int)

  def checkFormatCase(implicit C: Configs[FormatCaseSetting], D1: Configs[Duplicate1], D2: Configs[Duplicate2]) = {
    val original = forAll { (o: FormatCaseSetting, f: FormatCaseSetting) =>
      val config = ConfigFactory.parseString(
        s"""lowerCamel = ${o.lowerCamel}
           |lower-camel = ${f.lowerCamel}
           |UpperCamel = ${o.UpperCamel}
           |upper-camel = ${f.UpperCamel}
           |lower_snake = ${o.lower_snake}
           |lower-snake = ${f.lower_snake}
           |UPPER_SNAKE = ${o.UPPER_SNAKE}
           |upper-snake = ${f.UPPER_SNAKE}
           |lower-hyphen = ${o.`lower-hyphen`}
           |UPPERThenCamel = ${o.UPPERThenCamel}
           |upper-then-camel = ${f.UPPERThenCamel}
           |""".stripMargin
      )
      C.extract(config) === o
    }

    val duplicate1 = intercept { (n: Int) =>
      D1.extract(ConfigFactory.parseString(s"duplicate-name = $n"))
    } {
      case e: ConfigException.Missing => e.getMessage.contains("duplicateName")
    }

    val duplicate2 = intercept { (n: Int) =>
      D2.extract(ConfigFactory.parseString(s"duplicate-name = $n"))
    } {
      case e: ConfigException.Missing => e.getMessage.contains("duplicateName")
    }

    Properties.list(
      checkMat[FormatCaseSetting].toProperties("lower-hyphen"),
      original.toProperties("original"),
      duplicate1.toProperties("duplicate1"),
      duplicate2.toProperties("duplicate2")
    )
  }


  ////
  class Defaults(val a: Int, val b: Int = 2)(val c: Int = a + b + 3)(val d: Int = b * c * 4)

  implicit lazy val defaultsEqual: Equal[Defaults] = Equal.equalBy(d => (d.a, d.b, d.c, d.d))

  implicit lazy val defaultsConfigVal: ConfigVal[Defaults] =
    ConfigVal.fromMap(d => Map(
      "a" -> d.a.cv,
      "b" -> d.b.cv,
      "c" -> d.c.cv,
      "d" -> d.d.cv
    ))

  implicit lazy val defaultsGen: Gen[Defaults] =
    Apply[Gen].apply4(Gen[Int], Gen[Int], Gen[Int], Gen[Int])(new Defaults(_, _)(_)(_))

  def checkDefaults(implicit C: Configs[Defaults]) = {
    val withAllDefaults = forAll { a: Int =>
      val config = ConfigFactory.parseString(s"a = $a")
      C.extract(config) === new Defaults(a)()()
    }

    val withSomeDefaults = forAll { (a: Int, c: Int) =>
      val config = ConfigFactory.parseString(s"a = $a, c = $c")
      C.extract(config) === new Defaults(a)(c)()
    }

    Properties.list(
      checkMat[Defaults].toProperties("w/o defaults"),
      withAllDefaults.toProperties("w/ all defaults"),
      withSomeDefaults.toProperties("w/ some defaults")
    )
  }


  ///
  case class CaseDefaults(a: Int, b: Int = 2)(val c: Int = a + b + 3)(val d: Int = b * c * 4)

  implicit lazy val caseDefaultsEqual: Equal[CaseDefaults] = Equal.equalBy(d => (d.a, d.b, d.c, d.d))

  implicit lazy val caseDefaultsConfigVal: ConfigVal[CaseDefaults] =
    ConfigVal.fromMap(d => Map(
      "a" -> d.a.cv,
      "b" -> d.b.cv,
      "c" -> d.c.cv,
      "d" -> d.d.cv
    ))

  implicit lazy val caseDefaultsGen: Gen[CaseDefaults] =
    Apply[Gen].apply4(Gen[Int], Gen[Int], Gen[Int], Gen[Int])(CaseDefaults(_, _)(_)(_))

  def checkCaseDefaults(implicit C: Configs[CaseDefaults]) = {
    val caseWithAllDefaults = forAll { a: Int =>
      val config = ConfigFactory.parseString(s"a = $a")
      C.extract(config) === CaseDefaults(a)()()
    }

    val caseWithSomeDefaults = forAll { (a: Int, c: Int) =>
      val config = ConfigFactory.parseString(s"a = $a, c = $c")
      C.extract(config) === CaseDefaults(a)(c)()
    }

    Properties.list(
      checkMat[CaseDefaults].toProperties("w/o defaults"),
      caseWithAllDefaults.toProperties("w/ all defaults"),
      caseWithSomeDefaults.toProperties("w/ some defaults")
    )
  }


  ////
  class ImplicitParam(val a: Int, val b: Int)(implicit val c: Int)

  implicit lazy val implicitParamEqual: Equal[ImplicitParam] = Equal.equalBy(d => (d.a, d.b, d.c))

  implicit lazy val implicitParamConfigVal: ConfigVal[ImplicitParam] =
    ConfigVal.fromMap(i => Map(
      "a" -> i.a.cv,
      "b" -> i.b.cv,
      "c" -> i.c.cv
    ))

  implicit lazy val implicitParamGen: Gen[ImplicitParam] =
    Apply[Gen].apply3(Gen[Int], Gen[Int], Gen[Int])(new ImplicitParam(_, _)(_))

  def checkImplicitParam(f: Int => Configs[ImplicitParam]) = {
    val withImp = {
      forAll { (a: Int, b: Int, c: Int) =>
        val config = ConfigFactory.parseString(s"a = $a, b = $b")
        f(c).extract(config) === new ImplicitParam(a, b)(c)
      }
    }

    val overrideImp = {
      implicit val c: Configs[ImplicitParam] = f(42)
      checkMat[ImplicitParam]
    }

    Properties.list(
      withImp.toProperties("w/ implicit value"),
      overrideImp.toProperties("override implicit value")
    )
  }


  ////
  class ImplicitWithDefault(val a: Int, val b: Int)(implicit val c: Int = 1)

  implicit lazy val implicitWithDefaultEqual: Equal[ImplicitWithDefault] = Equal.equalBy(d => (d.a, d.b, d.c))

  implicit lazy val implicitWithDefaultConfigVal: ConfigVal[ImplicitWithDefault] =
    ConfigVal.fromMap(i => Map(
      "a" -> i.a.cv,
      "b" -> i.b.cv,
      "c" -> i.c.cv
    ))

  implicit lazy val implicitWithDefaultGen: Gen[ImplicitWithDefault] =
    Apply[Gen].apply3(Gen[Int], Gen[Int], Gen[Int])(new ImplicitWithDefault(_, _)(_))

  def checkImplicitWithDefault(f: Int => Configs[ImplicitWithDefault])(implicit C: Configs[ImplicitWithDefault]) = {
    val withImplicit = {
      forAll { (a: Int, b: Int, c: Int) =>
        val config = ConfigFactory.parseString(s"a = $a, b = $b")
        f(c).extract(config) === new ImplicitWithDefault(a, b)(c)
      }
    }
    val withoutImplicit = {
      forAll { (a: Int, b: Int) =>
        val config = ConfigFactory.parseString(s"a = $a, b = $b")
        C.extract(config) === new ImplicitWithDefault(a, b)(1)
      }
    }
    Properties.list(
      withImplicit.toProperties("w/ implicit value"),
      withoutImplicit.toProperties("w/o implicit value")
    )
  }

}
