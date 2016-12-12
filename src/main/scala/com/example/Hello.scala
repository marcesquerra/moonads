package com.example

import cats.implicits._
import moonads._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

object Hello {

  def main(args: Array[String]): Unit = {

    type M[T] = Future[List[Option[Vector[T]]]]

    def f1(i: Int): M[Int] = Future.successful(Some(Vector(i + 2)) :: Nil)
    def f2(i: Int): M[Option[Int]] = Future.successful(Some(Vector(Some(i - 4))) :: Some(Vector(None)) :: Nil)

    val tmp =
      for {
        a <- f1(7).as[Future, List, Option, Vector].run
        b <- f2(a).as[Future, List, Option, Vector].run
      } yield (a, b)

    println(Await.result(tmp.get, Duration.Inf))
  }
}


