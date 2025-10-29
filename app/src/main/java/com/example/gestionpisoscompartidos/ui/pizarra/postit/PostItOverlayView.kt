package com.example.gestionpisoscompartidos.ui.pizarra.postit

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PostItOverlayView
    @JvmOverloads
    constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0,
    ) : View(context, attrs, defStyleAttr) {
        var onOutsideClick: (() -> Unit)? = null

        override fun onTouchEvent(event: MotionEvent): Boolean {
            if (event.action == MotionEvent.ACTION_DOWN) {
                onOutsideClick?.invoke()
                return true
            }
            performClick()
            return super.onTouchEvent(event)
        }

        override fun performClick(): Boolean = super.performClick()
    }
