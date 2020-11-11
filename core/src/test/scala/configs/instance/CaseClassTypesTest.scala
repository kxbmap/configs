package configs.instance

import com.typesafe.config.ConfigFactory
import configs.{ConfigKeyNaming, ConfigReader}
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object CaseClassTypesTest extends Scalaprops {

  case class TestClass(myAttr1: String, myAttr2: String)

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
      d.isSuccess
    }
  }
}
