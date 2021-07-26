package com.example.selling_used_market.home

data class ArticleModel(
    val sellerId: String,
    val title : String,
    val createAt : Long,
    val  price : String,
    val imageUrl: String
) {
    constructor() : this("", "", 0, "", "")
}
