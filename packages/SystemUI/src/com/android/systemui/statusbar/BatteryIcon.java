/*
* Copyright (C) 2011 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;
import android.os.SystemClock;
import android.os.BatteryManager;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import java.lang.Runnable;

import com.android.internal.R;

public class BatteryIcon extends ImageView {

    static final int BATTERY_ICON_WIDTH_DIP = 4;
    static final int BATTERY_ICON_MARGIN_RIGHT_DIP = 6;
    private static final int BATTERY_STYLE_PERCENT = 1;
    private int mAnimDuration = 500;
    private int mBatteryLevel = 0;
    private int mStatusBarBattery;
    private boolean mBatteryCharging = false;

    private int mWidthPx;
    private int mMarginRightPx;
    private int mCurrentFrame = 0;
    private boolean mAttached;
    private Matrix mMatrix = new Matrix();
    private Paint mPaint = new Paint();
    private Handler mHandler;
    private float mDensity;
    private transient Bitmap[] mIconCache;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.STATUS_BAR_BATTERY), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    final Runnable onFakeTimer = new Runnable() {
        public void run() {
            ++mCurrentFrame;

            if (mCurrentFrame > 10)
                mCurrentFrame = mBatteryLevel / 10;

            invalidate();
            mHandler.postDelayed(onFakeTimer, mAnimDuration);
        }
    };

    public BatteryIcon(Context context) {
        this(context, null);
    }

    public BatteryIcon(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryIcon(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mHandler = new Handler();
        SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        settingsObserver.observe();
        Resources r = getResources();

        mDensity = r.getDisplayMetrics().density;
        mWidthPx = (int) (BATTERY_ICON_WIDTH_DIP * mDensity);
        mMarginRightPx = (int) (BATTERY_ICON_MARGIN_RIGHT_DIP * mDensity);

        updateIconCache();
        updateSettings();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (!mAttached) {
            mAttached = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_BATTERY_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAttached) {
            getContext().unregisterReceiver(mIntentReceiver);
            mAttached = false;
        }
    }

    protected void updateAnimDuration() {
        mAnimDuration = 200 + mBatteryLevel * 5;
    }

    private void startTimer() {
        mHandler.removeCallbacks(onFakeTimer);
        updateAnimDuration();
        mCurrentFrame = mBatteryLevel / 10;
        invalidate();
        mHandler.postDelayed(onFakeTimer, mAnimDuration);
    }

    private void stopTimer() {
        mHandler.removeCallbacks(onFakeTimer);
        mCurrentFrame = mBatteryLevel / 10;
        invalidate();
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                mBatteryLevel = intent.getIntExtra("level", 0);
                boolean oldChargingState = mBatteryCharging;

                mBatteryCharging = intent.getIntExtra("status", 0)
                 == BatteryManager.BATTERY_STATUS_CHARGING;

                if (mBatteryCharging && mBatteryLevel < 100) {
                    if (!oldChargingState)
                        startTimer();
                    if(mBatteryLevel % 10 == 0)
                        updateAnimDuration();
                } else {
                    stopTimer();
                }
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int height = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(mWidthPx + mMarginRightPx, height);
        updateMatrix();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (!mAttached || mStatusBarBattery != BATTERY_STYLE_PERCENT)
            return;

        canvas.drawBitmap(mIconCache[mCurrentFrame], mMatrix, mPaint);
    }

    private int getBatResourceID(int level) {
        switch (level) {
            case 0:
                return R.drawable.stat_sys_battery_5;
            case 1:
                return R.drawable.stat_sys_battery_10;
            case 2:
                return R.drawable.stat_sys_battery_20;
            case 3:
                return R.drawable.stat_sys_battery_30;
            case 4:
                return R.drawable.stat_sys_battery_40;
            case 5:
                return R.drawable.stat_sys_battery_50;
            case 6:
                return R.drawable.stat_sys_battery_60;
            case 7:
                return R.drawable.stat_sys_battery_70;
            case 8:
                return R.drawable.stat_sys_battery_80;
            case 9:
                return R.drawable.stat_sys_battery_90;
            case 10:
            default:
                return R.drawable.stat_sys_battery_100;
        }
    }

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();

        int statusBarBattery = (Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY, 1));
        mStatusBarBattery = Integer.valueOf(statusBarBattery);

        if (mStatusBarBattery == BATTERY_STYLE_PERCENT) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }

    public void updateIconCache() {
        mIconCache=new Bitmap[11];

        for(int i=0; i<=10; i++){
            Bitmap bmBat = getBitmapFor(getBatResourceID(i));
            mIconCache[i] = Bitmap.createBitmap(bmBat, 4, 0, 1, bmBat.getHeight());
        }
    }

    public void updateMatrix() {
        mMatrix.reset();
        mMatrix.setTranslate(0, 0);
        float scaleFactor=getHeight()/(float)(mIconCache[0].getHeight());
        mMatrix.postScale(mWidthPx, scaleFactor);
    }
}
