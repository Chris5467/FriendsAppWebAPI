package com.example.routes

import com.example.db
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.findNewFriendsRouting() {
    route("/findfriends") {
        // Searching/adding NEW friends
        get {
            val username = call.request.queryParameters["username"]
            if (username == null) {
                call.respond(FindNewFriendsResponse(false,"Invalid username", emptyList()))
                return@get
            }
            val set = db.asyncQuery("SELECT `username` FROM `users` WHERE `username` LIKE ? LIMIT 10;", "$username%")
            if (set.size() == 0) {
                call.respond(FindNewFriendsResponse(false,"No users found", emptyList()))
                return@get
            }
            val users = arrayListOf<String>()
            while (set.next()){
                users.add(set.getString("username"))
            }
            call.respond(FindNewFriendsResponse(true, "Users found", users))
        }
    }
}

fun Application.registerFindNewFriendsRoutes() {
    routing {
        findNewFriendsRouting()
    }
}

@kotlinx.serialization.Serializable
data class FindNewFriendsResponse(val ok: Boolean, val message: String, val users: List<String>)