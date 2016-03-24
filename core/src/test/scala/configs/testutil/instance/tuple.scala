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

import scalaz.{Equal, std}

object tuple {

  implicit def tuple2Equal[A: Equal, B: Equal]: Equal[(A, B)] =
    std.tuple.tuple2Equal[A, B]

  implicit def tuple3Equal[A: Equal, B: Equal, C: Equal]: Equal[(A, B, C)] =
    std.tuple.tuple3Equal[A, B, C]

  implicit def tuple4Equal[A: Equal, B: Equal, C: Equal, D: Equal]: Equal[(A, B, C, D)] =
    std.tuple.tuple4Equal[A, B, C, D]

  implicit def tuple5Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal]: Equal[(A, B, C, D, E)] =
    std.tuple.tuple5Equal[A, B, C, D, E]

  implicit def tuple6Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal]: Equal[(A, B, C, D, E, F)] =
    std.tuple.tuple6Equal[A, B, C, D, E, F]

  implicit def tuple7Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal]: Equal[(A, B, C, D, E, F, G)] =
    std.tuple.tuple7Equal[A, B, C, D, E, F, G]

  implicit def tuple8Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal]: Equal[(A, B, C, D, E, F, G, H)] =
    std.tuple.tuple8Equal[A, B, C, D, E, F, G, H]

  implicit def tuple9Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal]: Equal[(A, B, C, D, E, F, G, H, I)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i) => ((a, b, c, d, e, f, g, h), i)
    }

  implicit def tuple10Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j) => ((a, b, c, d, e, f, g, h), i, j)
    }

  implicit def tuple11Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k) => ((a, b, c, d, e, f, g, h), i, j, k)
    }

  implicit def tuple12Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l) => ((a, b, c, d, e, f, g, h), i, j, k, l)
    }

  implicit def tuple13Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal, M: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L, M)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l, m) => ((a, b, c, d, e, f, g, h), i, j, k, l, m)
    }

  implicit def tuple14Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal, M: Equal, N: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L, M, N)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l, m, n) => ((a, b, c, d, e, f, g, h), i, j, k, l, m, n)
    }

  implicit def tuple15Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal, M: Equal, N: Equal, O: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o) => ((a, b, c, d, e, f, g, h), i, j, k, l, m, n, o)
    }

  implicit def tuple16Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal, M: Equal, N: Equal, O: Equal, P: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p) => ((a, b, c, d, e, f, g, h), (i, j, k, l, m, n, o, p))
    }

  implicit def tuple17Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal, M: Equal, N: Equal, O: Equal, P: Equal, Q: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q) => ((a, b, c, d, e, f, g, h), (i, j, k, l, m, n, o, p), q)
    }

  implicit def tuple18Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal, M: Equal, N: Equal, O: Equal, P: Equal, Q: Equal, R: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r) => ((a, b, c, d, e, f, g, h), (i, j, k, l, m, n, o, p), q, r)
    }

  implicit def tuple19Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal, M: Equal, N: Equal, O: Equal, P: Equal, Q: Equal, R: Equal, S: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s) => ((a, b, c, d, e, f, g, h), (i, j, k, l, m, n, o, p), q, r, s)
    }

  implicit def tuple20Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal, M: Equal, N: Equal, O: Equal, P: Equal, Q: Equal, R: Equal, S: Equal, T: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t) => ((a, b, c, d, e, f, g, h), (i, j, k, l, m, n, o, p), q, r, s, t)
    }

  implicit def tuple21Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal, M: Equal, N: Equal, O: Equal, P: Equal, Q: Equal, R: Equal, S: Equal, T: Equal, U: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u) => ((a, b, c, d, e, f, g, h), (i, j, k, l, m, n, o, p), q, r, s, t, u)
    }

  implicit def tuple22Equal[A: Equal, B: Equal, C: Equal, D: Equal, E: Equal, F: Equal, G: Equal, H: Equal, I: Equal, J: Equal, K: Equal, L: Equal, M: Equal, N: Equal, O: Equal, P: Equal, Q: Equal, R: Equal, S: Equal, T: Equal, U: Equal, V: Equal]: Equal[(A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V)] =
    Equal.equalBy {
      case (a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v) => ((a, b, c, d, e, f, g, h), (i, j, k, l, m, n, o, p), q, r, s, t, u, v)
    }

}
