package com.example.softwareupdate.utils

import android.content.Context
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import com.example.softwareupdate.R

typealias OnItemClickListener = (position: Int) -> Unit

class PopupOptionSelector(private val context: Context) {
    fun showPopup(
        anchorView: View, menuResId: Int, listener: OnItemClickListener
    ) {
        val popupMenu =
            PopupMenu(context, anchorView, Gravity.NO_GRAVITY, 0, R.style.CustomPopupMenuStyle)
        popupMenu.menuInflater.inflate(menuResId, popupMenu.menu)
        popupMenu.setOnMenuItemClickListener { item: MenuItem ->
            val menu = popupMenu.menu
            val position = (0 until menu.size()).indexOfFirst { menu.getItem(it) == item }
            listener(position)
            true
        }
        popupMenu.show()
    }
}



