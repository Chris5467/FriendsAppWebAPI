package com.example.routes.user

import com.example.db
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userFriendsRouting() {
    route("/user/friends") {
        // Searching existing friends
        get {
            val username = call.request.queryParameters["username"]
            if (username == null) {
                call.respond(ListFriendsResponse(false,"Invalid username", emptyList()))
                return@get
            }
            val set = db.asyncQuery("SELECT `friend_username` FROM `friends` WHERE `username` = ?;", username)
            if (set.size() == 0) {
                call.respond(ListFriendsResponse(false,"No friends found", emptyList()))
                return@get
            }
            val friends = arrayListOf<String>()
            while (set.next()){
                friends.add(set.getString("friend_username"))
            }
            call.respond(ListFriendsResponse(true, "Friends found", friends))
        }
        // Add friends
        post {
            val request = call.receive<AddFriendRequest>()
            // Checking whether friend request has already been sent
            val set = db.asyncQuery(
                "SELECT `sender_username` FROM `friend_reqs` WHERE `sender_username` = ? AND `receiver_username` = ?;",
                request.friend_username,
                request.username
            )
            if (set.size() == 0) {
                call.respond(AddFriendResponse(false, "Friend request not found"))
                return@post
            }

            db.asyncUpdateQuery(
                "INSERT INTO `friends`(`username`, `friend_username`) VALUES (?,?);",
                request.username, request.friend_username
            )
            db.asyncUpdateQuery(
                "INSERT INTO `friends`(`username`, `friend_username`) VALUES (?,?);",
                request.friend_username, request.username
            )
            db.asyncUpdateQuery(
                "DELETE FROM `friend_reqs` WHERE sender_username = ? AND receiver_username = ?",
                request.friend_username, request.username
            )
            call.respond(AddFriendResponse(true, "Friend added"))
        }

        // Remove friends
        delete {
            val username = call.request.queryParameters["username"]
            val deletedFriend = call.request.queryParameters["deletedFriend"]
            if (username == null || deletedFriend == null) {
                call.respond(DeleteFriendsResponse(false,"Invalid username or friends username"))
                return@delete
            }
            val set = db.asyncQuery(
                "SELECT `username` FROM `friends` WHERE `username` = ? AND `friend_username` = ?;",
                username,
                deletedFriend
            )
            if (set.size() == 0) {
                call.respond(DeleteFriendsResponse(false, "No friends could be found with this username"))
                return@delete
            }
            db.asyncUpdateQuery(
                "DELETE FROM `friends` WHERE username = ? AND friend_username = ?;",
                username,
                deletedFriend
            )
            call.respond(DeleteFriendsResponse(true, "Friend has been deleted"))
        }
        // Get all friend locations
        get("/locations") {
            // Retrieving list of friends
            val username = call.request.queryParameters["username"]
            if (username == null) {
                call.respond(FriendsLocationsResponse(false,"Invalid username", emptyList()))
                return@get
            }
            val set = db.asyncQuery("SELECT `friend_username` FROM `friends` WHERE `username` = ?;", username)
            if (set.size() == 0) {
                call.respond(FriendsLocationsResponse(false,"No friends found", emptyList()))
                return@get
            }
            val friends = arrayListOf<String>()
            while (set.next()){
                friends.add(set.getString("friend_username"))
            }
            val friendsLocations = arrayListOf<UserLocation>()
            friends.forEach {
                val set2 = db.asyncQuery("SELECT * FROM `location` WHERE `username` = ?;", it)
                if (set2.next()) {
                    friendsLocations.add(UserLocation(set2.getString("username"), set2.getFloat("long"), set2.getFloat("lat")))
                }
            }
            call.respond(FriendsLocationsResponse(true, "Friends Locations sent", friendsLocations))
        }
    }
}

@kotlinx.serialization.Serializable
data class ListFriendsResponse(val ok: Boolean, val message: String, val users: List<String>)

@kotlinx.serialization.Serializable
data class AddFriendResponse(val ok: Boolean, val message: String)

@kotlinx.serialization.Serializable
data class AddFriendRequest(val username: String, val friend_username: String)

@kotlinx.serialization.Serializable
data class DeleteFriendsResponse(val ok: Boolean, val message: String)

@kotlinx.serialization.Serializable
data class FriendsLocationsResponse(val ok: Boolean, val message: String, val friendsLocations: List<UserLocation>)

@kotlinx.serialization.Serializable
data class UserLocation(val username: String, val long: Float, val lat: Float)