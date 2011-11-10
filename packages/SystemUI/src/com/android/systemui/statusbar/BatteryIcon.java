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
import android.graphics.drawable.Animatable;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.BatteryManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.android.internal.R;

public class BatteryIcon extends ImageView implements Animatable, Runnable {

    private static final String TAG = BatteryIcon.class.getSimpleName();
    private static final int iconWidthDp = 4;
    private static final int iconRightDp = 6;
    private static final int ANIM_DURATION = 5000;
    private static final int FRAME_DURATION = ANIM_DURATION / 100;

    private int mWidthPx;
    private boolean mAttached;
    private int mMarginRightPx;
    private int mBatteryLevel = 0;
    private int mCurrentFrame = 0;
    private boolean mShowBatteryIcon = false;
    private boolean mBatteryCharging = false;
    
    private float mDensity;
    private Handler mHandler;
    private Paint mPaint = new Paint();
    private Matrix mMatrix = new Matrix();
    private transient Bitmap[] mIconCache;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_BATTERY), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

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
        mWidthPx = (int) (iconWidthDp * mDensity);
        mMarginRightPx = (int) (iconRightDp * mDensity);
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
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(Intent.ACTION_SCREEN_ON);
            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAttached) {
            mAttached = false;
            getContext().unregisterReceiver(mIntentReceiver);
        }
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                mBatteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                mBatteryCharging = intent.getIntExtra(
                        BatteryManager.EXTRA_STATUS, 0) == BatteryManager.BATTERY_STATUS_CHARGING;
                if (mBatteryCharging && mBatteryLevel < 100) {
                    start();
                } else {
                    stop();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                stop();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                if (mBatteryCharging && mBatteryLevel < 100) {
                    start();
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

        if (!mAttached || !mShowBatteryIcon)
            return;

        canvas.drawBitmap(mIconCache[mCurrentFrame], mMatrix, mPaint);
    }

    public void updateIconCache() {
        mIconCache=new Bitmap[21];

        for(int i=0; i<=20; i++){
            Bitmap bmBat = getBitmapFor(getBatResourceID(i));
            mIconCache[i] = Bitmap.createBitmap(bmBat, 4, 0, 1, bmBat.getHeight());
        }
    }

    public void updateMatrix() {
        mMatrix.reset();
        mMatrix.setTranslate(0, 0);
        float scaleFactor = getHeight() / (float)(mIconCache[0].getHeight());
        mMatrix.postScale(mWidthPx, scaleFactor);
    }

    private int getBatResourceID(int level) {
        switch (level) {
            case 1:
                return R.drawable.stat_sys_battery_5;
            case 2:
                return R.drawable.stat_sys_battery_10;
            case 3:
                return R.drawable.stat_sys_battery_15;
            case 4:
                return R.drawable.stat_sys_battery_20;
            case 5:
                return R.drawable.stat_sys_battery_25;
            case 6:
                return R.drawable.stat_sys_battery_30;
            case 7:
                return R.drawable.stat_sys_battery_35;
            case 8:
                return R.drawable.stat_sys_battery_40;
            case 9:
                return R.drawable.stat_sys_battery_45;
            case 10:
                return R.drawable.stat_sys_battery_50;
            case 11:
                return R.drawable.stat_sys_battery_55;
            case 12:
                return R.drawable.stat_sys_battery_60;
            case 13:
                return R.drawable.stat_sys_battery_65;
            case 14:
                return R.drawable.stat_sys_battery_70;
            case 15:
                return R.drawable.stat_sys_battery_75;
            case 16:
                return R.drawable.stat_sys_battery_80;
            case 17:
                return R.drawable.stat_sys_battery_85;
            case 18:
                return R.drawable.stat_sys_battery_90;
            case 19:
                return R.drawable.stat_sys_battery_95;
            default:
                return R.drawable.stat_sys_battery_100;
        }
    }

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();
        mShowBatteryIcon = (Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY, 0) == 1);

        if (mShowBatteryIcon) {
            setVisibility(View.VISIBLE);
        } else {
            setVisibility(View.GONE);
        }
    }

    @Override
    public void run() {
        mCurrentFrame++;
        if (mCurrentFrame > 5) {
            mCurrentFrame = mBatteryLevel / 5;
        }
        mHandler.postDelayed(this, FRAME_DURATION);
    }

    @Override
    public void start() {
        if (!isRunning()) {
            mHandler.removeCallbacks(this);
            mCurrentFrame = mBatteryLevel / 5;
            mHandler.postDelayed(this, FRAME_DURATION);
        }
    }

    @Override
    public void stop() {
        if (isRunning()) {
            mHandler.removeCallbacks(this);
            mCurrentFrame = mBatteryLevel / 5;
        }
    }

    @Override
    public boolean isRunning() {
        return mCurrentFrame != -1;
    }
}
