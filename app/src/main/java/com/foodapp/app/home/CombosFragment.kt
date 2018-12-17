package com.foodapp.app.home

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.foodapp.app.R
import com.foodapp.app.home.adapter.FoodListAdapter
import kotlinx.android.synthetic.main.fragment_combos.view.*

class CombosFragment : Fragment(){
    val foods: ArrayList<String> = ArrayList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_combos, container, false)
        getData()
        rootView.recycler_view!!.layoutManager = LinearLayoutManager(activity!!.applicationContext)
        rootView.recycler_view!!.addItemDecoration(LinearLayoutSpaceItemDecoration(20))
        rootView.recycler_view!!.adapter = FoodListAdapter(foods, activity!!.applicationContext)
        return rootView
    }

    fun getData() {
        foods.clear()
        foods.add("Sandwich")
        foods.add("Puffs")
        foods.add("Muffins")
        foods.add("Pizza")
    }
}