package com.lajar.mystoryapp.Adapter

import androidx.fragment.app.Fragment
import com.lajar.mystoryapp.fragment.ListFragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lajar.mystoryapp.ListActivity
import com.lajar.mystoryapp.fragment.MapFragment

class SectionsPagerAdapter(activity:ListActivity): FragmentStateAdapter(activity) {
    override fun createFragment(position: Int): Fragment {
        val fragment =  when(position){
            0 -> ListFragment()
            else -> MapFragment()
        }
        return fragment
    }

    override fun getItemCount() = 2



}
