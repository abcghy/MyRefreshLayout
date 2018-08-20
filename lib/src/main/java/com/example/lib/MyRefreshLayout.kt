package com.example.lib

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.support.v4.view.NestedScrollingParent
import android.support.v4.view.ViewCompat
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout

fun Context.px2dp(px: Int): Float = px.toFloat() / this.resources.displayMetrics.density
fun Context.dp2px(dp: Float): Int = (resources.displayMetrics.density * dp).toInt()

enum class RefreshState {
    NORMAL,
    DRAGGING_TO_REFRESH,
    RELEASE_TO_REFRESH,
    REFRESHING,
    BACK_TO_NORMAL,
}

interface OnRefreshListener {
    fun onRefresh(refreshLayout: MyRefreshLayout)
}
interface OnLoadMoreListener

class MyRefreshLayout: LinearLayout, NestedScrollingParent {

    val refreshDistance = 150f
    var mState: RefreshState

    var onRefreshListener: OnRefreshListener? = null

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        mValueAnimator = ValueAnimator()
        mState = RefreshState.NORMAL
    }

    private fun setState(state: RefreshState) {
        if (mState != state) {
            Log.d("test", "state: $state")
            mState = state
            when (mState) {
                RefreshState.REFRESHING -> onRefreshListener?.onRefresh(this)
            }
        }
    }

    private var mValueAnimator: ValueAnimator

    override fun onStartNestedScroll(child: View, target: View, nestedScrollAxes: Int): Boolean {
//        Log.d("test", "onStartNestedScroll")
        return nestedScrollAxes.and(ViewCompat.SCROLL_AXIS_VERTICAL) != 0
    }

    private var mTotalUnconsumed = 0

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        var showHeader = dy < 0 && mTotalUnconsumed <= 0 && !ViewCompat.canScrollVertically(target, -1)
        var hideHeader = dy > 0 && mTotalUnconsumed < 0
        if (showHeader
                || hideHeader
        ) {
            if (mState == RefreshState.NORMAL) {
                setState(RefreshState.DRAGGING_TO_REFRESH)
            }
            val resultY = mTotalUnconsumed + dy
            val scrollYDP = context.px2dp(mTotalUnconsumed)
            if (Math.abs(scrollYDP) >= refreshDistance) {
                setState(RefreshState.RELEASE_TO_REFRESH)
            } else {
                setState(RefreshState.DRAGGING_TO_REFRESH)
            }
            if (resultY > 0) {
                val ty = -mTotalUnconsumed
                mTotalUnconsumed += ty
                moveSpinner(mTotalUnconsumed)
                consumed[1] = ty
            } else {
                mTotalUnconsumed += dy
                moveSpinner(mTotalUnconsumed)
                consumed[1] = dy
            }
        }
    }

    private fun moveSpinner(overScrollTop: Int) {
        Log.d("test", "overScrollTop: $overScrollTop")
        scrollTo(0, overScrollTop)
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int) {
//        Log.d("test", "onNestedScrollAccepted")
        super.onNestedScrollAccepted(child, target, axes)
    }

    override fun onStopNestedScroll(child: View) {
        if (mTotalUnconsumed != 0) {
            if (mState == RefreshState.RELEASE_TO_REFRESH) {
                setState(RefreshState.REFRESHING)
                // back to refresh distance
//                moveSpinnerTo(context.dp2px(-refreshDistance), true)
                mValueAnimator
                        .apply {
                            this.setIntValues(mTotalUnconsumed, context.dp2px(-refreshDistance))
                            this.addUpdateListener {
                                mTotalUnconsumed = it.animatedValue as Int
                                scrollTo(0, mTotalUnconsumed)
                            }
                            removeAllListeners()
                            duration = Math.abs(mTotalUnconsumed.toLong()) / 2
                        }.start()
            } else if (mState == RefreshState.DRAGGING_TO_REFRESH) {
                // back to normal
                setState(RefreshState.BACK_TO_NORMAL)
                Log.d("test", "onStopNestedScroll: mState: $mState")
                mValueAnimator
                        .apply {
                            this.setIntValues(mTotalUnconsumed, 0)
                            this.addUpdateListener {
                                mTotalUnconsumed = it.animatedValue as Int
                                scrollTo(0, mTotalUnconsumed)
                            }
                            this.addListener(object: Animator.AnimatorListener {
                                override fun onAnimationRepeat(animation: Animator?) {}

                                override fun onAnimationCancel(animation: Animator?) {}

                                override fun onAnimationStart(animation: Animator?) {}

                                override fun onAnimationEnd(animation: Animator?) {
                                    setState(RefreshState.NORMAL)
                                }

                            })
                            duration = Math.abs(mTotalUnconsumed.toLong()) / 2
                        }.start()
            }

        }
//        Log.d("test", "onStopNestedScroll: scrollY: $scrollY")
//        super.onStopNestedScroll(child)
    }

//    private fun moveSpinnerTo(px: Int, isAnimated: Boolean = true) {
//        if (isAnimated) {
//
//        } else {
//
//        }
//    }

    fun finishRefresh() {
        if (mState == RefreshState.REFRESHING) {
            mValueAnimator
                    .apply {
                        setIntValues(mTotalUnconsumed, 0)
                        addUpdateListener {
                            mTotalUnconsumed = it.animatedValue as Int
                            scrollTo(0, mTotalUnconsumed)
                        }
                        addListener(object: Animator.AnimatorListener {
                            override fun onAnimationRepeat(animation: Animator?) {}

                            override fun onAnimationCancel(animation: Animator?) {}

                            override fun onAnimationStart(animation: Animator?) {}

                            override fun onAnimationEnd(animation: Animator?) {
                                setState(RefreshState.NORMAL)
                            }

                        })
                        duration = Math.abs(mTotalUnconsumed.toLong()) / 2
                    }.start()
        }
    }

//    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
//        var msg = ""
//        when (ev.action) {
//            MotionEvent.ACTION_DOWN -> msg = "ACTION_DOWN"
//            MotionEvent.ACTION_MOVE -> msg = "ACTION_MOVE"
//            MotionEvent.ACTION_UP -> msg = "ACTION_UP"
//            MotionEvent.ACTION_CANCEL -> msg= "ACTION_CANCEL"
//        }
//        Log.d("test", "dispatchTouchEvent $msg")
//        return super.dispatchTouchEvent(ev)
//    }
//
//    override fun onTouchEvent(event: MotionEvent): Boolean {
//        var msg = ""
//        when (event.action) {
//            MotionEvent.ACTION_DOWN -> msg = "ACTION_DOWN"
//            MotionEvent.ACTION_MOVE -> msg = "ACTION_MOVE"
//            MotionEvent.ACTION_UP -> msg = "ACTION_UP"
//            MotionEvent.ACTION_CANCEL -> msg= "ACTION_CANCEL"
//        }
//        Log.d("test", "onTouchEvent $msg")
//        return super.onTouchEvent(event)
//    }
}