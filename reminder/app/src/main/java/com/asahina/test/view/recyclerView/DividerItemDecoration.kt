package com.asahina.test.view.recyclerView

import android.content.Context
import android.databinding.BindingAdapter
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View
import com.asahina.test.R


/**
 * RecyclerView 用 divider。
 */
class DividerItemDecoration(context: Context, private val listDivider: Boolean) : RecyclerView.ItemDecoration() {

    private val divider: Drawable?

    init {
        val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        divider = typedArray.getDrawable(0)
        typedArray.recycle()
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val left = parent.paddingLeft
        val right = parent.width - parent.paddingRight

        for (i in 0 until parent.childCount) {

            if (listDivider) {
                if (i + 1 == parent.childCount) {
                    continue
                }
            }

            val child = parent.getChildAt(i)
            val top = child.bottom + (child.layoutParams as RecyclerView.LayoutParams).bottomMargin
            val bottom = top + divider!!.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, 0, divider!!.intrinsicHeight)
    }

    object DividerItemDecorationBindingAdapter {

        private fun addDividerItemDecoration(recyclerView: RecyclerView) {
            if (recyclerView.getTag(R.id.key_list_divider_item_decoration) != null) {
                return
            }

            val itemDecoration = DividerItemDecoration(recyclerView.context, false)
            recyclerView.addItemDecoration(itemDecoration)
            recyclerView.setTag(R.id.key_list_divider_item_decoration, itemDecoration)
        }

        private fun removeDividerItemDecoration(recyclerView: RecyclerView) {
            val itemDecoration = recyclerView.getTag(R.id.key_list_divider_item_decoration) as DividerItemDecoration

            recyclerView.removeItemDecoration(itemDecoration)
            recyclerView.setTag(R.id.key_list_divider_item_decoration, null)
        }

        @BindingAdapter("dividerEnabled")
        fun setDividerEnabled(recyclerView: RecyclerView, enabled: Boolean) {
            if (enabled) {
                addDividerItemDecoration(recyclerView)
            } else {
                removeDividerItemDecoration(recyclerView)
            }
        }
    }
}