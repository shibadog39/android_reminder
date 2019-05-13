package com.asahina.test.view.recyclerView

import android.content.Context
import android.graphics.PointF
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSmoothScroller
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

// ref. http://stackoverflow.com/questions/31235183/recyclerview-how-to-smooth-scroll-to-top-of-item-on-a-certain-position/32819067
class SmoothScrollableLinearLayoutManager : LinearLayoutManager {

    constructor(context: Context) : super(context) {}

    constructor(context: Context, orientation: Int, reverseLayout: Boolean) : super(context, orientation, reverseLayout) {}

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {}

    override fun smoothScrollToPosition(recyclerView: RecyclerView, state: RecyclerView.State?, position: Int) {
        val linearSmoothScroller = object : LinearSmoothScroller(recyclerView.context) {
            override fun getVerticalSnapPreference(): Int {
                return LinearSmoothScroller.SNAP_TO_START
            }

            override fun computeScrollVectorForPosition(targetPosition: Int): PointF? {
                return this@SmoothScrollableLinearLayoutManager.computeScrollVectorForPosition(targetPosition)
            }
        }

        linearSmoothScroller.targetPosition = position
        startSmoothScroll(linearSmoothScroller)
    }
}