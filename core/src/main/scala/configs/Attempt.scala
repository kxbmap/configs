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

package configs

import scala.collection.generic.CanBuildFrom

sealed abstract class Attempt[+A] extends Product with Serializable {

  def fold[B](ifFailure: ConfigError => B, ifSuccess: A => B): B

  def map[B](f: A => B): Attempt[B]

  def flatMap[B](f: A => Attempt[B]): Attempt[B]

  def flatten[B](implicit ev: A <:< Attempt[B]): Attempt[B]

  def ap[B](f: Attempt[A => B]): Attempt[B]

  def orElse[B >: A](fallback: => Attempt[B]): Attempt[B]

  def getOrElse[B >: A](default: => B): B

  def rescue[B >: A](f: ConfigError => B): B

  def handleWith[B >: A](pf: PartialFunction[ConfigError, Attempt[B]]): Attempt[B]

  final def handle[B >: A](pf: PartialFunction[ConfigError, B]): Attempt[B] =
    handleWith(pf.andThen(Attempt.successful))

  def mapError(f: ConfigError => ConfigError): Attempt[A]

  def exists(f: A => Boolean): Boolean

  final def forall(f: A => Boolean): Boolean =
    !exists(f)

  def foreach(f: A => Unit): Unit

  def failed: Attempt[ConfigError]

  def toOption: Option[A]

  def toEither: Either[ConfigError, A]
}


object Attempt {

  def apply[A](a: => A): Attempt[A] =
    try
      Success(a)
    catch {
      case e: Throwable => fromThrowable(e)
    }

  def successful[A](value: A): Attempt[A] =
    Success(value)

  def failure[A](e: ConfigError): Attempt[A] =
    Failure(e)

  def fromEither[A](either: Either[ConfigError, A]): Attempt[A] =
    either.fold(Failure, Success(_))

  def fromThrowable[A](throwable: Throwable): Attempt[A] =
    Failure(ConfigError.fromThrowable(throwable))


  final case class Success[A](value: A) extends Attempt[A] {

    def fold[B](ifFailure: ConfigError => B, ifSuccess: A => B): B =
      ifSuccess(value)

    def map[B](f: A => B): Attempt[B] =
      Attempt(f(value))

    def flatMap[B](f: A => Attempt[B]): Attempt[B] =
      try
        f(value)
      catch {
        case e: Throwable => fromThrowable(e)
      }

    def flatten[B](implicit ev: A <:< Attempt[B]): Attempt[B] =
      value

    def ap[B](f: Attempt[A => B]): Attempt[B] =
      f match {
        case Success(f0)   => Attempt(f0(value))
        case fa@Failure(_) => fa
      }

    def orElse[B >: A](fallback: => Attempt[B]): Attempt[B] =
      this

    def getOrElse[B >: A](default: => B): B =
      value

    def rescue[B >: A](f: ConfigError => B): B =
      value

    def handleWith[B >: A](pf: PartialFunction[ConfigError, Attempt[B]]): Attempt[B] =
      this

    def mapError(f: ConfigError => ConfigError): Attempt[A] =
      this

    def exists(f: A => Boolean): Boolean =
      f(value)

    def foreach(f: A => Unit): Unit =
      f(value)

    def failed: Attempt[ConfigError] =
      Failure(ConfigError.Generic(new UnsupportedOperationException("Success.failed")))

    def toOption: Option[A] =
      Some(value)

    def toEither: Either[ConfigError, A] =
      Right(value)

  }


