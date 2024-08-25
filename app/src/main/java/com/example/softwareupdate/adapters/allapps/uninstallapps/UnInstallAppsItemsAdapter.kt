package com.example.softwareupdate.adapters.allapps.uninstallapps

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.databinding.ItemUninstallAppsLayoutBinding

class UnInstallAppsItemsAdapter(
    private val callbackSelection: (List<AllAppsEntity?>) -> Unit,
    var dataList: ArrayList<AllAppsEntity?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    @SuppressLint("NotifyDataSetChanged")
    fun updateVersionsList(mProducts: ArrayList<AllAppsEntity?>) {
        dataList = mProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemUninstallAppsLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VersionsItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerHolder = holder as VersionsItemsViewHolder
        dataList[position]?.let { headerHolder.bindData(it) }
    }

    override fun getItemCount(): Int = dataList.size
    inner class VersionsItemsViewHolder(private val binding: ItemUninstallAppsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: AllAppsEntity) {
            binding.allEntity = item
            binding.tvVersion.isSelected = true
            itemView.setOnClickListener {
                item.isSelected = !item.isSelected
                notifyItemChanged(adapterPosition)
                callbackSelection.invoke(dataList)
            }
        }
    }

    fun selectAllItems(selectAll: Boolean) {
        dataList.forEachIndexed { index, item ->
            if (item?.isSelected != selectAll) {
                item?.isSelected = selectAll
                notifyItemChanged(index)
            }
        }
    }
}
