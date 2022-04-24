package com.example.routes

import com.example.routes.user.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.registerUserRoutes() {
    routing {
        userRegisterRouting()
        userLoginRouting()
        userLocationRouting()
        userFriendsRouting()
        userFriendReqsRouting()
    }
}
