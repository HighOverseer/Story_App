package com.lajar.mystoryapp.Adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lajar.mystoryapp.DetailActivity
import com.lajar.mystoryapp.R
import com.lajar.mystoryapp.data.local.entity.Story
import com.lajar.mystoryapp.databinding.ItemCurrentUsersStoriesBinding
import de.hdodenhof.circleimageview.CircleImageView

class ListStoriesMapAdapter(
    private var stories: List<Story>,
    private val onItemGetClicked: ListStoriesAdapter.OnItemGetClicked
) : RecyclerView.Adapter<ListStoriesMapAdapter.ListStoriesMapViewHolder>() {

    var isAnItemHasBeenClicked = false

    inner class ListStoriesMapViewHolder(
        val binding: ItemCurrentUsersStoriesBinding,
        clickedAtPosition: (Int, ActivityOptionsCompat) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (!isAnItemHasBeenClicked) {
                    isAnItemHasBeenClicked = true
                    clickedAtPosition(absoluteAdapterPosition, getSharedElementTransition())
                }
            }
        }

        private fun getSharedElementTransition(): ActivityOptionsCompat {
            return ActivityOptionsCompat.makeSceneTransitionAnimation(
                itemView.context as Activity,
                Pair(binding.civMapItemStoryPhoto, DetailActivity.SHARED_ELEMENT_2),
                Pair(binding.tvMapItemStoryDesc, DetailActivity.SHARED_ELEMENT_4)
            )
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListStoriesMapViewHolder {
        val binding = ItemCurrentUsersStoriesBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListStoriesMapViewHolder(binding) { itemClickedPosition, sharedElementTransition ->
            onItemGetClicked.onClick(stories[itemClickedPosition], sharedElementTransition)
        }
    }

    override fun onBindViewHolder(holder: ListStoriesMapViewHolder, position: Int) {
        val currentItem = stories[position]
        holder.binding.apply {
            civMapItemStoryPhoto.loadPhoto(holder.itemView.context, currentItem.photoUrl)
            tvMapItemStoryDesc.text = currentItem.description
        }
    }

    private fun CircleImageView.loadPhoto(context: Context, photo: String) {
        Glide.with(context)
            .load(photo)
            .placeholder(R.drawable.ic_baseline_image_24)
            .error(R.drawable.image_error)
            .into(this)
    }

    override fun getItemCount() = stories.size

    fun updateList(stories: List<Story>) {
        this.stories = stories
    }
}