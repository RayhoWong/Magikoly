package com.glt.magikoly.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import magikoly.magiccamera.R;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class EasySwipeRefreshLayout extends SwipeRefreshLayout {

    private RecyclerView mRecyclerView;

    private int mFooterViewRes = R.layout.swipe_refresh_footer;

    private SwipeRefreshListener mSwipeRefreshListener;

    private SwipeUpRefreshHelper mSwipeUpRefreshHelper;

    public EasySwipeRefreshLayout(Context context) {
        super(context);
    }

    public EasySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof RecyclerView) {
                mRecyclerView = (RecyclerView) child;
            }
        }
    }

    public void setRecycleView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    private AdapterWrapper mAdapterWrapper;

    /**
     * 设置recyclerView的adapter
     *
     * @param adapter
     */
    public void setAdapter(RecyclerView.Adapter adapter) {
        if (mRecyclerView != null) {
            mAdapterWrapper = new AdapterWrapper(adapter);
            mRecyclerView.setAdapter(mAdapterWrapper);
            mSwipeUpRefreshHelper = new SwipeUpRefreshHelper(mAdapterWrapper);
        }
    }

    public AdapterWrapper getAdapterWrapper() {
        return mAdapterWrapper;
    }

    /**
     * 设置上拉刷新的加载布局。不设置则使用默认布局
     *
     * @param resId
     */
    public void setFooterView(int resId) {
        mFooterViewRes = resId;
    }

    public void setSwipeRefreshListener(SwipeRefreshListener listener) {
        mSwipeRefreshListener = listener;
        if (mSwipeRefreshListener != null) {
            setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh() {
                    if (mSwipeRefreshListener != null) {
                        mSwipeRefreshListener.onSwipeDownRefresh();
                    }
                }
            });
        } else {
            setOnRefreshListener(null);
        }
    }

    public void setSwipeDownRefreshEnable(boolean enable) {
        setEnabled(enable);
    }

    public void setSwipeUpRefreshEnable(boolean enable) {
        if (mSwipeUpRefreshHelper != null) {
            mSwipeUpRefreshHelper.setSwipeUpRefreshEnabled(enable);
        }
    }

    public void setSwipeDownRefreshFinish() {
        setRefreshing(false);
    }

    public void setSwipeUpRefreshFinish(String info) {
        if (mSwipeUpRefreshHelper != null) {
            mSwipeUpRefreshHelper.setSwipeUpRefreshFinish(info);
        }
    }

    public interface SwipeRefreshListener {
        void onFooterItemStateChanged(boolean isLoading, View itemView, String info);

        void onSwipeDownRefresh();

        void onSwipeUpRefresh();
    }


    public class AdapterWrapper extends RecyclerView.Adapter {

        private static final int ITEM_TYPE_LOAD = Integer.MAX_VALUE / 2;

        private RecyclerView.Adapter mAdapter;

        private boolean mShowLoadItem;

        private WrapperHolder mWrapperHolder;

        public AdapterWrapper(RecyclerView.Adapter adapter) {
            mAdapter = adapter;
        }

        public RecyclerView.Adapter getAdapter() {
            return mAdapter;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == ITEM_TYPE_LOAD) {
                if (mWrapperHolder == null) {
                    mWrapperHolder = new WrapperHolder(
                            View.inflate(parent.getContext(), mFooterViewRes, null));
                }
                return mWrapperHolder;
            } else {
                return mAdapter.onCreateViewHolder(parent, viewType);
            }
        }

        // 允许显示"加载更多"item, 并且position为末尾时,拦截
        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (mShowLoadItem && position == getItemCount() - 1) {
                // 最后一项 不需要做什么额外的事
            } else if (position < mAdapter.getItemCount()) {
                // 正常情况
                holder.itemView.setVisibility(View.VISIBLE);
                mAdapter.onBindViewHolder(holder, position);
            } else {
                // 网格的补空的情况
                holder.itemView.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List payloads) {
            if (mShowLoadItem && position == getItemCount() - 1) {
            } else if (position < mAdapter.getItemCount()) {
                mAdapter.onBindViewHolder(holder, position, payloads);
            }
        }

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            if (holder instanceof WrapperHolder) {
                super.onViewRecycled(holder);
            } else {
                mAdapter.onViewRecycled(holder);
            }
        }

        @Override
        public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
            if (holder instanceof WrapperHolder) {
                return super.onFailedToRecycleView(holder);
            }
            return mAdapter.onFailedToRecycleView(holder);
        }

        @Override
        public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
            if (holder instanceof WrapperHolder) {
                super.onViewDetachedFromWindow(holder);
            } else {
                mAdapter.onViewDetachedFromWindow(holder);
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            if (holder instanceof WrapperHolder) {
                super.onViewAttachedToWindow(holder);
            } else {
                mAdapter.onViewAttachedToWindow(holder);
            }
        }

        @Override
        public int getItemCount() {
            return mShowLoadItem ? mAdapter.getItemCount() + 1 : mAdapter.getItemCount();
        }

        @Override
        public int getItemViewType(int position) {
            // 当显示"加载更多"条目, 并且位置是最后一个时, wrapper进行拦截
            if (mShowLoadItem && position == getItemCount() - 1) {
                return ITEM_TYPE_LOAD;// 注意要避免和原生adapter返回值重复
            }
            // 其他情况交给原生adapter处理
            return mAdapter.getItemViewType(position);
        }

        public void setLoadItemVisibility(boolean isShow) {
            if (mShowLoadItem != isShow) {
                mShowLoadItem = isShow;
                notifyDataSetChanged();
            }
        }

        public void setLoadItemState(boolean isLoading, String info) {
            if (mWrapperHolder == null) {
                return;
            }
            if (mSwipeRefreshListener != null) {
                mSwipeRefreshListener.onFooterItemStateChanged(isLoading, mWrapperHolder.itemView, info);
            }
            if (mFooterViewRes == R.layout.swipe_refresh_footer) {
                if (isLoading) {
                    mWrapperHolder
                            .setLoadText(getContext().getResources().getText(R.string.loading));
                    mWrapperHolder.setProgressBarVisibility(true);
                } else {
                    mWrapperHolder
                            .setLoadText(getContext().getResources().getText(R.string.load_more));
                    mWrapperHolder.setProgressBarVisibility(false);
                }
            }
        }

        class WrapperHolder extends RecyclerView.ViewHolder {

            TextView mTxtLoad;

            ProgressBar mProgressBar;

            public WrapperHolder(View itemView) {
                super(itemView);
                if (mFooterViewRes == R.layout.swipe_refresh_footer) {
                    mTxtLoad = itemView.findViewById(R.id.txt_load);
                    mProgressBar = itemView.findViewById(R.id.pb_load);
                }
            }

            void setLoadText(CharSequence text) {
                if (mTxtLoad != null) {
                    mTxtLoad.setText(text);
                }
            }

            void setLoadTextVisibility(boolean show) {
                if (mTxtLoad != null) {
                    mTxtLoad.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            }

            void setProgressBarVisibility(boolean show) {
                if (mProgressBar != null) {
                    mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            }
        }
    }

    public class SwipeUpRefreshHelper extends RecyclerView.OnScrollListener {

        private RecyclerView.LayoutManager mLayoutManager;
        private AdapterWrapper mAdapterWrapper;
        /**
         * 是否正在加载中
         */
        private boolean mLoading = false;
        /**
         * 上拉刷新功能是否开启
         */
        private boolean mIsSwipeToLoadEnabled;

        public SwipeUpRefreshHelper(AdapterWrapper adapterWrapper) {
            mLayoutManager = mRecyclerView.getLayoutManager();
            mAdapterWrapper = adapterWrapper;
            // 将OnScrollListener设置RecyclerView
            mRecyclerView.addOnScrollListener(this);
            if (mLayoutManager instanceof GridLayoutManager) {
                final GridLayoutManager gridLayoutManager = (GridLayoutManager) mLayoutManager;
                final GridLayoutManager.SpanSizeLookup lookup = gridLayoutManager.getSpanSizeLookup();
                gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                    @Override
                    public int getSpanSize(int position) {
                        if (mIsSwipeToLoadEnabled) {
                            // 功能开启, 根据位置判断, 最后一个item时返回整个宽度, 其他位置返回1
                            // AdapterWrapper会保证最后一个item会从新的一行开始
                            if (position == mLayoutManager.getItemCount() - 1) {
                                return gridLayoutManager.getSpanCount();
                            } else {
                                return lookup.getSpanSize(position);
                            }
                        } else {
                            return lookup.getSpanSize(position);
                        }
                    }
                });
            }
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (mIsSwipeToLoadEnabled && SCROLL_STATE_IDLE == newState && !mLoading) {
                if (mLayoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mLayoutManager;
                    int lastCompletePosition =
                            linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    // only when the complete visible item is second last
                    if (lastCompletePosition == mLayoutManager.getItemCount() - 2) {
                        int firstCompletePosition =
                                linearLayoutManager.findFirstCompletelyVisibleItemPosition();
                        View child = linearLayoutManager.findViewByPosition(lastCompletePosition);
                        if (child == null) {
                            return;
                        }
                        int deltaY = recyclerView.getBottom() - recyclerView.getPaddingBottom() -
                                child.getBottom();
                        if (deltaY > 0 && firstCompletePosition != 0) {
                            recyclerView.smoothScrollBy(0, -deltaY);
                        }
                    } else if (lastCompletePosition == mLayoutManager.getItemCount() - 1) {
                        // 最后一项完全显示, 触发操作, 执行加载更多操作 禁用回弹判断
                        mLoading = true;
                        mAdapterWrapper.setLoadItemState(true, null);
                        if (mSwipeRefreshListener != null) {
                            mSwipeRefreshListener.onSwipeUpRefresh();
                        }
                    }
                }
            }
        }

        /**
         * 设置上拉刷新功能是否开启
         */
        public void setSwipeUpRefreshEnabled(boolean isSwipeToLoadEnabled) {
            if (mIsSwipeToLoadEnabled != isSwipeToLoadEnabled) {
                mIsSwipeToLoadEnabled = isSwipeToLoadEnabled;
                mAdapterWrapper.setLoadItemVisibility(isSwipeToLoadEnabled);
            }
        }

        /**
         * 上拉加载更多完成时调用
         */
        public void setSwipeUpRefreshFinish(String info) {
            mLoading = false;
            mAdapterWrapper.setLoadItemState(false, info);
        }
    }
}
