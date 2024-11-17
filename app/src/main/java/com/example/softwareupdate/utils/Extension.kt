package com.example.softwareupdate.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.hardware.display.DisplayManager
import android.media.projection.MediaProjection
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import com.example.softwareupdate.R
import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.adapters.home.HomeItemsModel
import com.example.softwareupdate.adapters.privacymanager.PrivacyManagerEntity
import com.example.softwareupdate.databinding.FragmentHomeBinding
import com.example.softwareupdate.utils.AppConstants.TAG
import java.io.ByteArrayOutputStream
import java.io.File
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow


fun getVirtualDisplayFlags(): Int {
    return DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC
}

/** Extension to convert bitmap tp compress bytearray*/
fun Bitmap?.compressBitmapToByteArray(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    this?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    return outputStream.toByteArray()
}

/** Extension to stop media projection and close current activity*/
fun Activity.stopCapturingImages(mediaProjection: MediaProjection?) {
    mediaProjection?.stop()
    this.finishAffinity()
    this.finish()
}

fun String.printLog() {
    Log.d(TAG, this)
}

fun View.show() {
    visibility = View.VISIBLE
}


fun View.invisible() {
    visibility = View.INVISIBLE
}

fun Context.finishPreviousActivity() {
    val am = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val tasks = am.appTasks
    if (tasks.isNotEmpty()) {
        tasks[0].finishAndRemoveTask()
    }
}


fun Activity.permissionDialog(
    yesButtonText: String,
    noButtonText: String,
    messageText: String,
    isCancelable: Boolean = true,
    onYesButtonClick: () -> Unit,
    onNoButtonClick: () -> Unit
) {
    val dialogClickListener: DialogInterface.OnClickListener =
        DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    dialog.dismiss()
                    onYesButtonClick()
                }

                DialogInterface.BUTTON_NEGATIVE -> {
                    dialog.dismiss()
                    onNoButtonClick()
                }
            }
        }

    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    builder.setMessage(messageText).setPositiveButton(yesButtonText, dialogClickListener)
        .setNegativeButton(noButtonText, dialogClickListener).setCancelable(isCancelable).show()
}

@Suppress("DEPRECATION")
fun isServiceRunning(context: Context, serviceClass: Class<*>): Boolean {
    val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager?
    val runningServices = activityManager?.getRunningServices(Integer.MAX_VALUE)

    if (runningServices != null) {
        for (service in runningServices) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
    }
    return false
}


fun Context.shareImage(imagePath: String, shareTitle: String = "Share Image") {
    val imageFile = File(imagePath)
    if (!imageFile.exists()) {
        return
    }
    val imageUri = FileProvider.getUriForFile(
        this, "${this.packageName}.provider", imageFile
    )
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "image/*"
    shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri)
    shareIntent.putExtra(Intent.EXTRA_SUBJECT, shareTitle)
    shareIntent.putExtra(Intent.EXTRA_TEXT, "Enjoy Your Image")
    val chooserIntent = Intent.createChooser(shareIntent, shareTitle)
    chooserIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    startActivity(chooserIntent)
}

fun Fragment?.isAddedAndNotDetached(): Boolean {
    return (this?.isAdded ?: false) && !(this?.isDetached ?: true)
}

