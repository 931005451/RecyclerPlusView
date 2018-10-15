package com.example.android.recyclerplusview;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public abstract class AbsRefreshHeaderView extends LinearLayout {

    //刷新监听
    private RefreshListener refreshListener;

    public static final int HEAD_STATE_NORMAL = 0;//正常
    public static final int HEAD_STATE_RELEASE_TO_REFRESH = 1;//释放
    public static final int HEAD_STATE_REFRESHING = 2;//刷新
    public static final int HEAD_STATE_SUCCESS = 3;//刷新成功
    public static final int HEAD_STATE_FAIL = 4;//刷新失败
    public static final int HEAD_STATE_RETREAT_NORMAL = 5;//后退到正常

    private int mHeadRefreshState;//刷新状态
    private int refreshHeight;//触发刷新的高度

    private boolean isAnimation = false;//动画是否完成

    public AbsRefreshHeaderView(Context context) {
        this(context, null);
    }

    public AbsRefreshHeaderView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AbsRefreshHeaderView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setRefreshHeight();
        init();
    }

    //初始化
    protected abstract void init();

    //正在下拉
    protected abstract void onPullingDown();

    //已经达到可以刷新的状态
    protected abstract void onReleaseState();

    //执行刷新
    protected abstract void onRefreshing();

    //刷新成功
    protected abstract void onResultSuccess();

    //刷新失败
    protected abstract void onResultFail();

    protected void setRefreshHeight() {
        refreshHeight = (int) (Resources.getSystem().getDisplayMetrics().density * 90 + 0.5F);
    }

    //获取head高度
    protected int getVisibleHeight() {
        return getLayoutParams().height;
    }

    protected boolean getIsNormal() {
        return getVisibleHeight() == 0 && mHeadRefreshState == HEAD_STATE_NORMAL;
    }

    //是否在刷新中(刷新中包含刷新成功或失败)
    protected boolean getIsRefreshing() {
        return mHeadRefreshState == HEAD_STATE_REFRESHING
                || mHeadRefreshState == HEAD_STATE_FAIL
                || mHeadRefreshState == HEAD_STATE_SUCCESS;
    }

    //设置head高度
    public void setVisibleHeight(int height) {
        height = height < 0 ? 0 : height;
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = height;
        setLayoutParams(lp);
    }

    private void smoothScrollTo(int height) {
        ValueAnimator animator = ValueAnimator.ofInt(getVisibleHeight(), height);
        animator.setDuration(260);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setVisibleHeight((int) animation.getAnimatedValue());
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isAnimation = animation.isRunning();//true
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                isAnimation = animation.isRunning();//false
                if (mHeadRefreshState == HEAD_STATE_REFRESHING) {
                    refreshListener.refresh();
                }
                //如果状态成功或者失败后恢复状态为默认的状态
                if (mHeadRefreshState == HEAD_STATE_SUCCESS || mHeadRefreshState == HEAD_STATE_FAIL) {
                    mHeadRefreshState = HEAD_STATE_NORMAL;
                }
            }
        });
        animator.start();
    }

    //手指移动 该方法只涉及到头部高度，没有涉及到动画
    final void onMove(float move) {
        //动画没有执行则为true
        if (!isAnimation) {
            //System.out.println("刷新高度: " + getVisibleHeight());
            if (mHeadRefreshState == HEAD_STATE_NORMAL
                    || mHeadRefreshState == HEAD_STATE_RETREAT_NORMAL
                    || mHeadRefreshState == HEAD_STATE_RELEASE_TO_REFRESH) {
                int newVisibleHeight = (int) (getVisibleHeight() + Math.floor(move / 2.5));
                setVisibleHeight(newVisibleHeight);
                //如果当前高度等于0
                if (getVisibleHeight() == 0) {
                    mHeadRefreshState = HEAD_STATE_NORMAL;
                }
                //如果当前高度小于刷新高度
                else if (getVisibleHeight() < refreshHeight) {
                    mHeadRefreshState = HEAD_STATE_RETREAT_NORMAL;
                    onPullingDown();
                }
                //如果当前高度大于刷新高度
                else if (getVisibleHeight() > refreshHeight) {
                    mHeadRefreshState = HEAD_STATE_RELEASE_TO_REFRESH;
                    onReleaseState();
                }
            }
        }
    }

    //手指离开
    final void onUp() {
        //动画没有执行则为true
        if (!isAnimation) {
            //状态正常和动画不在运行的时则为true
            if (mHeadRefreshState == HEAD_STATE_RETREAT_NORMAL) {
                smoothScrollTo(0);
            } else if (mHeadRefreshState == HEAD_STATE_RELEASE_TO_REFRESH) {
                mHeadRefreshState = HEAD_STATE_REFRESHING;
                onRefreshing();
                smoothScrollTo(refreshHeight);//回退到刷新的临界点高度
            }
        }
    }

    //判断刷新是否成功
    final void setRefreshState(boolean isSuccess) {
        if (!isAnimation) {
            if (isSuccess) {
                mHeadRefreshState = HEAD_STATE_SUCCESS;
                onResultSuccess();//刷新成功
                smoothScrollTo(0);
            } else {
                mHeadRefreshState = HEAD_STATE_FAIL;
                onResultFail();//刷新失败
                smoothScrollTo(0);
            }
        }
    }

    //刷新成功
    public void onRefreshSuccess() {
        setRefreshState(true);
    }

    //刷新失败
    public void onRefreshFail() {
        setRefreshState(false);
    }

    //定义刷新的监听
    public interface RefreshListener {
        void refresh();
    }

    final void setRefreshListener(RefreshListener listener) {
        refreshListener = listener;
    }
}
