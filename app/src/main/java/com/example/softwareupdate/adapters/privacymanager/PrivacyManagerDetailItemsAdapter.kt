package com.example.softwareupdate.adapters.privacymanager

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareupdate.databinding.ItemPrivacyManagerDetailLayoutBinding

class PrivacyManagerDetailItemsAdapter(
    private val callback: (AppPermissions) -> Unit,
    private var dataList: List<AppPermissions?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    @SuppressLint("NotifyDataSetChanged")
    fun updateVersionsList(mProducts: List<AppPermissions?>) {
        dataList = mProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemPrivacyManagerDetailLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VersionsItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerHolder = holder as VersionsItemsViewHolder
        dataList[position]?.let { headerHolder.bindData(it) }
    }

    override fun getItemCount(): Int = dataList.size
    inner class VersionsItemsViewHolder(private val binding: ItemPrivacyManagerDetailLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: AppPermissions) {
            binding.appPermission = item
            binding.executePendingBindings()
            binding.tvVersion.isSelected = true
            itemView.setOnClickListener {
                callback.invoke(item)
            }
        }
    }
}
