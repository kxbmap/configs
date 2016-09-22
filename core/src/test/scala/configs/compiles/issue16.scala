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

package configs.compiles

import com.typesafe.config.ConfigFactory.empty
import configs.syntax._

// Issue #16

case class TestConfiguration(test2: Option[Test2Configuration])

case class Test2Configuration(testOption: Option[Int])

object TestConfiguration {
  lazy val configuration: TestConfiguration = empty.get[TestConfiguration]("").value
}

// OK
case class Config1(parent: Config1.Parent)
object Config1 {
  case class Parent(child: Parent.Child)
  object Parent {
    case class Child()
  }
  lazy val configObject: Config1 = empty.get[Config1]("").value
}

// add A to parent param name - OK
case class Config2(parentA: Config2.Parent)
object Config2 {
  case class Parent(child: Parent.Child)
  object Parent {
    case class Child()
  }
  lazy val configObject: Config2 = empty.get[Config2]("").value
}

// add A to child param name (without the one on parent) - OK
case class Config3(parent: Config3.Parent)
object Config3 {
  case class Parent(childA: Parent.Child)
  object Parent {
    case class Child()
  }
  lazy val configObject: Config3 = empty.get[Config3]("").value
}

// add A to both parent & child param name - FAILS
case class Config4(parentA: Config4.Parent)
object Config4 {
  case class Parent(childA: Parent.Child)
  object Parent {
    case class Child()
  }
  lazy val configObject: Config4 = empty.get[Config4]("").value
}
