package com.foodapp.app.home.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.foodapp.app.R
import kotlinx.android.synthetic.main.list_item.view.*

class FoodListAdapter(val items : ArrayList<String>, val context: Context) : RecyclerView.Adapter<FoodViewHolder>() {
    override fun onBindViewHolder(p0: FoodViewHolder, p1: Int) {
        p0.food_name.text = items.get(p1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        return FoodViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }



}

class FoodViewHolder (view: View) : RecyclerView.ViewHolder(view){
    val food_name = view.food_name
}