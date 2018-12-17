package com.foodapp.app.home.model

import com.google.gson.annotations.SerializedName

data class Status(@SerializedName("Description")
                  val description: String = "",
                  @SerializedName("Id")
                  val id: String = "")