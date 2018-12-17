package com.foodapp.app.home.model

import com.google.gson.annotations.SerializedName

data class SubitemsItem(@SerializedName("SubitemPrice")
                        val subitemPrice: String = "",
                        @SerializedName("VistaSubFoodItemId")
                        val vistaSubFoodItemId: String = "",
                        @SerializedName("PriceInCents")
                        val priceInCents: String = "",
                        @SerializedName("VistaParentFoodItemId")
                        val vistaParentFoodItemId: String = "",
                        @SerializedName("Name")
                        val name: String = "")