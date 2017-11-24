package chdriver

import chdriver.columns.Column

trait Decoder[T] {
  /**
    * Checks if declared on call side names and types are compatible with runtime facts.
    * Runtime `names` and `types` come as a meta-information from Clickhouse.
    */
  def validate(names: Array[String], types: Array[String]): Boolean

  /**
    * Clickhouse sends data in chdriver.blocks, column by column. Let's consider a example:
    *
    * `class Foo(i: Int, s: String)
    * select * from foo`
    *
    * Result may be divided in 10 chdriver.blocks, each containing 1/10 of total number of rows.
    * For each block, we receive all `i` == `columns(0)`, then all `s` == `columns(1)`, and
    * `columns(0).data.length` == `columns(1).data.length` == `numberOfItems`.
    *
    * Task of this method -- return a combinable `Iterator[Foo]`, those iterators will be
    * concatenated and serve as a result of select query.
    *
    * Note: method takes not a single row but all of columns because
    * 1) with that approach there is no boxing/unboxing for primitive jvm types
    * 2) sometimes it's possible to do batch optimizations
    */
  def transpose(numberOfItems: Int, columns: Array[Column]): Iterator[T]
}