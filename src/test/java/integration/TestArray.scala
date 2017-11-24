package integration

import chdriver.{ClickhouseProperties, Client, Connection, Decoder}
import chdriver.columns.Column

case class TestArray(x: Array[Int]) {
  override def toString: String = s"TestArray(${x.mkString(" ")})"
}

object TestArray {
  implicit val testArrayDecoder = new Decoder[TestArray] {
    override def validate(names: Array[String], types: Array[String]) = {
      names.sameElements(Array("x")) &&
      types.sameElements(Array("Array(Int32)"))
    }

    override def transpose(numberOfItems: Int, columns: Array[Column]): Iterator[TestArray] = {
      val xs = new Array[Array[Int]](numberOfItems)
      System.arraycopy(columns(0).data, 0, xs, 0, numberOfItems)

      var i = 0
      new Iterator[TestArray] {
        override def hasNext = i < numberOfItems

        override def next() = {
          val res = TestArray(xs(i))
          i += 1
          res
        }
      }
    }
  }

  def client(port: Int) = new Client(connection = new Connection(port = port))
  val clickhouseProperties = new ClickhouseProperties()
}
