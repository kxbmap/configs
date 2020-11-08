package configs.instance

import com.typesafe.config.ConfigFactory
import configs.ConfigKeyNaming
import configs.Result.Failure
import configs.syntax.RichConfig
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object CaseClassTypesTest extends Scalaprops {

  case class TestClass(myAttr1: String, myAttr2: String)
  case class ComplexClass(complexAttr: TestClass, myAttr3: String, myAttr4: String)

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
    implicit val naming = ConfigKeyNaming.lowerCamelCase[TestClass].or(ConfigKeyNaming.hyphenSeparated[TestClass].apply)
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
    implicit def defaultNaming[A]: ConfigKeyNaming[A] =
      ConfigKeyNaming.lowerCamelCase[A].or(ConfigKeyNaming.hyphenSeparated[A].apply)
    forAll {
      val configStr = """
          complexAttr = {
            my-attr-1 = test
            myAttr2 = test
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
    implicit def defaultNaming[A]: ConfigKeyNaming[A] = ConfigKeyNaming.lowerCamelCase[A].or(ConfigKeyNaming.hyphenSeparated[A].apply)
      .withFailOnSuperfluousKeys()
    forAll {
      val configStr = """
          myAttr1 = test
          myAttr2 = test
          myAttrFail = test
      """
      val config = ConfigFactory.parseString(configStr)
      val d = config.extract[TestClass]
      d match {
        case Failure(e) => e.messages.head.contains("myAttrFail")
        case _ => false
      }
    }
  }

}
