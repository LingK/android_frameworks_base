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
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class CmBatteryBar extends ProgressBar {

    private static final String TAG = "CmBatteryBar";
    private boolean mAttached = false;
    private boolean mShowCmBatteryBar = false;
    private int mBatteryLevel = 0;
    private int mChargingLevel = -1;
    private int mAnimDuration = 500;
    private boolean mBatteryPlugged = false;
    private Handler mHandler = new Handler();

    class SettingsObserver extends ContentObserver {
        public SettingsObserver(Handler handler) {
            super(handler);
        }

        void observer() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.STATUS_BAR_CM_BATTERY), false, this);
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
                setProgress(mChargingLevel);
                if (mChargingLevel >= 100) {
                    mChargingLevel = mBatteryLevel;
                } else {
                    mChargingLevel += 10;
                }
                invalidate();
                mHandler.postDelayed(onFakeTimer, mAnimDuration);
            }
        }
    };

    public CmBatteryBar(Context context) {
        this(context, null);
    }

    public CmBatteryBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CmBatteryBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

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
                mBatteryLevel = intent.getIntExtra("level", 0);

                boolean oldBatteryPlugged = mBatteryPlugged;
                mBatteryPlugged = intent.getIntExtra("plugged", 0) != 0;
                if (mBatteryPlugged && mBatteryLevel < 100) {
                    if (!oldBatteryPlugged) {
                        startTimer();
                    }
                    if (mBatteryLevel % 10 == 0) {
                        updateAnimDuration();
                    }
                } else {
                    stopTimer();
                }
            } else if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                stopTimer();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                if (mBatteryPlugged && mBatteryLevel < 100) {
                    startTimer();
                }
            }
        }
    };

    private void updateAnimDuration() {
        mAnimDuration = 200 + (mBatteryLevel / 10) * 50;
    }

    private void startTimer() {
        mHandler.removeCallbacks(onFakeTimer);
        updateAnimDuration();
        mChargingLevel = mBatteryLevel;
        invalidate();
        mHandler.postDelayed(onFakeTimer, mAnimDuration);
    }

    private void stopTimer() {
        mHandler.removeCallbacks(onFakeTimer);
        setProgress(mBatteryLevel);
        mChargingLevel = -1;
        invalidate();
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();
        mShowCmBatteryBar = (Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_CM_BATTERY, 0) == 2);
        if (mShowCmBatteryBar) {
            setVisibility(VISIBLE);
        } else {
            setVisibility(GONE);
        }
    }
}
