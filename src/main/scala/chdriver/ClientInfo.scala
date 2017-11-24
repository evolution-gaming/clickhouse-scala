package chdriver

import java.io.DataOutputStream
import java.net.InetAddress

class ClientInfo(val queryKind: Int = ClientInfo.QueryKind.NO_QUERY,
                 val user: String = System.getProperty("user.name"),
                 val hostname: String = InetAddress.getLocalHost.getHostName,
                 val name: String) {
  // todo C++ what are these for
  val initialUser = ""
  val initialQueryId = ""
  val initialAddress = "0.0.0.0:0"
  val quotaKey = ""

  def writeItselfTo(out: DataOutputStream, serverRevision: Int): Unit = {
    import Protocol.DataOutputStreamOps

    if (serverRevision < ClickhouseVersionSpecific.DBMS_MIN_REVISION_WITH_CLIENT_INFO) ???

    out.writeUInt8(queryKind)
    if (queryKind == ClientInfo.QueryKind.NO_QUERY) return
    out.writeString(initialUser)
    out.writeString(initialQueryId)
    out.writeString(initialAddress)
    out.writeUInt8(ClientInfo.Interface.TCP) // hardcoded
    out.writeString(user)
    out.writeString(hostname)
    out.writeString(name)
    out.writeAsUInt128(DriverProperties.DBMS_VERSION_MAJOR)
    out.writeAsUInt128(DriverProperties.DBMS_VERSION_MINOR)
    out.writeAsUInt128(DriverProperties.CLIENT_VERSION)
    if (serverRevision >= ClickhouseVersionSpecific.DBMS_MIN_REVISION_WITH_QUOTA_KEY_IN_CLIENT_INFO) {
      out.writeString(queryKind.toString)
    }
  }

  private def isEmpty = queryKind == ClientInfo.QueryKind.NO_QUERY
}

object ClientInfo {
  object Interface {
    final val TCP = 1
    final val HTTP = 1
  }

  object QueryKind {
    final val NO_QUERY = 0 // uninitialized object
    final val INITIAL_QUERY = 1
    final val SECONDARY_QUERY = 2 // was initiated by another query for distributed query execution
  }
}
