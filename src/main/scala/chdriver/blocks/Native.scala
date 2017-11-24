package chdriver.blocks

import java.io.{DataInputStream, DataOutputStream}

import chdriver.columns.Column
import chdriver.{Block, BlockInfo, ClickhouseVersionSpecific, Decoder}

object Native {
  implicit class BlockOutputStream(val out: DataOutputStream) extends AnyVal {
    import chdriver.Protocol.DataOutputStreamOps

    def writeBlock(block: Block[_], serverRevision: Int): Unit = {
      if (serverRevision >= chdriver.ClickhouseVersionSpecific.DBMS_MIN_REVISION_WITH_BLOCK_INFO) {
        // todo for_insert write block info
        out.writeAsUInt128(1)
        out.writeUInt8(0)
        out.writeAsUInt128(2)
        out.writeInt32(-1)
        out.writeAsUInt128(0)
      }

      out.writeAsUInt128(0)
      out.writeAsUInt128(0)
      out.flush()
    }
  }

  implicit class BlockInputStream(val in: DataInputStream) extends AnyVal {
    import chdriver.Protocol.DataInputStreamOps

    def readBlock[T](serverRevision: Int)(implicit decoder: Decoder[T]): Block[T] = {
      val info = new BlockInfo
      if (serverRevision > ClickhouseVersionSpecific.DBMS_MIN_REVISION_WITH_BLOCK_INFO) {
        info.readItselfFrom(in)
      }
      val numberOfColumns = in.readAsUInt128()
      val numberOfRows = in.readAsUInt128()

      // todo constant_memory_optimization preallocate arrays and reuse them
      val names = new Array[String](numberOfColumns)
      val types = new Array[String](numberOfColumns)
      val data = new Array[Column](numberOfColumns)

      for (i <- 0 until numberOfColumns) {
        val columnName = in.readString()
        val columnType = in.readString()
        val isLastColumn = i == numberOfColumns - 1

        names(i) = columnName
        types(i) = columnType

        if (isLastColumn && !decoder.validate(names, types))
          throw new Exception(s"Incompatible runtime data, runtime names=[${names.mkString(",")}], types=[${types.mkString(",")}]")

        if (numberOfRows > 0) {
          val column = Column.from(in, numberOfRows, columnType)
          // todo streaming if it's last column, yield class immediately
          data(i) = column
        }
      }

      Block(numberOfRows, data = data, info = info, columnsWithTypes = Some(names.zip(types)))
    }
  }
}
