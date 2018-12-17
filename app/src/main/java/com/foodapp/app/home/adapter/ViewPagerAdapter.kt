package com.foodapp.app.home.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.foodapp.app.R
import com.foodapp.app.home.CombosFragment
import com.foodapp.app.home.CrepesFragment
import com.foodapp.app.home.DrinksFragment

class ViewPagerAdapter(fm: FragmentManager, con: Context) : FragmentPagerAdapter(fm){
    private var con: Context = con
    override fun getItem(position: Int): Fragment {
        return when (position){
            0 -> {
                CombosFragment()
            }
            1 -> {
                DrinksFragment()
            }else ->{
                return CrepesFragment()
            }
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position){
            0 -> con.getString(R.string.tab1)
            1 -> con.getString(R.string.tab2)
            else ->{
                return con.getString(R.string.tab3)
            }
        }
    }

}