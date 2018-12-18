package com.foodapp.app.home.model

data class CombosItem(
        val foodName: String,
        val price: String,
        val totalPrice: String,
        val subItem1: String,
        val subItem2: String,
        val subItem3: String,
        val isSubItem: Boolean
)