package com.asahina.test.view.recyclerView

import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.databinding.ViewDataBinding
import android.support.annotation.IdRes
import android.support.v4.util.Consumer
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.asahina.test.view.utils.VariableLayoutPair
import com.asahina.test.R

/**
 * データバインディング対応 RecyclerViewAdapter。並べ替え対応。セクション分け非対応。
 *
 *
 * 参考： https://github.com/radzio/android-data-binding-recyclerview
 */
class SortableRecyclerViewAdapter<T>(private val recyclerView: RecyclerView, itemCollection: Collection<T>, private val variableLayoutPair: VariableLayoutPair, @param:IdRes private val sortingHandlerViewId: Int, private val onItemClick: Consumer<T>?, private val onItemLongClick: Consumer<T>?) : RecyclerView.Adapter<SortableRecyclerViewAdapter.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {

    private val onListChangedCallback: ObservableList.OnListChangedCallback<ObservableList<T>>

    private val itemList: ObservableList<T>

    private var inflater: LayoutInflater? = null

    //region 並べ替え機能の実装
    private val itemSortingHelper: ItemTouchHelper

    init {
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
        notifyItemRangeInserted(0, itemList.size)
        itemList.addOnListChangedCallback(onListChangedCallback)

        //region 並べ替え機能の実装
        itemSortingHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

            private var fromPosition = -1

            private var toPosition = -1

            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                notifyItemMoved(fromPosition, toPosition)
                this.toPosition = toPosition
                return true
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)

                when (actionState) {
                    ItemTouchHelper.ACTION_STATE_DRAG -> fromPosition = viewHolder!!.adapterPosition
                    else -> {
                    }
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)

                if (fromPosition == toPosition || toPosition < 0) {
                    return
                }

                itemList.removeOnListChangedCallback(onListChangedCallback)
                val item = itemList.removeAt(fromPosition)
                itemList.add(toPosition, item)
                toPosition = -1
                itemList.addOnListChangedCallback(onListChangedCallback)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // do nothing
            }

            override fun isLongPressDragEnabled(): Boolean {
                return false
            }
        })
        //endregion
    }
    //endregion


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        //region 並べ替え機能の実装
        itemSortingHelper.attachToRecyclerView(recyclerView)
        //endregion
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        itemList.removeOnListChangedCallback(onListChangedCallback)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.context)
        }

        return ViewHolder(DataBindingUtil.inflate(inflater!!, viewType, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.binding.setVariable(variableLayoutPair.variableId, item)
        holder.binding.root.setTag(R.id.key_sortable_recycler_view_adapter_item, item)

        if (onItemClick != null) {
            holder.binding.root.setOnClickListener(this)
        }

        if (onItemLongClick != null) {
            holder.binding.root.setOnLongClickListener(this)
        }

        //region 並べ替えの実装
        val sortingHandlerView = holder.binding.root.findViewById<View>(sortingHandlerViewId)
        sortingHandlerView?.setOnTouchListener { _, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                itemSortingHelper.startDrag(holder)
                true
            } else {
                false
            }
        }
        //endregion

        holder.binding.executePendingBindings()
    }

    override fun getItemViewType(position: Int): Int {
        return variableLayoutPair.layoutId
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    @Suppress("UNCHECKED_CAST")
    override fun onClick(v: View) {
        if (onItemClick == null) {
            return
        }

        try {
            onItemClick.accept(v.getTag(R.id.key_sortable_recycler_view_adapter_item) as T)
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
            onItemLongClick.accept(v.getTag(R.id.key_sortable_recycler_view_adapter_item) as T)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    class ViewHolder internal constructor(internal val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
}