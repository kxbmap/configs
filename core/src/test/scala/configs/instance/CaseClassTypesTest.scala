package configs.instance

import com.typesafe.config.ConfigFactory
import configs.ConfigKeyNaming
import configs.Result.Failure
import configs.syntax.RichConfig
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object CaseClassTypesTest extends Scalaprops {

  case class TestClass(myAttr1: String, myAttr2: String, myAttr3: Option[String] = None, other: Option[String] = None, testSeal: Option[TestSeal] = None )
  case class ComplexClass(complexAttr: TestClass, myAttr3: String, myAttr4: String)

  sealed trait TestSeal
  case class Seal1(myAttr1: Option[String] = None, myAttr2: String) extends TestSeal
  case class Seal0() extends TestSeal {
  }

  val simple = {
    forAll {
      val configStr = """
          my-attr-1 = test
          my-attr-2 = test
      """
      val config = ConfigFactory.parseString(configStr)
      val d = config.extract[TestClass]
      d.isSuccess
    }
  }

  val multiNamingStrategies = {
    implicit val naming: ConfigKeyNaming[TestClass] =
      ConfigKeyNaming.lowerCamelCase[TestClass].or(ConfigKeyNaming.hyphenSeparated[TestClass].apply)
        .withFailOnSuperfluousKeys()
    forAll {
      val configStr = """
          my-attr-1 = test
          myAttr2 = test
          myAttr3 = test
      """
      val config = ConfigFactory.parseString(configStr)
      val d = config.extract[TestClass]
      d.isSuccess
    }
  }

  val complexMultiNamingStrategies = {
    // generic default naming
    implicit def myDefaultNaming[A]: ConfigKeyNaming[A] =
      ConfigKeyNaming.hyphenSeparated[A].or(ConfigKeyNaming.lowerCamelCase[A].apply)
        .withFailOnSuperfluousKeys()
    forAll {
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
      val d = config.extract[ComplexClass]
      d.isSuccess
    }
  }

  val complexMultiNamingStrategies1 = {
    // generic default naming
    implicit def myDefaultNaming[A]: ConfigKeyNaming[A] =
      ConfigKeyNaming.lowerCamelCase[A].or(ConfigKeyNaming.hyphenSeparated[A].apply)
        .withFailOnSuperfluousKeys()
    forAll {
      val configStr = """
          complex-attr = {
            my-attr-1 = test
            myAttr2 = test
            testSeal = {
              type = Seal1
              myAttr1 = test
              my-attr-2 = test
            }
          }
          my-attr-3 = test
          myAttr4 = test
      """
      val config = ConfigFactory.parseString(configStr)
      val d = config.extract[ComplexClass]
      d.isSuccess
    }
  }

  val failOnSuperfluousConfig = {
    implicit def myDefaultNaming[A]: ConfigKeyNaming[A] = ConfigKeyNaming.lowerCamelCase[A].or(ConfigKeyNaming.hyphenSeparated[A].apply)
      .withFailOnSuperfluousKeys()
    forAll {
      val configStr = """
          myAttr1 = test1
          myAttr2 = test2
          myAtrt3 = test3 // typo
      """
      val config = ConfigFactory.parseString(configStr)
      val d = config.extract[TestClass]
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
