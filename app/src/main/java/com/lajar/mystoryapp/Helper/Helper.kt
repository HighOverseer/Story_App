package com.lajar.mystoryapp.Helper

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.os.Environment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object Helper {
    private var numOfPhotos = 0
    private const val PHOTO = "photo_"
    fun createCustomTempFile(context: Context): File {
        val storageDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("${PHOTO}${numOfPhotos + 1}", ".jpg", storageDir)
    }

    fun uriToFile(selectedImg: Uri, context: Context): File? {
        val contentResolver: ContentResolver = context.contentResolver
        val myFile = createCustomTempFile(context)

        val inputStream = contentResolver.openInputStream(selectedImg)
        if (inputStream != null) {
            val outputStream: OutputStream = FileOutputStream(myFile)
            val buf = ByteArray(1024)
            var len: Int
            while (inputStream.read(buf).also { len = it } > 0) outputStream.write(buf, 0, len)
            outputStream.close()
            inputStream.close()
            return myFile
        }
        return null
    }

    fun reduceFileImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var compressQuality = 100
        var streamLength: Int
        do {
            val bmpStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, bmpStream)
            val bmpPictByteArray = bmpStream.toByteArray()
            streamLength = bmpPictByteArray.size
            compressQuality -= 5
        } while (streamLength > 1000_000)
        bitmap.compress(Bitmap.CompressFormat.JPEG, compressQuality, FileOutputStream(file))
        return file
    }

    suspend fun convertToAddressLine(
        lat: Float?,
        lon: Float?,
        geocoder: Geocoder,
        addressNameDefault: String
    ) = withContext(
        Dispatchers.IO
    ) {
        var addressName = addressNameDefault
        if (lat != null && lon != null) {
            try {
                val list = geocoder.getFromLocation(lat.toDouble(), lon.toDouble(), 1)
                println("lat: $lat, lon: $lon")
                if (list != null && list.size != 0) {
                    addressName = "${list[0].adminArea}, ${list[0].countryName}"
                    addressName
                } else {
                    addressName
                }
            } catch (e: Exception) {
                e.printStackTrace()
                addressName
            }
        } else addressName
    }

    suspend fun convertToPosition(addressLine: String, geocoder: Geocoder): Address? =
        withContext(Dispatchers.IO) {
            try {
                val listAddress = geocoder.getFromLocationName(addressLine, 1)
                if (listAddress != null && listAddress.size != 0) {
                    listAddress[0]
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
}