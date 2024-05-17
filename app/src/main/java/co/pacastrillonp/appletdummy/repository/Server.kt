package co.pacastrillonp.appletdummy.repository

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.content.file
import io.ktor.http.content.static
import io.ktor.routing.*
import io.ktor.response.*

fun Application.module() {
    install(ContentNegotiation) {
        gson()
    }

    routing {
        get("/api/data") {
            call.respond(mapOf("message" to "Hello, world!"))
        }

        static("/") {
            file("index.html")
        }
    }
}