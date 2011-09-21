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
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class BatteryText extends TextView {
    private boolean mAttached;
    private boolean mShowBattery;
    private int mPercentColor = 0xffffffff;
    Handler mHandler;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
					Settings.System.STATUS_BAR_BATTERY), false, this);
			resolver.registerContentObserver(Settings.System.getUriFor(
					Settings.System.COLOR_BATTERY_PERCENT), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    public BatteryText(Context context) {
        this(context, null);
    }

    public BatteryText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BatteryText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mHandler = new Handler();
        SettingsObserver settingsObserver = new SettingsObserver(mHandler);
        settingsObserver.observe();
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

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                updateBatteryText(intent);
            }
        }
    };

    final void updateBatteryText(Intent intent) {
        int level = intent.getIntExtra("level", 0);
        setText(Integer.toString(level));
    }

    private void updateSettings() {
        ContentResolver resolver = mContext.getContentResolver();

        mShowBattery = (Settings.System
                .getInt(resolver, Settings.System.STATUS_BAR_BATTERY, 0) == 1);
                
        updateColor();
        
        if (mShowBattery)
            setVisibility(View.VISIBLE);
        else
            setVisibility(View.GONE);
    }

    private void updateColor() {
		ContentResolver resolver = mContext.getContentResolver();

        mPercentColor = Settings.System
				.getInt(resolver, Settings.System.COLOR_BATTERY_PERCENT, mPercentColor);

       	setTextColor(mPercentColor);
        refreshDrawableState();
    }
}
