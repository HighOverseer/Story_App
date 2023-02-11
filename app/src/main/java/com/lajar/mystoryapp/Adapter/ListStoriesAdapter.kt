package com.lajar.mystoryapp.Adapter


import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import androidx.core.util.Pair
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityOptionsCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.lajar.mystoryapp.Model.Story
import com.lajar.mystoryapp.databinding.ItemStoryBinding

class ListStoriesAdapter(
    private var stories: List<Story>,
    private val onItemGetClicked: OnItemGetClicked
) : RecyclerView.Adapter<ListStoriesAdapter.ListStoriesViewHolder>() {

    var isAnItemHasBeenClicked = false

    inner class ListStoriesViewHolder(
        val binding: ItemStoryBinding,
        clickedAtPosition: (Int, ActivityOptionsCompat) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                if (!isAnItemHasBeenClicked) {
                    isAnItemHasBeenClicked = true
                    clickedAtPosition(adapterPosition, getSharedElementTransition())
                }
            }
        }

        private fun getSharedElementTransition(): ActivityOptionsCompat {
            return ActivityOptionsCompat.makeSceneTransitionAnimation(
                itemView.context as Activity,
                Pair(binding.ivItemPhoto, SHARED_ELEMENT_1),
                Pair(binding.tvItemName, SHARED_ELEMENT_2)
            )
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListStoriesViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListStoriesViewHolder(binding) { itemAdapterPosition, sharedElementTransition ->
            onItemGetClicked.onClick(stories[itemAdapterPosition], sharedElementTransition)
        }
    }

    override fun onBindViewHolder(holder: ListStoriesViewHolder, position: Int) {
        val currentItem = stories[position]
        holder.binding.apply {
            ivItemPhoto.loadPhoto(currentItem.photoUrl, holder.itemView.context)
            tvItemName.text = currentItem.name
        }
    }

    override fun getItemCount() = stories.size

    private fun ImageView.loadPhoto(photo: String, context: Context) {
        Glide.with(context)
            .load(photo)
            .into(this)
    }

    fun updateList(stories: List<Story>) {
        this.stories = stories
    }


    interface OnItemGetClicked {
        fun onClick(story: Story, sharedElementTransition: ActivityOptionsCompat)
    }

    companion object {
        private const val SHARED_ELEMENT_1 = "image"
        private const val SHARED_ELEMENT_2 = "name"
    }
}
