package com.example.softwareupdate.adapters.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareupdate.databinding.HomeItemsShapeBinding
import com.example.softwareupdate.utils.ActionType

class HomeScreensItemsAdapter(
    private val callback: (ActionType) -> Unit,
    private val dataList: List<HomeItemsModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = HomeItemsShapeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HomeItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerHolder = holder as HomeItemsViewHolder
        headerHolder.bindData(dataList[position])
    }

    override fun getItemCount(): Int = dataList.size
    inner class HomeItemsViewHolder(private val binding: HomeItemsShapeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: HomeItemsModel) {
            binding.homeModel = item
            itemView.setOnClickListener {
                when (adapterPosition) {
                    0 -> {
                        callback.invoke(ActionType.ACTION_APP_PRIVACY)
                    }

                    1 -> {
                        callback.invoke(ActionType.ACTION_APP_USAGE)
                    }

                    2 -> {
                        callback.invoke(ActionType.ACTION_APP_UNINSTALL)
                    }

                    3 -> {
                        callback.invoke(ActionType.ACTION_APP_INSTALL)
                    }
                }

            }
        }
    }
}

