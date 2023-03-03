package com.lajar.mystoryapp.fragment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.Resources
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.lajar.mystoryapp.Adapter.ListStoriesAdapter
import com.lajar.mystoryapp.Adapter.ListStoriesMapAdapter
import com.lajar.mystoryapp.DetailActivity
import com.lajar.mystoryapp.Helper.Helper
import com.lajar.mystoryapp.ListActivity
import com.lajar.mystoryapp.data.local.entity.Story
import com.lajar.mystoryapp.R
import com.lajar.mystoryapp.ViewModel.ListViewModel
import com.lajar.mystoryapp.data.Event
import kotlinx.coroutines.launch
import com.lajar.mystoryapp.databinding.FragmentMapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class MapFragment : SupportMapFragment(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private val listViewModel by activityViewModels<ListViewModel>()
    private lateinit var geocoder: Geocoder
    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private val adapter = getListStoriesMapAdapter()
    private var bounds: LatLngBounds? = null
    private val markers = mutableListOf<Marker?>()
    /*private lateinit var bottomSheetBehavior: BottomSheetBehavior<LinearLayout>*/


    companion object{
        private const val MATCHED_POSITION_MULTIPLIER = 0.0008
        const val EVENT_ADD_STORIES_TO_MAP = "added"
        private const val LL_HEIGHT = 220f
        private const val ANIMATION_DURATION = 250L
        private const val SCALE_START = 0.5f
        private const val SCALE_END = 1f
        private const val MAP_ZOOM_CONTROL_ID = 0x1
        private const val MAP_ZOOM_CONTROL_MARGIN = 10f
        private const val BOUNDS_VIEW_PADDING = 250
        private const val CAMERA_ZOOM_SCALE = 15f
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapView = super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        binding.frameLayout.addView(mapView, 0)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.getMapAsync(this)
        binding.rvMapStory.adapter = adapter
        geocoder = Geocoder(requireContext(), Locale.getDefault())
        /*bottomSheetBehavior = BottomSheetBehavior.from(binding.llCurrentUserStories)*/
    }

    override fun onResume() {
        super.onResume()
        adapter.isAnItemHasBeenClicked = false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listViewModel.setIsAllMarkerReady(false)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        this.setZoomButtonPosition()
        setMapStyle()
        setLayout()

    }

    private fun setLayout() {
        mMap.uiSettings.isZoomControlsEnabled = true
        listViewModel.listStoriesForMap.observe(viewLifecycleOwner) { stories ->
            settingUpAllMarkers(stories)
        }

        listViewModel.selectedUserStoriesServedWhenReady.observe(viewLifecycleOwner) { selectedUserStories ->
            showSelectedUserStoriesWhenReady(selectedUserStories)
        }

        listViewModel.singleEventMessage.observe(viewLifecycleOwner) { singleEventMessage ->
            showToastWhenStoryAddedToMap(singleEventMessage)
        }

        binding.llCurrentUserStories.setOnClickListener {
            binding.llCurrentUserStories.visibility = View.GONE
            setBnvVisibility(true)

        }


    }

    private fun showSelectedUserStoriesWhenReady(selectedUserStories: List<Story>?) {
        if (selectedUserStories != null) {
            if (selectedUserStories.isNotEmpty()) {
                showSelectedUserStories(selectedUserStories)
            } else {
/*              bottomSheetBehavior.peekHeight = 0
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN*/
                binding.llCurrentUserStories.visibility = View.GONE
                /*Log.d("showHidden", bottomSheetBehavior.state.toString())*/
                setBnvVisibility(true)
                if (bounds != null) {
                    viewAllMarker(bounds)
                }
            }
        }
    }

    private fun settingUpAllMarkers(stories: List<Story>) {
        listViewModel.setIsAllMarkerReady(false)
        mMap.clear()
        addManyMarker(stories)
    }

    private fun addManyMarker(stories: List<Story>) {
        viewLifecycleOwner.lifecycleScope.launch {
            val boundsBuilder = LatLngBounds.Builder()
            var isAtleastAStoryIncludedOnMap = false
            stories.forEach { story ->
                isAtleastAStoryIncludedOnMap =
                    if (addMarkerForStoryWithLatLng(
                            story,
                            geocoder,
                            boundsBuilder
                        ) && !isAtleastAStoryIncludedOnMap
                    ) {
                        true
                    } else isAtleastAStoryIncludedOnMap
            }
            if (isAtleastAStoryIncludedOnMap) {
                mMap.setOnMarkerClickListener { marker ->
                    listViewModel.updateSelectedUserStories(stories, marker)
                    marker.showInfoWindow()
                    true
                }
                mMap.setOnInfoWindowCloseListener {
                    listViewModel.updateSelectedUserStories()
                }
                bounds = boundsBuilder.build()
            }
            listViewModel.setIsAllMarkerReady(true)
        }
    }

    private fun viewAllMarker(bounds: LatLngBounds?) {
        if (bounds != null) {
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    bounds,
                    resources.displayMetrics.widthPixels,
                    resources.displayMetrics.heightPixels,
                    BOUNDS_VIEW_PADDING
                )
            )
        }
    }


    private fun showSelectedUserStories(selectedUserStories: List<Story>) {
        binding.apply {
            val markerLat = selectedUserStories[0].lat?.toDouble() ?: return
            val markerLon = selectedUserStories[0].lon?.toDouble() ?: return
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(markerLat, markerLon), CAMERA_ZOOM_SCALE))
            tvMapName.text = getString(R.string.users_stories, selectedUserStories[0].name)
            binding.rvMapStory.layoutManager = LinearLayoutManager(requireContext())
            adapter.updateList(selectedUserStories)
            setBnvVisibility(false)

            /*bottomSheetBehavior.peekHeight = 120f.toDp().toInt()
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED*/
            llCurrentUserStories.visibility = View.VISIBLE
            /*Log.d("show", bottomSheetBehavior.state.toString())*/

            /*playExtendLlAnimation()*/

        }

    }


    private suspend fun addMarkerForStoryWithLatLng(
        story: Story,
        geocoder: Geocoder,
        boundsBuilder: LatLngBounds.Builder
    ): Boolean {
        if (story.lat != null && story.lon != null) {
            val diffIfAnyPositionMatched =
                checkMatchedPositionWithOtherUser(story) * MATCHED_POSITION_MULTIPLIER
            val latLng = LatLng(
                story.lat.toDouble()+diffIfAnyPositionMatched,
                story.lon.toDouble()+diffIfAnyPositionMatched
            )
            val addressName = Helper.convertToAddressLine(
                story.lat,
                story.lon,
                geocoder,
                getString(R.string.no_location_found)
            )
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(story.name)
                    .snippet(addressName)
            )
            markers.add(marker)
            val isStoryAMarkerWithInfoWindow =
                listViewModel.checkIfStoryShouldShownInfoWindow(story)
            if (isStoryAMarkerWithInfoWindow) {
                marker?.showInfoWindow()
            }
            boundsBuilder.include(latLng)
            return true
        }
        return false
    }

    private fun setMapStyle() {
        try {
            val success = mMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    requireContext(),
                    R.raw.map_style
                )
            )
            if (!success) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.style_parsing_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        } catch (exception: Resources.NotFoundException) {
            exception.printStackTrace()
        }
    }

    private fun getListStoriesMapAdapter(): ListStoriesMapAdapter {
        return ListStoriesMapAdapter(listOf(), object : ListStoriesAdapter.OnItemGetClicked {
            override fun onClick(story: Story?, sharedElementTransition: ActivityOptionsCompat) {
                if (story != null) {
                    toDetailAct(story, sharedElementTransition)
                } else Toast.makeText(
                    requireContext(),
                    getString(R.string.data_is_null),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }


    private fun toDetailAct(story: Story, sharedElementTransition: ActivityOptionsCompat) {
        val intent = Intent(requireActivity(), DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_STORY, story)
        requireActivity().startActivity(intent, sharedElementTransition.toBundle())
    }

    private fun showToastWhenStoryAddedToMap(messageEvent: Event<String>) {
        val message = messageEvent.getContentIfNotHandled()
        if (message != null) {
            Toast.makeText(
                requireContext(),
                getString(R.string.stories_updated_to_map),
                Toast.LENGTH_SHORT
            ).show()
        }
    }
    private suspend fun checkMatchedPositionWithOtherUser(story: Story):Int = withContext(Dispatchers.Main){
        var matchedPosition = 0
        markers.forEach{ marker ->
            if (marker!=null && marker.title == story.name && story.lat != null && story.lon != null){
                val diff = (marker.position.latitude - story.lat.toDouble())/ MATCHED_POSITION_MULTIPLIER
                val check = diff%1
                if (check == 0.0 || (check >= 0.99999999 && check < 1.0)){
                    return@withContext diff.toInt()
                }
            }
        }
        markers.forEach {marker ->
            if (marker!=null){
                if (
                    story.name != marker.title
                    && story.lat == marker.position.latitude.toFloat()
                    && story.lon == marker.position.longitude.toFloat()
                ){
                    matchedPosition++
                }
            }

        }
        matchedPosition
    }

    @SuppressLint("ResourceType")
    private fun SupportMapFragment.setZoomButtonPosition(){
        val zoomControl = this.view?.findViewById<View>(MAP_ZOOM_CONTROL_ID)
        if (zoomControl!=null && zoomControl.layoutParams is RelativeLayout.LayoutParams){
            val params =  zoomControl.layoutParams as RelativeLayout.LayoutParams
            params.addRule(RelativeLayout.ALIGN_PARENT_TOP)
            params.addRule(RelativeLayout.ALIGN_PARENT_END)

            val margin = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, MAP_ZOOM_CONTROL_MARGIN, requireActivity().resources.displayMetrics)
            params.setMargins(margin.toInt(), margin.toInt(), 0, 0)

        }
    }

    private fun playExtendLlAnimation(){
        binding.apply {
            llCurrentUserStories.pivotX = (llCurrentUserStories.width/2).toFloat()
            llCurrentUserStories.pivotY = LL_HEIGHT.toDp()
            ObjectAnimator.ofFloat(llCurrentUserStories, View.SCALE_Y, SCALE_START, SCALE_END).setDuration(
                ANIMATION_DURATION).start()
        }
    }

    private fun Float.toDp():Float{
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, resources.displayMetrics)
    }


    private fun setBnvVisibility(isVisible:Boolean){
        val parentActivity = requireActivity()
        if (parentActivity is ListActivity){
            parentActivity.setBottomNavigationViewVisibility(isVisible)
        }
    }

}