  final case class Failure(error: ConfigError) extends Attempt[Nothing] {

    def fold[B](ifFailure: ConfigError => B, ifSuccess: Nothing => B): B =
      ifFailure(error)

    def map[B](f: Nothing => B): Attempt[B] =
      this

    def flatMap[B](f: Nothing => Attempt[B]): Attempt[B] =
      this

    def flatten[B](implicit ev: Nothing <:< Attempt[B]): Attempt[B] =
      this

    def ap[B](f: Attempt[Nothing => B]): Attempt[B] =
      f match {
        case Success(_) => this
        case Failure(e) => Failure(e + error)
      }

    def orElse[B >: Nothing](fallback: => Attempt[B]): Attempt[B] =
      fallback

    def getOrElse[B >: Nothing](default: => B): B =
      default

    def rescue[B >: Nothing](f: ConfigError => B): B =
      f(error)

    def handleWith[B >: Nothing](pf: PartialFunction[ConfigError, Attempt[B]]): Attempt[B] =
      try
        if (pf.isDefinedAt(error)) pf(error) else this
      catch {
        case e: Throwable => fromThrowable(e)
      }

    def mapError(f: ConfigError => ConfigError): Attempt[Nothing] =
      try
        Failure(f(error))
      catch {
        case e: Throwable => fromThrowable(e)
      }

    def exists(f: Nothing => Boolean): Boolean =
      false

    def foreach(f: Nothing => Unit): Unit =
      ()

    def failed: Attempt[ConfigError] =
      Success(error)

    def toOption: Option[Nothing] =
      None

    def toEither: Either[ConfigError, Nothing] =
      Left(error)
  }


  def sequence[F[X] <: TraversableOnce[X], A](fa: F[Attempt[A]])(implicit cbf: CanBuildFrom[F[Attempt[A]], A, F[A]]): Attempt[F[A]] =
    fa.foldLeft(successful(cbf(fa)))(apply2(_, _)(_ += _)).map(_.result())


  def apply2[A, B, C](a: Attempt[A], b: Attempt[B])(fn: (A, B) => C): Attempt[C] =
    b.ap(a.map(fn.curried))

  def apply3[A, B, C, D](a: Attempt[A], b: Attempt[B], c: Attempt[C])(fn: (A, B, C) => D): Attempt[D] =
    c.ap(b.ap(a.map(fn.curried)))

