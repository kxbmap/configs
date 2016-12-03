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

package configs.instance

import configs.testutil.JavaEnum
import configs.testutil.fun._
import configs.testutil.instance.enum._
import configs.testutil.instance.io._
import configs.testutil.instance.net._
import configs.testutil.instance.regex._
import configs.testutil.instance.symbol._
import configs.testutil.instance.util._
import java.io.File
import java.net.{InetAddress, URI}
import java.nio.file.Path
import java.util.regex.Pattern
import java.util.{Locale, UUID}
import scala.util.matching.Regex
import scalaprops.Scalaprops

object StringReprTypesTest extends Scalaprops {

  val symbol = check[Symbol]

  val javaEnum = check[JavaEnum]

  val uuid = check[UUID]

  val locale = check[Locale]

  val path = check[Path]

  val file = check[File]

  val inetAddress = check[InetAddress]

  val uri = check[URI]

  val regex = check[Regex]

  val pattern = check[Pattern]

}
