/*
 * Copyright 2013-2017 Tsukasa Kitachi
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

package configs.beans

import com.typesafe.config.ConfigFactory
import configs.{ConfigReader, ConfigWriter}
import scala.beans.BeanProperty
import scalaprops.Property.forAll
import scalaprops.Scalaprops

object IgnoredBeanPropertiesTest extends Scalaprops {

  trait NotHaveInstance

  class SomeBean(
      @BeanProperty var intValue: Int,
      @BeanProperty var notHaveInstance: NotHaveInstance,
      @BeanProperty var nested: Nested) {
    def this() = this(0, null, null)
  }

  class Nested(@BeanProperty var intValue: Int) {
    def this() = this(0)
  }

  val ignoreOnRead = forAll {
    @ignoredBeanProperties("notHaveInstance")
    implicit val reader: ConfigReader[SomeBean] = ConfigReader.derive[SomeBean]

    val config = ConfigFactory.parseString(
      """int-value = 100
        |nested.int-value = 200
        |""".stripMargin)

    reader.extract(config).exists { b =>
      b.intValue == 100 && b.nested.intValue == 200
    }
  }

  val ignoreOnWrite = forAll {
    @ignoredBeanProperties("notHaveInstance")
    implicit val writer: ConfigWriter[SomeBean] = ConfigWriter.derive[SomeBean]

    val value = writer.write(new SomeBean(100, new NotHaveInstance {}, new Nested(200)))

    value == ConfigFactory.parseString(
      """int-value = 100
        |nested.int-value = 200
        |""".stripMargin).root()
  }

}
