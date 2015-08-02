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
    ConfigVal.asMap(s => Map("user" -> s.user, "password" -> s.password))

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
    ConfigVal.asMap(s => Map(
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
    ConfigVal.asMap(s => Map(
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
    ConfigVal.asMap(s => Map(
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
    sub1.toProperties("most specific"),
    sub2.toProperties("others"),
    ignorePrivate.toProperties("ignore private"),
    primaryFirst.toProperties("primary first")
  )

  private lazy val sub1 = forAll { (first: String, last: String, age: Int) =>
    val expected = new SubCtorsSetting(first, last, age)
    val config = ConfigFactory.parseString(s"firstName = ${q(first)}, lastName = ${q(last)}, age = $age")
    Equal[SubCtorsSetting].equal(Configs[SubCtorsSetting].extract(config), expected)
  }

  private lazy val sub2 = forAll { (first: String, last: String) =>
    val expected = new SubCtorsSetting(first, last)
    val config = ConfigFactory.parseString(s"firstName = ${q(first)}, lastName = ${q(last)}")
    Equal[SubCtorsSetting].equal(Configs[SubCtorsSetting].extract(config), expected)
  }

  private lazy val ignorePrivate = intercept { (name: String, age: Int) =>
    val config = ConfigFactory.parseString(s"firstName = ${q(name)}, age = $age")
    Configs[SubCtorsSetting].extract(config)
  } {
    case _: ConfigException => true
  }

  private lazy val primaryFirst = forAll { (first: String, last: String, name: String, age: Int, country: String) =>
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

  class SubCtorsSetting(val name: String, val age: Int, val country: String) {

    private def this(name: String, age: Int) = this(name, age, "JPN")

    def this(firstName: String, lastName: String) = this(s"$firstName $lastName", 0)

    def this(firstName: String, lastName: String, age: Int) = this(s"$firstName $lastName", age)
  }

  implicit lazy val subCtorsSettingConfigVal: ConfigVal[SubCtorsSetting] =
    ConfigVal.asMap(s => Map(
      "name" -> s.name,
      "age" -> s.age,
      "country" -> s.country
    ))

  implicit lazy val subCtorsSettingGen: Gen[SubCtorsSetting] =
    Apply[Gen].apply3(Gen[String], Gen[Int], Gen[String])(new SubCtorsSetting(_, _, _))

  implicit lazy val subCtorsSettingEqual: Equal[SubCtorsSetting] =
    (s1, s2) => s1.name == s2.name && s1.age == s2.age && s1.country == s2.country


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
    ConfigVal.asMap(s => Map(
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

}
