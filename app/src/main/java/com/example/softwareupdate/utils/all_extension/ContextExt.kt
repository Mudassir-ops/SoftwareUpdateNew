package com.example.softwareupdate.utils.all_extension

import androidx.appcompat.app.AlertDialog
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.app.AppOpsManager

import android.content.Intent
import android.net.Uri
import android.os.SystemClock
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.softwareupdate.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.app.usage.StorageStats
import android.app.usage.StorageStatsManager
import android.content.pm.PackageManager
import android.os.Build
import android.os.UserHandle
import android.util.Log
import java.text.DecimalFormat


fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}



fun Context.showGenericAlertDialog(message: String) {
    AlertDialog.Builder(this).apply {
        setMessage(message)
        setPositiveButton("Ok") { dialog, _ ->
            dialog.dismiss()
        }
    }.show()
}




private var toast: Toast? = null
fun Activity.toast(message: String) {
    try {
        if (this.isDestroyed || this.isFinishing) return
        if (toast != null) {
            toast?.cancel()
        }
        toast = Toast.makeText(this, message, Toast.LENGTH_SHORT)
        if (this.isDestroyed || this.isFinishing) return
        toast?.show()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

var mLastClickTime:Long = 0
fun  View.clickListener(action: (view:View)->Unit){
    this.setOnClickListener {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 1000){
            return@setOnClickListener
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        action(it)
    }
}

fun Activity.shareApp(){
    try {
        val sendIntent = Intent(Intent.ACTION_SEND)
        sendIntent.type = "text/plain"
        sendIntent.putExtra(
            Intent.EXTRA_SUBJECT,"ChargingAnimation")
        var shareMessage = "\n Let me recommend you this application\n\n"
        shareMessage = """
             ${shareMessage}https://play.google.com/store/apps/details?id= ${this.packageName}
        """.trimIndent()
        sendIntent.putExtra(Intent.EXTRA_TEXT,shareMessage)
        this.startActivity(Intent.createChooser(sendIntent, "Choose one"))
    }catch (e:java.lang.Exception){
        e.printStackTrace()
        this.toast("No Launcher")
    }
}

fun Activity.feedBackWithEmail(title:String,message:String,emailId:String){
    try {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.flags  = Intent.FLAG_ACTIVITY_CLEAR_TASK
        emailIntent.data  = Uri.parse("mailto:")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, title)
        emailIntent.putExtra(Intent.EXTRA_TEXT, message)
        this.startActivity(emailIntent)

    }catch (e:java.lang.Exception){
        e.printStackTrace()
    }
}

fun Activity.privacyPolicyUrl(){
    try {
        this.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(this.getString(R.string.privacy_policy_link))))
    }catch (e:Exception){
        e.printStackTrace()
        toast(this.getString(R.string.no_launcher))

    }
}

fun Activity.moreApps(){
    try {
        this.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(this.getString(R.string.more_app_link))))
    }catch (e:Exception){
        e.printStackTrace()
        toast(this.getString(R.string.no_launcher))

    }
}

fun Activity.rateUs(){
    try {
        this.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=${this.packageName}")
            )
        )

    }catch (e:Exception){
        e.printStackTrace()
        toast("No Launcher")
    }
}

fun currentDateAndTime(context: Context,textView: TextView) {
    val currentDate = Date()
    val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm,a", Locale.getDefault())
    val formattedDate = dateFormat.format(currentDate)
    val formattedTime = timeFormat.format(currentDate)
    val dateTimeString = "$formattedTime\n$formattedDate"
    textView.text = dateTimeString
}





