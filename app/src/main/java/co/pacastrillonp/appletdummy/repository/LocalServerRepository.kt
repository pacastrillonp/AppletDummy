package co.pacastrillonp.appletdummy.repository

import android.util.Log
import fi.iki.elonen.NanoHTTPD
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.IOException

class LocalWebServer(private val file: File, private val rootDirectory: File) : NanoHTTPD(8080) {

    override fun serve(session: IHTTPSession): Response {
        return when (val uri = session.uri) {
            "/" -> {
                val msg =
                    "<html><body><h1>Hola, este es mi servidor web en Android</h1></body></html>"
                newFixedLengthResponse(msg)
            }
            "/web" -> {
                serveHtmlFile()
            }
            else -> {
                when {
                    uri.endsWith(".html") -> serveFile("text/html", uri)
                    uri.endsWith(".js") -> serveFile("application/javascript", uri)
                    uri.endsWith(".css") -> serveFile("text/css", uri)
                    uri.endsWith(".jpg") || uri.endsWith(".jpeg") -> serveFile("image/jpeg", uri)
                    uri.endsWith(".png") -> serveFile("image/png", uri)
                    else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
                }
            }
        }
    }

    private fun serveFile(mimeType: String, uri: String): Response {
      return  try {
            val fileInputStream = FileInputStream(File(rootDirectory, uri))
            return newChunkedResponse(Response.Status.OK, mimeType, fileInputStream)
        } catch (e: FileNotFoundException) {
            newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "File Not Found: $uri")
        } catch (e: IOException) {
            newFixedLengthResponse(Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, "Internal Server Error: ${e.message}")
        }
    }

    private fun serveHtmlFile(): Response {
        return try {
            val fileInputStream = FileInputStream(file)

            val buffer = file.inputStream().use { it.readBytes() }
            fileInputStream.close()
            newFixedLengthResponse(Response.Status.OK, "text/html", String(buffer, Charsets.UTF_8))
        } catch (ioe: IOException) {

            Log.e("HTTP server", "serveHtmlFile Internal Error: ${ioe.message}")
            newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR,
                MIME_PLAINTEXT,
                "Internal Error: ${ioe.message}"
            )
        }
    }

}