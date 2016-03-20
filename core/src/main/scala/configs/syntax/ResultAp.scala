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

object ResultAp {

  final class Builder2[A, B](a: Result[A], b: Result[B]) {

    def ~[X](x: Result[X]): Builder3[A, B, X] =
      new Builder3(a, b, x)

    def apply[X](fn: (A, B) => X): Result[X] =
      Result.apply2(a, b)(fn)

    def tupled: Result[(A, B)] =
      apply(Tuple2.apply)
  }

  final class Builder3[A, B, C](a: Result[A], b: Result[B], c: Result[C]) {

    def ~[X](x: Result[X]): Builder4[A, B, C, X] =
      new Builder4(a, b, c, x)

    def apply[X](fn: (A, B, C) => X): Result[X] =
      Result.apply3(a, b, c)(fn)

    def tupled: Result[(A, B, C)] =
      apply(Tuple3.apply)
  }

  final class Builder4[A, B, C, D](a: Result[A], b: Result[B], c: Result[C], d: Result[D]) {

    def ~[X](x: Result[X]): Builder5[A, B, C, D, X] =
      new Builder5(a, b, c, d, x)

    def apply[X](fn: (A, B, C, D) => X): Result[X] =
      Result.apply4(a, b, c, d)(fn)

    def tupled: Result[(A, B, C, D)] =
      apply(Tuple4.apply)
  }

