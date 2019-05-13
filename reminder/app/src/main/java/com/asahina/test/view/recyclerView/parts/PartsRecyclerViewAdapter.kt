package com.asahina.test.view.recyclerView.parts

import android.databinding.*
import android.support.v4.util.Consumer
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.StaggeredGridLayoutManager
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
class PartsRecyclerViewAdapter<H, I>(private val sectionList: List<Parts<H, I>>) : RecyclerView.Adapter<PartsRecyclerViewAdapter.ViewHolder>(), View.OnClickListener, View.OnLongClickListener {

    private var inflater: LayoutInflater? = null

    private var clickableView: Boolean = false

    private val loading = ObservableBoolean()

    private val bottomLoading = ObservableBoolean(false)

    init {

        val totalSize = sectionList
                .map { section -> section.size }
                .sum()

        notifyItemRangeInserted(0, totalSize)

        sectionList.forEachIndexed { index, section ->
            section.setAdapter(this@PartsRecyclerViewAdapter)

            section.setSubheaderPositionSupplier(
                    IntRange(0, index - 1)
                            .map { i -> this@PartsRecyclerViewAdapter.sectionList[i].size }
                            .sum())

            section.addOnSubheaderChangedCallback()
            section.addOnItemListChangedCallback()
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        //region 並べ替えの実装（SortableParts のみ）
        for (section in sectionList) {
            if (section is SortableParts<H, I>) {
                section.attachItemSortingHelperToRecyclerView(recyclerView)
            } else if (section is ScrollParts<H, I>) {

                //region ページング実装
                section.progressBarEnabled.addOnPropertyChangedCallback(object : Observable.OnPropertyChangedCallback() {

                    override fun onPropertyChanged(sender: Observable, propertyId: Int) {
                        loading.set(section.progressBarEnabled.get())

                        if (!loading.get()) {
                            if (bottomLoading.get()) {
                                bottomLoading.set(false)
                            }
                        }
                    }
                })
                //endregion

                //region ページング実装
                // ref. http://stackoverflow.com/questions/36127734/detect-when-recyclerview-reaches-the-bottom-most-position-while-scrolling
                recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {

                        if (section.progressBarEnabled.get() || section.itemList.isEmpty() || loading.get()) {
                            // プログレスが出ているときは、何もしない
                            return
                        }


                        if (recyclerView.layoutManager is StaggeredGridLayoutManager) {
                            val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager

                            val totalItemCount = layoutManager.itemCount
                            var firstVisibleItems: IntArray? = null
                            var lastVisibleItems: IntArray? = null
                            firstVisibleItems = layoutManager.findFirstVisibleItemPositions(firstVisibleItems)
                            lastVisibleItems = layoutManager.findLastVisibleItemPositions(lastVisibleItems)

                            section.onTopItemAppeared?.let {
                                if (firstVisibleItems.isNotEmpty() && firstVisibleItems[0] == 0) {
                                    it.run()
                                }
                            }

                            section.onBottomItemAppeared?.let {
                                if (lastVisibleItems.isNotEmpty()) {
                                    if (lastVisibleItems.filter { item -> item >= totalItemCount - 2 }.isNotEmpty()) {
                                        bottomLoading.set(true)
                                        it.run()
                                    }
                                }
                            }
                        } else {

                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager

                            val totalItemCount = layoutManager.itemCount
                            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                            val lastItemPosition = layoutManager.findLastVisibleItemPosition()

                            section.onTopItemAppeared?.let {
                                if (firstVisibleItemPosition == 0) {
                                    it.run()
                                }
                            }

                            section.onBottomItemAppeared?.let {
                                if (lastItemPosition >= totalItemCount - 1) {
                                    it.run()
                                    bottomLoading.set(true)
                                    notifyItemInserted(section.itemList.size)
                                }
                            }
                        }
                    }
                })
                //endregion
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

        sectionList.indices.forEach { i ->
            val section = sectionList[i]

            if (section is ClickableParts<H, I>) {
                clickViewItem(holder, position, section, i)
                return@forEach
            }

            val subheaderPosition = subheaderPosition(i)

            if (subheaderPosition == position) {
                if (holder.itemView.layoutParams is StaggeredGridLayoutManager.LayoutParams) {
                    val layoutParams = holder.itemView.layoutParams as StaggeredGridLayoutManager.LayoutParams
                    layoutParams.isFullSpan = true
                }
                holder.binding.setVariable(section.subheaderVariableLayoutPair.variableId, section.subheader.get())
                holder.binding.executePendingBindings()
                return@forEach
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

                //region 並べ替えの実装（SortableParts のみ）
                if (section is SortableParts<H, I>) {
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
                return@forEach
            }

        }
    }

    fun clickViewItem(holder: ViewHolder, position: Int, section: ClickableParts<H, I>, i: Int) {

        val subheaderPosition = subheaderPosition(i)

        val sortingHandlerView = holder.binding.root.findViewById<View>((section).clickHandlerViewId)

        if (subheaderPosition == position) {
            holder.binding.setVariable(section.subheaderVariableLayoutPair.variableId, section.subheader.get())
            sortingHandlerView.setTag(R.id.key_sectioned_recycler_view_adapter_item, null)
        } else if (subheaderPosition < position && position < subheaderPosition + section.size) {
            val item = section.itemList[position - (subheaderPosition + 1)]
            holder.binding.setVariable(section.itemVariableLayoutPair.variableId, item)
            sortingHandlerView.setTag(R.id.key_sectioned_recycler_view_adapter_item, item)
        }

        sortingHandlerView.setTag(R.id.key_simple_recycler_view_adapter_position, position)
        sortingHandlerView.setTag(R.id.key_sectioned_recycler_view_adapter_section_index, i)

        if (section.onItemClick != null) {
            sortingHandlerView.setOnClickListener(this)
        }

        holder.binding.executePendingBindings()
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
            val subheaderPosition = subheaderPosition(i)
            if (subheaderPosition == position) {
                return true
            }
        }

        return false
    }

    class ViewHolder internal constructor(internal val binding: ViewDataBinding) : RecyclerView.ViewHolder(binding.root)

    interface Parts<H, I> {

        val size: Int

        val subheader: ObservableField<H>

        val subheaderVariableLayoutPair: VariableLayoutPair

        val itemList: ObservableList<I>

        val itemVariableLayoutPair: VariableLayoutPair

        val onItemClick: Consumer<Pair<Int, I>>?

        val onItemLongClick: Consumer<Pair<Int, I>>?

        fun setAdapter(adapter: PartsRecyclerViewAdapter<H, I>)

        fun setSubheaderPositionSupplier(intSupplier: Int)

        fun addOnSubheaderChangedCallback()

        fun addOnItemListChangedCallback()

        fun removeOnSubheaderChangedCallback()

        fun removeOnItemListChangedCallback()
    }
}