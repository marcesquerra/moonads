package moonads

import cats.Monad

trait ComposableMonad[B[_]] {

    def composeInto[A[_]](a: Monad[A]): Monad[Lambda[T => A[B[T]]]]

}
