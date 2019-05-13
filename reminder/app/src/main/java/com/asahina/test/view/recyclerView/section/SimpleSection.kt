package com.asahina.test.view.recyclerView.section

import android.databinding.Observable
import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.databinding.ObservableList
import android.support.annotation.LayoutRes
import android.support.v4.util.Consumer

import com.asahina.test.view.utils.VariableLayoutPair

/**
 * SectionedRecyclerViewAdapter に表示するシンプルなセクション。
 */
class SimpleSection<H, I>
//endregion

private constructor(override val subheader: ObservableField<H>, override val subheaderVariableLayoutPair: VariableLayoutPair, itemCollection: Collection<I>, override val itemVariableLayoutPair: VariableLayoutPair, override val onItemClick: Consumer<Pair<Int, I>>?, override val onItemLongClick: Consumer<Pair<Int, I>>?) : SectionedRecyclerViewAdapter.Section<H, I> {

    private var adapter: SectionedRecyclerViewAdapter<*, *>? = null

    private var subheaderPositionSupplier: Int? = null

    private var onSubheaderChangedCallback: Observable.OnPropertyChangedCallback? = null

    private var onListChangedCallback: ObservableList.OnListChangedCallback<ObservableList<I>>? = null

    override val itemList: ObservableList<I>

    override val size: Int
        get() = if (0 < itemList.size)
            itemList.size + 1
        else
            1

    init {

        if (itemCollection is ObservableList<*>) {
            itemList = itemCollection as ObservableList<I>
        } else {
            itemList = ObservableArrayList()
            itemList.addAll(itemCollection)
        }
    }

    override fun setAdapter(adapter: SectionedRecyclerViewAdapter<H, I>) {
        this.adapter = adapter
    }

    override fun setSubheaderPositionSupplier(intSupplier: Int) {
        subheaderPositionSupplier = intSupplier
    }

    override fun addOnSubheaderChangedCallback() {
        if (onSubheaderChangedCallback != null) {
            return
        }

        onSubheaderChangedCallback = object : Observable.OnPropertyChangedCallback() {
            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                if (itemList.isEmpty()) {
                    return
                }

                adapter!!.notifyItemChanged(subheaderPositionSupplier!!)
            }
        }
        subheader.addOnPropertyChangedCallback(onSubheaderChangedCallback!!)
    }

    override fun addOnItemListChangedCallback() {
        if (onListChangedCallback != null) {
            return
        }

        onListChangedCallback = object : ObservableList.OnListChangedCallback<ObservableList<I>>() {

            private var isPreviousListEmpty = itemList.isEmpty()

            override fun onChanged(sender: ObservableList<I>) {
                adapter!!.notifyDataSetChanged()
            }

            override fun onItemRangeChanged(sender: ObservableList<I>, positionStart: Int, itemCount: Int) {
                adapter!!.notifyItemRangeChanged(subheaderPositionSupplier!! + 1 + positionStart, itemCount)
            }

            override fun onItemRangeInserted(sender: ObservableList<I>, positionStart: Int, itemCount: Int) {
                if (isPreviousListEmpty) {
                    // 変更前のリストが空だった場合、subheader も追加する
                    adapter!!.notifyItemRangeInserted(subheaderPositionSupplier!! + positionStart, itemCount + 1)
                } else {
                    adapter!!.notifyItemRangeInserted(subheaderPositionSupplier!! + 1 + positionStart, itemCount)
                }

                isPreviousListEmpty = sender.isEmpty()
            }

            override fun onItemRangeMoved(sender: ObservableList<I>, fromPosition: Int, toPosition: Int, itemCount: Int) {
                adapter!!.notifyItemMoved(subheaderPositionSupplier!! + 1 + fromPosition, subheaderPositionSupplier!! + 1 + toPosition)
            }

            override fun onItemRangeRemoved(sender: ObservableList<I>, positionStart: Int, itemCount: Int) {
                if (sender.isEmpty()) {
                    // 変更後のリストが空だった場合、subheader も削除する
                    adapter!!.notifyItemRangeRemoved(subheaderPositionSupplier!! + positionStart, itemCount + 1)
                } else {
                    adapter!!.notifyItemRangeRemoved(subheaderPositionSupplier!! + 1 + positionStart, itemCount)
                }

                isPreviousListEmpty = sender.isEmpty()
            }
        }
        itemList.addOnListChangedCallback(onListChangedCallback)
    }

    override fun removeOnSubheaderChangedCallback() {
        if (onSubheaderChangedCallback == null) {
            return
        }

        subheader.removeOnPropertyChangedCallback(onSubheaderChangedCallback!!)
        onSubheaderChangedCallback = null
    }

    override fun removeOnItemListChangedCallback() {
        if (onListChangedCallback == null) {
            return
        }

        itemList.removeOnListChangedCallback(onListChangedCallback)
        onListChangedCallback = null
    }

    companion object {

        //region ファクトリメソッド
        fun <H, I> create(subheader: ObservableField<H>, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int, onItemClick: Consumer<Pair<Int, I>>?, onItemLongClick: Consumer<Pair<Int, I>>?): SimpleSection<H, I> {
            return SimpleSection(subheader, VariableLayoutPair(subheaderVariableId, subheaderLayoutId), itemCollection, VariableLayoutPair(itemVariableId, itemLayoutId), onItemClick, onItemLongClick)
        }

        fun <H, I> create(subheader: ObservableField<H>, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int, onItemClick: Consumer<Pair<Int, I>>?): SimpleSection<H, I> {
            return create(subheader, subheaderVariableId, subheaderLayoutId, itemCollection, itemVariableId, itemLayoutId, onItemClick, null)
        }

        fun <H, I> create(subheader: ObservableField<H>, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int): SimpleSection<H, I> {
            return create(subheader, subheaderVariableId, subheaderLayoutId, itemCollection, itemVariableId, itemLayoutId, null, null)
        }

        fun <H, I> create(subheader: H, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int, onItemClick: Consumer<Pair<Int, I>>?, onItemLongClick: Consumer<Pair<Int, I>>?): SimpleSection<H, I> {
            return create(ObservableField(subheader), subheaderVariableId, subheaderLayoutId, itemCollection, itemVariableId, itemLayoutId, onItemClick, onItemLongClick)
        }

        fun <H, I> create(subheader: H, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int, onItemClick: Consumer<Pair<Int, I>>?): SimpleSection<H, I> {
            return create(ObservableField(subheader), subheaderVariableId, subheaderLayoutId, itemCollection, itemVariableId, itemLayoutId, onItemClick, null)
        }

        fun <H, I> create(subheader: H, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int): SimpleSection<H, I> {
            return create(ObservableField(subheader), subheaderVariableId, subheaderLayoutId, itemCollection, itemVariableId, itemLayoutId, null, null)
        }
    }
}
