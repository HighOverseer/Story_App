package com.lajar.mystoryapp.Adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lajar.mystoryapp.databinding.ItemLoadingBinding

class LoadingStateAdapter(private val retry: () -> Unit) :
    LoadStateAdapter<LoadingStateAdapter.LoadingStateViewHolder>() {

    class LoadingStateViewHolder(val binding: ItemLoadingBinding, retry: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        init {
            binding.btnRetryItemLoading.setOnClickListener { retry.invoke() }
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        loadState: LoadState
    ): LoadingStateViewHolder {
        val binding = ItemLoadingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadingStateViewHolder(binding, retry)
    }

    override fun onBindViewHolder(
        holder: LoadingStateViewHolder,
        loadState: LoadState
    ) {
        Log.d("LoadState", loadState.toString())
        holder.binding.apply {
            if (loadState is LoadState.Error) {
                Toast.makeText(
                    holder.itemView.context,
                    loadState.error.localizedMessage,
                    Toast.LENGTH_SHORT
                ).show()
            }
            pbItemLoading.isVisible = loadState is LoadState.Loading
            btnRetryItemLoading.isVisible = loadState is LoadState.Error
        }

    }
}