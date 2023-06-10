package me.doteq.dolinabaryczy.ui

import android.graphics.drawable.GradientDrawable
import android.view.View

object Animations {
    fun revealFromTop(view: View, progress: Float) {
        view.y = (progress * view.height) - view.height
    }

    fun bottomSheetCornersFill(view: View, progress: Float) {
        val corners = (progress * 48f)
        (view.background as GradientDrawable).cornerRadii = floatArrayOf(corners,corners,corners,corners,0f,0f,0f,0f)
    }
}