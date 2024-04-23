package co.pacastrillonp.appletdummy.repository
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.IOException

interface LocalServerRepository {
    fun startServer(file: File): Boolean
}

class LocalServerRepositoryImpl : LocalServerRepository {

    private var server: LocalWebServer? = null

    override fun startServer(file: File): Boolean {
        return try {
            server?.stop()
            server = LocalWebServer(8080, file)
            server?.start()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

}

class LocalWebServer(private val port: Int, private val file: File) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        val fileInputStream = FileInputStream(file)
        return newChunkedResponse(Response.Status.OK, "text/html", fileInputStream)
    }
}