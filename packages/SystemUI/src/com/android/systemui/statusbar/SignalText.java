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

import java.util.Calendar;
import java.util.TimeZone;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.CharacterStyle;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class SignalText extends TextView {

    int dBm = 0;
    int ASU = 0;
    Handler mHandler;
    private boolean mAttached;
    private SignalStrength signal;
    private int mValueColor = 0xffffffff;

    private static int style;
    private static final int STYLE_HIDE = 0;
    private static final int STYLE_SHOW = 1;
    private static final int STYLE_SHOW_DBM = 2;
    
    public SignalText(Context context) {
        this(context, null);
    }

    public SignalText(Context context, AttributeSet attrs) {
        super(context, attrs);

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
            filter.addAction(Intent.ACTION_SIGNAL_DBM_CHANGED);
            getContext().registerReceiver(mIntentReceiver, filter, null, getHandler());
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mAttached) {
            mAttached = false;
        }
    }
    
    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_SIGNAL_DBM_CHANGED)) {
                dBm = intent.getIntExtra("dbm", 0);
            }

            updateSettings();
        }
    };

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.STATUS_BAR_SIGNAL_TEXT), false, this);
            resolver.registerContentObserver(Settings.System.getUriFor(
                    Settings.System.PHONE_DBM_LEVEL), false, this);        
            resolver.registerContentObserver(Settings.System.getUriFor(
					Settings.System.COLOR_SIGNALTEXT_VALUE), false, this);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateSettings();
        }
    }

    private void updateSettings() {
        updateSignalText();
    }

    final void updateSignalText() {
        ContentResolver resolver = mContext.getContentResolver();

        style = Settings.System
                .getInt(resolver, Settings.System.STATUS_BAR_SIGNAL_TEXT, STYLE_HIDE);

        updateColor();

        if (style == STYLE_SHOW) {
            this.setVisibility(View.VISIBLE);
            String result = Integer.toString(dBm);
            setText(result + " ");
        } else if (style == STYLE_SHOW_DBM) {
            this.setVisibility(View.VISIBLE);
            String result = Integer.toString(dBm) + " dBm ";
            SpannableStringBuilder formatted = new SpannableStringBuilder(result);
            int start = result.indexOf("d");
            CharacterStyle style = new RelativeSizeSpan(0.7f);
            formatted.setSpan(style, start, start + 3, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            setText(formatted);
        } else {
            this.setVisibility(View.GONE);
        }
    }

    private void updateColor() {
		ContentResolver resolver = mContext.getContentResolver();

        mValueColor = Settings.System
				.getInt(resolver, Settings.System.COLOR_SIGNALTEXT_VALUE, mValueColor);

       	setTextColor(mValueColor);
    }
}
