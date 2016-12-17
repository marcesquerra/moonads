package moonads.composers

import cats.Monad
import cats.Traverse
import moonads.ComposableMonad

package object experimental {

  implicit def unlawfulTraverse[B[_]](implicit t: Traverse[B], b: Monad[B]): ComposableMonad[B] =
    new ComposableMonad[B] {
      def composeInto[A[_]](a: Monad[A]): Monad[Lambda[T => A[B[T]]]] = {
        new Monad[Lambda[T => A[B[T]]]] {

          override def map[T, U](abt: A[B[T]])(f: T => U): A[B[U]] = {
            a.map(abt){bt => b.map(bt)(f)}
          }

          def pure[T](value: T): A[B[T]] = a.pure(b.pure(value))

          def flatMap[T, U](abt: A[B[T]])(f: T => A[B[U]]): A[B[U]] = {
            val ababu: A[B[A[B[U]]]] = map(abt)(f)
            val aabu: A[A[B[U]]] =
              a.map(ababu){babu =>
                val abbu: A[B[B[U]]] =
                  t.sequence(babu)(a)

                a.map(abbu)(b.flatten)
              }

            a.flatten(aabu)
          }

          def tailRecM[T, U](value: T)(f: T => A[B[Either[T, U]]]): A[B[U]] =
            flatMap(f(value)) {
              case Left(t1) => tailRecM(t1)(f)
              case Right(u) => pure(u)
            }
        }
      }
    }

}
