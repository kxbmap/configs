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

import configs.testutil.gen._
import java.{time => jt}
import scala.concurrent.duration.{Duration, FiniteDuration}
import scalaprops.{Choose, Gen}
import scalaz.{Apply, Equal}

object duration {

  implicit lazy val finiteDurationGen: Gen[FiniteDuration] =
    javaDurationGen.map(d => Duration.fromNanos(d.toNanos))

  implicit lazy val finiteDurationEqual: Equal[FiniteDuration] =
    Equal.equalA[FiniteDuration]


  implicit lazy val durationGen: Gen[Duration] =
    Gen.frequency(
      5 -> Gen.value(Duration.Inf),
      5 -> Gen.value(Duration.MinusInf),
      5 -> Gen.value(Duration.Undefined),
      85 -> finiteDurationGen.as[Duration]
    )

  implicit lazy val durationEqual: Equal[Duration] =
    Equal.equalA[Duration]


  implicit lazy val javaDurationGen: Gen[jt.Duration] = {
    def duration(u: Long, f: Long => jt.Duration): Gen[jt.Duration] =
      Gen.oneOf(
        Choose[Long].withBoundaries(0, Long.MaxValue / u),
        Choose[Long].withBoundaries(Long.MinValue / (u * 1024L/*avoid precision loss*/), -1)
      ).map(f)
    Gen.oneOf(
      duration(24L * 60L * 60L * 1000000000L, jt.Duration.ofDays),
      duration(60L * 60L * 1000000000L, jt.Duration.ofHours),
      duration(60L * 1000000000L, jt.Duration.ofMinutes),
      duration(1000000000L, jt.Duration.ofSeconds),
      duration(1000000L, jt.Duration.ofMillis),
      duration(1000L, us => jt.Duration.ofNanos(us * 1000L)),
      duration(1L, jt.Duration.ofNanos)
    )
  }

  lazy val javaDurationAllGen: Gen[jt.Duration] =
    Apply[Gen].apply2(Gen[Long], Choose[Int].withBoundaries(0, 999999999)) {
      jt.Duration.ofSeconds(_, _)
    }

  implicit lazy val javaDurationEqual: Equal[jt.Duration] =
    Equal.equalA[jt.Duration]

}
