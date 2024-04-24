package co.pacastrillonp.appletdummy.repository

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream

class LocalWebServer(port: Int, private val file: File) : NanoHTTPD(port) {
    override fun serve(session: IHTTPSession): Response {
        return try {
            val fileInputStream = FileInputStream(file)
            val mimeType = when (file.extension) {
                "html" -> "text/html"
                "js" -> "application/javascript"
                "css" -> "text/css"
                else -> "application/octet-stream"
            }
            Log.d(
                "LocalWebServer",
                "Serving file with extension ${file.extension} and MIME type $mimeType"
            )
            newChunkedResponse(Response.Status.OK, mimeType, fileInputStream)
        } catch (e: Exception) {
            Log.e("LocalWebServer", "Error serving file", e)
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                "text/plain",
                "Internal Server Error"
            )
        }
    }
}