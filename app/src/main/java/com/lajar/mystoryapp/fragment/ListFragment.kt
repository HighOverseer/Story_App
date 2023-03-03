package com.lajar.mystoryapp.fragment

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.lajar.mystoryapp.Adapter.ListStoriesAdapter
import com.lajar.mystoryapp.Adapter.LoadingStateAdapter
import com.lajar.mystoryapp.DetailActivity
import com.lajar.mystoryapp.Helper.ConnectivityObserver
import com.lajar.mystoryapp.ListActivity
import com.lajar.mystoryapp.R
import com.lajar.mystoryapp.data.local.entity.Story
import com.lajar.mystoryapp.ViewModel.ListViewModel
import com.lajar.mystoryapp.databinding.FragmentListBinding

class ListFragment : Fragment() {
    private var _binding: FragmentListBinding? = null
    private val binding get() = _binding!!
    private val listViewModel by activityViewModels<ListViewModel>()
    private val adapter = getListStoriesAdapter()

    companion object{
        private const val NUM_COLUMN = 2
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        listViewModel.setWhetherConnectionEverUnavailable(!isNetworkAvailable())
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setLayout()

    }

    override fun onResume() {
        super.onResume()
        adapter.isAnItemHasBeenClicked = false
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private fun setLayout() {
        val loadingStateAdapter = LoadingStateAdapter { adapter.retry() }
        val gridLayoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
        binding.rvListStory.layoutManager = gridLayoutManager
        binding.rvListStory.setHasFixedSize(true)
        binding.rvListStory.adapter = adapter.withLoadStateFooter(
            footer = loadingStateAdapter
        )
        listViewModel.listStoriesWithPagination.observe(viewLifecycleOwner) { pagingData ->
            adapter.submitData(lifecycle, pagingData)
        }

        listViewModel.connectivityStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                ConnectivityObserver.Status.Available -> {
                    if (listViewModel.isConnectionEverUnavailable == true) {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.internet_detected_info),
                            Toast.LENGTH_SHORT
                        ).show()
                        listViewModel.setWhetherConnectionEverUnavailable(false)
                        val intent = Intent(requireActivity(), ListActivity::class.java)
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
                ConnectivityObserver.Status.Lost -> {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.connection_lost_info),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {}
            }
        }
    }

    private fun getListStoriesAdapter(): ListStoriesAdapter {
        return ListStoriesAdapter(object : ListStoriesAdapter.OnItemGetClicked {
            override fun onClick(story: Story?, sharedElementTransition: ActivityOptionsCompat) {
                if (story != null) {
                    toDetailAct(story, sharedElementTransition)
                } else Toast.makeText(requireContext(), "Data is Null", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toDetailAct(story: Story, sharedElementTransition: ActivityOptionsCompat) {
        val intent = Intent(requireActivity(), DetailActivity::class.java)
        intent.putExtra(DetailActivity.EXTRA_STORY, story)
        requireActivity().startActivity(intent, sharedElementTransition.toBundle())
    }


    private fun isNetworkAvailable(): Boolean {
        val result: Boolean
        val connectivityManager =
            requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
        result = when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
        return result

    }

}