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
        val expectedAdress = "${DataDummy.dummyAdminArea}, ${DataDummy.dummyCountryName}"

        `when`(
            geocoder.getFromLocation(
                DataDummy.dummyLat.toDouble(),
                DataDummy.dummyLon.toDouble(),
            1
            )
        ).thenReturn(
            listAddress
        )
        `when`(listAddress[0].adminArea).thenReturn(DataDummy.dummyAdminArea)
        `when`(listAddress[0].countryName).thenReturn(DataDummy.dummyCountryName)

        assertEquals(expectedAdress, Helper.convertToAddressLine(DataDummy.dummyLat, DataDummy.dummyLon, geocoder, "No Location Found"))
        assertEquals("No Location Found", Helper.convertToAddressLine(DataDummy.dummyLat, null, geocoder, "No Location Found"))
        assertEquals("No Location Found", Helper.convertToAddressLine(null, DataDummy.dummyLon, geocoder, "No Location Found"))
        assertEquals("No Location Found", Helper.convertToAddressLine(null, null, geocoder, "No Location Found"))
    }

    @Test
    fun `when given addressLine Should return Lat & Lon position, If Not Found return Null`() = runTest{
        val expectedAddress = DataDummy.dummyAddres
        val listAddress = listOf(expectedAddress)

        `when`(geocoder.getFromLocationName(DataDummy.dummyAdminArea, 1)).thenReturn(listAddress)
        `when`(geocoder.getFromLocationName("", 1)).thenReturn(listOf<Address>())

        assertEquals(expectedAddress, Helper.convertToPosition(DataDummy.dummyAdminArea, geocoder))
        assertNull(Helper.convertToPosition("", geocoder))
    }


}