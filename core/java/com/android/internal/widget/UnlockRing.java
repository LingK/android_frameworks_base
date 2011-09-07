/*
 * Copyright (C) 2011 Roman Birg
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.internal.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.android.internal.R;
import com.android.internal.widget.SlidingTab.OnTriggerListener;

public class UnlockRing extends ViewGroup {

    private static final String TAG = "UnlockRing";
    private static Context mContext;
    private static final boolean DBG = true;
    private static final int HORIZONTAL = 0;
    public static final int MAX_RADIUS = 185;
    public static final int HALO_RADIUS = 45;
    public static int mQuadrant = 0;
    private static final int TRACKING_MARGIN = 50;
    private static final int ANIM_DURATION = 250;
    private static final int ANIM_TARGET_TIME = 500;

    private OnUnlockRingTriggerListener mOnTriggerListener;
    private int mGrabbedState = OnUnlockRingTriggerListener.NO_HANDLE;
    private boolean mTriggered = false;
    private Vibrator mVibrator;
    private float mDensity;

    private int mOrientation;
    private Ring ring;
    private boolean mTracking;
    private boolean mAnimating;
    private static final long VIBRATE_SHORT = 30;
    private static final long VIBRATE_LONG = 40;
    private boolean mEnableAppLauncherMode = false;
    private boolean mUnlockMode;
    private Rect mTmpRect;

    public void enableUnlockMode() {
        mUnlockMode = true;
        ring.unlocker.getDrawable().setAlpha(255);
        refreshDrawableState();
        invalidate();
    }

    private final AnimationListener mAnimationDoneListener = new AnimationListener() {
        public void onAnimationStart(Animation animation) {

        }

        public void onAnimationRepeat(Animation animation) {

        }

        public void onAnimationEnd(Animation animation) {
            onAnimationDone();
        }
    };

    public interface OnUnlockRingTriggerListener {

        public static final int NO_HANDLE = 0;
        public static final int UNLOCK_HANDLE = 1;
        public static final int POKE_LOCK = 2;
        public static final int QUADRANT_1 = 3;
        public static final int QUADRANT_2 = 4;
        public static final int QUADRANT_3 = 5;
        public static final int QUADRANT_4 = 6;

        void onUnlockRingTrigger(View v, int whichHandle);
        void onUnlockRingGrabbedStateChange(View v, int grabbedState);
    }

    private static class Ring {

        private static final int STATE_NORMAL = 0;
        private static final int STATE_PRESSED = 1;
        private static final int STATE_ACTIVE = 2;

        private final ImageView unlocker;
        private final ImageView unlockRing;
        private int currentState = STATE_NORMAL;
        private int alignment_x;
        private int alignment_y;
        private int maxRadius;

        Ring(ViewGroup parent, int unlockId, int unlockHalo, int targetId) {

            unlocker = new ImageView(parent.getContext());
            unlocker.setImageResource(unlockId);
            unlocker.setBackgroundResource(unlockHalo);

            unlocker.setScaleType(ScaleType.CENTER);
            unlocker.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));

            unlockRing = new ImageView(parent.getContext());
            unlockRing.setImageResource(targetId);
            unlockRing.setScaleType(ScaleType.CENTER);

            unlockRing.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT));
            unlockRing.setVisibility(View.INVISIBLE);

            maxRadius = 183;
            parent.addView(unlockRing);
            parent.addView(unlocker);
        }

        void setIcon(int iconId) {
            unlocker.setImageResource(iconId);
        }

        void setIcon(Bitmap icon) {
            unlocker.setImageBitmap(icon);
        }

        void setTabBackgroundResource(int tabId) {
            unlocker.setBackgroundResource(tabId);
        }

        void setState(int state) {
            unlocker.setPressed(state == STATE_PRESSED);

            if (state == STATE_ACTIVE) {
                final int[] activeState = new int[] {
                    com.android.internal.R.attr.state_active
                };

                if (unlocker.getBackground().isStateful()) {
                    unlocker.getBackground().setState(activeState);
                }
            }
            currentState = state;
        }

        void showTarget() {
            AlphaAnimation alphaAnim = new AlphaAnimation(0.0f, 1.0f);
            alphaAnim.setDuration(ANIM_TARGET_TIME);
            unlockRing.startAnimation(alphaAnim);
            unlockRing.setVisibility(View.VISIBLE);
        }

        void reset(boolean animate) {
            setState(STATE_NORMAL);

            unlocker.setVisibility(View.VISIBLE);
            unlockRing.setVisibility(View.INVISIBLE);

            int dx = alignment_x - unlocker.getLeft();
            dx -= (unlocker.getWidth() / 2);

            int dy = alignment_y - unlocker.getTop();
            dy -= (unlocker.getHeight() / 2);

            if (animate) {
                TranslateAnimation trans = new TranslateAnimation(0, dx, 0, dy);
                trans.setDuration(ANIM_DURATION);
                trans.setFillAfter(false);
                unlocker.startAnimation(trans);
            } else {
                unlocker.offsetLeftAndRight(dx);
                unlocker.offsetTopAndBottom(dy);
                unlocker.clearAnimation();
                unlockRing.clearAnimation();
            }
        }

        void setTarget(int targetId) {
            unlockRing.setImageResource(targetId);
        }

        void layout(int l, int t, int r, int b) {
            final Drawable tabBackground = unlocker.getBackground();
            final int handleWidth = tabBackground.getIntrinsicWidth();
            final int handleHeight = tabBackground.getIntrinsicHeight();

            final Drawable targetDrawable = unlockRing.getDrawable();
            final int targetWidth = targetDrawable.getIntrinsicWidth();
            final int targetHeight = targetDrawable.getIntrinsicHeight();

            final int parentWidth = r - l;
            final int parentHeight = b - t;

            final int leftUnlocker = (int) (parentWidth - handleWidth) / 2;
            final int rightUnlocker = (int) (parentWidth + handleWidth) / 2;

            final int topUnlocker = (int) (parentHeight - handleHeight) / 2;
            final int bottomUnlocker = (int) (parentHeight + handleHeight) / 2;

            final int leftTarget = (int) (parentWidth - targetWidth) / 2;
            final int rightTarget = (int) (parentWidth + targetWidth) / 2;

            final int topTarget = (int) (parentHeight - targetHeight) / 2;
            final int bottomTarget = (int) (parentHeight + targetHeight) / 2;

            unlocker.layout(leftUnlocker, topUnlocker, rightUnlocker, bottomUnlocker);
            unlockRing.layout(leftTarget, topTarget, rightTarget, bottomTarget);

            alignment_x = (int) (leftUnlocker + rightUnlocker) / 2;
            alignment_y = (int) (topTarget + bottomTarget) / 2;

            if (DBG) {
                Log.d(TAG, "Align x: " + alignment_x);
                Log.d(TAG, "Align y: " + alignment_y);
            }
        }

        public void updateDrawableStates() {
            setState(currentState);
        }

        public void measure() {
            unlocker.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            unlockRing.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        }

        public int getTabWidth() {
            return unlocker.getMeasuredWidth();
        }

        public int getTabHeight() {
            return unlocker.getMeasuredHeight();
        }

        public int getOuterRingHeight() {
            return unlockRing.getMeasuredHeight();
        }

        public int getOuterRingWidth() {
            return unlockRing.getMeasuredWidth();
        }

        public void startAnimation(Animation anim1, Animation anim2) {
            unlocker.startAnimation(anim1);
        }

        public void hideTarget() {
            unlockRing.clearAnimation();
            unlockRing.setVisibility(View.INVISIBLE);
            unlockRing.clearFocus();
        }
    }

    public UnlockRing(Context context) {
        this(context, null);
    }

    public UnlockRing(Context context, AttributeSet attrs) {
        super(context, attrs);

        mContext = context;
        mTmpRect = new Rect();

        Resources r = getResources();
        mDensity = r.getDisplayMetrics().density;

        if (DBG)
            log("- Density: " + mDensity);

        ring = new Ring(this, R.drawable.unlock_default, R.drawable.unlock_halo,
                R.drawable.unlock_ring);

        mVibrator = (android.os.Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        mUnlockMode = true;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);

        int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

        if (widthSpecMode == MeasureSpec.UNSPECIFIED || heightSpecMode == MeasureSpec.UNSPECIFIED) {
            Log.e("SlidingTab", "SlidingTab cannot have UNSPECIFIED MeasureSpec" + "(wspec="
                    + widthSpecMode + ", hspec=" + heightSpecMode + ")", new RuntimeException(TAG
                    + "stack:"));
        }

        ring.measure();
        final int leftTabWidth = ring.getTabWidth();
        final int leftTabHeight = ring.getTabHeight();

        final int ringWidth = ring.getOuterRingWidth();
        final int ringHeight = ring.getOuterRingHeight();

        final int width;
        final int height;

        width = Math.max(widthSpecSize, ringWidth);
        height = Math.max(ringHeight, 0);
        setMeasuredDimension(width, height);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        final int action = event.getAction();
        final float x = event.getX();
        final float y = event.getY();

        View unlocker = ring.unlocker;
        unlocker.getHitRect(mTmpRect);
        boolean unlockerHit = inHalo(x, y);

        if (mEnableAppLauncherMode) {
            if (inRing(x, y) && !unlockerHit) {
                Log.d(TAG, "Touched at : (" + x + ", " + y + ") and  unlocker is "
                        + (unlockerHit ? "hit" : "not hit"));
                mUnlockMode = !mUnlockMode;
                if (mUnlockMode) {
                    ring.unlocker.getDrawable().setAlpha(255);
                } else {
                    ring.unlocker.getDrawable().setAlpha(0);
                }
                ring.unlocker.refreshDrawableState();
                refreshDrawableState();
                invalidate();
                return false;
            }
        }

        if ((!mTracking && !unlockerHit) || mAnimating) {
            return false;
        }

        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mTracking = true;
                mTriggered = false;
                vibrate(VIBRATE_LONG);

                if (unlockerHit) {
                    setGrabbedState(OnUnlockRingTriggerListener.UNLOCK_HANDLE);
                }

                ring.setState(Ring.STATE_PRESSED);
                ring.showTarget();
                break;
            }
        }

        return true;
    }

    public void reset(boolean animate) {
        ring.reset(animate);
        if (!animate) {
            mAnimating = false;
        }
    }

    @Override
    public void setVisibility(int visibility) {
        if (visibility != getVisibility() && visibility == View.INVISIBLE) {
            reset(false);
        }
        super.setVisibility(visibility);
    }

    public void setQuadrant(float absX, float absY) {
        double x = (double) absX - ring.alignment_x;
        double y = (double) (absY - ring.alignment_y) * -1;

        if (x > 0 && y > 0) {
            mQuadrant = 1;
            ring.unlockRing.setColorFilter(0xAA0276FD);
        } else if (x > 0 && y < 0) {
            mQuadrant = 2;
            ring.unlockRing.setColorFilter(0xAA0AC92B);
        } else if (x < 0 && y < 0)
            mQuadrant = 3;
        else
            mQuadrant = 4;

        Log.d(TAG, "(" + x + ", " + y + ") quadrant: " + mQuadrant);
    }

    public void updateColor(float x, float y) {
        ring.unlockRing.clearColorFilter();

        if (mUnlockMode) {
            ring.unlockRing.clearColorFilter();
        } else {
            switch (mQuadrant) {
                case 1:
                    ring.unlockRing.setColorFilter(0xAA0276FD);
                    break;
                case 2:
                    ring.unlockRing.setColorFilter(0xAA0AC92B);
                    break;
                case 3:
                    ring.unlockRing.setColorFilter(0xAA9933FF);
                    break;
                case 4:
                    ring.unlockRing.setColorFilter(0xAAFF9933);
                    break;
                default:
                    ring.unlockRing.clearColorFilter();
                    break;
            }
        }
        refreshDrawableState();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (mTracking) {
            final int action = event.getAction();
            final float x = event.getX();
            final float y = event.getY();

            switch (action) {
                case MotionEvent.ACTION_MOVE:
                    if (withinView(x, y, this)) {

                        boolean thresholdReached;
                        thresholdReached = !inRing(x, y);

                        if (!thresholdReached) {
                            if (mEnableAppLauncherMode) {
                                setQuadrant(x, y);
                                updateColor(x, y);
                            }
                            moveHandle(x, y);
                        }

                        if (!mTriggered && thresholdReached) {
                            if (!mUnlockMode && mEnableAppLauncherMode) {
                                switch (mQuadrant) {
                                    case 1:
                                        dispatchTriggerEvent(OnUnlockRingTriggerListener.QUADRANT_1);
                                        break;
                                    case 2:
                                        dispatchTriggerEvent(OnUnlockRingTriggerListener.QUADRANT_2);
                                        break;
                                    case 3:
                                        dispatchTriggerEvent(OnUnlockRingTriggerListener.QUADRANT_3);
                                        break;
                                    case 4:
                                        dispatchTriggerEvent(OnUnlockRingTriggerListener.QUADRANT_4);
                                        break;
                                    default:
                                        dispatchTriggerEvent(OnUnlockRingTriggerListener.NO_HANDLE);
                                        break;
                                }
                            } else {
                                dispatchTriggerEvent(OnUnlockRingTriggerListener.UNLOCK_HANDLE);
                            }
                            mTriggered = true;
                            mTracking = false;
                            enableUnlockMode();
                            setGrabbedState(OnTriggerListener.NO_HANDLE);
                        }
                        break;
                    }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    mTracking = false;
                    mTriggered = false;
                    ring.reset(false);
                    ring.hideTarget();

                    this.refreshDrawableState();
                    ring.unlockRing.refreshDrawableState();
                    setGrabbedState(OnTriggerListener.NO_HANDLE);
                    break;
            }
        }

        this.refreshDrawableState();
        return mTracking || super.onTouchEvent(event);
    }

    public boolean inRing(float xPos, float yPos) {
        double x = (double) Math.abs(xPos - ring.alignment_x);
        double y = (double) Math.abs(yPos - ring.alignment_y);

        double grabRadius = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        final int r = 185;

        Log.d(TAG, "X: " + x + " , Y: " + y);
        Log.d(TAG, "Max radius: " + r);
        Log.d(TAG, "Grab radius: " + grabRadius);

        final int padding = 0;

        if (grabRadius < (r + padding))
            return true;

        return false;
    }

    public boolean inHalo(float xPos, float yPos) {
        double x = (double) Math.abs(xPos - (ring.unlocker.getLeft() + ring.unlocker.getRight())
                / 2);
        double y = (double) Math.abs(yPos - (ring.unlocker.getBottom() + ring.unlocker.getTop())
                / 2);

        int r = HALO_RADIUS;

        if (mTracking) {
            r += 15;
        } else {
            r += 20;
        }

        double grabRadius = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));

        if (grabRadius < r)
            return true;

        return false;
    }

    void startAnimating() {
    }

    private void onAnimationDone() {
        resetView();
        mAnimating = false;
    }

    private boolean withinView(final float x, final float y, final View view) {
        return isHorizontal() && y > -TRACKING_MARGIN && y < view.getHeight() || !isHorizontal()
                && x > -TRACKING_MARGIN && x < TRACKING_MARGIN + view.getWidth();
    }

    private boolean isHorizontal() {
        return mOrientation == HORIZONTAL;
    }

    private void resetView() {
        this.refreshDrawableState();
        ring.reset(false);
        onLayout(true, getLeft(), getTop(), getLeft() + getWidth(), getTop() + getHeight());
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!changed)
            return;
        ring.layout(l, t, r, b);
    }

    private void moveHandle(float x, float y) {
        final View handle = ring.unlocker;
        int deltaX = (int) x - handle.getLeft() - (handle.getWidth() / 2);
        handle.offsetLeftAndRight(deltaX);

        int deltaY = (int) y - handle.getTop() - (handle.getHeight() / 2);
        handle.offsetTopAndBottom(deltaY);
        invalidate();
    }

    public void setRingResources(int iconId, int targetId, int barId) {
        ring.setIcon(iconId);
        ring.setTarget(targetId);
        ring.updateDrawableStates();
    }

    public void setOnUnlockRingTriggerListener(OnUnlockRingTriggerListener listener) {
        mOnTriggerListener = listener;
    }

    private void dispatchTriggerEvent(int whichHandle) {
        vibrate(VIBRATE_LONG);

        if (mOnTriggerListener != null) {
            mOnTriggerListener.onUnlockRingTrigger(this, whichHandle);
        }
    }

    private synchronized void vibrate(long duration) {
        if (mVibrator == null) {
            mVibrator = (android.os.Vibrator) getContext().getSystemService(
                    Context.VIBRATOR_SERVICE);
        }
        mVibrator.vibrate(duration);
    }

    private void setGrabbedState(int newState) {
        if (newState != mGrabbedState) {
            mGrabbedState = newState;

            if (mOnTriggerListener != null) {
                mOnTriggerListener.onUnlockRingGrabbedStateChange(this, mGrabbedState);
            }
        }
    }

    private void log(String msg) {
        Log.d(TAG, msg);
    }
}
