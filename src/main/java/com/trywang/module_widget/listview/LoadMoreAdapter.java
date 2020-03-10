package com.trywang.module_widget.listview;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * Created by shenminjie on 2017/8/9.
 * mail:shenminjie@100bei.com
 */

public class LoadMoreAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * viewType
     */
    public static final int ITEM_TYPE_LOAD_FAILED_VIEW = Integer.MAX_VALUE - 1;
    public static final int ITEM_TYPE_NO_MORE_VIEW = Integer.MAX_VALUE - 2;
    public static final int ITEM_TYPE_LOAD_MORE_VIEW = Integer.MAX_VALUE - 3;
    public static final int ITEM_TYPE_NO_VIEW = Integer.MAX_VALUE - 4;//不展示footer view

    private Context mContext;

    /**
     * 要封装的adaoter
     */
    private RecyclerView.Adapter mInnerAdapter;

    /**
     * 四大显示view
     */
    private View mLoadMoreView;
    private View mLoadMoreFailedView;
    private View mNoMoreView;

    /**
     * 当前view
     */
    private int mCurrentItemType = ITEM_TYPE_LOAD_MORE_VIEW;
    private LoadMoreScrollListener mLoadMoreScrollListener;


    private boolean isLoadError = false;//标记是否加载出错

    /**
     * 是否当前状态稳定
     */
    private boolean isHaveStatesView = true;


    public LoadMoreAdapter(Context context, RecyclerView.Adapter adapter) {
        this.mContext = context;
        this.mInnerAdapter = adapter;
        mLoadMoreScrollListener = new LoadMoreScrollListener() {
            @Override
            public void loadMore() {
                if (mOnLoadListener != null && isHaveStatesView) {
                    if (!isLoadError && mCurrentItemType != ITEM_TYPE_NO_MORE_VIEW) {
                        showLoadMore();
                        mOnLoadListener.onLoadMore();
                    }
                }
            }
        };
    }

    /**
     * 显示加载中
     */
    public void showLoadMore() {
        mCurrentItemType = ITEM_TYPE_LOAD_MORE_VIEW;
        isLoadError = false;
        isHaveStatesView = true;
//        notifyItemChanged(getItemCount());
        notifyDataSetChanged();
    }

    /**
     * 显示加载失败
     */
    public void showLoadError() {
        mCurrentItemType = ITEM_TYPE_LOAD_FAILED_VIEW;
        isLoadError = true;
        isHaveStatesView = true;
//        notifyItemChanged(getItemCount());
        notifyDataSetChanged();
    }

    /**
     * 显示加载完成
     */
    public void showLoadComplete() {
        mCurrentItemType = ITEM_TYPE_NO_MORE_VIEW;
        isLoadError = false;
        isHaveStatesView = true;
//        notifyItemChanged(getItemCount());
        notifyDataSetChanged();
    }


    /**
     * 隐藏加载更多视图
     */
    public void hideLoadMoreView() {
        mCurrentItemType = ITEM_TYPE_NO_VIEW;
        isHaveStatesView = false;
        notifyDataSetChanged();
//        notifyItemChanged(getItemCount());
    }

    //region Get SelectViewHolder
    private RecyclerView.ViewHolder getLoadMoreViewHolder() {
        if (mLoadMoreView == null) {
            mLoadMoreView = new TextView(mContext);
            mLoadMoreView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            mLoadMoreView.setPadding(20, 20, 20, 20);
            ((TextView) mLoadMoreView).setText("正在加载中");
            ((TextView) mLoadMoreView).setTextColor(Color.parseColor("#303030"));
            ((TextView) mLoadMoreView).setGravity(Gravity.CENTER);
        }
        return new RecyclerView.ViewHolder(mLoadMoreView) {
        };
    }

    private RecyclerView.ViewHolder getLoadFailedViewHolder() {
        if (mLoadMoreFailedView == null) {
            mLoadMoreFailedView = new TextView(mContext);
            mLoadMoreFailedView.setPadding(20, 20, 20, 20);
            mLoadMoreFailedView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            ((TextView) mLoadMoreFailedView).setText("加载失败，请点我重试");
            ((TextView) mLoadMoreFailedView).setTextColor(Color.parseColor("#303030"));
            ((TextView) mLoadMoreFailedView).setGravity(Gravity.CENTER);
        }
        return new RecyclerView.ViewHolder(mLoadMoreFailedView) {
        };
    }

    private RecyclerView.ViewHolder getNoMoreViewHolder() {
        if (mNoMoreView == null) {
            mNoMoreView = new TextView(mContext);
            mNoMoreView.setPadding(20, 20, 20, 20);
            mNoMoreView.setLayoutParams(new ViewGroup.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            ((TextView) mNoMoreView).setText("没有更多了");
            ((TextView) mNoMoreView).setTextColor(Color.parseColor("#303030"));
            ((TextView) mNoMoreView).setGravity(Gravity.CENTER);
        }
        return new RecyclerView.ViewHolder(mNoMoreView) {
        };
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1 && isHaveStatesView) {
            return mCurrentItemType;
        }
        return mInnerAdapter.getItemViewType(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ITEM_TYPE_NO_MORE_VIEW) {
            return getNoMoreViewHolder();
        } else if (viewType == ITEM_TYPE_LOAD_MORE_VIEW) {
            return getLoadMoreViewHolder();
        } else if (viewType == ITEM_TYPE_LOAD_FAILED_VIEW) {
            return getLoadFailedViewHolder();
        }
        return mInnerAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == ITEM_TYPE_LOAD_FAILED_VIEW) {
            mLoadMoreFailedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnLoadListener != null) {
                        mOnLoadListener.onRetry();
                        showLoadMore();
                    }
                }
            });
            return;
        }
        if (!isFooterType(holder.getItemViewType())) {
            mInnerAdapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(mLoadMoreScrollListener);
        notifyDataSetChanged();
    }




    @Override
    public int getItemCount() {
        return mInnerAdapter.getItemCount() + (isHaveStatesView ? 1 : 0);
    }

    /**
     * 是否底部viewType
     *
     * @param type type
     * @return return
     */
    public boolean isFooterType(int type) {
        return type == ITEM_TYPE_NO_VIEW ||
                type == ITEM_TYPE_LOAD_FAILED_VIEW ||
                type == ITEM_TYPE_NO_MORE_VIEW ||
                type == ITEM_TYPE_LOAD_MORE_VIEW;
    }

    /**
     * 加载监听事件
     */
    public interface OnLoadListener {

        /**
         * 重新加载
         */
        void onRetry();

        /**
         * 加载更多
         */
        void onLoadMore();
    }

    /**
     * 监听回调
     */
    private OnLoadListener mOnLoadListener;

    /**
     * 设置加载更多的监听事件
     *
     * @param onLoadListener onLoadListener
     * @return LoadMoreAdapter
     */
    public LoadMoreAdapter setOnLoadListener(OnLoadListener onLoadListener) {
        mOnLoadListener = onLoadListener;
        return this;
    }

}
