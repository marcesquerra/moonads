package moonads
package syntax

import cats.Monad

trait ComposeMonads {

    implicit class ComposedMonadExtender[T](val full: T) {

      case class As1[A[_]](implicit m: Monad[A]){
        def run[I](implicit ev: A[I] =:= T): Wrapper[A, I] =
          new Wrapper[A, I](full.asInstanceOf[A[I]])
      }

      def as[A[_]](implicit m: Monad[A]): As1[A] =
        As1[A]

      case class As2[A[_], B[_]](implicit a: Monad[A], b: Monad[B], tb: ComposableMonad[B]){
        type MR[TT] = A[B[TT]]
        def run[I](implicit ev: A[B[I]] =:= T): Wrapper[MR, I] =
          new Wrapper[MR, I](full.asInstanceOf[A[B[I]]])(Moon[A, B])
      }

      def as[A[_], B[_]](implicit a: Monad[A], b: Monad[B], tb: ComposableMonad[B]): As2[A, B] =
        As2[A, B]

      case class As3[A[_], B[_], C[_]](implicit a: Monad[A], b: Monad[B], tb: ComposableMonad[B], c: Monad[C], tc: ComposableMonad[C]){
        type MR[TT] = A[B[C[TT]]]
        def run[I](implicit ev: A[B[C[I]]] =:= T): Wrapper[MR, I] =
          new Wrapper[MR, I](full.asInstanceOf[A[B[C[I]]]])(Moon[A, B, C])
      }

      def as[A[_], B[_], C[_]](implicit a: Monad[A], b: Monad[B], tb: ComposableMonad[B], c: Monad[C], tc: ComposableMonad[C]): As3[A, B, C] =
        As3[A, B, C]

      case class As4[A[_], B[_], C[_], D[_]](implicit a: Monad[A], b: Monad[B], tb: ComposableMonad[B], c: Monad[C], tc: ComposableMonad[C], d: Monad[D], td: ComposableMonad[D]){
        type MR[TT] = A[B[C[D[TT]]]]
        def run[I](implicit ev: A[B[C[D[I]]]] =:= T): Wrapper[MR, I] =
          new Wrapper[MR, I](full.asInstanceOf[A[B[C[D[I]]]]])(Moon[A, B, C, D])
      }

      def as[A[_], B[_], C[_], D[_]](implicit a: Monad[A], b: Monad[B], tb: ComposableMonad[B], c: Monad[C], tc: ComposableMonad[C], d: Monad[D], td: ComposableMonad[D]): As4[A, B, C, D] =
        As4[A, B, C, D]

    }

}