  def apply4[A, B, C, D, E](a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D])(fn: (A, B, C, D) => E): Attempt[E] =
    d.ap(c.ap(b.ap(a.map(fn.curried))))

  def apply5[A, B, C, D, E, F](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E])(fn: (A, B, C, D, E) => F): Attempt[F] =
    e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))

  def apply6[A, B, C, D, E, F, G](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F])(
      fn: (A, B, C, D, E, F) => G): Attempt[G] =
    f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))

  def apply7[A, B, C, D, E, F, G, H](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G])(
      fn: (A, B, C, D, E, F, G) => H): Attempt[H] =
    g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))

  def apply8[A, B, C, D, E, F, G, H, I](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H])(
      fn: (A, B, C, D, E, F, G, H) => I): Attempt[I] =
    h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))

  def apply9[A, B, C, D, E, F, G, H, I, J](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I])(
      fn: (A, B, C, D, E, F, G, H, I) => J): Attempt[J] =
    i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))

  def apply10[A, B, C, D, E, F, G, H, I, J, K](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J])(
      fn: (A, B, C, D, E, F, G, H, I, J) => K): Attempt[K] =
    j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))

  def apply11[A, B, C, D, E, F, G, H, I, J, K, L](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K])(
      fn: (A, B, C, D, E, F, G, H, I, J, K) => L): Attempt[L] =
    k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))

  def apply12[A, B, C, D, E, F, G, H, I, J, K, L, M](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L) => M): Attempt[M] =
    l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))

  def apply13[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M) => N): Attempt[N] =
    m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))))

  def apply14[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => O): Attempt[O] =
    n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))))

  def apply15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => P): Attempt[P] =
    o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))))))

  def apply16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => Q): Attempt[Q] =
    p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))))))

  def apply17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => R): Attempt[R] =
    q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))))))))

  def apply18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q], r: Attempt[R])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => S): Attempt[S] =
    r.ap(q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))))))))

  def apply19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q], r: Attempt[R], s: Attempt[S])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => T): Attempt[T] =
    s.ap(r.ap(q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))))))))))

  def apply20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q], r: Attempt[R], s: Attempt[S], t: Attempt[T])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T) => U): Attempt[U] =
    t.ap(s.ap(r.ap(q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))))))))))

  def apply21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q], r: Attempt[R], s: Attempt[S], t: Attempt[T],
      u: Attempt[U])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U) => V): Attempt[V] =
    u.ap(t.ap(s.ap(r.ap(q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))))))))))))

  def apply22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q], r: Attempt[R], s: Attempt[S], t: Attempt[T],
      u: Attempt[U], v: Attempt[V])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V) => W): Attempt[W] =
    v.ap(u.ap(t.ap(s.ap(r.ap(q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))))))))))))


  def tuple2[A, B](a: Attempt[A], b: Attempt[B]): Attempt[(A, B)] =
    apply2(a, b)((_, _))

  def tuple3[A, B, C](a: Attempt[A], b: Attempt[B], c: Attempt[C]): Attempt[(A, B, C)] =
    apply3(a, b, c)((_, _, _))

  def tuple4[A, B, C, D](a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D]): Attempt[(A, B, C, D)] =
    apply4(a, b, c, d)((_, _, _, _))

  def tuple5[A, B, C, D, E](a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E]): Attempt[(A, B, C, D, E)] =
    apply5(a, b, c, d, e)((_, _, _, _, _))

  def tuple6[A, B, C, D, E, F](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F]): Attempt[(A, B, C, D, E, F)] =
    apply6(a, b, c, d, e, f)((_, _, _, _, _, _))

  def tuple7[A, B, C, D, E, F, G](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G]): Attempt[(A, B, C, D, E, F, G)] =
    apply7(a, b, c, d, e, f, g)((_, _, _, _, _, _, _))

  def tuple8[A, B, C, D, E, F, G, H](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H]): Attempt[(A, B, C, D, E, F, G, H)] =
    apply8(a, b, c, d, e, f, g, h)((_, _, _, _, _, _, _, _))

  def tuple9[A, B, C, D, E, F, G, H, I](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I]): Attempt[(A, B, C, D, E, F, G, H, I)] =
    apply9(a, b, c, d, e, f, g, h, i)((_, _, _, _, _, _, _, _, _))

  def tuple10[A, B, C, D, E, F, G, H, I, J](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J]): Attempt[(A, B, C, D, E, F, G, H, I, J)] =
    apply10(a, b, c, d, e, f, g, h, i, j)((_, _, _, _, _, _, _, _, _, _))

  def tuple11[A, B, C, D, E, F, G, H, I, J, K](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K]): Attempt[(A, B, C, D, E, F, G, H, I, J, K)] =
    apply11(a, b, c, d, e, f, g, h, i, j, k)((_, _, _, _, _, _, _, _, _, _, _))

  def tuple12[A, B, C, D, E, F, G, H, I, J, K, L](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L)] =
    apply12(a, b, c, d, e, f, g, h, i, j, k, l)((_, _, _, _, _, _, _, _, _, _, _, _))

  def tuple13[A, B, C, D, E, F, G, H, I, J, K, L, M](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L, M)] =
    apply13(a, b, c, d, e, f, g, h, i, j, k, l, m)((_, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple14[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] =
    apply14(a, b, c, d, e, f, g, h, i, j, k, l, m, n)((_, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] =
    apply15(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] =
    apply16(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] =
    apply17(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q], r: Attempt[R]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] =
    apply18(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q], r: Attempt[R], s: Attempt[S]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] =
    apply19(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q], r: Attempt[R], s: Attempt[S], t: Attempt[T]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] =
    apply20(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q], r: Attempt[R], s: Attempt[S], t: Attempt[T],
      u: Attempt[U]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] =
    apply21(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
      a: Attempt[A], b: Attempt[B], c: Attempt[C], d: Attempt[D], e: Attempt[E], f: Attempt[F], g: Attempt[G], h: Attempt[H], i: Attempt[I], j: Attempt[J],
      k: Attempt[K], l: Attempt[L], m: Attempt[M], n: Attempt[N], o: Attempt[O], p: Attempt[P], q: Attempt[Q], r: Attempt[R], s: Attempt[S], t: Attempt[T],
      u: Attempt[U], v: Attempt[V]): Attempt[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] =
    apply22(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

}
