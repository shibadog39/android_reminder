package com.asahina.test.view.recyclerView

import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View


/**
 * RecyclerView 用 divider。
 */
class FooterItemDecoration(context: Context, private val listDivider: Boolean) : RecyclerView.ItemDecoration() {

    private val divider: Drawable?

    init {
        val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        divider = typedArray.getDrawable(0)
        typedArray.recycle()
    }


    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.getChildAdapterPosition(view) == parent.childCount) {
            outRect.set(0, 0, 0, 200)
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }
}