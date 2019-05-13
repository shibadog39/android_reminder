package com.asahina.test.view.recyclerView

import android.databinding.BindingAdapter
import android.databinding.ObservableBoolean
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v4.util.Consumer
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.asahina.test.view.recyclerView.section.SectionedRecyclerViewAdapter
import com.asahina.test.view.utils.ScrollEvent
import com.asahina.test.view.utils.VariableLayoutPair
import com.asahina.test.view.recyclerView.parts.PartsRecyclerViewAdapter

/**
 * データバインディング対応 RecyclerViewAdapter 用のユーティリティ。
 */
object RecyclerViewBindingUtils {

    //region SimpleRecyclerViewAdapter
    fun <T> bindAdapter(itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, onItemClick: Consumer<Pair<Int, T>>?, onItemLongClick: Consumer<Pair<Int, T>>?): SimpleRecyclerViewAdapter<T> {
        return SimpleRecyclerViewAdapter(itemCollection, VariableLayoutPair(variableId, layoutId), onItemClick, onItemLongClick)
    }

    fun <T> bindScroll(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, onItemClick: Consumer<Pair<Int, T>>?, onItemLongClick: Consumer<Pair<Int, T>>?, scrollEvent: ScrollEvent) {
        bind(recyclerView, itemCollection, variableId, layoutId, onItemClick, onItemLongClick)
        recyclerView.addOnScrollListener(SimpleScrollListener(scrollEvent))
    }

    fun <T> bindScroll(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, onItemClick: Consumer<Pair<Int, T>>?, scrollEvent: ScrollEvent) {
        bind(recyclerView, itemCollection, variableId, layoutId, onItemClick, null)
        recyclerView.addOnScrollListener(SimpleScrollListener(scrollEvent))
    }

    fun <T> bind(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, onItemClick: Consumer<Pair<Int, T>>?, onItemLongClick: Consumer<Pair<Int, T>>?) {
        val adapter = SimpleRecyclerViewAdapter(itemCollection, VariableLayoutPair(variableId, layoutId), onItemClick, onItemLongClick)
        recyclerView.setItemViewCacheSize(7)
        recyclerView.adapter = adapter
    }

    fun <T> bind(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, onItemClick: Consumer<Pair<Int, T>>?) {
        bind(recyclerView, itemCollection, variableId, layoutId, onItemClick, null)
    }

    fun <T> bind(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int) {
        bind(recyclerView, itemCollection, variableId, layoutId, null, null)
    }
    //endregion

    //region SortableRecyclerViewAdapter
    fun <T> bindSortable(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, @IdRes sortingHandlerViewId: Int, onItemClick: Consumer<T>?, onItemLongClick: Consumer<T>?) {
        val adapter = SortableRecyclerViewAdapter(recyclerView, itemCollection, VariableLayoutPair(variableId, layoutId), sortingHandlerViewId, onItemClick, onItemLongClick)
        recyclerView.adapter = adapter
    }

    fun <T> bindSortable(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, @IdRes sortingHandlerViewId: Int, onItemClick: Consumer<T>?) {
        bindSortable(recyclerView, itemCollection, variableId, layoutId, sortingHandlerViewId, onItemClick, null)
    }

    fun <T> bindSortable(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, @IdRes sortingHandlerViewId: Int) {
        bindSortable(recyclerView, itemCollection, variableId, layoutId, sortingHandlerViewId, null, null)
    }
    //endregion

    //region PagingRecyclerViewAdapter
    fun <T> bindBottomPaging(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, progressBarEnabled: ObservableBoolean, onBottomItemAppeared: Runnable, onItemClick: Consumer<Pair<Int, T>>?, onItemLongClick: Consumer<Pair<Int, T>>?) {
        val adapter = PagingRecyclerViewAdapter(itemCollection, VariableLayoutPair(variableId, layoutId), progressBarEnabled, null, onBottomItemAppeared, onItemClick, onItemLongClick)
        recyclerView.setItemViewCacheSize(5)
        recyclerView.adapter = adapter
    }

