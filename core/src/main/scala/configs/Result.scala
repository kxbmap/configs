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

import scala.collection.compat._

sealed abstract class Result[+A] extends Product with Serializable {

  def value: A

  def isSuccess: Boolean

  final def isFailure: Boolean =
    !isSuccess

  def fold[B](ifFailure: ConfigError => B, ifSuccess: A => B): B

  def map[B](f: A => B): Result[B]

  def flatMap[B](f: A => Result[B]): Result[B]

  def flatten[B](implicit ev: A <:< Result[B]): Result[B]

  def ap[B](f: Result[A => B]): Result[B]

  def orElse[B >: A](fallback: => Result[B]): Result[B]

  def valueOr[B >: A](f: ConfigError => B): B

  def valueOrElse[B >: A](default: => B): B

  def valueOrThrow(e: ConfigError => Throwable): A

  def handleWith[B >: A](pf: PartialFunction[ConfigError, Result[B]]): Result[B]

  final def handle[B >: A](pf: PartialFunction[ConfigError, B]): Result[B] =
    handleWith(pf.andThen(Result.successful(_)))

  def mapError(f: ConfigError => ConfigError): Result[A]

  final def pushPath(path: String): Result[A] =
    mapError(_.pushPath(path))

  final def popPath: Result[A] =
    mapError(_.popPath)

  def contains[A1 >: A](value: A1): Boolean

  def exists(f: A => Boolean): Boolean

  def forall(f: A => Boolean): Boolean

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

  def fromOption[A](option: Option[A])(err: => ConfigError): Result[A] =
    option.fold(failure[A](err))(successful)

  def fromEither[A](either: Either[ConfigError, A]): Result[A] =
    either.fold(Failure, Success(_))

  def fromThrowable[A](throwable: Throwable): Result[A] =
    failure(ConfigError.fromThrowable(throwable))


  final case class Success[A](value: A) extends Result[A] {

    override def isSuccess: Boolean = true

    override def fold[B](ifFailure: ConfigError => B, ifSuccess: A => B): B =
      ifSuccess(value)

    override def map[B](f: A => B): Result[B] =
      Try(f(value))

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
        case Success(f0) => Try(f0(value))
        case fa@Failure(_) => fa
      }

    override def orElse[B >: A](fallback: => Result[B]): Result[B] =
      this

    override def valueOr[B >: A](f: ConfigError => B): B =
      value

    override def valueOrElse[B >: A](default: => B): B =
      value

    override def valueOrThrow(e: ConfigError => Throwable): A =
      value

    override def handleWith[B >: A](pf: PartialFunction[ConfigError, Result[B]]): Result[B] =
      this

    override def mapError(f: ConfigError => ConfigError): Result[A] =
      this

    override def contains[A1 >: A](value: A1): Boolean =
      this.value == value

    override def exists(f: A => Boolean): Boolean =
      f(value)

    override def forall(f: A => Boolean): Boolean =
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

    override def value: Nothing =
      valueOrThrow(_.configException)

