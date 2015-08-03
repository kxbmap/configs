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

import com.github.kxbmap.configs.util._
import com.typesafe.config.{ConfigException, ConfigFactory}
import scala.collection.JavaConverters._
import scalaprops.Property.forAll
import scalaprops.{Gen, Properties, Scalaprops}
import scalaz.std.string._
import scalaz.{Apply, Equal}

object MaterializeConfigsTest extends Scalaprops with ConfigProp {

  def checkMat[A: Gen : Configs : ConfigVal : Equal] = forAll { a: A =>
    Equal[A].equal(Configs[A].extract(a.configValue), a)
  }


  ////
  val simple = checkMat[SimpleSetting]

  case class SimpleSetting(user: String, password: String)

  implicit lazy val simpleSettingConfigVal: ConfigVal[SimpleSetting] =
    ConfigVal.fromMap(s => Map("user" -> s.user, "password" -> s.password))

  implicit lazy val simpleSettingGen: Gen[SimpleSetting] =
    Apply[Gen].apply2(Gen[String], Gen[String])(SimpleSetting.apply)


  ////
  val nested = checkMat[NestedSetting]

  case class NestedSetting(
    simple: SimpleSetting,
    simples: Seq[SimpleSetting],
    simpleMap: Map[String, SimpleSetting],
    optional: Option[SimpleSetting])

  implicit lazy val nestedSettingConfigVal: ConfigVal[NestedSetting] =
    ConfigVal.fromMap(s => Map(
      "simple" -> s.simple,
      "simples" -> s.simples,
      "simpleMap" -> s.simpleMap,
      "optional" -> s.optional
    ))

  implicit lazy val nestedSettingGen: Gen[NestedSetting] =
    Apply[Gen].apply4(
      Gen[SimpleSetting],
      Gen[List[SimpleSetting]],
      Gen[Map[String, SimpleSetting]],
      Gen[Option[SimpleSetting]]
    )(NestedSetting.apply)


  ////
  val recursive = checkMat[RecursiveSetting]

  case class RecursiveSetting(value: Int, next: Option[RecursiveSetting])

  implicit lazy val recursiveSettingConfigVal: ConfigVal[RecursiveSetting] =
    ConfigVal.fromMap(s => Map(
      "value" -> s.value,
      "next" -> s.next
    ))

  implicit lazy val recursiveSettingGen: Gen[RecursiveSetting] =
    Apply[Gen].apply2(
      Gen[Int],
      Gen[Option[RecursiveSetting]]
    )(RecursiveSetting.apply)


  ////
  val paramLists = checkMat[ParamListsSetting]

  class ParamListsSetting(val firstName: String, val lastName: String)(val age: Int)

  implicit lazy val paramListsSettingConfigVal: ConfigVal[ParamListsSetting] =
    ConfigVal.fromMap(s => Map(
      "firstName" -> s.firstName,
      "lastName" -> s.lastName,
      "age" -> s.age
    ))

  implicit lazy val paramListsSettingGen: Gen[ParamListsSetting] =
    Apply[Gen].apply3(Gen[String], Gen[String], Gen[Int])(new ParamListsSetting(_, _)(_))

  implicit lazy val paramListsSettingEqual: Equal[ParamListsSetting] =
    (s1, s2) => s1.firstName == s2.firstName && s1.lastName == s2.lastName && s1.age == s2.age


  ////
  val subCtors = Properties.list(
    checkMat[SubCtorsSetting].toProperties("primary"),
    subCtor1.toProperties("sub decl order"),
    subCtor2.toProperties("sub next"),
    primaryCtorFirst.toProperties("primary first"),
    ignorePrivateCtor.toProperties("ignore private")
  )

  private lazy val subCtor1 = forAll { (first: String, last: String, age: Int) =>
    val expected = new SubCtorsSetting(first, last, age)
    val config = ConfigFactory.parseString(s"firstName = ${q(first)}, lastName = ${q(last)}, age = $age")
    Equal[SubCtorsSetting].equal(Configs[SubCtorsSetting].extract(config), expected)
  }

