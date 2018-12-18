package com.foodapp.app.home.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.foodapp.app.R
import com.foodapp.app.home.model.FnblistItem

class FoodListAdapter(val items : ArrayList<FnblistItem>, val context: Context) : RecyclerView.Adapter<FoodViewHolder>() {
    override fun onBindViewHolder(p0: FoodViewHolder, p1: Int) {
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        return FoodViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }



}

class FoodViewHolder (view: View) : RecyclerView.ViewHolder(view){
    fun bindView(){

    }
}