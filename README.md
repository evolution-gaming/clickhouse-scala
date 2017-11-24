## What is this? 
Alternative driver for [ClickHouse](https://github.com/yandex/ClickHouse).

## Why? It already has official Java jdbc (via http) driver!
* This one communicates with CH via native tcp protocol.
  That offers more features and possibilities for optimizations.
* Sometime you don't need a jdbc.

## Is this Scala-specific implementation? Can I call it from Java?
Project is divided in several modules. This one, 'core', contains only basic protocol implementation and has seamless compatibility with java (and no external dependencies).
Other modules (integration with reactive-streams, jdbc layer, automatic derivation of decoders) will reside in neighboring repositories.

## What features do you have in mind?
* [ ] support all CH datatypes (including multidimensional arrays), selects / inserts
* [ ] pool of connections (load balancing)
* [ ] ssl / tls
* [ ] streaming selects / inserts
* [ ] jdbc layer
* [ ] automatic derivation of decoders (for Scala)
* [ ] compression (in CH terms)

## You said it has better performance, show me tests and numbers!
See [here](src/test).

## Can I use it now?
At the moment, version is **0.0.1**, and it's not ready for production. You can play around with it, send feature / pull requests.
At the moment, it supports only selects and 'Int32', 'String, 'Nullable(T), 'Array(Array(...(T)' datatypes.

## What about code examples?
(requires locally running CH)
```scala
import chdriver.{Client, ClickhouseProperties, Decoder}
import chdriver.columns.Column

// that's your domain class
case class Test(x: Int, y: Int)

// 'Decoder' is a typeclass that knows how to return a combinable `Iterator[Test]`, 
// those iterators will be concatenated and serve as a result of select query.
implicit val TestDecoder = new Decoder[Test] {
  override def validate(names: Array[String], types: Array[String]): Boolean = {
    names.sameElements(Array("x", "y")) &&
      types.sameElements(Array("Int32", "Int32"))
  }

  // it's a sample implementation, another one may be more performant with your constrains
  // see Decoder's sources for design explanations
  override def transpose(numberOfItems: Int, columns: Array[Column]): Iterator[Test] = {
    val xs = new Array[Int](numberOfItems)
    System.arraycopy(columns(0).data, 0, xs, 0, numberOfItems)
    val ys = new Array[Int](numberOfItems)
    System.arraycopy(columns(1).data, 0, ys, 0, numberOfItems)

    var i = 0
    new Iterator[Test] {
      override def hasNext = i < numberOfItems

      override def next() = {
        val res = Test(xs(i), ys(i))
        i += 1
        res
      }
    }
  }
}
    
val client = new Client()
val clickhouseProperties = new ClickhouseProperties()
val result = client.execute("select x, y from test limit 1000000", clickhouseProperties)(TestDecoder)
println(result.toList)
```