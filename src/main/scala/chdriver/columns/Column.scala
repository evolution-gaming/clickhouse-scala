package chdriver.columns

import java.io.DataInputStream

abstract class Column {
  type T
  val data: Array[T]

  def writeItselfTo(): Unit = ??? // todo basic_functionality for inserts
  override def toString: String = s"Column(${data.mkString(" ")})"
}

object Column {
  val ArrayRegex = "Array\\(([\\(\\)0-9A-Za-z]+)\\)".r
  val NullableRegex = "Nullable\\(([\\(\\)0-9A-Za-z]+)\\)".r

  def from(in: DataInputStream, itemsNumber: Int, chtype: String): Column = chtype match {
      case "Int32" => Int32Column.readAllFrom(in, itemsNumber)
      case "String" => StringColumn.readAllFrom(in, itemsNumber)
      case ArrayRegex(innerType) => ArrayColumn.readAllFrom(in, itemsNumber, innerType)
      case NullableRegex(innerType) => NullableColumn.readAllFrom(in, itemsNumber, innerType)
    }
}