fun Activity?.handleSystemUpdate() {
    try {
        this?.startActivity(Intent("android.settings.SYSTEM_UPDATE_SETTINGS"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun Context?.launchOtherApp(packageName: String?) {
    try {
        val launchIntent: Intent? =
            this?.packageManager?.getLaunchIntentForPackage(packageName ?: return)
        if (launchIntent != null) {
            this?.startActivity(launchIntent)
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun Context?.isPlayStoreAvailableForApp(packageName: String?): Boolean {
    return try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        intent.resolveActivity(this?.packageManager ?: return false) != null
    } catch (e: Exception) {
        false
    }
}


fun Context?.openPlayStoreForApp(packageName: String?) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        this?.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
        )
        this?.startActivity(intent)
    }
}

fun PackageManager?.getPackageInfoCompat(packageName: String, flags: Int = 0): PackageInfo? =
    try {
        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            this?.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
        } else {
            (this?.getPackageInfo(packageName, flags))
        }
    } catch (e: PackageManager.NameNotFoundException) {
        // Handle the case where the package is not found
        null
    }

fun Context?.getPermissionInfo(permission: String): PermissionInfo? {
    try {
        return this@getPermissionInfo?.packageManager?.getPermissionInfo(
            permission, PackageManager.GET_META_DATA
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun List<PrivacyManagerEntity?>.calculateRiskValues(): Triple<Int, Int, Int> {
    val highRiskValue = this@calculateRiskValues.count { (it?.protectionValue ?: 0) > 21 }
    val averageRiskValue = this@calculateRiskValues.count { (it?.protectionValue ?: 0) in 12..21 }
    val lowRiskValue = this@calculateRiskValues.count { (it?.protectionValue ?: 0) < 12 }

    return Triple(
        first = highRiskValue.plus(3), second = averageRiskValue, third = lowRiskValue.plus(10)
    )
}

inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
    SDK_INT >= 33 -> getParcelable(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}

inline fun <reified T : Parcelable> Bundle.parcelableArrayList(key: String): java.util.ArrayList<T>? =
    when {
        SDK_INT >= 33 -> getParcelableArrayList(key, T::class.java)
        else -> @Suppress("DEPRECATION") getParcelableArrayList(key)
    }

inline fun <reified T : Parcelable> Intent.parcelableArrayList(key: String): ArrayList<T>? = when {
    SDK_INT >= 33 -> getParcelableArrayListExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableArrayListExtra(key)
}

fun calculateProgress(appsProcessed: Int, totalApps: Int): Int {
    if (totalApps == 0) return 100
    val progress = (appsProcessed.toDouble() / totalApps.toDouble()) * 100
    return progress.toInt()
}

fun AppCompatTextView.setGradientTextShader(context: Context?, text: String) {
    val paint = paint
    val width = paint.measureText(text)
    val textShader: Shader = LinearGradient(
        0f, 0f, width, textSize, intArrayOf(
            ContextCompat.getColor(context ?: return, R.color.shader_first_color),
            ContextCompat.getColor(context, R.color.shader_first_color),
            ContextCompat.getColor(context, R.color.shader_second_color),
            ContextCompat.getColor(context, R.color.shader_first_color),
            ContextCompat.getColor(context, R.color.shader_second_color),
            ContextCompat.getColor(context, R.color.shader_first_color)
        ), null, Shader.TileMode.REPEAT
    )
    paint.shader = textShader
    this.text = text
}

fun Drawable.drawableToByteArray(): ByteArray {
    val bitmap = drawableToBitmap(this@drawableToByteArray)
    val stream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.toByteArray()
}

fun byteArrayToDrawable(byteArray: ByteArray): Drawable {
    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    return BitmapDrawable(Resources.getSystem(), bitmap)
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable) {
        return drawable.bitmap
    }
    val bitmap = Bitmap.createBitmap(
        drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bitmap)
    drawable.setBounds(0, 0, canvas.width, canvas.height)
    drawable.draw(canvas)
    return bitmap
}

fun Context.openAppInPlayStore(packageName: String) {
    try {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
    } catch (e: ActivityNotFoundException) {
        startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
        )
    }
}

fun formatSize(sizeInBytes: Long): String {
    if (sizeInBytes <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(sizeInBytes.toDouble()) / log10(1024.0)).toInt()
    return DecimalFormat("#,##0.#").format(sizeInBytes / 1024.0.pow(digitGroups.toDouble())) + " " + units[digitGroups]
}

fun FragmentHomeBinding?.initDrawerClicks(
    colorString: String,
    clickCallback: (Int) -> Unit
) {
    this@initDrawerClicks?.drawerLayout?.apply {
        this@initDrawerClicks.highlightDrawerMenuItem(
            clickedViewPosition = 0, colorString
        )
        drawerMenuHome.setOnClickListener {
            clickCallback.invoke(0)
            this@initDrawerClicks.highlightDrawerMenuItem(
                clickedViewPosition = 0, colorString
            )
        }
        drawerMenuDeviceInfo.setOnClickListener {
            clickCallback.invoke(1)
            this@initDrawerClicks.highlightDrawerMenuItem(
                clickedViewPosition = 1, colorString
            )
        }
        drawerMenuPrivacyPolicy.setOnClickListener {
            clickCallback.invoke(2)
            this@initDrawerClicks.highlightDrawerMenuItem(
                clickedViewPosition = 2, colorString
            )
        }
        drawerMenuShareApp.setOnClickListener {
            clickCallback.invoke(3)
            this@initDrawerClicks.highlightDrawerMenuItem(
                clickedViewPosition = 3, colorString
            )
        }
        drawerMenuMoreApp.setOnClickListener {
            clickCallback.invoke(4)
            this@initDrawerClicks.highlightDrawerMenuItem(
                clickedViewPosition = 4, colorString
            )
        }
        drawerMenuRateUs.setOnClickListener {
            clickCallback.invoke(5)
            this@initDrawerClicks.highlightDrawerMenuItem(
                clickedViewPosition = 5, colorString
            )
        }
        drawerMenuFeedback.setOnClickListener {
            clickCallback.invoke(6)
            this@initDrawerClicks.highlightDrawerMenuItem(
                clickedViewPosition = 6, colorString
            )
        }
    }
}

fun FragmentHomeBinding?.highlightDrawerMenuItem(
    clickedViewPosition: Int = 0, colorString: String = "#80FFC107"
) {
    val drawerMenuItems = listOf(
        this@highlightDrawerMenuItem?.drawerLayout?.drawerMenuHome,
        this@highlightDrawerMenuItem?.drawerLayout?.drawerMenuDeviceInfo,
        //  this@highlightDrawerMenuItem?.drawerLayout?.drawerMenuChangeTheme,
        this@highlightDrawerMenuItem?.drawerLayout?.drawerMenuPrivacyPolicy,
        this@highlightDrawerMenuItem?.drawerLayout?.drawerMenuShareApp,
        this@highlightDrawerMenuItem?.drawerLayout?.drawerMenuMoreApp,
        this@highlightDrawerMenuItem?.drawerLayout?.drawerMenuRateUs,
        this@highlightDrawerMenuItem?.drawerLayout?.drawerMenuFeedback
    )
    for (menuItem in 0..drawerMenuItems.size.minus(1)) {
        if (menuItem == clickedViewPosition) {
            drawerMenuItems[menuItem]?.let {
                it.backgroundTintList = ColorStateList.valueOf(Color.parseColor(colorString))
            }
        } else {
            drawerMenuItems[menuItem]?.let {
                it.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
            }
        }
    }
}

fun Context.feedBackWithEmail(title: String, message: String, emailId: String) {
    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(emailId))
        putExtra(Intent.EXTRA_SUBJECT, title)
        putExtra(Intent.EXTRA_TEXT, message)
    }
    try {
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        } else {
            Toast.makeText(this, "No email client installed on the device", Toast.LENGTH_SHORT)
                .show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun Context.initHomeItemsData(): MutableList<HomeItemsModel> {
    val dataList = mutableListOf<HomeItemsModel>()
    dataList.clear()
    dataList.add(
        HomeItemsModel(
            itemImage = R.drawable.home_item_image_1,
            lbOne = "App Privacy",
            lbTwo = getString(R.string.analyze_the_risk_state_of_each_app)
        )
    )
    dataList.add(
        HomeItemsModel(
            itemImage = R.drawable.home_item_image_2,
            lbOne = "App Usage",
            lbTwo = getString(R.string.analyze_the_risk_state_of_each_app)
        )
    )
    dataList.add(
        HomeItemsModel(
            itemImage = R.drawable.home_item_image_3,
            lbOne = "Uninstall Apps",
            lbTwo = getString(R.string.delete_unnecessary_apps)
        )
    )
    dataList.add(
        HomeItemsModel(
            itemImage = R.drawable.home_item_image_4,
            lbOne = "Install Apps",
            lbTwo = getString(R.string.check_for_downloaded_app_updates)
        )
    )
    return dataList
}

fun Context?.isAppInstalled(packageName: String): Boolean {
    return try {
        this?.packageManager?.getApplicationInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

fun Context.shareApp(pkg: AllAppsEntity) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.setType("text/plain")
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, pkg.appName)
        var shareMessage = "\nLet me recommend you this application\n\n"
        shareMessage =
            (shareMessage + "https://play.google.com/store/apps/details?id=" + pkg.pName) + "\n\n"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        ContextCompat.startActivity(
            this@shareApp, Intent.createChooser(shareIntent, "choose one"), null
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


/**
 * Extension function to check if the Internet is available.
 */
fun Context.isInternetAvailable(): Boolean {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val network = connectivityManager.activeNetwork ?: return false
    val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
    return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}
