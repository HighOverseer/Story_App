package com.lajar.mystoryapp.Helper

import android.location.Address
import com.lajar.mystoryapp.data.local.entity.Story
import java.util.*

object DataDummy {
    fun generatDummyStories():List<Story>{
        val stories = mutableListOf<Story>()
        for (i in 0..99){
            stories.add(
                Story(
                    i.toString(),
                    "name ${i+1}",
                    "desc ${i+1}",
                    "url ${i+1}",
                    i.toFloat(),
                    i.toFloat()
                )
            )
        }
        return stories
    }

    val dummyAddres = Address(Locale.getDefault())
    const val dummyAdminArea = "Jawa Barat"
    const val dummyCountryName = "Indonesia"
    const val dummyLat:Float = -6.780725f
    const val dummyLon:Float = 107.637405f

}