  private lazy val subCtor2 = forAll { (first: String, last: String) =>
    val expected = new SubCtorsSetting(first, last)
    val config = ConfigFactory.parseString(s"firstName = ${q(first)}, lastName = ${q(last)}")
    Equal[SubCtorsSetting].equal(Configs[SubCtorsSetting].extract(config), expected)
  }

  private lazy val primaryCtorFirst = forAll { (first: String, last: String, name: String, age: Int, country: String) =>
    val expected = new SubCtorsSetting(name, age, country)
    val config = ConfigFactory.parseString(
      s"""firstName = ${q(first)}
         |lastName = ${q(last)}
         |name = ${q(name)}
         |age = $age
         |country = ${q(country)}
         |""".stripMargin)
    Equal[SubCtorsSetting].equal(Configs[SubCtorsSetting].extract(config), expected)
  }

  private lazy val ignorePrivateCtor = intercept { (name: String, age: Int) =>
    val config = ConfigFactory.parseString(s"firstName = ${q(name)}, age = $age")
    Configs[SubCtorsSetting].extract(config)
  } {
    case _: ConfigException => true
  }

  class SubCtorsSetting(val name: String, val age: Int, val country: String) {

    private def this(name: String, age: Int) = this(name, age, "JPN")

    def this(firstName: String, lastName: String, age: Int) = this(s"$firstName $lastName", age)

    def this(firstName: String, lastName: String) = this(firstName, lastName, 0)
  }

  implicit lazy val subCtorsSettingConfigVal: ConfigVal[SubCtorsSetting] =
    ConfigVal.fromMap(s => Map(
      "name" -> s.name,
      "age" -> s.age,
      "country" -> s.country
    ))

  implicit lazy val subCtorsSettingGen: Gen[SubCtorsSetting] =
    Apply[Gen].apply3(Gen[String], Gen[Int], Gen[String])(new SubCtorsSetting(_, _, _))

  implicit lazy val subCtorsSettingEqual: Equal[SubCtorsSetting] =
    (s1, s2) => s1.name == s2.name && s1.age == s2.age && s1.country == s2.country


  ////
  val multiApply = Properties.list(
    checkMat[MultiApply].toProperties("synthetic"),
    subApply1.toProperties("sub decl order"),
    subApply2.toProperties("sub next"),
    syntheticApplyFirst.toProperties("synthetic first")
  )

  private lazy val subApply1 = forAll { (a0: Int, b0: Int) =>
    val expected = MultiApply(a0, b0)
    val config = ConfigFactory.parseString(s"a0 = $a0, b0 = $b0")
    Equal[MultiApply].equal(Configs[MultiApply].extract(config), expected)
  }

  private lazy val subApply2 = forAll { a0: Int =>
    val expected = MultiApply(a0)
    val config = ConfigFactory.parseString(s"a0 = $a0")
    Equal[MultiApply].equal(Configs[MultiApply].extract(config), expected)
  }

  private lazy val syntheticApplyFirst = forAll { (a: Int, b: Int, c: Int, a0: Int, b0: Int) =>
    val expected = new MultiApply(a, b, c)
    val config = ConfigFactory.parseString(
      s"""a = $a
         |b = $b
         |c = $c
         |a0 = $a0
         |b0 = $b0
         |""".stripMargin)
    Equal[MultiApply].equal(Configs[MultiApply].extract(config), expected)
  }

  case class MultiApply(a: Int, b: Int, c: Int)

  object MultiApply {

    def apply(a0: Int, b0: Int): MultiApply = MultiApply(a0, b0, 0)

    def apply(a0: Int): MultiApply = MultiApply(a0, 0, 0)
  }

  implicit lazy val multiApplyConfigVal: ConfigVal[MultiApply] =
    ConfigVal.fromMap(s => Map(
      "a" -> s.a,
      "b" -> s.b,
      "c" -> s.c
    ))

