package com.asahina.test.view.recyclerView.section

import android.databinding.Observable
import android.databinding.ObservableArrayList
import android.databinding.ObservableField
import android.databinding.ObservableList
import android.support.annotation.IdRes
import android.support.annotation.LayoutRes
import android.support.v4.util.Consumer
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import com.asahina.test.view.utils.VariableLayoutPair

/**
 * SectionedRecyclerViewAdapter に表示する並べ替え可能なセクション。
 */
class SortableSection<H, I>
//endregion

private constructor(override val subheader: ObservableField<H>, override val subheaderVariableLayoutPair: VariableLayoutPair, itemCollection: Collection<I>, override val itemVariableLayoutPair: VariableLayoutPair, @param:IdRes val sortingHandlerViewId: Int, override val onItemClick: Consumer<Pair<Int, I>>?, override val onItemLongClick: Consumer<Pair<Int, I>>?) : SectionedRecyclerViewAdapter.Section<H, I> {

    private var adapter: SectionedRecyclerViewAdapter<H, I>? = null

    private var subheaderPositionSupplier: Int? = null

    private var onSubheaderChangedCallback: Observable.OnPropertyChangedCallback? = null

    private var onListChangedCallback: ObservableList.OnListChangedCallback<ObservableList<I>>? = null

    override val itemList: ObservableList<I>

    //region 並べ替え機能の実装
    private val itemSortingHelper: ItemTouchHelper

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

        //region 並べ替え機能の実装
        itemSortingHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

            private var fromPosition = -1

            private var toPosition = -1

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                val firstItemPosition = subheaderPositionSupplier!! + 1
                if (toPosition < firstItemPosition || firstItemPosition + itemList.size <= toPosition) {
                    return false
                }

                adapter!!.notifyItemMoved(fromPosition, toPosition)
                this.toPosition = toPosition - firstItemPosition
                return true
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> {
                        val adapterPosition = viewHolder!!.adapterPosition
                        val firstItemPosition = subheaderPositionSupplier!! + 1
                        if (adapterPosition < firstItemPosition || firstItemPosition + itemList.size <= adapterPosition) {
                            return
                        }

                        fromPosition = adapterPosition - firstItemPosition
                    }
                    else -> {
                    }
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                if (fromPosition == toPosition || toPosition < 0) {
                    return
                }

                this@SortableSection.subheader.removeOnPropertyChangedCallback(onSubheaderChangedCallback!!)
                itemList.removeOnListChangedCallback(onListChangedCallback)
                val item = itemList.removeAt(fromPosition)
                itemList.add(toPosition, item)
                toPosition = -1
                itemList.addOnListChangedCallback(onListChangedCallback)
                this@SortableSection.subheader.addOnPropertyChangedCallback(onSubheaderChangedCallback!!)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // do nothing
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }

            override fun interpolateOutOfBoundsScroll(recyclerView: RecyclerView, viewSize: Int, viewSizeOutOfBounds: Int, totalSize: Int, msSinceStartScroll: Long): Int {
                if (viewSizeOutOfBounds > 1) {
                    return 22
                } else if (viewSizeOutOfBounds < -1) {
                    return -16
                }
                return 0
            }
        })
        //endregion
    }

    fun attachItemSortingHelperToRecyclerView(recyclerView: RecyclerView) {
        itemSortingHelper.attachToRecyclerView(recyclerView)
    }

    fun startSorting(holder: RecyclerView.ViewHolder) {
        itemSortingHelper.startDrag(holder)
    }
    //endregion

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
        fun <H, I> create(subheader: ObservableField<H>, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int, @IdRes sortingHandlerViewId: Int, onItemClick: Consumer<Pair<Int, I>>?, onItemLongClick: Consumer<Pair<Int, I>>?): SortableSection<H, I> {
            return SortableSection(subheader, VariableLayoutPair(subheaderVariableId, subheaderLayoutId), itemCollection, VariableLayoutPair(itemVariableId, itemLayoutId), sortingHandlerViewId, onItemClick, onItemLongClick)
        }

        fun <H, I> create(subheader: ObservableField<H>, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int, @IdRes sortingHandlerViewId: Int, onItemClick: Consumer<Pair<Int, I>>?): SortableSection<H, I> {
            return create(subheader, subheaderVariableId, subheaderLayoutId, itemCollection, itemVariableId, itemLayoutId, sortingHandlerViewId, onItemClick, null)
        }

        fun <H, I> create(subheader: ObservableField<H>, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int, @IdRes sortingHandlerViewId: Int): SortableSection<H, I> {
            return create(subheader, subheaderVariableId, subheaderLayoutId, itemCollection, itemVariableId, itemLayoutId, sortingHandlerViewId, null, null)
        }

        fun <H, I> create(subheader: H, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int, @IdRes sortingHandlerViewId: Int, onItemClick: Consumer<Pair<Int, I>>?, onItemLongClick: Consumer<Pair<Int, I>>?): SortableSection<H, I> {
            return create(ObservableField(subheader), subheaderVariableId, subheaderLayoutId, itemCollection, itemVariableId, itemLayoutId, sortingHandlerViewId, onItemClick, onItemLongClick)
        }

        fun <H, I> create(subheader: H, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int, @IdRes sortingHandlerViewId: Int, onItemClick: Consumer<Pair<Int, I>>?): SortableSection<H, I> {
            return create(ObservableField(subheader), subheaderVariableId, subheaderLayoutId, itemCollection, itemVariableId, itemLayoutId, sortingHandlerViewId, onItemClick, null)
        }

        fun <H, I> create(subheader: H, subheaderVariableId: Int, @LayoutRes subheaderLayoutId: Int, itemCollection: Collection<I>, itemVariableId: Int, @LayoutRes itemLayoutId: Int, @IdRes sortingHandlerViewId: Int): SortableSection<H, I> {
            return create(ObservableField(subheader), subheaderVariableId, subheaderLayoutId, itemCollection, itemVariableId, itemLayoutId, sortingHandlerViewId, null, null)
        }
    }
}
