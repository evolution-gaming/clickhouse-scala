package chdriver.columns

import java.io.DataInputStream

class StringColumn(_data: Array[String]) extends Column {
  override type T = String
  override val data = _data
}

object StringColumn {
  import chdriver.Protocol.DataInputStreamOps

  def readAllFrom(in: DataInputStream, itemsNumber: Int): StringColumn = {
    val data = new Array[String](itemsNumber)
    var i = 0
    while (i < itemsNumber) {
      data(i) = in.readString()
      i += 1
    }
    new StringColumn(data)
  }
}