  implicit lazy val multiApplyGen: Gen[MultiApply] =
    Apply[Gen].apply3(Gen[Int], Gen[Int], Gen[Int])(MultiApply(_, _, _))


  ////
  val formatCase = Properties.list(
    checkMat[FormatCaseSetting].toProperties("lower-hyphen"),
    original.toProperties("original"),
    duplicate1.toProperties("duplicate1"),
    duplicate2.toProperties("duplicate2")
  )

  private lazy val original = forAll { (o: FormatCaseSetting, f: FormatCaseSetting) =>
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
    Equal[FormatCaseSetting].equal(Configs[FormatCaseSetting].extract(config), o)
  }

  private lazy val duplicate1 = intercept { (n: Int) =>
    val config = ConfigFactory.parseString(s"duplicate-name = $n")
    config.extract[Duplicate1]
  } {
    case e: ConfigException.Missing => e.getMessage.contains("duplicateName")
  }

  private lazy val duplicate2 = intercept { (n: Int) =>
    val config = ConfigFactory.parseString(s"duplicate-name = $n")
    config.extract[Duplicate2]
  } {
    case e: ConfigException.Missing => e.getMessage.contains("duplicateName")
  }

  case class FormatCaseSetting(
    lowerCamel: Int,
    UpperCamel: Int,
    lower_snake: Int,
    UPPER_SNAKE: Int,
    `lower-hyphen`: Int,
    UPPERThenCamel: Int)

  implicit lazy val formatCaseSettingConfigVal: ConfigVal[FormatCaseSetting] =
    ConfigVal.fromMap(s => Map(
      "lower-camel" -> s.lowerCamel,
      "upper-camel" -> s.UpperCamel,
      "lower-snake" -> s.lower_snake,
      "upper-snake" -> s.UPPER_SNAKE,
      "lower-hyphen" -> s.`lower-hyphen`,
      "upper-then-camel" -> s.UPPERThenCamel
    ))

  implicit lazy val formatCaseSettingGen: Gen[FormatCaseSetting] =
    Apply[Gen].apply6(Gen[Int], Gen[Int], Gen[Int], Gen[Int], Gen[Int], Gen[Int])(FormatCaseSetting.apply)


  case class Duplicate1(duplicateName: Int, DuplicateName: Int)

  case class Duplicate2(duplicateName: Int, `duplicate-name`: Int)


  ////
  val sealedTrait = checkMat[SealedTrait]

  sealed trait SealedTrait

  case class SealedChild1(n: Int) extends SealedTrait

  case object SealedChild2 extends SealedTrait

  class SealedChild3(val m: Int) extends SealedTrait

  object SealedChild4 extends SealedTrait

  sealed trait SealedNest extends SealedTrait

  case class SealedNestChild(nn: Int) extends SealedNest

  implicit lazy val sealedTraitConfigs: Configs[SealedTrait] = Configs.of[SealedTrait]

  implicit lazy val sealedTraitEqual: Equal[SealedTrait] = (a, b) =>
    (a, b) match {
      case (c3a: SealedChild3, c3b: SealedChild3) => c3a.m == c3b.m
      case _                                      => a == b
    }

  implicit lazy val sealedTraitConfigVal: ConfigVal[SealedTrait] = {
    case SealedChild1(n)     => Map("n" -> n).asJava
    case SealedChild2        => "SealedChild2"
    case s: SealedChild3     => Map("m" -> s.m).asJava
    case SealedChild4        => "SealedChild4"
    case SealedNestChild(nn) => Map("nn" -> nn).asJava
  }

  implicit lazy val sealedTraitGen: Gen[SealedTrait] =
    Gen.oneOf(
      Gen[Int].map(SealedChild1),
      Gen.value(SealedChild2),
      Gen[Int].map(new SealedChild3(_)),
      Gen.value(SealedChild4),
      Gen[Int].map(SealedNestChild)
    )

}
