package com.example.softwareupdate.adapters.privacymanager

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareupdate.databinding.ItemPrivacyManagerAppsLayoutBinding
import com.example.softwareupdate.utils.getPackageInfoCompat

class PrivacyManagerItemsAdapter(
    private val callback: (PrivacyManagerEntity) -> Unit,
    private val callbackPercentage: (Int) -> Unit,
    private var dataList: List<PrivacyManagerEntity?>,
    private val context: Context?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var originalDataListSize: Int = 0

    @SuppressLint("NotifyDataSetChanged")
    fun updateVersionsList(mProducts: List<PrivacyManagerEntity?>, spinnerPosition: Int) {
        originalDataListSize = mProducts.size
        dataList = when (spinnerPosition) {
            0 -> mProducts.filter {
                (it?.protectionValue ?: 0) > 21
            }

            1 -> mProducts.filter {
                (it?.protectionValue ?: 0) >= 12 && (it?.protectionValue ?: 0) <= 21
            }
           

            else -> mProducts.filter {
                val protectionValue = it?.protectionValue ?: 0
                val isSystemApp = isSystemApp(it?.pName)
                if (!isSystemApp && protectionValue < 12) {
                    true
                } else {
                    isUpdatedSystemApp(it?.pName, protectionValue)
                }
            }
        }.sortedBy { it?.appName?.lowercase() }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemPrivacyManagerAppsLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VersionsItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerHolder = holder as VersionsItemsViewHolder
        dataList[position]?.let { headerHolder.bindData(it) }
    }

    override fun getItemCount(): Int = dataList.size
    inner class VersionsItemsViewHolder(private val binding: ItemPrivacyManagerAppsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: PrivacyManagerEntity) {
            binding.allAppsEntity = item
            binding.executePendingBindings()
            binding.tvVersion.isSelected = true
            Log.wtf(
                "ProtectionValue--->", "bindData:${item.appName}----> ${item.protectionValue}"
            )
            calculatePercentage {
                callbackPercentage.invoke(it.toInt())
            }
            itemView.setOnClickListener {
                callback.invoke(item)
            }
        }
    }

    fun calculatePercentage(callback: (Double) -> Unit) {
        val percentage = (dataList.size.toDouble() / originalDataListSize) * 100
        callback(percentage)
    }

    private fun isSystemApp(packName: String?): Boolean {
        val packageInfo = packName?.let { it1 ->
            context?.packageManager?.getPackageInfoCompat(
                it1, PackageManager.GET_PERMISSIONS
            )
        }
        return (packageInfo?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM)) != 0
    }

    private fun isUpdatedSystemApp(packName: String?, protectionValue: Int): Boolean {
        val packageInfo = packName?.let { it1 ->
            context?.packageManager?.getPackageInfoCompat(
                it1, PackageManager.GET_PERMISSIONS
            )
        }
        val isSystemApp =
            (packageInfo?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_SYSTEM)) != 0
        if (!isSystemApp && protectionValue < 12) {
            return true
        } else if (isSystemApp) {
            return (packageInfo?.applicationInfo?.flags?.and(ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)
                ?: false) != 0
        }
        return false
    }

}
