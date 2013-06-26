/*
 * Copyright 2013 Tsukasa Kitachi
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

package com.github.kxbmap.configs.sample

object Sample extends App {

  import com.github.kxbmap.configs._
  import com.typesafe.config.ConfigFactory
  import scala.util.Try

  case class DBConfig(driver: String,
                      url: String,
                      user: Option[String],
                      password: Option[String])

  implicit val DBConfigs: Configs[DBConfig] = Configs { c =>
    import Catch.Implicits.missing
    DBConfig(
      c.get[String]("driver"),
      c.get[String]("url"),
      c.opt[String]("user"),
      c.opt[String]("password")
    )
  }

  val config = ConfigFactory.load("sample")

  val default = config.get[DBConfig]("db.default")
  println(default)

  val break = {
    import Catch.Implicits.configException
    config.opt[DBConfig]("db.break")
  }
  println(break)

  val dbs = config.get[Map[Symbol, Try[DBConfig]]]("db")
  println(dbs)

}
