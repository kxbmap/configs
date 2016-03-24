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

import configs.testutil.fun._
import configs.testutil.instance.duration._
import scala.concurrent.duration.{Duration, FiniteDuration}
import scalaprops.Scalaprops
import java.{time => jt}

object DurationTypesTest extends Scalaprops {

  val finiteDuration = check[FiniteDuration]

  val duration = check[Duration]

  val javaDuration = check[jt.Duration]

}