    override def isSuccess: Boolean = false

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
        case Success(_) => this
        case Failure(e) => Failure(e + error)
      }

    override def orElse[B >: Nothing](fallback: => Result[B]): Result[B] =
      fallback

    override def valueOr[B >: Nothing](f: ConfigError => B): B =
      f(error)

    override def valueOrElse[B >: Nothing](default: => B): B =
      default

    override def valueOrThrow(e: ConfigError => Throwable): Nothing =
      throw e(error)

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

    override def contains[A1 >: Nothing](value: A1): Boolean =
      false

    override def exists(f: Nothing => Boolean): Boolean =
      false

    override def forall(f: Nothing => Boolean): Boolean =
      true

    override def foreach(f: Nothing => Unit): Unit =
      ()

    override def failed: Result[ConfigError] =
      Success(error)

    override def toOption: Option[Nothing] =
      None

    override def toEither: Either[ConfigError, Nothing] =
      Left(error)
  }


  def traverse[F[X] <: IterableOnce[X], A, B, That](fa: F[A])(f: A => Result[B])(implicit bf: BuildFrom[F[A], B, That]): Result[That] =
    fa.foldLeft(successful(bf.newBuilder(fa)))((rb, a) => apply2(rb, f(a))(_ += _)).map(_.result())

  def sequence[F[X] <: IterableOnce[X], A, That](fa: F[Result[A]])(implicit cbf: BuildFrom[F[Result[A]], A, That]): Result[That] =
    traverse(fa)(x => x)


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
    apply2(a, b)(Tuple2.apply)

  def tuple3[A, B, C](a: Result[A], b: Result[B], c: Result[C]): Result[(A, B, C)] =
    apply3(a, b, c)(Tuple3.apply)

  def tuple4[A, B, C, D](a: Result[A], b: Result[B], c: Result[C], d: Result[D]): Result[(A, B, C, D)] =
    apply4(a, b, c, d)(Tuple4.apply)

  def tuple5[A, B, C, D, E](a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E]): Result[(A, B, C, D, E)] =
    apply5(a, b, c, d, e)(Tuple5.apply)

  def tuple6[A, B, C, D, E, F](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F]): Result[(A, B, C, D, E, F)] =
    apply6(a, b, c, d, e, f)(Tuple6.apply)

  def tuple7[A, B, C, D, E, F, G](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G]): Result[(A, B, C, D, E, F, G)] =
    apply7(a, b, c, d, e, f, g)(Tuple7.apply)

  def tuple8[A, B, C, D, E, F, G, H](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H]): Result[(A, B, C, D, E, F, G, H)] =
    apply8(a, b, c, d, e, f, g, h)(Tuple8.apply)

  def tuple9[A, B, C, D, E, F, G, H, I](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I]): Result[(A, B, C, D, E, F, G, H, I)] =
    apply9(a, b, c, d, e, f, g, h, i)(Tuple9.apply)

  def tuple10[A, B, C, D, E, F, G, H, I, J](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J]): Result[(A, B, C, D, E, F, G, H, I, J)] =
    apply10(a, b, c, d, e, f, g, h, i, j)(Tuple10.apply)

  def tuple11[A, B, C, D, E, F, G, H, I, J, K](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K]): Result[(A, B, C, D, E, F, G, H, I, J, K)] =
    apply11(a, b, c, d, e, f, g, h, i, j, k)(Tuple11.apply)

  def tuple12[A, B, C, D, E, F, G, H, I, J, K, L](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L]): Result[(A, B, C, D, E, F, G, H, I, J, K, L)] =
    apply12(a, b, c, d, e, f, g, h, i, j, k, l)(Tuple12.apply)

  def tuple13[A, B, C, D, E, F, G, H, I, J, K, L, M](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M)] =
    apply13(a, b, c, d, e, f, g, h, i, j, k, l, m)(Tuple13.apply)

  def tuple14[A, B, C, D, E, F, G, H, I, J, K, L, M, N](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] =
    apply14(a, b, c, d, e, f, g, h, i, j, k, l, m, n)(Tuple14.apply)

  def tuple15[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] =
    apply15(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o)(Tuple15.apply)

  def tuple16[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] =
    apply16(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p)(Tuple16.apply)

  def tuple17[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] =
    apply17(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q)(Tuple17.apply)

  def tuple18[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] =
    apply18(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r)(Tuple18.apply)

  def tuple19[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] =
    apply19(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s)(Tuple19.apply)

  def tuple20[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S], t: Result[T]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] =
    apply20(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t)(Tuple20.apply)

  def tuple21[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S], t: Result[T],
      u: Result[U]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] =
    apply21(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u)(Tuple21.apply)

  def tuple22[A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V](
      a: Result[A], b: Result[B], c: Result[C], d: Result[D], e: Result[E], f: Result[F], g: Result[G], h: Result[H], i: Result[I], j: Result[J],
      k: Result[K], l: Result[L], m: Result[M], n: Result[N], o: Result[O], p: Result[P], q: Result[Q], r: Result[R], s: Result[S], t: Result[T],
      u: Result[U], v: Result[V]): Result[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] =
    apply22(a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v)(Tuple22.apply)

}
