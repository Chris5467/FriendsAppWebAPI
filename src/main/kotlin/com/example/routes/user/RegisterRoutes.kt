package com.example.routes.user

import com.example.db
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.userRegisterRouting() {
    route("/user/register") {
        post {
            val request = call.receive<RegisterRequest>()
            val set = db.asyncQuery(
                "SELECT `username` FROM `users` WHERE `username` = ? OR `email` = ?;",
                request.username,
                request.email
            )
            if (set.size() == 1) {
                call.respond(RegisterResponse(false, "Email or username have already been used"))
                return@post
            }
            db.asyncUpdateQuery(
                "INSERT INTO `users`(`username`, `password`, `email`) VALUES (?,?,?);",
                request.username, request.password, request.email
            )
            call.respond(RegisterResponse(true, "User has been added"))
        }
    }
}

@kotlinx.serialization.Serializable
data class RegisterRequest(val email: String, val username: String, val password: String)

@kotlinx.serialization.Serializable
data class RegisterResponse(val ok:Boolean, val message: String)