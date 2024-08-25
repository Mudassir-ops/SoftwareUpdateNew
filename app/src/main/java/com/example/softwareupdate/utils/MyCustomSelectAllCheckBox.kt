package com.example.softwareupdate.utils

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatCheckBox
import com.example.softwareupdate.R

class MyCustomSelectAllCheckBox(context: Context?, attrs: AttributeSet?) : AppCompatCheckBox(
    context!!, attrs
) {
    override fun setChecked(t: Boolean) {
        if (t) {
            setBackgroundResource(R.drawable.select_all)
        } else {
            setBackgroundResource(R.drawable.deselect_all)
        }
        super.setChecked(t)
    }
}