    fun <T> bindBottomPaging(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, progressBarEnabled: ObservableBoolean, onBottomItemAppeared: Runnable, onItemClick: Consumer<Pair<Int, T>>?) {
        bindBottomPaging(recyclerView, itemCollection, variableId, layoutId, progressBarEnabled, onBottomItemAppeared, onItemClick, null)
    }

    fun <T> bindBottomPaging(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, progressBarEnabled: ObservableBoolean, onBottomItemAppeared: Runnable) {
        bindBottomPaging(recyclerView, itemCollection, variableId, layoutId, progressBarEnabled, onBottomItemAppeared, null, null)
    }
    //endregion

    //region PagingRecyclerViewAdapter
    fun <T> bindTopBottomPaging(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, progressBarEnabled: ObservableBoolean, onTopItemAppeared: Runnable, onBottomItemAppeared: Runnable, onItemClick: Consumer<Pair<Int, T>>?, onItemLongClick: Consumer<Pair<Int, T>>?) {
        val adapter = PagingRecyclerViewAdapter(itemCollection, VariableLayoutPair(variableId, layoutId), progressBarEnabled, onTopItemAppeared, onBottomItemAppeared, onItemClick, onItemLongClick)
        recyclerView.setItemViewCacheSize(5)
        recyclerView.adapter = adapter
    }

    fun <T> bindTopBottomPaging(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, progressBarEnabled: ObservableBoolean, onTopItemAppeared: Runnable, onBottomItemAppeared: Runnable, onItemClick: Consumer<Pair<Int, T>>?) {
        bindTopBottomPaging(recyclerView, itemCollection, variableId, layoutId, progressBarEnabled, onTopItemAppeared, onBottomItemAppeared, onItemClick, null)
    }

    fun <T> bindTopBottomPaging(recyclerView: RecyclerView, itemCollection: Collection<T>, variableId: Int, @LayoutRes layoutId: Int, progressBarEnabled: ObservableBoolean, onTopItemAppeared: Runnable, onBottomItemAppeared: Runnable) {
        bindTopBottomPaging(recyclerView, itemCollection, variableId, layoutId, progressBarEnabled, onTopItemAppeared, onBottomItemAppeared, null, null)
    }
    //endregion

    //region SectionedRecyclerViewAdapter
    fun <H, I> bindSectioned(recyclerView: RecyclerView, vararg sections: SectionedRecyclerViewAdapter.Section<H, I>): RecyclerView {
        bindSectioned(recyclerView, sections.toList())
        return recyclerView
    }

    fun <H, I> bindSectioned(recyclerView: RecyclerView, sections: List<SectionedRecyclerViewAdapter.Section<H, I>>): RecyclerView {
        val adapter = SectionedRecyclerViewAdapter(sections)
        recyclerView.adapter = adapter
        return recyclerView
    }
    //endregion

    //region SectionedRecyclerViewAdapter
    fun <H, I> bindParts(recyclerView: RecyclerView, vararg sections: PartsRecyclerViewAdapter.Parts<H, I>): RecyclerView {
        bindParts(recyclerView, sections.toList())
        return recyclerView
    }

    fun <H, I> bindParts(recyclerView: RecyclerView, sections: List<PartsRecyclerViewAdapter.Parts<H, I>>): RecyclerView {
        val adapter = PartsRecyclerViewAdapter(sections)
        recyclerView.adapter = adapter
        return recyclerView
    }
    //endregion


    @BindingAdapter("showDivider")
    @JvmStatic
    fun setShowDivider(recyclerView: RecyclerView, showDivider: Boolean) {
        val dividerItemDecoration = DividerItemDecoration(recyclerView.context, (recyclerView.layoutManager as LinearLayoutManager).orientation)
        if (showDivider) {
            recyclerView.addItemDecoration(dividerItemDecoration, 0)
        }
    }
}

