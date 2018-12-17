package com.foodapp.app.home.webservice

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

class WebService{
    companion object {
        fun create(): APIService {

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(
                            RxJava2CallAdapterFactory.create())
                    .addConverterFactory(
                            GsonConverterFactory.create())
                    .baseUrl("http://www.mocky.io/")
                    .build()

            return retrofit.create(APIService::class.java)
        }
    }
}