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

package configs.testutil.instance

import java.util.regex.Pattern
import scala.util.matching.Regex
import scalaprops.Gen
import scalaz.Equal
import scalaz.std.anyVal._
import scalaz.std.string._
import scalaz.std.tuple._

object regex {

  private val regexStringGen: Gen[String] =
    Gen.nonEmptyString(Gen.alphaNumChar)

  implicit val patternGen: Gen[Pattern] =
    regexStringGen.map(Pattern.compile)

  implicit val patternEqual: Equal[Pattern] =
    Equal.equalBy(p => (p.pattern(), p.flags()))

  implicit val regexGen: Gen[Regex] =
    regexStringGen.map(_.r)

  implicit val regexEqual: Equal[Regex] =
    Equal.equalBy(_.pattern)

}
