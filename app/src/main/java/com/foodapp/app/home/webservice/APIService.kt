package com.foodapp.app.home.webservice

import com.foodapp.app.home.model.FoodListModel
import io.reactivex.Observable
import retrofit2.http.GET

interface APIService{
    @GET("v2/5b700cff2e00005c009365cf")
    fun foodList(): Observable<FoodListModel>
}