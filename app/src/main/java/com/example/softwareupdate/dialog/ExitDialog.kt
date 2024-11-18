package com.example.softwareupdate.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.WindowManager
import com.example.softwareupdate.databinding.ExitDialogBinding

class ExitDialog(
    private  val activity: Activity
) : Dialog(activity) {
    private val inflater = activity.getSystemService(
        Context.LAYOUT_INFLATER_SERVICE
    ) as LayoutInflater
    private val binding = ExitDialogBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
       // this.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT)

        val marginHorizontal = 40 // Set the desired margin in pixels
        val displayMetrics = DisplayMetrics()
        window?.windowManager?.defaultDisplay?.getMetrics(displayMetrics)
        val dialogWidth = displayMetrics.widthPixels - (2 * marginHorizontal)

        this.window?.setLayout(dialogWidth, WindowManager.LayoutParams.WRAP_CONTENT)
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        binding.apply {
            btnExit.setOnClickListener {
                activity.finishAffinity()
            }
            btnCancel.setOnClickListener {
                dismiss()
            }
        }
    }
}