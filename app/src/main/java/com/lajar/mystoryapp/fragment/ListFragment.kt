package com.lajar.mystoryapp.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.lajar.mystoryapp.Adapter.ListStoriesAdapter
import com.lajar.mystoryapp.DetailActivity
import com.lajar.mystoryapp.ListActivity
import com.lajar.mystoryapp.data.Result
import com.lajar.mystoryapp.Model.Story
import com.lajar.mystoryapp.ViewModel.ListViewModel
import com.lajar.mystoryapp.databinding.FragmentListBinding

class ListFragment : Fragment() {
    private var _binding:FragmentListBinding?=null
    private val binding get() = _binding!!
    private val listViewModel by activityViewModels<ListViewModel>()
    private val adapter = getListStoriesAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listViewModel.listStories.observe(viewLifecycleOwner){ result ->
            checkResult(result)
        }
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

    private fun checkResult(result:Result<List<Story>>){
        binding.apply {
            when(result){
                is Result.Success ->{
                    val listStories = result.data
                    setLayout(listStories)
                }
                else -> return //Not Yet Implemented
            }
        }
    }

    private fun setLayout(listStories:List<Story>){
        binding.apply {
            adapter.updateList(listStories)
            rvListStory.adapter = adapter
            rvListStory.layoutManager = LinearLayoutManager(requireContext())
            rvListStory.setHasFixedSize(true)
        }
    }

    private fun getListStoriesAdapter():ListStoriesAdapter{
        return ListStoriesAdapter(listOf(), object:ListStoriesAdapter.OnItemGetClicked{
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
        if (activityOwner is ListActivity ){
            activityOwner.setUserInputEnabledViewPager(true)
        }
    }

}