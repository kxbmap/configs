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

package configs.syntax

import configs.Result
import configs.testutil.instance.result._
import configs.testutil.instance.string._
import scalaprops.Property.{forAll, forAllG}
import scalaprops.{Gen, Properties, Scalaprops}

object ResultBuilderTest extends Scalaprops {

  val builder2 = Properties.list(
    forAll { (a: Result[Int], b: Result[Int], fn: (Int, Int) => Int) =>
      (a ~ b)(fn) == Result.apply2(a, b)(fn)
    }.toProperties("apply"),
    forAll { (a: Result[Int], b: Result[Int]) =>
      (a ~ b).tupled == Result.tuple2(a, b)
    }.toProperties("tupled")
  )

  val builder3 = Properties.list(
    forAll { (a: Result[Int], b: Result[Int], c: Result[Int], fn: (Int, Int, Int) => Int) =>
      (a ~ b ~ c)(fn) == Result.apply3(a, b, c)(fn)
    }.toProperties("apply"),
    forAll { (a: Result[Int], b: Result[Int], c: Result[Int]) =>
      (a ~ b ~ c).tupled == Result.tuple3(a, b, c)
    }.toProperties("tupled")
  )

  val builder4 = Properties.list(
    forAll { (a: Result[Int], b: Result[Int], c: Result[Int], d: Result[Int], fn: (Int, Int, Int, Int) => Int) =>
      (a ~ b ~ c ~ d)(fn) == Result.apply4(a, b, c, d)(fn)
    }.toProperties("apply"),
    forAll { (a: Result[Int], b: Result[Int], c: Result[Int], d: Result[Int]) =>
      (a ~ b ~ c ~ d).tupled == Result.tuple4(a, b, c, d)
    }.toProperties("tupled")
  )

  val builder5 = {
    val gr = Gen[(Result[Int], Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e), fn) =>
        (a ~ b ~ c ~ d ~ e)(fn) == Result.apply5(a, b, c, d, e)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e) =>
        (a ~ b ~ c ~ d ~ e).tupled == Result.tuple5(a, b, c, d, e)
      }.toProperties("tupled")
    )
  }

  val builder6 = {
    val gr = Gen[(Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f)(fn) == Result.apply6(a, b, c, d, e, f)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f) =>
        (a ~ b ~ c ~ d ~ e ~ f).tupled == Result.tuple6(a, b, c, d, e, f)
      }.toProperties("tupled")
    )
  }

  val builder7 = {
    val gr = Gen[(Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g)(fn) == Result.apply7(a, b, c, d, e, f, g)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g).tupled == Result.tuple7(a, b, c, d, e, f, g)
      }.toProperties("tupled")
    )
  }

  val builder8 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h)(fn) == Result.apply8(a, b, c, d, e, f, g, h)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h).tupled == Result.tuple8(a, b, c, d, e, f, g, h)
      }.toProperties("tupled")
    )
  }

  val builder9 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i)(fn) == Result.apply9(a, b, c, d, e, f, g, h, i)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i).tupled == Result.tuple9(a, b, c, d, e, f, g, h, i)
      }.toProperties("tupled")
    )
  }

  val builder10 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j)(fn) == Result.apply10(a, b, c, d, e, f, g, h, i, j)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j).tupled == Result.tuple10(a, b, c, d, e, f, g, h, i, j)
      }.toProperties("tupled")
    )
  }

  val builder11 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k)(fn) == Result.apply11(a, b, c, d, e, f, g, h, i, j, k)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k).tupled == Result.tuple11(a, b, c, d, e, f, g, h, i, j, k)
      }.toProperties("tupled")
    )
  }

  val builder12 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l)(fn) == Result.apply12(a, b, c, d, e, f, g, h, i, j, k, l)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l).tupled == Result.tuple12(a, b, c, d, e, f, g, h, i, j, k, l)
      }.toProperties("tupled")
    )
  }

  val builder13 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l, m), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m)(fn) ==
          Result.apply13(a, b, c, d, e, f, g, h, i, j, k, l, m)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l, m) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m).tupled ==
          Result.tuple13(a, b, c, d, e, f, g, h, i, j, k, l, m)
      }.toProperties("tupled")
    )
  }

  val builder14 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l, m, n), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n)(fn) ==
          Result.apply14(a, b, c, d, e, f, g, h, i, j, k, l, m, n)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l, m, n) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n).tupled ==
          Result.tuple14(a, b, c, d, e, f, g, h, i, j, k, l, m, n)
      }.toProperties("tupled")
    )
  }

  val builder15 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l, m, n, o), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o)(fn) ==
          Result.apply15(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o).tupled ==
          Result.tuple15(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)
      }.toProperties("tupled")
    )
  }

  val builder16 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p)(fn) ==
          Result.apply16(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p).tupled ==
          Result.tuple16(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)
      }.toProperties("tupled")
    )
  }

  val builder17 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q)(fn) ==
          Result.apply17(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q).tupled ==
          Result.tuple17(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)
      }.toProperties("tupled")
    )
  }

  val builder18 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q ~ r)(fn) ==
          Result.apply18(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q ~ r).tupled ==
          Result.tuple18(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)
      }.toProperties("tupled")
    )
  }

  val builder19 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int])]
    val gf = Gen[(Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q ~ r ~ s)(fn) ==
          Result.apply19(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q ~ r ~ s).tupled ==
          Result.tuple19(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s)
      }.toProperties("tupled")
    )
  }

  val builder20 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[
      (Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q ~ r ~ s ~ t)(fn) ==
          Result.apply20(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q ~ r ~ s ~ t).tupled ==
          Result.tuple20(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t)
      }.toProperties("tupled")
    )
  }

  val builder21 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[
      (Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q ~ r ~ s ~ t ~ u)(fn) ==
          Result.apply21(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q ~ r ~ s ~ t ~ u).tupled ==
          Result.tuple21(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u)
      }.toProperties("tupled")
    )
  }

  val builder22 = {
    val gr = Gen[
      (Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int],
        Result[Int], Result[Int], Result[Int], Result[Int], Result[Int], Result[Int])]
    val gf = Gen[
      (Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int, Int) => Int]
    Properties.list(
      forAllG(gr, gf) { case ((a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v), fn) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q ~ r ~ s ~ t ~ u ~ v)(fn) ==
          Result.apply22(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v)(fn)
      }.toProperties("apply"),
      forAllG(gr) { case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v) =>
        (a ~ b ~ c ~ d ~ e ~ f ~ g ~ h ~ i ~ j ~ k ~ l ~ m ~ n ~ o ~ p ~ q ~ r ~ s ~ t ~ u ~ v).tupled ==
          Result.tuple22(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v)
      }.toProperties("tupled")
    )
  }

}
