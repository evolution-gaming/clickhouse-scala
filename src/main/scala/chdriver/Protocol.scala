package chdriver

import java.io.{DataInputStream, DataOutputStream}

object Protocol {
  implicit class DataOutputStreamOps(val out: DataOutputStream) extends AnyVal {
    def writeAsUInt128(value: Int): Unit = {
      assert(value >= 0)
      var v = value
      var remaining = v >>> 7
      while (remaining != 0) {
        out.write(((v & 0x7f) | 0x80).toByte)
        v = remaining
        remaining >>>= 7
      }
      out.write((v & 0x7f).toByte)
    }

    def writeString(v: String): Unit = {
      val bytes = v.getBytes
      writeAsUInt128(bytes.length)
      out.write(bytes)
    }

    def writeUInt8(v: Int): Unit = {
      assert(v >= 0 && v <= DataOutputStreamOps.U_INT8_MAX)
      val unsigned = (v & 0xffL).toByte
      out.writeByte(unsigned)
    }

    def writeUInt8(v: Boolean): Unit = {
      out.writeByte(if (v) 1 else 0)
    }

    def writeInt32(v: Int): Unit = {
      writeIntLeb(v)
    }

    private def writeIntLeb(v: Int): Unit = {
      out.write(0xFF & v)
      out.write(0xFF & (v >> 8))
      out.write(0xFF & (v >> 16))
      out.write(0xFF & (v >> 24))
    }
  }

  object DataOutputStreamOps {
    private final val U_INT8_MAX = (1 << 8) - 1
    private final val U_INT16_MAX = (1 << 16) - 1
    private final val U_INT32_MAX = (1L << 32) - 1
  }

  implicit class DataInputStreamOps(val in: DataInputStream) extends AnyVal {
    def readAsUInt128(): Int = { // todo advanced_functionality change to longer type?
      var shift = 0
      var result = 0
      var i = 0x80
      while ((i & 0x80) != 0) {
        i = in.readByte()
        result |= (i & 0x7f) << shift
        shift += 7
      }
      result
    }

    def readString(): String = {
      val length = readAsUInt128()
      val result = new Array[Byte](length)
      in.readFully(result)
      new String(result, "UTF-8")
    }

    def readUInt8(): Int = in.read()

    def readInt32(): Int = {
      val b1 = in.readByte()
      val b2 = in.readByte()
      val b3 = in.readByte()
      val b4 = in.readByte()
      DataInputStreamOps.fromBytes(b4, b3, b2, b1)
    }

    def readInt64(): Long = {
      val b1 = in.readByte()
      val b2 = in.readByte()
      val b3 = in.readByte()
      val b4 = in.readByte()
      val b5 = in.readByte()
      val b6 = in.readByte()
      val b7 = in.readByte()
      val b8 = in.readByte()
      DataInputStreamOps.fromBytes(b8, b7, b6, b5, b4, b3, b2, b1)
    }

    def readArrayInt64(length: Int): Array[Long] = {
      val data = new Array[Byte](8 * length)
      in.readFully(data)
      val result = new Array[Long](length)
      var i = 0
      while (i < data.length) {
        val b1 = data(i)
        val b2 = data(i + 1)
        val b3 = data(i + 2)
        val b4 = data(i + 3)
        val b5 = data(i + 4)
        val b6 = data(i + 5)
        val b7 = data(i + 6)
        val b8 = data(i + 7)
        result(i / 8) = DataInputStreamOps.fromBytes(b8, b7, b6, b5, b4, b3, b2, b1)
        i += 8
      }
      result
    }
  }

  object DataInputStreamOps {
    @inline
    final def fromBytes(b1: Byte, b2: Byte, b3: Byte, b4: Byte): Int = {
      b1 << 24 | (b2 & 0xFF) << 16 | (b3 & 0xFF) << 8 | (b4 & 0xFF)
    }

    @inline
    final def fromBytes(b1: Byte, b2: Byte, b3: Byte, b4: Byte, b5: Byte, b6: Byte, b7: Byte, b8: Byte): Long = {
      (b1 & 0xFFL) << 56 |
        (b2 & 0xFFL) << 48 |
        (b3 & 0xFFL) << 40 |
        (b4 & 0xFFL) << 32 |
        (b5 & 0xFFL) << 24 |
        (b6 & 0xFFL) << 16 |
        (b7 & 0xFFL) << 8 |
        (b8 & 0xFFL)
    }
  }
}
