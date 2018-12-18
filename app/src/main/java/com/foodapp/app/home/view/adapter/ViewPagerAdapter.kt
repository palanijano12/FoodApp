package com.foodapp.app.home.view.adapter

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.foodapp.app.home.model.FoodListModel
import com.foodapp.app.home.view.CombosFragment
import com.foodapp.app.home.view.DrinksFragment
import com.google.gson.Gson

class ViewPagerAdapter(fm: FragmentManager, val con: Context, val fList: FoodListModel) : FragmentPagerAdapter(fm){

    override fun getItem(position: Int): Fragment {
        return when (position){

            0 -> {
                var bundle = Bundle()
                bundle.putString("data", Gson().toJson(fList))
                bundle.putInt("position", position)
                var fr = DrinksFragment()
                fr.arguments = bundle
                fr
            }
            else -> {
                var bundle = Bundle()
                bundle.putString("data", Gson().toJson(fList))
                bundle.putInt("position", position)
                var fr = CombosFragment()
                fr.arguments = bundle
                fr

            }/*else ->{
                return CrepesFragment()
            }*/
        }
    }

    override fun getCount(): Int {
        return fList.foodList!!.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        /*return when (position){
            0 -> con.getString(R.string.tab1)
            1 -> con.getString(R.string.tab2)
            else ->{
                return con.getString(R.string.tab3)
            }
        }*/
        return fList.foodList!![position].tabName
    }


}