package com.lajar.mystoryapp

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import com.lajar.mystoryapp.data.Result
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.CompoundButton
import android.widget.ImageView
import android.widget.RadioGroup.OnCheckedChangeListener
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.lajar.mystoryapp.Helper.Helper
import com.lajar.mystoryapp.ViewModel.AddViewModel
import com.lajar.mystoryapp.ViewModel.ViewModelFactory
import com.lajar.mystoryapp.data.Event
import com.lajar.mystoryapp.databinding.ActivityAddBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class AddActivity : AppCompatActivity() {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("user_info")
    private lateinit var addViewModel: AddViewModel
    private lateinit var binding: ActivityAddBinding
    private lateinit var currentPhotoPath: String
    private var btnAddStory: MenuItem? = null
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var geocoder: Geocoder


    companion object {
        private const val AUTHORITY = "com.lajar.mystoryapp"
    }

    private val launcherIntentCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val myFile = File(currentPhotoPath)
            addViewModel.setCurrentImage(myFile)
        }
    }

    private val launcherIntentGallery = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            val nSelectedImg: Uri? = it.data?.data
            val selectedImg: Uri
            if (nSelectedImg != null) {
                selectedImg = nSelectedImg
                val myFile = Helper.uriToFile(selectedImg, this@AddActivity)
                if (myFile != null) {
                    addViewModel.setCurrentImage(myFile)
                }
            }

        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ){ permissions ->
        when{
            permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION]?:false ->{
                uploadStory()
            }
            permissions[android.Manifest.permission.ACCESS_FINE_LOCATION]?:false ->{
                uploadStory()
            }else ->{
                Toast.makeText(this, "Can't access location, permissions is not granted", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()

        addViewModel.currentImage.observe(this) { imageFile ->
            binding.ivAddPhoto.loadImageFile(imageFile)
        }

        addViewModel.addStoryResponse.observe(this) { result ->
            checkResult(result)
        }

        binding.btnAddCamera.setOnClickListener {
            openCamera()
        }

        binding.btnAddGallery.setOnClickListener {
            openGallery()
        }

        binding.switchAutoLocation.setOnCheckedChangeListener { _, isChecked ->
            checkSwitch(isChecked)
        }


    }

    private fun init(){
        addViewModel = obtainViewModel(this, dataStore)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        geocoder = Geocoder(this, Locale.getDefault())
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_tambah, menu)
        btnAddStory = menu?.findItem(R.id.button_add)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.button_add) {
            uploadStory()
        } else if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)

        Helper.createCustomTempFile(application).also {
            val photoUri: Uri = FileProvider.getUriForFile(
                this@AddActivity,
                AUTHORITY,
                it
            )
            currentPhotoPath = it.absolutePath
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
            launcherIntentCamera.launch(intent)

        }
    }

    private fun openGallery() {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, getString(R.string.choose_a_picture))
        launcherIntentGallery.launch(chooser)
    }


    private fun uploadStory() = lifecycleScope.launch(Dispatchers.Main){
        binding.apply {
            val desc = edAddDescription.text.toString()
            when {
                desc.isBlank() || addViewModel.currentImage.value == null
                -> {
                    edAddDescription.error =
                        getString(R.string.description_fill_error)
                }
                else -> {
                    if (!switchAutoLocation.isChecked && edAddLocation.text.isBlank()){
                        addViewModel.uploadStory(desc, null, null)
                    } else if (switchAutoLocation.isChecked) {
                        uploadStoryWithAutoLoc(fusedLocationProviderClient, desc)
                    }else{
                        val address = Helper.convertToPosition(edAddLocation.text.toString(), geocoder)
                        if (address != null){
                            addViewModel.uploadStory(desc, address.latitude.toFloat(), address.longitude.toFloat())
                        }else{
                            addViewModel.uploadStory(desc, null, null)
                        }

                    }
                }
            }
        }

    }

    private fun checkResult(result: Result<Event<String>>) {
        binding.apply {
            when (result) {
                is Result.Loading -> {
                    btnAddStory?.isEnabled = false
                    btnAddCamera.isEnabled = false
                    btnAddGallery.isEnabled = false
                    switchAutoLocation.isEnabled = false
                    pbAdd.visibility = View.VISIBLE
                }
                is Result.Success -> {
                    pbAdd.visibility = View.GONE
                    val messageEvent = result.data
                    showToast(messageEvent)
                    setResult(ListActivity.ADD_ACTIVITY_RESULT)
                    finish()
                }
                is Result.Error -> {
                    pbAdd.visibility = View.GONE
                    btnAddStory?.isEnabled = true
                    btnAddCamera.isEnabled = true
                    btnAddGallery.isEnabled = true
                    switchAutoLocation.isEnabled = true
                    val messageEvent = result.error
                    showToast(messageEvent)
                }

            }
        }


    }

    private fun ImageView.loadImageFile(imageFile: File) {
        val image = BitmapFactory.decodeFile(imageFile.path)
        Glide.with(this@AddActivity)
            .load(image)
            .into(this)
    }

    private fun obtainViewModel(
        activity: AppCompatActivity,
        dataStore: DataStore<Preferences>
    ): AddViewModel {
        val factory = ViewModelFactory.getInstance(dataStore)
        return ViewModelProvider(activity, factory)[AddViewModel::class.java]
    }

    private fun showToast(messageEvent: Event<String>) {
        val message = messageEvent.getContentIfNotHandled()
        if (message != null) {
            Toast.makeText(this@AddActivity, message, Toast.LENGTH_SHORT).show()
        }
    }


    private fun uploadStoryWithAutoLoc(fusedLocationProviderClient: FusedLocationProviderClient, desc:String){
        if (
            checkPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) &&
            checkPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
        ){
            fusedLocationProviderClient.lastLocation.apply {
                addOnSuccessListener {location ->
                    if (location!=null) {
                        addViewModel.uploadStory(desc, location.latitude.toFloat(), location.longitude.toFloat())
                    }else{
                        addViewModel.uploadStory(desc, null, null)
                    }
                }
                addOnFailureListener {exception ->
                    exception.printStackTrace()
                    addViewModel.uploadStory(desc, null, null)
                }

            }
        }else{
            requestPermissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }

    }

    private fun checkPermission(permission:String):Boolean{
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkSwitch(isChecked:Boolean){
        if (isChecked){
            Toast.makeText(this, "Auto detect location used", Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(this, "Auto detect location is not used", Toast.LENGTH_SHORT).show()
        }
    }
}