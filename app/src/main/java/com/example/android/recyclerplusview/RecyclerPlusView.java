package com.example.android.recyclerplusview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class RecyclerPlusView extends RecyclerView {

    private RecyclerPlusAdapter mRecyclerPlusAdapter;
    private AbsRefreshHeaderView absRefreshHeaderView;
    private PullListener pullListener;
    private float lastY;

    public RecyclerPlusView(Context context) {
        super(context);
    }

    public RecyclerPlusView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public RecyclerPlusView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (adapter != null) {
            mRecyclerPlusAdapter = new RecyclerPlusAdapter(adapter);
            super.setAdapter(mRecyclerPlusAdapter);
        }
    }

    private class RecyclerPlusAdapter extends Adapter<ViewHolder> {

        private static final int TYPE_REFRESH_HEADER = 10000;
        private static final int TYPE_LOAD_MORE_FOOTER = 10001;

        private Adapter mAdapter;

        public RecyclerPlusAdapter(Adapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public int getItemCount() {
            int count = 0;
            if (absRefreshHeaderView != null) {
                count++;
            }
            if (mAdapter != null) {
                count += mAdapter.getItemCount();
            }
            return count;
        }

        @Override
        public int getItemViewType(int position) {
            if (absRefreshHeaderView != null && position == 0) {
                return TYPE_REFRESH_HEADER;
            }
            return 0;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_REFRESH_HEADER) {
                return new PlusHolder(absRefreshHeaderView);
            }
            return mAdapter.onCreateViewHolder(parent, viewType);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        }

        private class PlusHolder extends ViewHolder {

            public PlusHolder(View itemView) {
                super(itemView);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (absRefreshHeaderView != null) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    lastY = e.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveY = e.getRawY() - lastY;
                    lastY = e.getRawY();
                    if (isTop()) {
                        absRefreshHeaderView.onMove(moveY);
                        //Log.d("headView: ", String.valueOf(refreshHeadView.getVisibleHeight()));
                        if (absRefreshHeaderView.getIsNormal()) {
                            //Log.d("isNormal: ", String.valueOf(refreshHeadView.getIsNormal()));
                            return super.onTouchEvent(e);
                        }
                        return false;
                    }
                case MotionEvent.ACTION_UP:
                    absRefreshHeaderView.onUp();
                    break;
            }
        }
        return super.onTouchEvent(e);
    }

    private boolean isTop() {
        return absRefreshHeaderView.getParent() != null;
    }

    //定义下拉上拉监听
    public interface PullListener {
        void refresh();
    }

    //设置下拉刷新上拉加载监听
    public void setPullListener(PullListener listener) {
        this.pullListener = listener;
        if (getAdapter() != null) {
            initRefresh();
        }
    }

    //初始化刷新
    private void initRefresh() {
        if (absRefreshHeaderView == null) {
            absRefreshHeaderView = new SimpleRefreshHeaderView(getContext());
        }
        //mPlushAdapter.setRefreshHeaderView(absRefreshHeaderView);
        absRefreshHeaderView.setRefreshListener(new AbsRefreshHeaderView.RefreshListener() {
            @Override
            public void refresh() {
                pullListener.refresh();
            }
        });
    }

    //判断刷新是否成功
    public void onRefreshComplete(boolean success) {
        if (success) {
            absRefreshHeaderView.onRefreshSuccess();
        } else {
            absRefreshHeaderView.onRefreshFail();
        }
    }
}
