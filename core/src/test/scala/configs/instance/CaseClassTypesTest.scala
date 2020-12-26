package configs.instance

import com.typesafe.config.ConfigFactory
import configs.{ConfigKeyNaming, ConfigReader}
import scalaprops.Property.forAll
import scalaprops.Scalaprops

case class TestClass(myAttr1: String, myAttr2: String, testSeal: Option[TestSeal])
case class ComplexClass(complexAttr: TestClass, myAttr3: String, myAttr4: String)
sealed trait TestSeal
case class Seal1(myAttr1: Option[String] = None, myAttr2: String) extends TestSeal
case class Seal0() extends TestSeal

object CaseClassTypesTest extends Scalaprops {

  val caseClassMultiNaming = {
    implicit val naming = ConfigKeyNaming.lowerCamelCase[TestClass].or(ConfigKeyNaming.hyphenSeparated[TestClass].apply)
    forAll {
      val reader = ConfigReader.derive[TestClass](naming)
      val configStr = """
          my-attr-1 = test
          myAttr2 = test
      """
      val config = ConfigFactory.parseString(configStr)
      val d = reader.extract(config)
      d.isSuccess &&
        d.value.myAttr1.contains("test") &&
        d.value.myAttr2.contains("test")
    }
  }

  val caseClassComplexMultiNaming = {
    // generic default naming
    implicit def myDefaultNaming[A]: ConfigKeyNaming[A] =
      ConfigKeyNaming.hyphenSeparated[A].or(ConfigKeyNaming.lowerCamelCase[A].apply)
    forAll {
      val reader = ConfigReader.derive[ComplexClass]
      val configStr = """
          complexAttr = {
            my-attr-1 = test
            myAttr2 = test
            test-seal = {
              type = Seal1
              myAttr1 = test
              my-attr-2 = test
            }
          }
          my-attr-3 = test
          myAttr4 = test
      """
      val config = ConfigFactory.parseString(configStr)
      val d = reader.extract(config)
      d.isSuccess &&
        d.value.complexAttr.testSeal.get.asInstanceOf[Seal1].myAttr1.contains("test") &&
        d.value.complexAttr.testSeal.get.asInstanceOf[Seal1].myAttr2.contains("test")
    }
  }
}
