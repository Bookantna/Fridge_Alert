package com.example.fridgealert.data

data class item(
    var name: String? = null,
    var category: String? = null,
    var expDate: String? = null,
    var quantity: Int? = null,
    var userId: String? = null
) {
    // No-argument constructor required by Firestore
    constructor() : this(null, null, null, null)
}
