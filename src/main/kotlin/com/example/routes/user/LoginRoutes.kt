package com.example.routes.user

import com.example.db
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.userLoginRouting() {
    route("/user/login") {
        get {
            val username = call.request.queryParameters["username"]
            val password = call.request.queryParameters["password"]
            if (username == null || password == null) {
                call.respond(LoginResponse(false,"Invalid username or password", ""))
                return@get
            }
            val set = db.asyncQuery("SELECT `username` FROM `users` WHERE `username` = ? AND `password` = ?;", username, password)
            if (set.size() != 1) {
                call.respond(LoginResponse(false,"Invalid username or password", ""))
                return@get
            }
            call.respond(LoginResponse(true, "Login successful", username))
        }
    }
}

@kotlinx.serialization.Serializable
data class LoginResponse(val ok: Boolean, val message: String, val username: String)
