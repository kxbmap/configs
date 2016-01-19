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

package sample

import com.typesafe.config.ConfigFactory
import configs.syntax.attempt._
import configs.{Attempt, Configs}

object Sample1 extends App {

  case class A(
      a1: Int, a2: Int, a3: Int, a4: Int, a5: Int, a6: Int, a7: Int, a8: Int, a9: Int, a10: Int,
      a11: Int, a12: Int, a13: Int, a14: Int, a15: Int, a16: Int, a17: Int, a18: Int, a19: Int, a20: Int,
      a21: Int, a22: Int, a23: Int, next: Option[A])

  implicit lazy val aConfigs: Configs[A] =
    Configs.attemptOnPath { c =>
      Attempt.apply2(
        Attempt.tuple12(
          c.get[Int]("a1"), c.get[Int]("a2"), c.get[Int]("a3"), c.get[Int]("a4"), c.get[Int]("a5"),
          c.get[Int]("a6"), c.get[Int]("a7"), c.get[Int]("a8"), c.get[Int]("a9"), c.get[Int]("a10"),
          c.get[Int]("a11"), c.get[Int]("a12")
        ),
        Attempt.tuple12(
          c.get[Int]("a13"), c.get[Int]("a14"), c.get[Int]("a15"),
          c.get[Int]("a16"), c.get[Int]("a17"), c.get[Int]("a18"), c.get[Int]("a19"), c.get[Int]("a20"),
          c.get[Int]("a21"), c.get[Int]("a22"), c.get[Int]("a23"),
          c.get[Option[A]]("next")
        )
      )((t1, t2) => A(
        t1._1, t1._2, t1._3, t1._4, t1._5, t1._6, t1._7, t1._8, t1._9, t1._10, t1._11, t1._12,
        t2._1, t2._2, t2._3, t2._4, t2._5, t2._6, t2._7, t2._8, t2._9, t2._10, t2._11, t2._12
      ))
    }

  val config = ConfigFactory.parseResources("sample1.conf")

  println(config.origin())

  println("-" * 80)
  config.get[A]("success").foreach { a =>
    val expected =
      A(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, Some(
        A(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, None)))

    println(a)
    println(a == expected)
  }

  println("-" * 80)
  config.get[A]("failure").failed.foreach { e =>
    e.messages.foreach(println)
  }

}
