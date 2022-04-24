package com.example

import com.example.routes.registerFindNewFriendsRoutes
import com.example.routes.registerUserRoutes
import com.example.storage.Database
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import kotlinx.serialization.json.Json

val db = Database("167.86.73.179", 2010, "fyp_db", "fyp", "fyp")

fun main(args: Array<String>): Unit = EngineMain.main(args)

fun Application.module() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
    registerUserRoutes()
    registerFindNewFriendsRoutes()
}
