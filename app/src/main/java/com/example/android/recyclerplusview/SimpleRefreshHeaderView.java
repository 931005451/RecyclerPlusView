package com.example.android.recyclerplusview;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SimpleRefreshHeaderView extends AbsRefreshHeaderView {

    private TextView mTvTip;

    public SimpleRefreshHeaderView(Context context) {
        super(context);
    }

    public SimpleRefreshHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SimpleRefreshHeaderView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void init() {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        setLayoutParams(params);
        setGravity(Gravity.BOTTOM);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.header_refresh_view, this, false);
        mTvTip = view.findViewById(R.id.tv_tip);
        addView(view);
    }

    @Override
    protected void onPullingDown() {
        mTvTip.setText(R.string.pull_to_refresh);
    }

    @Override
    protected void onReleaseState() {
        mTvTip.setText(R.string.release_refresh);
    }

    @Override
    protected void onRefreshing() {
        mTvTip.setText(R.string.refreshing);
    }

    @Override
    protected void onResultSuccess() {
        mTvTip.setText(R.string.refresh_success);
    }

    @Override
    protected void onResultFail() {
        mTvTip.setText(R.string.refresh_fail);
    }
}
