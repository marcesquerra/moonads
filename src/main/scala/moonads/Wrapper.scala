package moonads

import cats.Monad

case class Wrapper[O[_], I](get: O[I])(implicit m: Monad[O]) {
  def map[B](f: I => B): Wrapper[O, B] =
    new Wrapper(m.map(get)(f))
  def flatMap[B](f: I => Wrapper[O, B]): Wrapper[O, B] =
    new Wrapper(m.flatMap(get)(i => f(i).get))
}
