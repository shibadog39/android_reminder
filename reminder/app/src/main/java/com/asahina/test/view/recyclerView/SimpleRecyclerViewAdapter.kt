package com.asahina.test.view.recyclerView

import android.databinding.DataBindingUtil
import android.databinding.ObservableArrayList
import android.databinding.ObservableList
import android.databinding.ViewDataBinding
import android.support.v4.util.Consumer
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.asahina.test.R
import com.asahina.test.view.utils.VariableLayoutPair


/**
 * データバインディング対応 RecyclerViewAdapter。セクション分け非対応。
 *
 *
 * 参考： https://github.com/radzio/android-data-binding-recyclerview
 */
class SimpleRecyclerViewAdapter<T>(itemCollection: Collection<T>, private val variableLayoutPair: VariableLayoutPair, private val onItemClick: Consumer<Pair<Int, T>>?, private val onItemLongClick: Consumer<Pair<Int, T>>?) : RecyclerView.Adapter<SimpleRecyclerViewAdapter.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {

    private val onListChangedCallback: ObservableList.OnListChangedCallback<ObservableList<T>>

    private val itemList: ObservableList<T>

    private var inflater: LayoutInflater? = null

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
        holder.binding.root.setTag(R.id.key_simple_recycler_view_adapter_position, position)
        holder.binding.root.setTag(R.id.key_simple_recycler_view_adapter_item, item)

        if (onItemClick != null) {
            holder.binding.root.setOnClickListener(this)
        }

        if (onItemLongClick != null) {
            holder.binding.root.setOnLongClickListener(this)
        }

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
            onItemClick.accept(Pair(v.getTag(R.id.key_simple_recycler_view_adapter_position) as Int, v.getTag(R.id.key_simple_recycler_view_adapter_item) as T))
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
            onItemLongClick.accept(Pair(v.getTag(R.id.key_simple_recycler_view_adapter_position) as Int, v.getTag(R.id.key_simple_recycler_view_adapter_item) as T))
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    class ViewHolder internal constructor(internal val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)
}