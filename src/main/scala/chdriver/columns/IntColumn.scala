package chdriver.columns

import java.io.DataInputStream

class Int32Column(_data: Array[Int]) extends Column {
  override type T = Int
  override val data: Array[Int] = _data
}

object Int32Column {
  def readAllFrom(in: DataInputStream, itemsNumber: Int): Int32Column = {
    import chdriver.Protocol.DataInputStreamOps

    val data = new Array[Byte](itemsNumber * 4)
    in.readFully(data)
    val result = new Array[Int](itemsNumber)
    var i = 0
    while (i < 4 * itemsNumber) {
      result(i / 4) = DataInputStreamOps.fromBytes(data(i + 3), data(i + 2), data(i + 1), data(i))
      i += 4
    }
    new Int32Column(result)
  }
}
