package com.foodapp.app.home.model

import com.google.gson.annotations.SerializedName

data class FnblistItem(@SerializedName("VegClass")
                       val vegClass: String = "",
                       @SerializedName("ItemPrice")
                       val itemPrice: String = "",
                       @SerializedName("ImageUrl")
                       val imageUrl: String = "",
                       @SerializedName("ImgUrl")
                       val imgUrl: String = "",
                       @SerializedName("Name")
                       val name: String = "",
                       @SerializedName("OtherExperience")
                       val otherExperience: String = "",
                       @SerializedName("Cinemaid")
                       val cinemaid: String = "",
                       @SerializedName("PriceInCents")
                       val priceInCents: String = "",
                       @SerializedName("subitems")
                       val subitems: List<SubitemsItem>?,
                       @SerializedName("SevenStarExperience")
                       val sevenStarExperience: String = "",
                       @SerializedName("TabName")
                       val tabName: String = "",
                       @SerializedName("SubItemCount")
                       val subItemCount: Int = 0,
                       @SerializedName("VistaFoodItemId")
                       val vistaFoodItemId: String = "")