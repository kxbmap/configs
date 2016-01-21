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

sealed abstract class Result[+A] extends Product with Serializable {

  def fold[B](ifFailure: ConfigError => B, ifSuccess: A => B): B

  def map[B](f: A => B): Result[B]

  def flatMap[B](f: A => Result[B]): Result[B]

  def flatten[B](implicit ev: A <:< Result[B]): Result[B]

  def ap[B](f: Result[A => B]): Result[B]

  def orElse[B >: A](fallback: => Result[B]): Result[B]

  def getOrElse[B >: A](default: => B): B

  def getOrHandle[B >: A](f: ConfigError => B): B

  final def getOrThrow: A =
    getOrHandle(e => throw e.toConfigException)

  def handleWith[B >: A](pf: PartialFunction[ConfigError, Result[B]]): Result[B]

  final def handle[B >: A](pf: PartialFunction[ConfigError, B]): Result[B] =
    handleWith(pf.andThen(Result.successful))

  def mapError(f: ConfigError => ConfigError): Result[A]

  def exists(f: A => Boolean): Boolean

  final def forall(f: A => Boolean): Boolean =
    !exists(f)

  def foreach(f: A => Unit): Unit

  def failed: Result[ConfigError]

  def toOption: Option[A]

  def toEither: Either[ConfigError, A]
}


object Result {

  def Try[A](a: => A): Result[A] =
    try
      Success(a)
    catch {
      case e: Throwable => fromThrowable(e)
    }

  def successful[A](value: A): Result[A] =
    Success(value)

  def failure[A](e: ConfigError): Result[A] =
    Failure(e)

  def fromEither[A](either: Either[ConfigError, A]): Result[A] =
    either.fold(Failure, Success(_))

  def fromThrowable[A](throwable: Throwable): Result[A] =
    failure(ConfigError.fromThrowable(throwable))


  final case class Success[A](value: A) extends Result[A] {

    override def fold[B](ifFailure: ConfigError => B, ifSuccess: A => B): B =
      ifSuccess(value)

    override def map[B](f: A => B): Result[B] =
      Result.Try(f(value))

    override def flatMap[B](f: A => Result[B]): Result[B] =
      try
        f(value)
      catch {
        case e: Throwable => fromThrowable(e)
      }

    override def flatten[B](implicit ev: A <:< Result[B]): Result[B] =
      value

    override def ap[B](f: Result[A => B]): Result[B] =
      f match {
        case Success(f0)   => Result.Try(f0(value))
        case fa@Failure(_) => fa
      }

    override def orElse[B >: A](fallback: => Result[B]): Result[B] =
      this

    override def getOrElse[B >: A](default: => B): B =
      value

    override def getOrHandle[B >: A](f: ConfigError => B): B =
      value

    override def handleWith[B >: A](pf: PartialFunction[ConfigError, Result[B]]): Result[B] =
      this

    override def mapError(f: ConfigError => ConfigError): Result[A] =
      this

    override def exists(f: A => Boolean): Boolean =
      f(value)

    override def foreach(f: A => Unit): Unit =
      f(value)

    override def failed: Result[ConfigError] =
      Failure(ConfigError("Success.failed"))

    override def toOption: Option[A] =
      Some(value)

    override def toEither: Either[ConfigError, A] =
      Right(value)

  }


  final case class Failure(error: ConfigError) extends Result[Nothing] {

    override def fold[B](ifFailure: ConfigError => B, ifSuccess: Nothing => B): B =
      ifFailure(error)

    override def map[B](f: Nothing => B): Result[B] =
      this

    override def flatMap[B](f: Nothing => Result[B]): Result[B] =
      this

    override def flatten[B](implicit ev: Nothing <:< Result[B]): Result[B] =
      this

    override def ap[B](f: Result[Nothing => B]): Result[B] =
      f match {
        case Success(_)  => this
        case Failure(es) => Failure(es ++ error)
      }

    override def orElse[B >: Nothing](fallback: => Result[B]): Result[B] =
      fallback

    override def getOrElse[B >: Nothing](default: => B): B =
      default

    override def getOrHandle[B >: Nothing](f: ConfigError => B): B =
      f(error)

    override def handleWith[B >: Nothing](pf: PartialFunction[ConfigError, Result[B]]): Result[B] =
      try
        if (pf.isDefinedAt(error)) pf(error) else this
      catch {
        case e: Throwable => fromThrowable(e)
      }

    override def mapError(f: ConfigError => ConfigError): Result[Nothing] =
      try
        Failure(f(error))
      catch {
        case e: Throwable => fromThrowable(e)
      }

    override def exists(f: Nothing => Boolean): Boolean =
      false

    override def foreach(f: Nothing => Unit): Unit =
      ()

    override def failed: Result[ConfigError] =
      Success(error)

    override def toOption: Option[Nothing] =
      None

    override def toEither: Either[ConfigError, Nothing] =
      Left(error)
  }


  def sequence[F[X] <: TraversableOnce[X], A](fa: F[Result[A]])(implicit cbf: CanBuildFrom[F[Result[A]], A, F[A]]): Result[F[A]] =
    fa.foldLeft(successful(cbf(fa)))(apply2(_, _)(_ += _)).map(_.result())


  def apply2[A, B, C](a: Result[A], b: Result[B])(fn: (A, B) => C): Result[C] =
    b.ap(a.map(fn.curried))

  def apply3[A, B, C, D](a: Result[A], b: Result[B], c: Result[C])(fn: (A, B, C) => D): Result[D] =
    c.ap(b.ap(a.map(fn.curried)))