  final class Builder5[A, B, C, D, E](a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E]) {

    def ~[X](x: Result[X]): Builder6[A, B, C, D, E, X] =
      new Builder6(a, b, c, d, e, x)

    def apply[X](fn: (A, B, C, D, E) => X): Result[X] =
      Result.apply5(a, b, c, d, e)(fn)

    def tupled: Result[(A, B, C, D, E)] =
      apply(Tuple5.apply)
  }

  final class Builder6[A, B, C, D, E, F](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F]) {

    def ~[X](x: Result[X]): Builder7[A, B, C, D, E, F, X] =
      new Builder7(a, b, c, d, e, f, x)

    def apply[X](fn: (A, B, C, D, E, F) => X): Result[X] =
      Result.apply6(a, b, c, d, e, f)(fn)

    def tupled: Result[(A, B, C, D, E, F)] =
      apply(Tuple6.apply)
  }

  final class Builder7[A, B, C, D, E, F, G](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G]) {

    def ~[X](x: Result[X]): Builder8[A, B, C, D, E, F, G, X] =
      new Builder8(a, b, c, d, e, f, g, x)

    def apply[X](fn: (A, B, C, D, E, F, G) => X): Result[X] =
      Result.apply7(a, b, c, d, e, f, g)(fn)

    def tupled: Result[(A, B, C, D, E, F, G)] =
      apply(Tuple7.apply)
  }

  final class Builder8[A, B, C, D, E, F, G, H](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H]) {

    def ~[X](x: Result[X]): Builder9[A, B, C, D, E, F, G, H, X] =
      new Builder9(a, b, c, d, e, f, g, h, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H) => X): Result[X] =
      Result.apply8(a, b, c, d, e, f, g, h)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H)] =
      apply(Tuple8.apply)
  }

  final class Builder9[A, B, C, D, E, F, G, H, I](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I]) {

    def ~[X](x: Result[X]): Builder10[A, B, C, D, E, F, G, H, I, X] =
      new Builder10(a, b, c, d, e, f, g, h, i, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I) => X): Result[X] =
      Result.apply9(a, b, c, d, e, f, g, h, i)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I)] =
      apply(Tuple9.apply)
  }

  final class Builder10[A, B, C, D, E, F, G, H, I, J](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J]) {

    def ~[X](x: Result[X]): Builder11[A, B, C, D, E, F, G, H, I, J, X] =
      new Builder11(a, b, c, d, e, f, g, h, i, j, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J) => X): Result[X] =
      Result.apply10(a, b, c, d, e, f, g, h, i, j)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J)] =
      apply(Tuple10.apply)
  }

  final class Builder11[A, B, C, D, E, F, G, H, I, J, K](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K]) {

    def ~[X](x: Result[X]): Builder12[A, B, C, D, E, F, G, H, I, J, K, X] =
      new Builder12(a, b, c, d, e, f, g, h, i, j, k, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K) => X): Result[X] =
      Result.apply11(a, b, c, d, e, f, g, h, i, j, k)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K)] =
      apply(Tuple11.apply)
  }

  final class Builder12[A, B, C, D, E, F, G, H, I, J, K, L](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L]) {

    def ~[X](x: Result[X]): Builder13[A, B, C, D, E, F, G, H, I, J, K, L, X] =
      new Builder13(a, b, c, d, e, f, g, h, i, j, k, l, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L) => X): Result[X] =
      Result.apply12(a, b, c, d, e, f, g, h, i, j, k, l)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L)] =
      apply(Tuple12.apply)
  }

  final class Builder13[A, B, C, D, E, F, G, H, I, J, K, L, M](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L], m: Result[M]) {

    def ~[X](x: Result[X]): Builder14[A, B, C, D, E, F, G, H, I, J, K, L, M, X] =
      new Builder14(a, b, c, d, e, f, g, h, i, j, k, l, m, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L, M) => X): Result[X] =
      Result.apply13(a, b, c, d, e, f, g, h, i, j, k, l, m)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L, M)] =
      apply(Tuple13.apply)
  }

  final class Builder14[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L], m: Result[M], n: Result[N]) {

    def ~[X](x: Result[X]): Builder15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, X] =
      new Builder15(a, b, c, d, e, f, g, h, i, j, k, l, m, n, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => X): Result[X] =
      Result.apply14(a, b, c, d, e, f, g, h, i, j, k, l, m, n)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] =
      apply(Tuple14.apply)
  }

  final class Builder15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O]) {

    def ~[X](x: Result[X]): Builder16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, X] =
      new Builder16(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => X): Result[X] =
      Result.apply15(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] =
      apply(Tuple15.apply)
  }

  final class Builder16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P]) {

    def ~[X](x: Result[X]): Builder17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, X] =
      new Builder17(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => X): Result[X] =
      Result.apply16(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] =
      apply(Tuple16.apply)
  }

  final class Builder17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P],
      q: Result[Q]) {

    def ~[X](x: Result[X]): Builder18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, X] =
      new Builder18(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => X): Result[X] =
      Result.apply17(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] =
      apply(Tuple17.apply)
  }

  final class Builder18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P],
      q: Result[Q], r: Result[R]) {

    def ~[X](x: Result[X]): Builder19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, X] =
      new Builder19(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => X): Result[X] =
      Result.apply18(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] =
      apply(Tuple18.apply)
  }

  final class Builder19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P],
      q: Result[Q], r: Result[R], s: Result[S]) {

    def ~[X](x: Result[X]): Builder20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, X] =
      new Builder20(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => X): Result[X] =
      Result.apply19(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] =
      apply(Tuple19.apply)
  }

  final class Builder20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P],
      q: Result[Q], r: Result[R], s: Result[S], t: Result[T]) {

    def ~[X](x: Result[X]): Builder21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, X] =
      new Builder21(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T) => X): Result[X] =
      Result.apply20(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] =
      apply(Tuple20.apply)
  }

  final class Builder21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P],
      q: Result[Q], r: Result[R], s: Result[S], t: Result[T], u: Result[U]) {

    def ~[X](x: Result[X]): Builder22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, X] =
      new Builder22(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, x)

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U) => X): Result[X] =
      Result.apply21(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] =
      apply(Tuple21.apply)
  }

  final class Builder22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H],
      i: Result[I], j: Result[J], k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P],
      q: Result[Q], r: Result[R], s: Result[S], t: Result[T], u: Result[U], v: Result[V]) {

    def apply[X](fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V) => X): Result[X] =
      Result.apply22(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v)(fn)

    def tupled: Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] =
      apply(Tuple22.apply)
  }

}
