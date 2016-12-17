## Moonads

> Fly me to the moon</br>
> Let me play among the stars

[*Fly me to the moon*](https://en.wikipedia.org/wiki/Fly_Me_to_the_Moon), Wikipedia

### Overview

Moonads is library with:

* Helpers to easily compose monads
* Various implementations of monad composition. Currently:
  * one based on Monad Transformers
  * one based on Traverse (unlawful)
* A way to select which implementation to use (with just one import)

### Examples
Monad instance creation

```
scala> import moonads._
import moonads._

scala> import moonads.composers.monadT._ // This selects the MonadTransformer based composition
import moonads.composers.monadT._

scala> import cats.implicits._
import cats.implicits._

scala> val m = Moon[List, Option]
m: cats.Monad[[TT]List[Option[TT]]] = moonads.composers.package$monadTHelpers$$anon$4$$anon$1@420ea4c7

scala> val l = Some(1) :: Some(2) :: Nil
l: List[Some[Int]] = List(Some(1), Some(2))

scala> m.flatMap(l)(a => Some(a + 1) :: None :: Nil)
res0: List[Option[Int]] = List(Some(2), None, Some(3), None)
```

For comprehension

```
scala> import moonads._
import moonads._

scala> import moonads.composers.monadT._ // This selects the MonadTransformer based composition
import moonads.composers.monadT._

scala> import cats.implicits._
import cats.implicits._

scala> type M[T] = List[Option[T]]
defined type alias M

scala> def f1(i: Int): M[Int] = Some(i + 2) :: Nil
f1: (i: Int)M[Int]

scala> def f2(i: Int): M[Option[Int]] = Some(Some(i - 4)) :: Some(None) :: Nil
f2: (i: Int)M[Option[Int]]

scala> val tmp =
     |   for {
     |     a <- f1(7).as[List, Option].run
     |     b <- f2(a).as[List, Option].run
     |   } yield (a, b)
tmp: moonads.Wrapper[[TT]List[Option[TT]],(Int, Option[Int])] = Wrapper(List(Some((9,Some(5))), Some((9,None))))

scala> tmp.get
res1: List[Option[(Int, Option[Int])]] = List(Some((9,Some(5))), Some((9,None)))
```

### Getting Moonads

Moonads is in its earliest stages and has not been released yet.

### Detailed Information

Moonads was inspired by this talk at the Scala Exchange 2016:

[Extensible Effects vs. Monad Transformers](https://skillsmatter.com/skillscasts/8974-extensible-effects-vs-monad-transformers)

More specifically, I wanted to try and see if I could find a generic way to compose monads.
It turns out that Monad composition is a hot topic with a lot of research going on. My own initial approach (based on Traverse) is unlawful, but during that initial development and some online conversations, I was able to separate how the composition is implemented from how is used.

#### The ComposableMonad type class

The `ComposableMonad` type class defined for a `Monad` `B` provides a single operation `composeInto` receiving a `Monad` `A` and returning a new `Monad` resulting from the composition of `A` wrapping `B`.

This is its definition:

```scala
trait ComposableMonad[B[_]] {

    def composeInto[A[_]](a: Monad[A]): Monad[Lambda[T => A[B[T]]]]

}

```

This provides a mechanism for components that want to be able to compose monads, and don't want to worry about *how*, to depend on the type class.

Currently Moonads has two sets of implementations:

1. `import moonads.composers.monadT._`
  * Provides a `ComposableMonad` for every Monad that happens to have a Monad Transformer implemented in Cats. (Well, currently only `Option` and `Id`. Implementations for `Either`, `State` and `Writer` are under way)
2. `import moonads.composers.experimental.unlawfulTraverse`
  * Provides a `ComposableMonad` for every Monad for which it also exists a `Traverse`
  * This type of composition is unlawful (I believe investigation on how to restrict this to cases where the composition is lawful is being made)
  * I want to spend some time understanding what exactly this kind of composition produces
  * EXPERIMENTAL

It's important to be noted that, to compose two monads, you only need a `ComposableMonad` for the innermost monad. That is, you can compose `List[Option[?]]` even when no Monad Transformer exist for `List` (in Cats, as of now)  

#### Monad instance generation

You can get an instance of `Monad` for the composition of up to four stacked monads like this:

```scala
import moonads._
import moonads.composers.monadT._ // you might choose another composition method
import cats.implicits._

val one   = Moon[List]
val two   = Moon[List, Id]
val three = Moon[List, Id, Option]

// ...

```

#### For comprehension syntax

If you have a series of methods that operate with the same stack of monads, it tends to be useful to use for comprehension to have a clearer syntax. Moonads provides helpers to make it easier to work with:

```scala
import moonads._
import moonads.composers.monadT._ // you might choose another composition method
import cats.implicits._

type M[T] = List[Id[Option[T]]]

def f1: M[Int] = ???
def f2: M[String] = ???

val response =
  for {
    a <- f1.as[List, Id, Option]
    b <- f2.as[List, Id, Option]
  } yield (a, b)

response.get  // (Int, String)
```

You can choose up to which level you want to stack to:

```scala
val response2 =
  for {
    a <- f1.as[List, Id]
    b <- f2.as[List, Id]
  } yield (a, b)

response2.get  // (Option[Int], Option[String])
```

### Known Issues

* The Monad Transformer based implementation still has no implementation for `Either`, `State` or `Writer`
* As pointed out by Daniel Spiewak [here](https://twitter.com/djspiewak/status/808357325136240640)
  and [here](https://twitter.com/djspiewak/status/808358646648279040), composition through `Traverse` is not lawful
* You can only stack up to 4 monads with the current implementation. I
  will increase this limit up to 22 (see Future Work)

### Future Work

* Implement the `ComposableMonad` for `Either`, `State` and `Writer` in the Monad Transformer based implementation
* Create a third composer implementation that contain all the `ComposableMonad` based on monad transformers, extended with all (some) the `ComposableMonad` based on `Traverse` that are lawful (if any)
* Investigate how to compose two monads by taking their free product,
  as suggested by Rúnar Bjarnason in [here](https://twitter.com/runarorama/status/808556289353744385)
* Increase the maximum stack size from 4 to 22, using code generator with SBT
* Try to make the for comprehension syntax cleaner/friendlier

### Related Projects

* [Cats](https://github.com/typelevel/cats) provides the monads, Moonads the composition
* [Emm](https://github.com/djspiewak/emm) - A general monad for managing stacking effects (It's a really similar project to Moonads)

### Thanks

I would like to thank Erik Osheim for his keynote in the last scala exchange:

[Visions for collaboration, competition, and interop in Scala](https://skillsmatter.com/skillscasts/8541-visions-for-collaboration-competition-and-interop-in-scala)

which convinced me to publish this and provided the template for this README file.

Also to all the friendly feedback I've recieved on twitter!

### Copyright and License

All code is available to you under the MIT license, available at
http://opensource.org/licenses/mit-license.php and also in the
[LICENSE](LICENSE) file.

Copyright Marc Esquerra, 2016.

### No Warranty

> THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
> EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
> MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
> NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
> BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
> ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
> CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
> SOFTWARE.
