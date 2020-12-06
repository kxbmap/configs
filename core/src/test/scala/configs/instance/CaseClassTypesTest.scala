package configs.instance

import com.typesafe.config.ConfigFactory
import configs.{ConfigKeyNaming, ConfigReader}
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object CaseClassTypesTest extends Scalaprops {

  case class TestClass(myAttr1: String, myAttr2: String)
  case class ComplexClass(complexAttr: TestClass, myAttr3: String, myAttr4: String)
  sealed trait TestSeal
  case class Seal1(myAttr1: Option[String] = None, myAttr2: String) extends TestSeal
  case class Seal0() extends TestSeal {
  }

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
      println(d)
      d.isSuccess &&
        d.value.myAttr1.contains("test") &&
        d.value.myAttr2.contains("test")
    }
  }

  val caseClassComplexMultiNaming = {
    // generic default naming
    implicit def myDefaultNaming[A]: ConfigKeyNaming[A] =
      ConfigKeyNaming.hyphenSeparated[A].or(ConfigKeyNaming.lowerCamelCase[A].apply)
        .withFailOnSuperfluousKeys()
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

  val failOnSuperfluousConfig = {
    implicit def myDefaultNaming[A]: ConfigKeyNaming[A] =
      ConfigKeyNaming.lowerCamelCase[A].or(ConfigKeyNaming.hyphenSeparated[A].apply)
        .withFailOnSuperfluousKeys()
    forAll {
      val reader = ConfigReader.derive[ComplexClass]
      val configStr = """
          myAttr1 = test1
          myAttr2 = test2
          myAtrt3 = test3 // typo
      """
      val config = ConfigFactory.parseString(configStr)
      val d = reader.extract(config)
      d match {
        case Failure(e) =>
          // message should contain wrong attr,
          e.messages.head.contains("myAtrt3") &&
            e.messages.head.contains("myAttr3") && // similar attributes should be mentioned in error message
            !e.messages.head.contains("other") // not enough similar attribute should not be mentioned in error message
        case _ => false
      }
    }
  }
}