  def apply4[A, B, C, D, E](a: Result[A], b: Result[B], c: Result[C], d: Result[D])(fn: (A, B, C, D) => E): Result[E] =
    d.ap(c.ap(b.ap(a.map(fn.curried))))

  def apply5[A, B, C, D, E, F](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E])(fn: (A, B, C, D, E) => F): Result[F] =
    e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))

  def apply6[A, B, C, D, E, F, G](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F])(
      fn: (A, B, C, D, E, F) => G): Result[G] =
    f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))

  def apply7[A, B, C, D, E, F, G, H](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G])(
      fn: (A, B, C, D, E, F, G) => H): Result[H] =
    g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))

  def apply8[A, B, C, D, E, F, G, H, I](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H])(
      fn: (A, B, C, D, E, F, G, H) => I): Result[I] =
    h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))

  def apply9[A, B, C, D, E, F, G, H, I, J](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I])(
      fn: (A, B, C, D, E, F, G, H, I) => J): Result[J] =
    i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))

  def apply10[A, B, C, D, E, F, G, H, I, J, K](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J])(
      fn: (A, B, C, D, E, F, G, H, I, J) => K): Result[K] =
    j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))

  def apply11[A, B, C, D, E, F, G, H, I, J, K, L](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K])(
      fn: (A, B, C, D, E, F, G, H, I, J, K) => L): Result[L] =
    k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))

  def apply12[A, B, C, D, E, F, G, H, I, J, K, L, M](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L) => M): Result[M] =
    l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))

  def apply13[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M) => N): Result[N] =
    m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))))

  def apply14[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N) => O): Result[O] =
    n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))))

  def apply15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O) => P): Result[P] =
    o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))))))

  def apply16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P) => Q): Result[Q] =
    p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))))))

  def apply17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q) => R): Result[R] =
    q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))))))))

  def apply18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R) => S): Result[S] =
    r.ap(q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))))))))

  def apply19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S) => T): Result[T] =
    s.ap(r.ap(q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))))))))))

  def apply20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S], t: Result[T])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T) => U): Result[U] =
    t.ap(s.ap(r.ap(q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))))))))))

  def apply21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S], t: Result[T],
      u: Result[U])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U) => V): Result[V] =
    u.ap(t.ap(s.ap(r.ap(q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried)))))))))))))))))))))

  def apply22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S], t: Result[T],
      u: Result[U], v: Result[V])(
      fn: (A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V) => W): Result[W] =
    v.ap(u.ap(t.ap(s.ap(r.ap(q.ap(p.ap(o.ap(n.ap(m.ap(l.ap(k.ap(j.ap(i.ap(h.ap(g.ap(f.ap(e.ap(d.ap(c.ap(b.ap(a.map(fn.curried))))))))))))))))))))))


  def tuple2[A, B](a: Result[A], b: Result[B]): Result[(A, B)] =
    apply2(a, b)((_, _))

  def tuple3[A, B, C](a: Result[A], b: Result[B], c: Result[C]): Result[(A, B, C)] =
    apply3(a, b, c)((_, _, _))

  def tuple4[A, B, C, D](a: Result[A], b: Result[B], c: Result[C], d: Result[D]): Result[(A, B, C, D)] =
    apply4(a, b, c, d)((_, _, _, _))

  def tuple5[A, B, C, D, E](a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E]): Result[(A, B, C, D, E)] =
    apply5(a, b, c, d, e)((_, _, _, _, _))

  def tuple6[A, B, C, D, E, F](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F]): Result[(A, B, C, D, E, F)] =
    apply6(a, b, c, d, e, f)((_, _, _, _, _, _))

  def tuple7[A, B, C, D, E, F, G](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G]): Result[(A, B, C, D, E, F, G)] =
    apply7(a, b, c, d, e, f, g)((_, _, _, _, _, _, _))

  def tuple8[A, B, C, D, E, F, G, H](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H]): Result[(A, B, C, D, E, F, G, H)] =
    apply8(a, b, c, d, e, f, g, h)((_, _, _, _, _, _, _, _))

  def tuple9[A, B, C, D, E, F, G, H, I](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I]): Result[(A, B, C, D, E, F, G, H, I)] =
    apply9(a, b, c, d, e, f, g, h, i)((_, _, _, _, _, _, _, _, _))

  def tuple10[A, B, C, D, E, F, G, H, I, J](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J]): Result[(A, B, C, D, E, F, G, H, I, J)] =
    apply10(a, b, c, d, e, f, g, h, i, j)((_, _, _, _, _, _, _, _, _, _))

  def tuple11[A, B, C, D, E, F, G, H, I, J, K](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K]): Result[(A, B, C, D, E, F, G, H, I, J, K)] =
    apply11(a, b, c, d, e, f, g, h, i, j, k)((_, _, _, _, _, _, _, _, _, _, _))

  def tuple12[A, B, C, D, E, F, G, H, I, J, K, L](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L]): Result[(A, B, C, D, E, F, G, H, I, J, K, L)] =
    apply12(a, b, c, d, e, f, g, h, i, j, k, l)((_, _, _, _, _, _, _, _, _, _, _, _))

  def tuple13[A, B, C, D, E, F, G, H, I, J, K, L, M](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M)] =
    apply13(a, b, c, d, e, f, g, h, i, j, k, l, m)((_, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple14[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] =
    apply14(a, b, c, d, e, f, g, h, i, j, k, l, m, n)((_, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] =
    apply15(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] =
    apply16(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] =
    apply17(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] =
    apply18(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] =
    apply19(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S], t: Result[T]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] =
    apply20(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S], t: Result[T],
      u: Result[U]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] =
    apply21(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

  def tuple22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S], t: Result[T],
      u: Result[U], v: Result[V]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] =
    apply22(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v)((_, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _, _))

}
