package com.example.softwareupdate.utils

import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.example.softwareupdate.R
import com.google.android.material.textview.MaterialTextView

@BindingAdapter("android:text")
fun setText(view: MaterialTextView, text: CharSequence?) {
    view.text = text
}

@BindingAdapter("android:visibility")
fun setVisibility(view: View, isVisible: Boolean) {
    view.visibility = if (isVisible) View.VISIBLE else View.GONE
}


@BindingAdapter("app:imageUrl")
fun loadImage(view: AppCompatImageView, imageUrl: String) {
    imageUrl.let {
        Glide.with(view.context).load(it).placeholder(R.drawable.imageplace)
            .error(R.drawable.android_icon_hd).into(view)
    }
}

@BindingAdapter("app:imageUrl")
fun loadImageWithResID(view: AppCompatImageView, imageUrl: Int) {
    imageUrl.let {
        Glide.with(view.context).load(it).placeholder(R.drawable.imageplace)
            .error(R.drawable.system_app_icon).into(view)
    }
}

@BindingAdapter("app:drawableImage")
fun setDrawableImage(view: AppCompatImageView, drawable: Drawable?) {
    Glide.with(view.context).load(drawable).placeholder(R.drawable.imageplace)
        .error(R.drawable.system_app_icon).into(view)
}

@BindingAdapter("app:byteArrayImage")
fun setByteArrayImage(view: AppCompatImageView, byteArray: ByteArray?) {
    if (byteArray?.isNotEmpty() == true) {
        Glide.with(view.context).load(byteArray).placeholder(R.drawable.imageplace)
            .error(R.drawable.system_app_icon).into(view)
    } else {
        view.setImageResource(R.drawable.imageplace)
    }
}
