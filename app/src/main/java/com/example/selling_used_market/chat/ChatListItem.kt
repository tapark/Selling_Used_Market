package com.example.selling_used_market.chat

data class ChatListItem(
    val sellerId: String,
    val buyerId: String,
    val itemTitle: String,
    val key: Long
) {
    constructor(): this("", "", "", 0)
}
