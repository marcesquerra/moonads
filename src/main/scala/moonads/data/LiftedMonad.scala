package moonads

import cats.Monad

case class LiftedMonad[O[_], I](get: O[I])(implicit m: Monad[O]) {

  /**
   * A sample function.
   *
   * {{{
   * # Property based test
   * prop> (i: Option[Int]) => {
   *     |   import composers._
   *     |   import cats.implicits._
   *     |   LiftedMonad(i).map(_ + 4).get == i.map(_ + 4) }
   * }}}
   */
  def map[B](f: I => B): LiftedMonad[O, B] =
    new LiftedMonad(m.map(get)(f))

  def flatMap[B](f: I => LiftedMonad[O, B]): LiftedMonad[O, B] =
    new LiftedMonad(m.flatMap(get)(i => f(i).get))

}
