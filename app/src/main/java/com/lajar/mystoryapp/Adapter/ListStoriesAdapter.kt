package com.lajar.mystoryapp.Adapter


import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import androidx.core.util.Pair
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.app.ActivityOptionsCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lajar.mystoryapp.DetailActivity
import com.lajar.mystoryapp.R
import com.lajar.mystoryapp.data.local.entity.Story
import com.lajar.mystoryapp.databinding.ItemStoriesBinding

class ListStoriesAdapter(
    private val onItemGetClicked: OnItemGetClicked
) : PagingDataAdapter<Story, ListStoriesAdapter.ListStoriesViewHolder>(DIFF_CALLBACK) {

    var isAnItemHasBeenClicked = false

    inner class ListStoriesViewHolder(
        val binding: ItemStoriesBinding,
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
                Pair(binding.root, DetailActivity.SHARED_ELEMENT_1),
                Pair(binding.ivItemPhoto, DetailActivity.SHARED_ELEMENT_2),
                Pair(binding.tvItemName, DetailActivity.SHARED_ELEMENT_3)
            )
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListStoriesViewHolder {
        val binding = ItemStoriesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListStoriesViewHolder(binding) { itemAdapterPosition, sharedElementTransition ->
            onItemGetClicked.onClick(getItem(itemAdapterPosition), sharedElementTransition)
        }
    }


    override fun onBindViewHolder(holder: ListStoriesViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (currentItem != null) {
            holder.binding.apply {
                ivItemPhoto.loadPhoto(currentItem.photoUrl, holder.itemView.context)
                tvItemName.text = currentItem.name
            }
        }

    }

    private fun ImageView.loadPhoto(photo: String, context: Context) {
        Glide.with(context)
            .load(photo)
            .placeholder(R.drawable.ic_image_loading)
            .error(R.drawable.image_error)
            .into(this)
    }

    interface OnItemGetClicked {
        fun onClick(story: Story?, sharedElementTransition: ActivityOptionsCompat)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Story>() {
            override fun areItemsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem == newItem
            }

            override fun areContentsTheSame(oldItem: Story, newItem: Story): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }


}
