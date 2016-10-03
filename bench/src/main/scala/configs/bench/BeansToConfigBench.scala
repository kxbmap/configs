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

package configs.bench

import configs.testutil.Bean484
import configs.{ConfigValue, ConfigWriter}
import org.openjdk.jmh.annotations.{Benchmark, Fork, Measurement, Scope, Setup, State, Threads, Warmup}
import scala.collection.JavaConverters._
import scala.util.Random

@Fork(1)
@Threads(1)
@Warmup(iterations = 3)
@Measurement(iterations = 3)
@State(Scope.Thread)
class BeansToConfigBench {

  private[this] var bean: Bean484 = _
  private[this] var derivedWriter: ConfigWriter[Bean484] = _
  private[this] var handwrittenWriter: ConfigWriter[Bean484] = _

  @Setup
  def prepare(): Unit = {
    bean = Bean484.fromArray(Array.fill(484)(Random.nextInt()))
    derivedWriter = ConfigWriter.derive[Bean484]
    handwrittenWriter = ConfigWriter.by(
      _.values().asScala.zipWithIndex.map {
        case (n, i) => s"a${i + 1}" -> n.intValue()
      }.toMap)
  }

  @Benchmark
  def derived(): ConfigValue = {
    derivedWriter.write(bean)
  }

  @Benchmark
  def handwritten(): ConfigValue = {
    handwrittenWriter.write(bean)
  }

}
