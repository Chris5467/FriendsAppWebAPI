package com.example.routes.user

import com.example.db
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userFriendReqsRouting() {
    route("/user/friendreqs") {
        // Sending friend requests
        post {
            val request = call.receive<ReqRequest>()
            // Checking whether friend request has already been sent
            val set = db.asyncQuery(
                "SELECT `sender_username` FROM `friend_reqs` WHERE `sender_username` = ? AND `receiver_username` = ?;",
                request.sender_username,
                request.receiver_username
            )
            if (set.size() == 1) {
                call.respond(SendReqResponse(false, "Friend request has already been sent"))
                return@post
            }

            db.asyncUpdateQuery(
                "INSERT INTO `friend_reqs`(`sender_username`, `receiver_username`) VALUES (?,?);",
                request.sender_username, request.receiver_username
            )
            call.respond(SendReqResponse(true, "Friend request sent"))
        }
        // Listing friend requests
        get {
            val username = call.request.queryParameters["username"]
            if (username == null) {
                call.respond(FriendReqResponse(false,"Invalid username", emptyList()))
                return@get
            }
            val set = db.asyncQuery("SELECT `sender_username` FROM `friend_reqs` WHERE `receiver_username` = ?;", username)
            if (set.size() == 0) {
                call.respond(FriendReqResponse(false,"No friend requests found", emptyList()))
                return@get
            }
            val friendRequests = arrayListOf<String>()
            while (set.next()){
                friendRequests.add(set.getString("sender_username"))
            }
            call.respond(FriendReqResponse(true, "Friend requests found", friendRequests))
        }
        // Delete friend request if denied
        delete {
            val username = call.request.queryParameters["username"]
            val deletedFriendReq = call.request.queryParameters["deletedRequest"]
            if (username == null || deletedFriendReq == null) {
                call.respond(DeleteFriendsResponse(false,"Invalid username or friends username"))
                return@delete
            }
            val set = db.asyncQuery(
                "SELECT `sender_username` FROM `friend_reqs` WHERE `sender_username` = ? AND `receiver_username` = ?;",
                username,
                deletedFriendReq
            )
            if (set.size() == 0) {
                call.respond(DeleteReqResponse(false, "No friend request found"))
                return@delete
            }
            db.asyncUpdateQuery(
                "DELETE FROM `friend_reqs` WHERE sender_username = ? AND receiver_username = ?;",
                username,
                deletedFriendReq
            )
            call.respond(DeleteReqResponse(true, "Friend request has been deleted"))
        }
    }
}

@kotlinx.serialization.Serializable
data class ReqRequest(val sender_username: String, val receiver_username: String)

@kotlinx.serialization.Serializable
data class SendReqResponse(val ok: Boolean, val message: String)

@kotlinx.serialization.Serializable
data class FriendReqResponse(val ok: Boolean, val message: String, val friendRequests: List<String>)

@kotlinx.serialization.Serializable
data class DeleteReqResponse(val ok: Boolean, val message: String)

@kotlinx.serialization.Serializable
data class DeleteReqRequest(val username: String, val deletedFriendReq: String)




