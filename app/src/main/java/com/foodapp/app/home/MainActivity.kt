package com.foodapp.app.home

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.foodapp.app.R
import com.foodapp.app.home.adapter.ViewPagerAdapter
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        toolbar.toolbar_title.text = getString(R.string.title)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager, this)
        viewpager_main.adapter = viewPagerAdapter
        tabs_main.setupWithViewPager(viewpager_main)
    }
}
