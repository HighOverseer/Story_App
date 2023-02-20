package com.lajar.mystoryapp.fragment

import android.content.Intent
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.lajar.mystoryapp.Adapter.ListStoriesAdapter
import com.lajar.mystoryapp.Adapter.ListStoriesMapAdapter
import com.lajar.mystoryapp.DetailActivity
import com.lajar.mystoryapp.Helper.Helper
import com.lajar.mystoryapp.ListActivity
import com.lajar.mystoryapp.Model.Story
import com.lajar.mystoryapp.R
import com.lajar.mystoryapp.ViewModel.ListViewModel
import kotlinx.coroutines.launch
import com.lajar.mystoryapp.data.Result
import com.lajar.mystoryapp.databinding.FragmentMapBinding
import java.util.*

class MapFragment:SupportMapFragment(), OnMapReadyCallback{
    private lateinit var mMap:GoogleMap
    private val listViewModel by activityViewModels<ListViewModel>()
    private lateinit var geocoder: Geocoder
    private var _binding:FragmentMapBinding?=null
    private val binding get() = _binding!!
    private val adapter = getListStoriesMapAdapter()
    private var bounds: LatLngBounds?=null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapView = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        binding.root.addView(mapView, 0)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.getMapAsync(this)
        geocoder = Geocoder(requireContext(), Locale.getDefault())
    }

    override fun onResume() {
        super.onResume()
        adapter.isAnItemHasBeenClicked = false
        setUserInputEnabledViewPager()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        setMapStyle()
        mMap.uiSettings.isZoomControlsEnabled = true


        listViewModel.listStories.observe(viewLifecycleOwner){ result ->
            if (result is Result.Success){
                mMap.clear()
                val stories = result.data
                listViewModel.setIsAllMarkerReady(false)
                addManyMarker(stories)
            }
        }

        listViewModel.isAllMarkerReady.observe(viewLifecycleOwner){ isAllMarkerReady ->
            if (isAllMarkerReady){
                val selectedUserStories = listViewModel.selectedUserStories.value
                if (selectedUserStories != null){
                    showSelectedUserStories(selectedUserStories)
                }else{
                    binding.llCurrentUserStories.visibility = View.GONE
                    if (bounds!=null){
                        viewAllMarker(bounds)
                    }
                }
            }
        }


        binding.llCurrentUserStories.setOnClickListener {
            binding.llCurrentUserStories.visibility = View.GONE
        }

    }
    private fun addManyMarker(stories:List<Story>){
        viewLifecycleOwner.lifecycleScope.launch {
            val boundsBuilder = LatLngBounds.Builder()
            var isAtleastAStoryIncludedOnMap = false
            stories.forEach {story ->
                isAtleastAStoryIncludedOnMap =
                    if (addMarkerForStoryWithLatLng(story, geocoder, boundsBuilder) && !isAtleastAStoryIncludedOnMap){
                        true
                    }else isAtleastAStoryIncludedOnMap
            }
            if (isAtleastAStoryIncludedOnMap){
                mMap.setOnMarkerClickListener { marker ->
                    listViewModel.updateSelectedUserStories(stories, marker)
                    marker.showInfoWindow()
                    true
                }
                mMap.setOnInfoWindowCloseListener {
                    listViewModel.updateSelectedUserStories(null)
                    // binding.llCurrentUserStories.visibility = View.GONE
                }
                bounds = boundsBuilder.build()
            }
            listViewModel.setIsAllMarkerReady(true)
        }
    }

    private fun viewAllMarker(bounds: LatLngBounds?) {
        if (bounds!=null){
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                bounds,
                resources.displayMetrics.widthPixels,
                resources.displayMetrics.heightPixels,
                250
                )
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listViewModel.setIsAllMarkerReady(false)
    }

    private fun showSelectedUserStories(selectedUserStories: List<Story>) {
        binding.apply {
            val markerLat = selectedUserStories[0].lat?.toDouble() ?: return
            val markerLon = selectedUserStories[0].lon?.toDouble() ?: return
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(markerLat, markerLon), 15f))
            tvMapName.text = getString(R.string.users_stories, selectedUserStories[0].name)
            adapter.updateList(selectedUserStories)
            rvMapStory.adapter = adapter
            rvMapStory.layoutManager = LinearLayoutManager(requireContext())
            llCurrentUserStories.visibility = View.VISIBLE
        }

    }

    private suspend fun addMarkerForStoryWithLatLng(story: Story, geocoder: Geocoder, boundsBuilder: LatLngBounds.Builder):Boolean {
        if (story.lat!=null && story.lon!=null){
            val latLng = LatLng(story.lat.toDouble(), story.lon.toDouble())
            val addressName = Helper.convertToAddressLine(story.lat, story.lon, geocoder, getString(R.string.no_location_found))

            val marker = mMap.addMarker(MarkerOptions()
                .position(latLng)
                .title(story.name)
                .snippet(addressName)
            )
            val isStoryAMarkerWithInfoWindow = listViewModel.checkIfStoryShouldShownInfoWindow(story)
            if (isStoryAMarkerWithInfoWindow) {
                marker?.showInfoWindow()
            }
            boundsBuilder.include(latLng)
            return true
        }
        return false
    }

    private fun setMapStyle(){
        try {
            val success = mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style))
            if (!success){
                Toast.makeText(requireContext(), getString(R.string.style_parsing_failed), Toast.LENGTH_SHORT).show()
            }
        }catch (exception: Resources.NotFoundException){
            exception.printStackTrace()
        }
    }

    private fun getListStoriesMapAdapter(): ListStoriesMapAdapter {
        return ListStoriesMapAdapter(listOf(), object: ListStoriesAdapter.OnItemGetClicked{
            override fun onClick(story: Story, sharedElementTransition: ActivityOptionsCompat) {
                toDetailAct(story, sharedElementTransition)
            }
        })
    }

    private fun toDetailAct(story: Story, sharedElementTransition:ActivityOptionsCompat){
        val intent = Intent(requireActivity(), DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_STORY, story)
        requireActivity().startActivity(intent, sharedElementTransition.toBundle())
    }

    private fun setUserInputEnabledViewPager() {
        val activityOwner = requireActivity()
        if (activityOwner is ListActivity){
            activityOwner.setUserInputEnabledViewPager(false)
        }
    }

}