package com.example.routes.user

import com.example.db
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userLocationRouting() {
    route("/user/location") {
        // Update location of the current user
        post {
            val request = call.receive<LocationRequest>()
            db.asyncUpdateQuery(
                "INSERT INTO `location`(`username`, `long`, `lat`) VALUES (?,?,?) " +
                        "ON DUPLICATE KEY UPDATE `long` = ?,`lat` = ?;",
                request.username, request.long, request.lat,
                request.long, request.lat
            )
            call.respond(LocationResponse(true, "Location has been updated"))
        }
    }
}

@kotlinx.serialization.Serializable
data class LocationRequest(val username: String, val long: Float, val lat: Float)

@kotlinx.serialization.Serializable
data class LocationResponse(val ok:Boolean, val message: String)