package moonads

import cats.Monad

  object Moon {

    def apply[A[_]](implicit ma: Monad[A]): Monad[A] =
      ma

    def apply[A[_], B[_]](
          implicit ma: Monad[A],
                   mb: Monad[B], cb: ComposableMonad[B]): Monad[Lambda[TT => A[B[TT]]]] =
      cb.composeInto(ma)

    def apply[A[_], B[_], C[_]](
          implicit ma: Monad[A],
                   mb: Monad[B], cb: ComposableMonad[B],
                   mc: Monad[C], cc: ComposableMonad[C]): Monad[Lambda[TT => A[B[C[TT]]]]] = {
      type AB[TT] = A[B[TT]]
      val mab: Monad[AB] =
        cb.composeInto(ma)

      cc.composeInto(mab)
    }

    def apply[A[_], B[_], C[_], D[_]](
          implicit ma: Monad[A],
                   mb: Monad[B], cb: ComposableMonad[B],
                   mc: Monad[C], cc: ComposableMonad[C],
                   md: Monad[D], cd: ComposableMonad[D]): Monad[Lambda[TT => A[B[C[D[TT]]]]]] = {
      type AB[TT] = A[B[TT]]
      val mab: Monad[AB] =
        cb.composeInto(ma)

      type ABC[TT] = AB[C[TT]]
      val mabc: Monad[ABC] =
        cc.composeInto(mab)

      cd.composeInto(mabc)
    }
  }
