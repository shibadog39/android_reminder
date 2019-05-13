package com.asahina.test.view.recyclerView

import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

import com.asahina.test.view.utils.ScrollEvent

/**
 * Created by takuasahina on 2017/11/22.
 */

class SimpleScrollListener(private val scrollEvent: ScrollEvent) : RecyclerView.OnScrollListener() {


    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val totalCount = recyclerView.adapter!!.itemCount
        val childCount = recyclerView.childCount
        val layoutManager = recyclerView.layoutManager

        if (layoutManager is GridLayoutManager) {
            val firstPosition = layoutManager.findFirstVisibleItemPosition()

            scrollEvent.onScrollTop(firstPosition == 0)
            scrollEvent.onScrollBottom(totalCount == firstPosition + childCount)

        } else if (layoutManager is LinearLayoutManager) {
            val firstPosition = layoutManager.findFirstVisibleItemPosition()

            scrollEvent.onScrollTop(firstPosition == 0)
            scrollEvent.onScrollBottom(totalCount == firstPosition + childCount)
        }
    }
}
