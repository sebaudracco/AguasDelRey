package com.sebaudracco.aguasdelrey.data.model

data class Product (
    var id: String,
    var description: String,
    var quantity: Int,
    var delivered: Boolean,
    var price: String
)