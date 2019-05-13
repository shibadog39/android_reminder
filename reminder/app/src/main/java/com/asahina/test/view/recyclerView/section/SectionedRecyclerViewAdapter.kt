package com.asahina.test.view.recyclerView.section

import android.databinding.DataBindingUtil
import android.databinding.ObservableField
import android.databinding.ObservableList
import android.databinding.ViewDataBinding
import android.support.v4.util.Consumer
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.asahina.test.view.utils.VariableLayoutPair
import com.asahina.test.R

/**
 * データバインディング対応 RecyclerViewAdapter。セクション分け対応。
 *
 *
 * 参考： https://github.com/radzio/android-data-binding-recyclerview
 */
class SectionedRecyclerViewAdapter<H, I>(private val sectionList: List<Section<H, I>>) : RecyclerView.Adapter<SectionedRecyclerViewAdapter.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {

    private var inflater: LayoutInflater? = null

    init {

        val totalSize = sectionList
                .map { section -> section.size }
                .sum()

        notifyItemRangeInserted(0, totalSize)

        sectionList.forEachIndexed { index, section ->
            section.setAdapter(this@SectionedRecyclerViewAdapter)

            section.setSubheaderPositionSupplier(
                    IntRange(0, index - 1)
                            .map { i -> this@SectionedRecyclerViewAdapter.sectionList[i].size }
                            .sum())

            section.addOnSubheaderChangedCallback()
            section.addOnItemListChangedCallback()
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        //region 並べ替えの実装（SortableSection のみ）
        for (section in sectionList) {
            if (section is SortableSection<H, I>) {
                section.attachItemSortingHelperToRecyclerView(recyclerView)
            }
        }
        //endregion
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        for (section in sectionList) {
            section.removeOnItemListChangedCallback()
            section.removeOnSubheaderChangedCallback()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        if (inflater == null) {
            inflater = LayoutInflater.from(parent.context)
        }

        return ViewHolder(DataBindingUtil.inflate(inflater!!, viewType, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        for (i in sectionList.indices) {
            val section = sectionList[i]
            if (section.size == 0) {
                continue
            }

            val subheaderPosition = subheaderPosition(i)

            if (subheaderPosition == position) {
                holder.binding.setVariable(section.subheaderVariableLayoutPair.variableId, section.subheader.get())
                holder.binding.executePendingBindings()
                break
            } else if (subheaderPosition < position && position < subheaderPosition + section.size) {
                val item = section.itemList[position - (subheaderPosition + 1)]
                holder.binding.setVariable(section.itemVariableLayoutPair.variableId, item)
                holder.binding.root.setTag(R.id.key_sectioned_recycler_view_adapter_item, item)
                holder.binding.root.setTag(R.id.key_simple_recycler_view_adapter_position, position)
                holder.binding.root.setTag(R.id.key_sectioned_recycler_view_adapter_section_index, i)

                if (section.onItemClick != null) {
                    holder.binding.root.setOnClickListener(this)
                } else {
                    holder.binding.root.setOnClickListener(null)
                    holder.binding.root.isClickable = false
                }

                if (section.onItemLongClick != null) {
                    holder.binding.root.setOnLongClickListener(this)
                } else {
                    holder.binding.root.setOnLongClickListener(null)
                    holder.binding.root.isLongClickable = false
                }

                //region 並べ替えの実装（SortableSection のみ）
                if (section is SortableSection<H, I>) {
                    val sortingHandlerView = holder.binding.root.findViewById<View>(section.sortingHandlerViewId)
                    sortingHandlerView?.setOnTouchListener { _, event ->
                        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                            section.startSorting(holder)
                            true
                        } else {
                            false
                        }
                    }
                }
                //endregion

                holder.binding.executePendingBindings()
                break
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        for (i in sectionList.indices) {
            val section = sectionList[i]
            if (section.size != 0) {

                val subheaderPosition = subheaderPosition(i)

                if (subheaderPosition == position) {
                    return section.subheaderVariableLayoutPair.layoutId
                } else if (subheaderPosition < position && position < subheaderPosition + section.size) {
                    return section.itemVariableLayoutPair.layoutId
                }
            }
        }

        throw IllegalStateException()
    }

    fun subheaderPosition(i: Int): Int {
        return IntRange(0, i - 1)
                .map { j -> sectionList[j].size }
                .sum()
    }

    override fun getItemCount(): Int {
        return sectionList
                .map { section -> section.size }
                .sum()
    }

    @Suppress("UNCHECKED_CAST")
    override fun onClick(v: View) {
        val sectionIndex = v.getTag(R.id.key_sectioned_recycler_view_adapter_section_index) as Int

        sectionList[sectionIndex].onItemClick?.let {
            it.accept(Pair(v.getTag(R.id.key_simple_recycler_view_adapter_position) as Int, v.getTag(R.id.key_sectioned_recycler_view_adapter_item) as I))
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onLongClick(v: View): Boolean {
        val sectionIndex = v.getTag(R.id.key_sectioned_recycler_view_adapter_section_index) as Int

        sectionList[sectionIndex].onItemLongClick?.let {
            it.accept(Pair(v.getTag(R.id.key_simple_recycler_view_adapter_position) as Int, v.getTag(R.id.key_sectioned_recycler_view_adapter_item) as I))
            return true
        }

        return false
    }

    fun isSubheader(position: Int): Boolean {
        for (i in sectionList.indices) {
            val section = sectionList[i]
            if (section.size != 0) {

                val subheaderPosition = subheaderPosition(i)

                if (subheaderPosition == position) {
                    return true
                }
            }
        }

        return false
    }

    class ViewHolder internal constructor(internal val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    interface Section<H, I> {

        val size: Int

        val subheader: ObservableField<H>

        val subheaderVariableLayoutPair: VariableLayoutPair

        val itemList: ObservableList<I>

        val itemVariableLayoutPair: VariableLayoutPair

        val onItemClick: Consumer<Pair<Int, I>>?

        val onItemLongClick: Consumer<Pair<Int, I>>?

        fun setAdapter(adapter: SectionedRecyclerViewAdapter<H, I>)

        fun setSubheaderPositionSupplier(intSupplier: Int)

        fun addOnSubheaderChangedCallback()

        fun addOnItemListChangedCallback()

        fun removeOnSubheaderChangedCallback()

        fun removeOnItemListChangedCallback()
    }
}