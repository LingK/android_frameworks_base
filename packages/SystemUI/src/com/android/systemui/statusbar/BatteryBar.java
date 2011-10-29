/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.BatteryManager;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ProgressBar;

public class BatteryBar extends ProgressBar {

    private static final String TAG = "BatteryBar";
    private static final int BATTERY_STYLE_BAR = 2;
    private Integer mColor = null;
    private boolean mAttached = false;
    private boolean mShowLowBattery = true;
    private boolean mBatteryCharging = false;

    private int mStatusBarBattery;
    private int mBatteryLevel = 0;
    private int mChargingLevel = -1;
    private int mAnimDuration = 500;
    private int mLowBatteryWarningLevel;
    private int mLowBatteryCloseWarningLevel;
    private Handler mHandler = new Handler();

    class SettingsObserver extends ContentObserver {

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        void observer() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_BATTERY), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_BATTERY_COLOR), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
            Settings.System.STATUS_BAR_BATTERY_LOW_BATT), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    private final Runnable onFakeTimer = new Runnable() {
        @Override
        public void run() {
            if (mChargingLevel > -1) {
                if (mChargingLevel < 20) {
                    mChargingLevel += mChargingLevel % 5;
                } else if (mChargingLevel < 90) {
                    mChargingLevel += mChargingLevel % 10;
                }

                setProgress(mChargingLevel);

                if (mChargingLevel >= 100) {
                    mChargingLevel = mBatteryLevel;
                } else {
                    if (mChargingLevel < 20) {
                        mChargingLevel += 5;
                    } else {
                        mChargingLevel += 10;
                    }
                }

                invalidate();
                mHandler.postDelayed(onFakeTimer, mAnimDuration);
            }
        }
    };

    public BatteryBar(Context context) {
        this(context, null);
    }

    public BatteryBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        Drawable d = getProgressDrawable();

        if (d instanceof LayerDrawable) {
            Drawable background = ((LayerDrawable) d)
                    .findDrawableByLayerId(com.android.internal.R.id.background);

            if (background != null) {
                background.mutate();
                background.setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC);
            }
        }

        mLowBatteryWarningLevel = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lowBatteryWarningLevel);
        mLowBatteryCloseWarningLevel = mContext.getResources().getInteger(
                com.android.internal.R.integer.config_lowBatteryCloseWarningLevel);

        SettingsObserver observer = new SettingsObserver(mHandler);
        observer.observer();
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
                mBatteryCharging = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0) 
                    == BatteryManager.BATTERY_STATUS_CHARGING;

                if (mBatteryCharging && mBatteryLevel < 100) {
                    startTimer();

                    if (mBatteryLevel % 10 == 0) {
                        updateAnimDuration();
                    }
                } else {
                    stopTimer();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                stopTimer();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                if (mBatteryCharging && mBatteryLevel < 100) {
                    startTimer();
                }
            }
        }
    };

    @Override
    public synchronized void setProgress(int progress) {
        super.setProgress(progress);
        Drawable d = getProgressDrawable();

        if (d instanceof LayerDrawable) {
            Drawable progressBar = ((LayerDrawable) d)
                    .findDrawableByLayerId(com.android.internal.R.id.progress);

            if (progressBar != null) {
                progressBar.mutate();
                if (mShowLowBattery && progress <= mLowBatteryWarningLevel) {
                    progressBar.setColorFilter(Color.RED, PorterDuff.Mode.SRC);
                } else if (mShowLowBattery && progress <= mLowBatteryCloseWarningLevel) {
                    progressBar.setColorFilter(Color.YELLOW, PorterDuff.Mode.SRC);
                } else if (mColor != null) {
                    progressBar.setColorFilter(mColor, PorterDuff.Mode.SRC);
                } else {
                    progressBar.clearColorFilter();
                }
            }
        }
    }

    private void updateAnimDuration() {
        mAnimDuration = 200 + (mBatteryLevel / 10) * 50;
    }

    private void startTimer() {
        if (mChargingLevel == -1) {
            mHandler.removeCallbacks(onFakeTimer);
            updateAnimDuration();
            mChargingLevel = mBatteryLevel;
            invalidate();
            mHandler.postDelayed(onFakeTimer, mAnimDuration);
        }
    }

    private void stopTimer() {
        mHandler.removeCallbacks(onFakeTimer);
        setProgress(mBatteryLevel);
        mChargingLevel = -1;
        invalidate();
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();

        int statusBarBattery = (Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY, 1));
        mStatusBarBattery = Integer.valueOf(statusBarBattery);

        if (mStatusBarBattery == BATTERY_STYLE_BAR) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }

        mShowLowBattery = (Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BATTERY_LOW_BATT, 1) == 1);

        String color = Settings.System.getString(resolver,
                Settings.System.STATUS_BAR_BATTERY_COLOR);

        if (!TextUtils.isEmpty(color)) {
            try {
                mColor = Color.parseColor(color);
            } catch (IllegalArgumentException e) {
                mColor = null;
            }
        } else {
            mColor = null;
        }

        if (mBatteryCharging && mBatteryLevel < 100) {
            startTimer();
        } else {
            stopTimer();
        }
    }
}
