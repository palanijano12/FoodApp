package com.foodapp.app.home.viewmodel

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.util.Log
import com.foodapp.app.home.model.FoodListModel
import com.foodapp.app.home.webservice.WebService
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class HomeVM : ViewModel() {
    val response = MutableLiveData<FoodListModel>()
    fun apiCall(){
        val api = WebService.create()
        val observable = api.foodList()
        observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
            { response -> getResponse(response) },
            { error -> displayError(error)}
        )
    }
    fun displayError(error: Throwable) {
        Log.i("VM", "Error while doing something", error)
    }
    fun getResponse(response: FoodListModel) {
        //Log.i("VM", "Message"+ Gson().toJson(response.foodList))
        this.response.value = response
    }

}