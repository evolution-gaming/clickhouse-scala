package chdriver.columns

import java.io.DataInputStream

class NullableColumn(_data: Array[Any]) extends Column {
  override type T = Any
  override val data = _data
}

object NullableColumn {
  def readAllFrom(in: DataInputStream, itemsNumber: Int, innerType: String): NullableColumn = {
    val nullables = new Array[Byte](itemsNumber)
    in.readFully(nullables)
    val original = Column.from(in, itemsNumber, innerType).data
    val result = new Array[Any](itemsNumber)
    var i = 0
    while (i < itemsNumber) {
      result(i) =
        if (nullables(i) == 0) original(i)
        else null
      i += 1
    }
    new NullableColumn(result)
  }
}
