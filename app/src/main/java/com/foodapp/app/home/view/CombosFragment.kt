package com.foodapp.app.home.view

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.foodapp.app.R
import com.foodapp.app.home.model.FnblistItem
import com.foodapp.app.home.model.FoodListModel
import com.foodapp.app.home.view.adapter.FoodListAdapter
import com.google.gson.Gson
import kotlinx.android.synthetic.main.fragment_combos.view.*

class CombosFragment : Fragment(){
    var foods: ArrayList<FnblistItem> = ArrayList()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        var rootView = inflater!!.inflate(R.layout.fragment_combos, container, false)
        getData(rootView)

        return rootView
    }

    fun getData(rootView: View) {
        foods.clear()
        val args = arguments
        if (args != null) {
            Log.e("Position", ">>"+args.getInt("position"))
            var position: Int = args.getInt("position")
            val fList: FoodListModel = Gson().fromJson(args.getString("data"), FoodListModel::class.java)
            foods = fList.foodList!!.get(position).fnblist!!
            rootView.recycler_view!!.layoutManager = LinearLayoutManager(activity!!.applicationContext)
            rootView.recycler_view!!.addItemDecoration(LinearLayoutSpaceItemDecoration(20))
            rootView.recycler_view!!.adapter = FoodListAdapter(foods, activity!!.applicationContext)
        }else{

        }


    }
}