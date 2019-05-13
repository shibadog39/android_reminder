package com.asahina.test.view.recyclerView

import android.databinding.*
import android.support.v4.util.Consumer
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asahina.test.view.utils.VariableLayoutPair
import com.asahina.test.R

/**
 * データバインディング対応 RecyclerViewAdapter。ページング対応。セクション分け非対応。
 *
 *
 * 参考： https://github.com/radzio/android-data-binding-recyclerview
 */
class PagingRecyclerViewAdapter<T>(itemCollection: Collection<T>, private val variableLayoutPair: VariableLayoutPair, private val progressBarEnabled: ObservableBoolean, private val onTopItemAppeared: Runnable?, private val onBottomItemAppeared: Runnable?, private val onItemClick: Consumer<Pair<Int, T>>?, private val onItemLongClick: Consumer<Pair<Int, T>>?) : RecyclerView.Adapter<PagingRecyclerViewAdapter.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {

    private val onListChangedCallback: ObservableList.OnListChangedCallback<ObservableList<T>>

    private val itemList: ObservableList<T>

    private var inflater: LayoutInflater? = null

    private val onProgressBarEnabledPropertyChangedCallback: Observable.OnPropertyChangedCallback

    private val loading = ObservableBoolean()

    private val bottomLoading = ObservableBoolean(false)

    init {

        loading.set(progressBarEnabled.get())

        onListChangedCallback = object : ObservableList.OnListChangedCallback<ObservableList<T>>() {

            override fun onChanged(sender: ObservableList<T>) {
                notifyDataSetChanged()
            }

            override fun onItemRangeChanged(sender: ObservableList<T>, positionStart: Int, itemCount: Int) {
                notifyItemRangeChanged(positionStart, itemCount)
            }

            override fun onItemRangeInserted(sender: ObservableList<T>, positionStart: Int, itemCount: Int) {
                notifyItemRangeInserted(positionStart, itemCount)
            }

            override fun onItemRangeMoved(sender: ObservableList<T>, fromPosition: Int, toPosition: Int, itemCount: Int) {
                notifyItemMoved(fromPosition, toPosition)
            }

            override fun onItemRangeRemoved(sender: ObservableList<T>, positionStart: Int, itemCount: Int) {
                notifyItemRangeRemoved(positionStart, itemCount)
            }
        }

        if (itemCollection is ObservableList<*>) {
            itemList = itemCollection as ObservableList<T>
        } else {
            itemList = ObservableArrayList()
            itemList.addAll(itemCollection)
        }
        itemList.addOnListChangedCallback(onListChangedCallback)
        notifyItemRemoved(itemList.size)

        //region ページング実装
        onProgressBarEnabledPropertyChangedCallback = object : Observable.OnPropertyChangedCallback() {

            override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                loading.set(progressBarEnabled.get())

                if (!loading.get()) {
                    if (bottomLoading.get()) {
                        notifyItemRemoved(itemList.size)
                        bottomLoading.set(false)
                    }
                }
            }
        }
        progressBarEnabled.addOnPropertyChangedCallback(onProgressBarEnabledPropertyChangedCallback)
        //endregion
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        //region ページング実装
        // ref. http://stackoverflow.com/questions/36127734/detect-when-recyclerview-reaches-the-bottom-most-position-while-scrolling
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                if (progressBarEnabled.get() || itemList.isEmpty() || loading.get()) {
                    // プログレスが出ているときは、何もしない
                    return
                }


                if (recyclerView.layoutManager is StaggeredGridLayoutManager) {
                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager

                    val totalItemCount = layoutManager.itemCount
                    var firstVisibleItems: IntArray? = null
                    firstVisibleItems = layoutManager.findFirstVisibleItemPositions(firstVisibleItems)
                    var lastVisibleItems: IntArray? = null
                    lastVisibleItems = layoutManager.findLastVisibleItemPositions(lastVisibleItems)

                    onTopItemAppeared?.let {
                        if (firstVisibleItems.isNotEmpty() && firstVisibleItems[0] == 0) {
//                        if (!loading.get()) {
                            it.run()
//                            loading.set(true)
//                        } else {
//                            loading.set(false)
//                        }
                        }
                    }

                    onBottomItemAppeared?.let {
                        if (lastVisibleItems.isNotEmpty()) {
                            if (lastVisibleItems.filter { item -> item >= totalItemCount - 1 }.isNotEmpty()) {
//                        if (!loading.get()) {
                                bottomLoading.set(true)
                                notifyItemInserted(itemList.size)
                                it.run()

//                            loading.set(true)
//                        } else {
//                            loading.set(false)
//                        }
                            }
                        }
                    }
                } else {

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    val lastItemPosition = layoutManager.findLastVisibleItemPosition()

                    onTopItemAppeared?.let {
                        if (firstVisibleItemPosition == 0) {
//                        if (!loading.get()) {
                            it.run()
//                            loading.set(true)
//                        } else {
//                            loading.set(false)
//                        }
                        }
                    }

                    onBottomItemAppeared?.let {
                        if (lastItemPosition >= totalItemCount - 1) {
//                        if (!loading.get()) {
                            it.run()
                            bottomLoading.set(true)
                            notifyItemInserted(itemList.size)

//                            loading.set(true)
//                        } else {
//                            loading.set(false)
//                        }
                        }
                    }
                }
            }
        })
        //endregion
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        itemList.removeOnListChangedCallback(onListChangedCallback)

        //region ページング実装
        progressBarEnabled.removeOnPropertyChangedCallback(onProgressBarEnabledPropertyChangedCallback)
        //endregion
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.context)
        }

        return ViewHolder(DataBindingUtil.inflate(inflater!!, viewType, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (position < itemList.size) {
            // プログレスバー以外ならば、 variable や listener などをセットする
            val item = itemList[position]
            holder.binding.setVariable(variableLayoutPair.variableId, item)
            holder.binding.root.setTag(R.id.key_paging_recycler_view_adapter_position, position)
            holder.binding.root.setTag(R.id.key_paging_recycler_view_adapter_item, item)

            if (onItemClick != null) {
                holder.binding.root.setOnClickListener(this)
            }

            if (onItemLongClick != null) {
                holder.binding.root.setOnLongClickListener(this)
            }
        }

        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < itemList.size)
            variableLayoutPair.layoutId
        else
            R.layout.progress_bar_item // プログレスバー表示用のレイアウト
    }

    override fun getItemCount(): Int {
        return if (bottomLoading.get())
            itemList.size + 1 // プログレスバーが有効ならば、プログレスバー表示用のアイテムの分も加える
        else
            itemList.size
    }

    @Suppress("UNCHECKED_CAST")
    override fun onClick(v: View) {
        if (onItemClick == null) {
            return
        }

        try {
            onItemClick.accept(Pair(v.getTag(R.id.key_paging_recycler_view_adapter_position) as Int, v.getTag(R.id.key_paging_recycler_view_adapter_item) as T))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    @Suppress("UNCHECKED_CAST")
    override fun onLongClick(v: View): Boolean {
        if (onItemLongClick == null) {
            return false
        }

        try {
            onItemLongClick.accept(Pair(v.getTag(R.id.key_paging_recycler_view_adapter_position) as Int, v.getTag(R.id.key_paging_recycler_view_adapter_item) as T))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    class ViewHolder internal constructor(internal val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
}