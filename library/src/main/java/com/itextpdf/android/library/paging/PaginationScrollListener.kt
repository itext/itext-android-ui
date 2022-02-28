package com.itextpdf.android.library.paging

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

abstract class PaginationScrollListener(
    private val layoutManager: LinearLayoutManager,
    /**
     * If there are only x more items before the end of the recycler, load more.
     */
    private val loadMoreOffset: Int
) :
    RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        super.onScrolled(recyclerView, dx, dy)

        val visibleItemCount = layoutManager.childCount
        val totalItemCount = layoutManager.itemCount
        val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

        if (!isLoading && !isLastPage) {
            val shouldLoadMoreCount = visibleItemCount + firstVisibleItemPosition + loadMoreOffset
            if (shouldLoadMoreCount >= totalItemCount && firstVisibleItemPosition >= 0) {
                loadMoreItems()
            }
        }
    }

    protected abstract fun loadMoreItems()
    abstract val isLastPage: Boolean
    abstract val isLoading: Boolean
}