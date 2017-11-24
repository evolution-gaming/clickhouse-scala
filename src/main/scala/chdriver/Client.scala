package chdriver

class Client(val insertBlockSize: Int = DriverProperties.DEFAULT_INSERT_BLOCK_SIZE,
             val connection: Connection = new Connection()) {
  def disconnect(): Unit = connection.disconnect()

  def execute[T](query: String, settings: ClickhouseProperties)(implicit decoder: Decoder[T]): Iterator[T] = {
    // todo basic_functionality insert vs select distinction
    connection.forceConnect()

    connection.sendQuery(query, settings)
    connection.sendExternalTables()

    // todo advanced_functionality progress vs no_progress
    receiveResult()
  }

  def receiveResult[T: Decoder](withColumnTypes: Boolean = false,
                                progress: Boolean = false,
                                columnar: Boolean = false): Iterator[T] = {
    receiveResultNoProgress(columnar, Iterator[T]())
  }

  @annotation.tailrec
  final def receiveResultNoProgress[T: Decoder](columnar: Boolean = false, result: Iterator[T]): Iterator[T] = {
    connection.receivePacket() match {
      case data: DataPacket[T] @unchecked =>
        receiveResultNoProgress(columnar, result ++ data.block.iterator)

      case profileInto: ProfileInfoPacket =>
        // todo do smth
        receiveResultNoProgress(columnar, result)

      case progress: ProgressPacket =>
        // todo do smth, backpressure?
        receiveResultNoProgress(columnar, result)

      case EndOfStreamPacket =>
        result

      case UnrecognizedPacket =>
        receiveResultNoProgress(columnar, result)
    }
  }
}
