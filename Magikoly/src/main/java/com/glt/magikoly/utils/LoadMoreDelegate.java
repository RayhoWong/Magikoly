package com.glt.magikoly.utils;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

/**
 * Created by X-FAN on 2017/3/21.
 */

public class LoadMoreDelegate {

    private RecyclerView.Adapter mAdapter;
    private OnLoadMoreListener mOnLoadMoreListener;
    private ScrollListener mScrollListener;


    public LoadMoreDelegate(RecyclerView.Adapter adapter, OnLoadMoreListener onLoadMoreListener) {
        mAdapter = adapter;
        this.mOnLoadMoreListener = onLoadMoreListener;
    }

    public void attach(RecyclerView recyclerView) {
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        mScrollListener = new ScrollListener(mAdapter, linearLayoutManager, mOnLoadMoreListener);
        recyclerView.addOnScrollListener(mScrollListener);
    }

    public void loadMoreComplete() {
        mScrollListener.setLoading(false);
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    private static class ScrollListener extends RecyclerView.OnScrollListener {
        private final int size = 2;
        private boolean mLoading = false;

        private LinearLayoutManager mLinearLayoutManager;
        private OnLoadMoreListener mOnLoadMoreListener;
        private RecyclerView.Adapter mAdapter;

        ScrollListener(RecyclerView.Adapter adapter, LinearLayoutManager linearLayoutManager, OnLoadMoreListener onLoadMoreListener) {
            mAdapter = adapter;
            this.mLinearLayoutManager = linearLayoutManager;
            this.mOnLoadMoreListener = onLoadMoreListener;
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            if (dy <= 0) {//下滑忽略
                return;
            }
            int totalNum = mLinearLayoutManager.getItemCount();
            int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
            if (!mLoading && lastVisiblePosition >= totalNum - size) {//最后可见的view的位置为倒数第size个,触发加载更多
                mLoading = true;
//                mAdapter.notifyItemInserted(mItems.size() - 1);
                mOnLoadMoreListener.onLoadMore();
            }
        }

        void setLoading(boolean loading) {
            this.mLoading = loading;
        }
    }

}