package com.asahina.test.view.recyclerView

import android.content.Context
import android.databinding.BindingAdapter
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.view.View
import com.asahina.test.R
import com.asahina.test.view.recyclerView.section.SectionedRecyclerViewAdapter


/**
 * RecyclerView 用 inset divider。
 */
class InsetDividerItemDecoration(context: Context) : RecyclerView.ItemDecoration() {

    private val divider: Drawable?

    init {
        val typedArray = context.obtainStyledAttributes(intArrayOf(android.R.attr.listDivider))
        divider = typedArray.getDrawable(0)
        typedArray.recycle()
    }

    override fun  onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (parent.adapter is SectionedRecyclerViewAdapter<*, *>) {
            // SectionedRecyclerViewAdapter の場合、subheader かどうか判別できるので、material design guidelines に合わせて、subheader の下には divider を引かないようにする。
            val sectionedRecyclerViewAdapter = parent.adapter as SectionedRecyclerViewAdapter<*, *>

            val left = parent.paddingLeft + parent.resources.getDimension(R.dimen.inset_divider_padding_left).toInt()
            val right = parent.width - parent.paddingRight

            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                if (sectionedRecyclerViewAdapter.isSubheader(parent.getChildAdapterPosition(child))) {
                    // subheader なので divider は引かない。
                    continue
                }

                val top = child.bottom + (child.layoutParams as RecyclerView.LayoutParams).bottomMargin
                val bottom = top + divider!!.intrinsicHeight
                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }
        } else {
            val left = parent.paddingLeft + parent.resources.getDimension(R.dimen.inset_divider_padding_left).toInt()
            val right = parent.width - parent.paddingRight

            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                val top = child.bottom + (child.layoutParams as RecyclerView.LayoutParams).bottomMargin
                val bottom = top + divider!!.intrinsicHeight
                divider.setBounds(left, top, right, bottom)
                divider.draw(c)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        outRect.set(0, 0, 0, divider!!.intrinsicHeight)
    }

    object InsetDividerItemDecorationBindingAdapter {

        private fun addInsetDividerItemDecoration(recyclerView: RecyclerView) {
            if (recyclerView.getTag(R.id.key_list_divider_item_decoration) != null) {
                return
            }

            val itemDecoration = InsetDividerItemDecoration(recyclerView.context)
            recyclerView.addItemDecoration(itemDecoration)
            recyclerView.setTag(R.id.key_list_divider_item_decoration, itemDecoration)
        }

        private fun removeInsetDividerItemDecoration(recyclerView: RecyclerView) {
            val itemDecoration = recyclerView.getTag(R.id.key_list_divider_item_decoration) as InsetDividerItemDecoration

            recyclerView.removeItemDecoration(itemDecoration)
            recyclerView.setTag(R.id.key_list_divider_item_decoration, null)
        }

        @BindingAdapter("insetDividerEnabled")
        fun setInsetDividerEnabled(recyclerView: RecyclerView, enabled: Boolean) {
            if (enabled) {
                addInsetDividerItemDecoration(recyclerView)
            } else {
                removeInsetDividerItemDecoration(recyclerView)
            }
        }
    }
}