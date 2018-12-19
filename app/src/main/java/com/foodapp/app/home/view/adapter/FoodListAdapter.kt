package com.foodapp.app.home.view.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.foodapp.app.R
import com.foodapp.app.home.model.FnblistItem
import com.foodapp.app.home.webservice.PriceUpdateListener
import kotlinx.android.synthetic.main.list_item.view.*

class FoodListAdapter(val items : ArrayList<FnblistItem>, val context: Context, val priceListener: PriceUpdateListener) : RecyclerView.Adapter<FoodViewHolder>() {
    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        var total: Int = 0
        if(items.get(position).subItemCount == 0){
            holder.subItem.visibility = View.GONE
            holder.price.text = "AED "+items.get(position).itemPrice
        }else if(items.get(position).subItemCount == 2){
            holder.subItem.visibility = View.VISIBLE
            holder.segment1.visibility = View.VISIBLE
            holder.segment1.text = items.get(position).subitems!!.get(0).name
            holder.segment2.visibility = View.VISIBLE
            holder.segment2.text = items.get(position).subitems!!.get(1).name
            holder.segment3.visibility = View.GONE
            holder.subItemSelection(items.get(position).subItemCount)
            holder.price.text = "AED "+items.get(position).subitems!!.get(0).subitemPrice
        }else{
            holder.subItem.visibility = View.VISIBLE
            holder.segment1.visibility = View.VISIBLE
            holder.segment1.text = items.get(position).subitems!!.get(0).name
            holder.segment2.visibility = View.VISIBLE
            holder.segment2.text = items.get(position).subitems!!.get(1).name
            holder.segment3.visibility = View.VISIBLE
            holder.segment3.text = items.get(position).subitems!!.get(2).name
            holder.subItemSelection(items.get(position).subItemCount)
            holder.price.text = "AED "+items.get(position).subitems!!.get(0).subitemPrice
        }

        holder.foodName.text = items.get(position).name.toUpperCase()
        Glide.with(context)
                .load(items.get(position).imageUrl)
                .into(holder.foodImage)
        holder.totalCount.text = "0"

        holder.segment1.setOnClickListener{v ->
            holder.segment1.setBackgroundResource(R.drawable.segment_select_bg)
            holder.segment2.setBackgroundResource(R.drawable.segment_bg)
            holder.segment3.setBackgroundResource(R.drawable.segment_bg)
            holder.price.text = "AED "+items.get(position).subitems!!.get(0).subitemPrice
            holder.totalCount.text = "0"
        }
        holder.segment2.setOnClickListener{v ->
            holder.segment1.setBackgroundResource(R.drawable.segment_bg)
            holder.segment2.setBackgroundResource(R.drawable.segment_select_bg)
            holder.segment3.setBackgroundResource(R.drawable.segment_bg)
            holder.price.text = "AED "+items.get(position).subitems!!.get(1).subitemPrice
            holder.totalCount.text = "0"
        }
        holder.segment3.setOnClickListener{v ->
            holder.segment1.setBackgroundResource(R.drawable.segment_bg)
            holder.segment2.setBackgroundResource(R.drawable.segment_bg)
            holder.segment3.setBackgroundResource(R.drawable.segment_select_bg)
            holder.price.text = "AED "+items.get(position).subitems!!.get(2).subitemPrice
            holder.totalCount.text = "0"
        }

        holder.addItem.setOnClickListener{v ->
            if(holder.totalCount.text.toString().toInt() >= 0){
                val add: Int = holder.totalCount.text.toString().toInt()+1
                val str = holder.price.text.toString().split("AED ").get(1)
                val amt = str.toFloat();
                Log.e("Adapter", ""+amt.toInt())
                val price: Int = amt.toInt()
                val totalPrice = price * add
                priceListener.updatePrice(totalPrice)
                holder.totalCount.text = add.toString()
            }
        }

        holder.divideItem.setOnClickListener{v ->
            if(holder.totalCount.text.toString().toInt() > 0){
                val add: Int = holder.totalCount.text.toString().toInt()-1
                val str = holder.price.text.toString().split("AED ").get(1)
                val amt = str.toFloat();
                Log.e("Adapter", ""+amt.toInt())
                val price: Int = amt.toInt()
                val totalPrice = price * add
                priceListener.updatePrice(totalPrice)
                holder.totalCount.text = add.toString()
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        return FoodViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item, parent, false), items)
    }

    override fun getItemCount(): Int {
        return items.size
    }


}

class FoodViewHolder (view: View, val items : ArrayList<FnblistItem>) : RecyclerView.ViewHolder(view){
    val foodName =   view.food_name
    val foodImage = view.food_image
    val segment1 = view.segment1
    val segment2 = view.segment2
    val segment3 = view.segment3
    val subItem = view.sub_item
    val divideItem = view.minus_img
    val addItem = view.add_img
    val totalCount = view.txt_count
    val price = view.price

    fun subItemSelection(count: Int){
        if(count == 2){
            segment1.setBackgroundResource(R.drawable.segment_select_bg)
            segment2.setBackgroundResource(R.drawable.segment_bg)
        }else if(count == 3){
            segment1.setBackgroundResource(R.drawable.segment_select_bg)
            segment2.setBackgroundResource(R.drawable.segment_bg)
            segment3.setBackgroundResource(R.drawable.segment_bg)
        }
    }

    fun totalPrice(list: ArrayList<Int>): Int{
        var total: Int = 0
        for(e in list){
            total = total + e
        }
        return total
    }

}