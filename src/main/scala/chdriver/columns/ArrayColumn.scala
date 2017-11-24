package chdriver.columns

import java.io.DataInputStream
import java.util.ArrayDeque

import chdriver.DriverException

class ArrayColumn(_data: Array[Any]) extends Column {
  override type T = Any
  override val data = _data
}

object ArrayColumn {
  import chdriver.Protocol.DataInputStreamOps

  final val prefix = "Array("

  /*
  for [[42,43]] [[44],[45,46]] :
  itemsNumber = 2
  length of 1 array = 1
  length of 1 array + length of 2 array = 1 + 2 = 3
  length of 1.1 array = 2
  length of 1.1 array + length of 2.1 array = 2 + 1 = 3
  length of 1.1 array + length of 2.1 array + length of 2.2 array = 2 + 1 + 2 = 5
  data = 42 43 44 45 46

  for [[[47]],[[48,49,50,51],[],[52],[53]]] :
  itemsNumber = 1
  length of 1 array = 2
  length of 1.1 array = 1
  length of 1.1 array + length of 1.2 array = 1 + 4 = 5
  length of 1.1.1 array = 1
  length of 1.1.1 array + length of 1.2.1 array = 1 + 4 = 5
  length of 1.1.1 array + length of 1.2.1 array + length of 1.2.2 array = 1 + 4 + 0 = 5
  length of 1.1.1 array + length of 1.2.1 array + length of 1.2.2 array + length of 1.2.3 array = 1 + 4 + 1 = 6
  length of 1.1.1 array + length of 1.2.1 array + length of 1.2.2 array + length of 1.2.3 array + length of 1.2.4 array = 1 + 4 + 1 + 1 = 7
  data = 47 48 49 50 50 51 52 53
   */
  def readAllFrom(in: DataInputStream, itemsNumber: Int, innerType: String): ArrayColumn = {
    def fillOffsets(): ArrayDeque[Int] = {
      var level = {
        var current = 0
        var res = 1
        if (innerType.startsWith(prefix))
          while (innerType.substring(current, current + prefix.length) == prefix) {
            current += prefix.length
            res += 1
          }
        res
      }
      var onThisLevel = itemsNumber
      val q = new ArrayDeque[Int]
      while (level > 0) {
        val offsets = in.readArrayInt64(onThisLevel) // todo should be UInt64 here, but anyway exception is thrown atm
        for (offset <- offsets) {
          if (offset < 0 || (!q.isEmpty && (offset - q.getLast) > Integer.MAX_VALUE))
            throw new DriverException(s"Too big length=$offset of array, 2^31 max is supported.")
          q.addFirst(offset.toInt)
        }
        onThisLevel = q.getFirst
        level -= 1
      }
      q
    }

    def fillData(datas: ArrayDeque[(Array[Any], String)], offsets: ArrayDeque[Int]): Unit = {
      var previousOffset = 0
      var stopResetOffset = false
      while (!datas.isEmpty) {
        val (data, innerType) = datas.removeLast()
        var i = 0

        if (!stopResetOffset) previousOffset = 0

        if (!innerType.startsWith(prefix)) {
          stopResetOffset = true
          while (i < data.length && !offsets.isEmpty) {
            val o = offsets.removeLast()
            data(i) = Column.from(in, o - previousOffset, innerType).data
            previousOffset = o
            i += 1
          }
        } else {
          while (i < data.length) {
            val o = offsets.removeLast()
            val newNodes = new Array[Any](o - previousOffset)
            data(i) = newNodes
            previousOffset = o
            datas.addFirst(newNodes, innerType.substring(prefix.length, innerType.length - 1))
            i += 1
          }
        }
      }
    }

    val data = new Array[Any](itemsNumber)
    val offsets = fillOffsets()
    val q = new ArrayDeque[(Array[Any], String)]
    q.addFirst(data, innerType)
    fillData(q, offsets)

    new ArrayColumn(data)
  }
}
