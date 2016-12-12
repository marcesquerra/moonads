## Moonads

> Fly me to the moon</br>
> Let me play among the stars

[*Fly me to the moon*](https://en.wikipedia.org/wiki/Fly_Me_to_the_Moon), Wikipedia

### Overview

Moonads is library with tools for composing monads. It uses a Traverse of the inner monad
to achieve this.

### Examples

```
scala> import moonads._
import moonads._

scala> import cats.implicits._
import cats.implicits._

scala> val m = Moon[List, Option]
m: cats.Monad[[TT]List[Option[TT]]] = moonads.Moon$$anon$1@5958955e

scala> val l = Some(1) :: Some(2) :: Nil
l: List[Some[Int]] = List(Some(1), Some(2))

scala> m.flatMap(l)(a => Some(a + 1) :: None :: Nil)
res3: List[Option[Int]] = List(Some(2), None, Some(3), None)
```

### Getting Moonads

Moonads is in its earliest stages and has not been released yet.

### Detailed Information

Moonads was inspired by this talk at the Scala Exchange 2016:

[Extensible Effects vs. Monad Transformers](https://skillsmatter.com/skillscasts/8974-extensible-effects-vs-monad-transformers)

More specifically, I wanted to try and see if I could find a generic way to compose monads.
It turns out that, to compose two monads, you can, if you have a Traverse for the inner monad
to compose.

At its core, Moonads is built around a method with a signature more or less like this:

```scala
	
  def compose[A[_], B[_]](a: Monad[A], b: Monad[B], t: Traverse[B]): Monad[A[B[?]]]

```

NOTE: I've simplified the signature a bit, for readability

#### Why it works?

The key point on trying to compose two monads is to implement a `flatMap` method. Once a
composed version of `map` has already been implemented, the new `flatMap` does this:

1. Uses the new `map` with the provided `f`. Converts a `A[B[T]]` into a `A[B[A[B[T2]]]]`
2. Uses the traverse to swap the two middle monads, giving you back a `A[A[B[B[T2]]]]`
3. Flattens the As and the Bs (this can be done out of the box with the Monad[A] and Monad[B]).
   This generates the desired `A[B[T2]]`

### Known Issues

* You can only stack up to 4 monads with the current implementation. I
  will increase this limit up to 22 (see Future Work)
* For all, except the outermost, monads in the stack, there must exist a Traverse.
  In most cases this should not be a problem (as of now, I've only found
  the 'State Monad' as an example of a monad that does not hava a Traverse

### Future Work

* Increase the maximum stack size from 4 to 22, using code generator with SBT
* Try to make the for comprehension syntax cleaner/friendlier

### Thanks

I would like to thank Erik Osheim for his keynote in the last scala exchange:

[Visions for collaboration, competition, and interop in Scala](https://skillsmatter.com/skillscasts/8541-visions-for-collaboration-competition-and-interop-in-scala)

which convinced me to publish this and provided the template for this README file.

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
