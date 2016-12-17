package moonads

import cats.Monad
import cats.Functor
import cats.TransLift
import cats.data._
import cats.Id

package object composers {

  object monadT extends monadTHelpers with MTransLiftInstances {
	
    implicit def optionT(implicit b: Monad[Option]): ComposableMonad[Option] =
	builder[Option, OptionT]

    implicit def IdT(implicit b: Monad[Id]): ComposableMonad[Id] =
	builder[Id, IdT]
	
//    implicit def EitherT(implicit b: Monad[Either]): ComposableMonad[Either] =
//	builder[Either, EitherT]
//
//    implicit def StateT(implicit b: Monad[State]): ComposableMonad[State] =
//	builder[State, StateT]
//	
//    implicit def WriterT(implicit b: Monad[Writer]): ComposableMonad[Writer] =
//	builder[Writer, WriterT]


    /* Cats has not implemented ListT or VectorT yet
    implicit def listT(implicit b: Monad[List]): ComposableMonad[List] =
	builder[List, ListT]

    implicit def vectorT(implicit b: Monad[Vector]): ComposableMonad[Vector] =
	builder[Vector, VectorT]
    */

  }

  trait MTransLiftInstances extends MTransLiftDef {

    protected implicit val OptionTransLift: MTransLift[Option, OptionT] = new MTransLift[Option, OptionT] {
      def extract [A[_], T](         in: OptionT[A,T]): A[Option[T]]                     = in.value
      def lift    [A[_], T](         in: A[Option[T]]): OptionT[A,T]                     = OptionT(in)
      def monad   [A[_]]   (implicit a:  Monad[A]):     Monad[Lambda[t => OptionT[A,t]]] = implicitly
    }

    protected implicit val IdTransLift: MTransLift[Id, IdT] = new MTransLift[Id, IdT] {
      def extract [A[_], T](         in: IdT[A,T]): A[Id[T]]                     = in.value
      def lift    [A[_], T](         in: A[Id[T]]): IdT[A,T]                     = IdT(in)
      def monad   [A[_]]   (implicit a:  Monad[A]):     Monad[Lambda[t => IdT[A,t]]] = implicitly
    }

//    protected implicit val EitherTransLift: MTransLift[Either, EitherT] = new MTransLift[Either, EitherT] {
//      def extract [A[_], T](         in: EitherT[A,T]): A[EitherT[T]]                     = in.value
//      def lift    [A[_], T](         in: A[Either[T]]): EitherT[A,T]                     = EitherT(in)
//      def monad   [A[_]]   (implicit a:  Monad[A]):     Monad[Lambda[t => EitherT[A,t]]] = implicitly
//    }
//
//    protected implicit val StateTransLift: MTransLift[State, StateT] = new MTransLift[State, StateT] {
//      def extract [A[_], T](         in: StateT[A,T]): A[State[T]]                     = in.value
//      def lift    [A[_], T](         in: A[State[T]]): StateT[A,T]                     = StateT(in)
//      def monad   [A[_]]   (implicit a:  Monad[A]):     Monad[Lambda[t => StateT[A,t]]] = implicitly
//    }
//
//    protected implicit val WriterTransLift: MTransLift[Writer, WriterT] = new MTransLift[Writer, WriterT] {
//      def extract [A[_], T](         in: WriterT[A,T]): A[Writer[T]]                     = in.value
//      def lift    [A[_], T](         in: A[Writer[T]]): WriterT[A,T]                     = WriterT(in)
//      def monad   [A[_]]   (implicit a:  Monad[A]):     Monad[Lambda[t => WriterT[A,t]]] = implicitly
//    }

    /* Cats has not implemented ListT or VectorT yet
    protected implicit val ListTransLift: MTransLift[List, ListT] = new MTransLift[List, ListT] {
      def extract [A[_], T](         in: ListT[A,T]): A[List[T]]                     = in.value
      def lift    [A[_], T](         in: A[List[T]]): ListT[A,T]                     = ListT(in)
      def monad   [A[_]]   (implicit a:  Monad[A]):   Monad[Lambda[t => ListT[A,t]]] = implicitly
    }

    protected implicit val VectorTransLift: MTransLift[Vector, VectorT] = new MTransLift[Vector, VectorT] {
      def extract [A[_], T](         in: VectorT[A,T]): A[Vector[T]]                     = in.value
      def lift    [A[_], T](         in: A[Vector[T]]): VectorT[A,T]                     = VectorT(in)
      def monad   [A[_]]   (implicit a:  Monad[A]):     Monad[Lambda[t => VectorT[A,t]]] = implicitly
    }
    */

  }

  trait monadTHelpers extends MTransLiftDef {

    protected[this] def builder[B[_], BT[_[_], _]](implicit b: Monad[B], mtl: MTransLift[B, BT]): ComposableMonad[B] =
      new ComposableMonad[B] {
        def composeInto[A[_]](a: Monad[A]): Monad[Lambda[T => A[B[T]]]] = {

          new Monad[Lambda[T => A[B[T]]]] {

            override def map[T, U](abt: A[B[T]])(f: T => U): A[B[U]] = {
              a.map(abt){bt => b.map(bt)(f)}
            }

            def pure[T](value: T): A[B[T]] = a.pure(b.pure(value))

            def flatMap[T, U](abt: A[B[T]])(f: T => A[B[U]]): A[B[U]] = {
              import cats.syntax.all
              implicit val m: Monad[A] = a
              val r =
                for {
                        t <- mtl.liftW(abt)
                        u <- mtl.liftW(f(t))
                      } yield u
              mtl.extract(r.value)
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

  trait MTransLiftDef {

    protected[this] trait MTransLift[B[_], BT[_[_], _]] {
      def lift[A[_], T](abt: A[B[T]]): BT[A, T]
      def monad[A[_]](implicit a: Monad[A]): Monad[Lambda[t => BT[A, t]]]
      def extract[A[_], T](in: BT[A, T]): A[B[T]]

      case class Wrapper[A[_], T](value: BT[A, T])(implicit a: Monad[A]) {
        def map[U](f: T => U): Wrapper[A, U] = Wrapper(monad.map(value)(f))
        def flatMap[U](f: T => Wrapper[A, U]): Wrapper[A, U] =
          Wrapper(monad.flatMap(value)(t => f(t).value))
      }

      def liftW[A[_], T](abt: A[B[T]])(implicit a: Monad[A]): Wrapper[A, T] =
        Wrapper(lift(abt))
    }
  }

}
