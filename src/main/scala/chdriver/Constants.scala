package chdriver

object ClickhouseVersionSpecific {
  final val DBMS_MIN_REVISION_WITH_TEMPORARY_TABLES = 50264
  final val DBMS_MIN_REVISION_WITH_TOTAL_ROWS_IN_PROGRESS = 51554
  final val DBMS_MIN_REVISION_WITH_BLOCK_INFO = 51903
  final val DBMS_MIN_REVISION_WITH_CLIENT_INFO = 54032
  final val DBMS_MIN_REVISION_WITH_SERVER_TIMEZONE = 54058
  final val DBMS_MIN_REVISION_WITH_QUOTA_KEY_IN_CLIENT_INFO = 54060
}

object ClientPacketTypes {
  // Name, version, revision, default DB
  final val HELLO = 0

  // Query id, query settings, stage up to which the query must be executed,
  // whether the compression must be used, query text
  // (without data for INSERTs).
  final val QUERY = 1

  // A block of data (compressed or not).
  final val DATA = 2

  // Cancel the query execution.
  final val CANCEL = 3

  // Check that connection to the server is alive.
  final val PING = 4

  // Check status of tables on the server.
  final val TABLES_STATUS_REQUEST = 5
}

object QueryProcessingStage {
  final val FETCH_COLUMNS = 0
  final val WITH_MERGEABLE_STATE = 1
  final val COMPLETE = 2
}

object Compression {
  final val DISABLED = 0
  final val ENABLED = 1
}

object DriverProperties {
  final val DBMS_VERSION_MAJOR = 1
  final val DBMS_VERSION_MINOR = 1
  final val CLIENT_VERSION = 54276
  final val DEFAULT_PORT = 9000
  final val DBMS_DEFAULT_CONNECT_TIMEOUT_SEC = 10
  final val DBMS_DEFAULT_TIMEOUT_SEC = 300
  final val DBMS_DEFAULT_SYNC_REQUEST_TIMEOUT_SEC = 5
  final val DEFAULT_COMPRESS_BLOCK_SIZE = 1048576
  final val DEFAULT_INSERT_BLOCK_SIZE = 1048576
  final val DBMS_NAME = "ClickHouse"
  final val CLIENT_NAME = "scala-driver"
  final val BUFFER_SIZE = 65536 // todo basic_functionality read from config
}

object ServerPacketType {
  // Name, version, revision.
  final val HELLO = 0

  // A block of data (compressed or not).
  final val DATA = 1

  // The exception during query execution.
  final val EXCEPTION = 2

  // Query execution progress: rows read, bytes read.
  final val PROGRESS = 3

  // Ping response
  final val PONG = 4

  // All packets were transmitted
  final val END_OF_STREAM = 5

  // Packet with profiling info.
  final val PROFILE_INFO = 6

  // A block with totals (compressed or not).
  final val TOTALS = 7

  // A block with minimums and maximums (compressed or not).
  final val EXTREMES = 8

  // A response to TablesStatus request.
  final val TABLES_STATUS_RESPONSE = 9
}
