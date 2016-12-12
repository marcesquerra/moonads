package moonads

import cats.Monad
import cats.Traverse

  object Moon {

    def apply[A[_]](implicit ma: Monad[A]): Monad[A] =
      ma

    def apply[A[_], B[_]](implicit ma: Monad[A], mb: Monad[B], tb: Traverse[B]): Monad[Lambda[TT => A[B[TT]]]] =
      compose(ma, mb, tb)

    def apply[A[_], B[_], C[_]](implicit ma: Monad[A], mb: Monad[B], tb: Traverse[B], mc: Monad[C], tc: Traverse[C]): Monad[Lambda[TT => A[B[C[TT]]]]] = {
      type AB[TT] = A[B[TT]]
      val mab: Monad[AB] =
        compose(ma, mb, tb)

      compose(mab, mc, tc)
    }

    def apply[A[_], B[_], C[_], D[_]](implicit ma: Monad[A], mb: Monad[B], tb: Traverse[B], mc: Monad[C], tc: Traverse[C], md: Monad[D], td: Traverse[D]): Monad[Lambda[TT => A[B[C[D[TT]]]]]] = {
      type AB[TT] = A[B[TT]]
      val mab: Monad[AB] =
        compose(ma, mb, tb)

      type ABC[TT] = AB[C[TT]]
      val mabc = compose(mab, mc, tc)

      compose[ABC, D](mabc, md, td)
    }

    def compose[M[_], N[_]](m: Monad[M], n: Monad[N], t: Traverse[N]): Monad[Lambda[A => M[N[A]]]] =
      new Monad[Lambda[A => M[N[A]]]] {

        override def map[A, B](mna: M[N[A]])(f: A => B): M[N[B]] = {
          m.map(mna){na => n.map(na)(f)}
        }

        def pure[A](a: A): M[N[A]] = m.pure(n.pure(a))

        def flatMap[A, B](mna: M[N[A]])(f: A => M[N[B]]): M[N[B]] = {
          val mnmnb: M[N[M[N[B]]]] = map(mna)(f)
          val mmnb: M[M[N[B]]] =
            m.map(mnmnb){nmnb =>
              val mnnb: M[N[N[B]]] =
                t.sequence(nmnb)(m)

              m.map(mnnb)(n.flatten)
            }

          m.flatten(mmnb)
        }

        def tailRecM[A, B](a: A)(f: A => M[N[Either[A, B]]]): M[N[B]] =
          flatMap(f(a)) {
            case Left(b1) => tailRecM(b1)(f)
            case Right(c) => pure(c)
          }
      }
  }
