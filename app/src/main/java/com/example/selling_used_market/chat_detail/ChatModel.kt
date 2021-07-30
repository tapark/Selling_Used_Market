package com.example.selling_used_market.chat_detail

data class ChatModel(
    val senderId: String,
    val message: String,
) {
    constructor(): this("", "")
}
