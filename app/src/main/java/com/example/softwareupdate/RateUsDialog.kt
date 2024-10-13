package com.example.softwareupdate

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Toast
import com.example.softwareupdate.databinding.RateusDialogBinding

class RateUsDialog(
    activity: Activity
) : Dialog(activity) {
    private val inflater = activity.getSystemService(
        Context.LAYOUT_INFLATER_SERVICE
    ) as LayoutInflater
    private val binding = RateusDialogBinding.inflate(inflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        this.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT
        )
        setCancelable(true)
        setCanceledOnTouchOutside(true)
        binding.ratingBar.rating = 0f

        binding.apply {

            btnRateUs.setOnClickListener {
                val rating = ratingBar.rating.toInt()

                if (rating <= 3) {
                    // Show a toast message if rating is 3 or below
                    Toast.makeText(context, "Thank you for your feedback", Toast.LENGTH_SHORT).show()
                } else if (rating >= 4) {
                    // Redirect to Play Store if rating is 4 or 5
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}"))
                    context.startActivity(intent)
                }

                dismiss()
            }
            btnLater.setOnClickListener { dismiss()  }
        }
    }
}