package com.example.softwareupdate.dialog

import android.app.Activity
import android.app.AppOpsManager
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import com.example.softwareupdate.databinding.AppUsageAccessDialogBinding

class AppUsageAccess(
    activity: Activity,
    val callback: (Boolean) -> Unit
) : Dialog(activity) {
    private val inflater = activity.getSystemService(
        Context.LAYOUT_INFLATER_SERVICE
    ) as LayoutInflater
    private val binding = AppUsageAccessDialogBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(this.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        layoutParams.gravity = Gravity.CENTER
        this.window?.attributes = layoutParams

        setCancelable(true)
        setCanceledOnTouchOutside(true)
        binding.apply {
            btnDeny.setOnClickListener { dismiss() }
            btnAllow.setOnClickListener {
                if (isUsageAccessGranted()) {
                    callback.invoke(true)  // Permission already granted
                } else {
                    // Guide user to enable the permission
                    context.startActivity(
                        Intent(
                        Settings.ACTION_USAGE_ACCESS_SETTINGS
                    )
                    )
                }
                dismiss()
            }
        }
    }
    private fun isUsageAccessGranted(): Boolean {
        val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val applicationInfo = context.applicationInfo
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            appOpsManager.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                applicationInfo?.uid ?: return false,
                context.packageName ?: return false
            )
        } else {
            AppOpsManager.MODE_IGNORED
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }
}