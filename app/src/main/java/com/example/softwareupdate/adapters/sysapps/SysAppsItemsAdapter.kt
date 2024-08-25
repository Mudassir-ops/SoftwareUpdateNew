package com.example.softwareupdate.adapters.sysapps

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareupdate.databinding.ItemSysAppsLayoutBinding

class SysAppsItemsAdapter(
    private val callback: (MySysAppsEntity) -> Unit,
    private var dataList: List<MySysAppsEntity?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    @SuppressLint("NotifyDataSetChanged")
    fun updateVersionsList(mProducts: List<MySysAppsEntity?>) {
        dataList = mProducts
        Log.e("mProducts", "updateVersionsList:$mProducts")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemSysAppsLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VersionsItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerHolder = holder as VersionsItemsViewHolder
        dataList[position]?.let { headerHolder.bindData(it) }
    }

    override fun getItemCount(): Int = dataList.size
    inner class VersionsItemsViewHolder(private val binding: ItemSysAppsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: MySysAppsEntity) {
            binding.sysEntity = item
            binding.executePendingBindings()
            binding.tvVersion.isSelected = true
            itemView.setOnClickListener {
                callback.invoke(item)
            }
        }
    }
}
