package moonads

import cats.Monad

case class LiftedMonad[O[_], I](get: O[I])(implicit m: Monad[O]) {
  def map[B](f: I => B): LiftedMonad[O, B] =
    new LiftedMonad(m.map(get)(f))
  def flatMap[B](f: I => LiftedMonad[O, B]): LiftedMonad[O, B] =
    new LiftedMonad(m.flatMap(get)(i => f(i).get))
}
