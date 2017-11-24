package chdriver

import java.io.{DataInputStream, DataOutputStream}

import chdriver.columns.Column

class BlockInfo { // todo C++ what info is encoded in this BlockInfo?
  var isOverflows: Boolean = false
  var bucketNum: Int = -1

  def writeItselfTo(out: DataOutputStream): Unit = ???

  @annotation.tailrec // todo basic_functionality copy-pasted, WTF, why while(true) loop; is order fixed?; use companion object here
  final def readItselfFrom(in: DataInputStream): Unit = {
    import Protocol.DataInputStreamOps
    in.readAsUInt128() match {
      case 0 =>
      case 1 =>
        isOverflows = in.readUInt8() > 0
        readItselfFrom(in)

      case 2 =>
        bucketNum = in.readInt32()
        readItselfFrom(in)

      case _ =>
        readItselfFrom(in)
    }
  }
}

case class Block[T](numberOfRows: Int,
                    data: Array[Column],
                    info: BlockInfo = new BlockInfo,
                    columnsWithTypes: Option[Array[(String, String)]])(implicit decoder: Decoder[T]) {

  def iterator: Iterator[T] = {
    if (numberOfRows > 0) decoder.transpose(numberOfRows, data)
    else Iterator.empty
  }
}
