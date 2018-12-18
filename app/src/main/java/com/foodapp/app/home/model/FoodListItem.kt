package com.foodapp.app.home.model

import com.google.gson.annotations.SerializedName

data class FoodListItem(@SerializedName("fnblist")
                        val fnblist: ArrayList<FnblistItem>?,
                        @SerializedName("TabName")
                        val tabName: String = "")