package com.lajar.mystoryapp.Helper

import android.location.Address
import android.location.Geocoder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class HelperTest{

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var geocoder: Geocoder
    @Mock
    private lateinit var address: Address

    @Test
    fun `When Given Coordinate Should Return Address If Lat & Lon Not Null`() = runTest{
        val listAddress = listOf(address)
        val expectedAddressLine = DataDummy.dummyAddressLine

        `when`(
            geocoder.getFromLocation(
                DataDummy.dummyLat.toDouble(),
                DataDummy.dummyLon.toDouble(),
            1
            )
        ).thenReturn(
            listAddress
        )
        `when`(listAddress[0].getAddressLine(0)).thenReturn(expectedAddressLine)

        assertEquals(Helper.convertToAddressLine(DataDummy.dummyLat, DataDummy.dummyLon, geocoder, "No Location Found"), expectedAddressLine)
        assertEquals(Helper.convertToAddressLine(DataDummy.dummyLat, null, geocoder, "No Location Found"), "No Location Found")
        assertEquals(Helper.convertToAddressLine(null, DataDummy.dummyLon, geocoder, "No Location Found"), "No Location Found")
        assertEquals(Helper.convertToAddressLine(null, null, geocoder, "No Location Found"), "No Location Found")
    }

    @Test
    fun `when given addressLine Should return Lat & Lon position, If Not Found return Null`() = runTest{
        val expectedAddress = DataDummy.dummyAddres
        val listAddress = listOf(expectedAddress)

        `when`(geocoder.getFromLocationName(DataDummy.dummyAddressLine, 1)).thenReturn(listAddress)
        `when`(geocoder.getFromLocationName("", 1)).thenReturn(listOf<Address>())

        assertEquals(expectedAddress, Helper.convertToPosition(DataDummy.dummyAddressLine, geocoder))
        assertNull(Helper.convertToPosition("", geocoder))
    }


}