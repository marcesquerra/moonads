package com.example

import cats.implicits._
import cats.Id
import moonads._
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration
//import moonads.composers.experimental.unlawfulTraverse
import moonads.composers.monadT._

object Hello {

  def main(args: Array[String]): Unit = {

    type M[T] = List[Either[String,Option[Id[T]]]]

    def f1(i: Int): M[Int] = List(Right(Some(i + 2)))
    def f2(i: Int): M[Option[Int]] = Right(Some(Some(i - 4))) :: Right(Some(None)) :: Nil

    val tmp =
      for {
        a <- f1(7).as[List, Either[String,?], Option, Id].run
        b <- f2(a).as[List, Either[String,?], Option, Id].run
      } yield (a, b)

    println(tmp.get)

  }
}

