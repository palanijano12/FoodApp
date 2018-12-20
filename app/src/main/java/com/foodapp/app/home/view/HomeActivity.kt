package com.foodapp.app.home.view

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.foodapp.app.R
import com.foodapp.app.home.model.FoodListModel
import com.foodapp.app.home.view.adapter.ViewPagerAdapter
import com.foodapp.app.home.viewmodel.HomeVM
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class HomeActivity : AppCompatActivity(){
    lateinit var homeVM: HomeVM
    var price: Int = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initViews()
    }

    private fun initViews(){
        setSupportActionBar(toolbar)
        toolbar.toolbar_title.text = getString(R.string.title)

        homeVM = ViewModelProviders.of(this).get(HomeVM::class.java)
        homeVM.apiCall()
        homeVM.response.observe(this, object: Observer<FoodListModel>{
            override fun onChanged(t: FoodListModel?) {
                val str: String = Gson().toJson(t)
                Log.e("MainActivity", Gson().toJson(t))
                val foodList: FoodListModel = Gson().fromJson(str, FoodListModel::class.java)
                val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, applicationContext, foodList)
                viewpager_main.adapter = viewPagerAdapter
                tabs_main.setupWithViewPager(viewpager_main)
            }
        })
        total_amount.text = "AED "+price+" ^"
        registerReceiver(broadcastPriceReduce, IntentFilter("price_reduce_receiver"))
        registerReceiver(broadcastPriceAdd, IntentFilter("price_receiver"))
    }

    val broadcastPriceAdd = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            if(p1 != null){
                price += p1.getIntExtra("price", -1)
                total_amount.text = "AED "+price+"  ^"
            }
        }

    }
    val broadcastPriceReduce = object : BroadcastReceiver(){
        override fun onReceive(p0: Context?, p1: Intent?) {
            if(p1 != null){
                price -= p1.getIntExtra("price", -1)
                total_amount.text = "AED "+price+"  ^"
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(broadcastPriceAdd)
        unregisterReceiver(broadcastPriceReduce)
    }

}
