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

import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.provider.CmSystem;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Slog;
import android.view.IWindowManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.systemui.R;

public class CmStatusBarView extends StatusBarView {
    private static final boolean DEBUG = false;
    private static final String TAG = "CmStatusBarView";
    public static final int KEYCODE_VIRTUAL_HOME_LONG=KeyEvent.getMaxKeyCode()+1;
    public static final int KEYCODE_VIRTUAL_BACK_LONG=KeyEvent.getMaxKeyCode()+2;

    ViewGroup mSoftButtons;
    ImageButton mHomeButton;
    ImageButton mMenuButton;
    ImageButton mBackButton;
    ImageButton mSearchButton;
    ImageButton mQuickNaButton;
    ImageButton mHideButton;
    ImageButton mEdgeLeft;
    ImageButton mEdgeRight;
    ImageButton mSeperator1;
    ImageButton mSeperator2;
    ImageButton mSeperator3;
    ImageButton mSeperator4;
    ImageButton mSeperator5;

    boolean mHasSoftButtons;
    boolean mIsBottom;
    boolean mIsLeft;
    boolean mShowHome;
    boolean mShowMenu;
    boolean mShowBack;
    boolean mShowSearch;
    boolean mShowQuickNa;
    ViewGroup mIcons;

    ActivityManager mActivityManager;
    RunningAppProcessInfo mFsCallerProcess;
    ComponentName mFsCallerActivity;
    Intent mFsForceIntent;
    Intent mFsOffIntent;
    Handler mHandler;

    class FullscreenReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent){
            onFullscreenAttempt();
        }
    }

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.STATUS_BAR_BOTTOM), false, this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SOFT_BUTTONS_LEFT), false, this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SOFT_BUTTON_SHOW_HOME), false, this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SOFT_BUTTON_SHOW_MENU), false, this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SOFT_BUTTON_SHOW_BACK), false, this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SOFT_BUTTON_SHOW_SEARCH), false, this);
            resolver.registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SOFT_BUTTON_SHOW_QUICK_NA), false, this);
            onChange(true);
        }

        @Override
        public void onChange(boolean selfChange) {
            ContentResolver resolver = mContext.getContentResolver();
            int defValue;

            defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_BOTTOM_STATUS_BAR) ? 1 : 0);
            mIsBottom = (Settings.System.getInt(resolver,
                    Settings.System.STATUS_BAR_BOTTOM, defValue) == 1);
            defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_SOFT_BUTTONS_LEFT) ? 1 : 0);
            mIsLeft = (Settings.System.getInt(resolver,
                    Settings.System.SOFT_BUTTONS_LEFT, defValue) == 1);
            defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_SHOW_SOFT_HOME) ? 1 : 0);
            mShowHome = (Settings.System.getInt(resolver,
                    Settings.System.SOFT_BUTTON_SHOW_HOME, defValue) == 1);
            defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_SHOW_SOFT_MENU) ? 1 : 0);
            mShowMenu = (Settings.System.getInt(resolver,
                    Settings.System.SOFT_BUTTON_SHOW_MENU, defValue) == 1);
            defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_SHOW_SOFT_BACK) ? 1 : 0);
            mShowBack = (Settings.System.getInt(resolver,
                    Settings.System.SOFT_BUTTON_SHOW_BACK, defValue) == 1);
            defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_SHOW_SOFT_SEARCH) ? 1 : 0);
            mShowSearch = (Settings.System.getInt(resolver,
                    Settings.System.SOFT_BUTTON_SHOW_SEARCH, defValue) == 1);
            defValue=(CmSystem.getDefaultBool(mContext, CmSystem.CM_DEFAULT_SHOW_SOFT_QUICK_NA) ? 1 : 0);
            mShowQuickNa = (Settings.System.getInt(resolver,
                    Settings.System.SOFT_BUTTON_SHOW_QUICK_NA, defValue) == 1);
            updateSoftButtons();
            updateQuickNaImage();
        }
    }

    public CmStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate(){
        super.onFinishInflate();

        mHasSoftButtons = CmSystem.getDefaultBool(mContext, CmSystem.CM_HAS_SOFT_BUTTONS);
        mHandler=new Handler();
        mSoftButtons = (ViewGroup)findViewById(R.id.buttons);

        if(!mHasSoftButtons)
            mSoftButtons.setVisibility(View.GONE);

        if (mHasSoftButtons) {
            mHomeButton = (ImageButton)findViewById(R.id.status_home);
            mHomeButton.setOnClickListener(
                new ImageButton.OnClickListener() {
                    public void onClick(View v) {
                        if(mService.mExpanded)
                            mQuickNaButton.performClick();
                        if(DEBUG) Slog.i(TAG, "Home clicked");
                        Intent setIntent = new Intent(Intent.ACTION_MAIN);
                        setIntent.addCategory(Intent.CATEGORY_HOME);
                        setIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        getContext().startActivity(setIntent);
                    }
                }
            );
            mHomeButton.setOnLongClickListener(
                new ImageButton.OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        simulateKeypress(KEYCODE_VIRTUAL_HOME_LONG);
                        return true;
                    }
                }
            );
            mMenuButton = (ImageButton)findViewById(R.id.status_menu);
            mMenuButton.setOnClickListener(
                new ImageButton.OnClickListener() {
                    public void onClick(View v) {
                        if(DEBUG) Slog.i(TAG, "Menu clicked");
                        simulateKeypress(KeyEvent.KEYCODE_MENU);
                    }
                }
            );
            mBackButton = (ImageButton)findViewById(R.id.status_back);
            mBackButton.setOnClickListener(
                new ImageButton.OnClickListener() {
                    public void onClick(View v) {
                        if(DEBUG) Slog.i(TAG, "Back clicked");
                        simulateKeypress(KeyEvent.KEYCODE_BACK);
                    }
                }
            );
            mBackButton.setOnLongClickListener(
                    new ImageButton.OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            simulateKeypress(KEYCODE_VIRTUAL_BACK_LONG);
                            return true;
                        }
                    }
                );
            mSearchButton = (ImageButton)findViewById(R.id.status_search);
            mSearchButton.setOnClickListener(
                new ImageButton.OnClickListener() {
                    public void onClick(View v) {
                        if(DEBUG) Slog.i(TAG, "Search clicked");
                        CmStatusBarView.this.simulateKeypress(KeyEvent.KEYCODE_SEARCH);
                    }
                }
            );
            mSearchButton.setOnLongClickListener(
                    new ImageButton.OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            boolean mCustomLongSearchAppToggle=(Settings.System.getInt(getContext().getContentResolver(),
                                    Settings.System.USE_CUSTOM_LONG_SEARCH_APP_TOGGLE, 0) == 1);

                            if(mCustomLongSearchAppToggle){
                                runCustomApp(Settings.System.getString(getContext().getContentResolver(),
                                    Settings.System.USE_CUSTOM_LONG_SEARCH_APP_ACTIVITY));
                            }
                            return true;
                        }
                    }
                );
            mQuickNaButton = (ImageButton)findViewById(R.id.status_quick_na);
            mQuickNaButton.setOnClickListener(
                new ImageButton.OnClickListener() {
                    public void onClick(View v) {
                        if(mService.mExpanded){
                            mService.animateCollapse();
                            mService.performCollapse();
                        }else {
                            mService.performExpand();
                        }
                        if(DEBUG) Slog.i(TAG, "Quick Notification Area clicked");
                    }
                }
            );
            mHideButton = (ImageButton)findViewById(R.id.status_hide);
            mHideButton.setOnClickListener(
                new ImageButton.OnClickListener() {
                    public void onClick(View v) {
                        if(isStillActive(mFsCallerProcess, mFsCallerActivity))
                            mContext.sendBroadcast(mFsForceIntent);
                        if(DEBUG) Slog.i(TAG, "Fullscreen Hide clicked");
                    }
                }
            );

            mEdgeLeft = (ImageButton)findViewById(R.id.status_edge_left);
            mEdgeRight = (ImageButton)findViewById(R.id.status_edge_right);
            mSeperator1 = (ImageButton)findViewById(R.id.status_sep1);
            mSeperator2 = (ImageButton)findViewById(R.id.status_sep2);
            mSeperator3 = (ImageButton)findViewById(R.id.status_sep3);
            mSeperator4 = (ImageButton)findViewById(R.id.status_sep4);
            mSeperator5 = (ImageButton)findViewById(R.id.status_sep5);
            mIcons = (ViewGroup)findViewById(R.id.icons);

            SettingsObserver settingsObserver = new SettingsObserver(mHandler);
            settingsObserver.observe();

            FullscreenReceiver fullscreenReceiver = new FullscreenReceiver();
            mContext.registerReceiver(fullscreenReceiver, new IntentFilter("android.intent.action.FULLSCREEN_ATTEMPT"));
            mFsForceIntent=new Intent("android.intent.action.FORCE_FULLSCREEN");
            mFsOffIntent=new Intent("android.intent.action.FULLSCREEN_REAL_OFF");
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateQuickNaImage();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event){
        boolean skipService=
            (isEventInButton(mHomeButton, event)
                || isEventInButton(mMenuButton, event)
                || isEventInButton(mBackButton, event)
                || isEventInButton(mSearchButton, event)
                || isEventInButton(mQuickNaButton, event));

        return super.onInterceptTouchEvent(event, skipService);
    }
    private boolean isEventInButton(final ImageButton button, final MotionEvent event) {
        return mHasSoftButtons && button != null
            && button.getLeft() <= event.getRawX()
            && button.getRight() >= event.getRawX()
            && button.getTop() <= event.getRawY()
            && button.getBottom() >= event.getRawY();
        }

    public void updateQuickNaImage(){
        if(!mHasSoftButtons || getParent()==null)
            return;

        int resCollapsed=mIsBottom ? R.drawable.ic_statusbar_na_up_bottom : R.drawable.ic_statusbar_na_down_top;
        int resExpanded=mIsBottom ? R.drawable.ic_statusbar_na_down_bottom : R.drawable.ic_statusbar_na_up_top;
        int resUse=mService.mExpanded ? resExpanded : resCollapsed;
        mQuickNaButton.setBackgroundResource(resUse);
    }

    public boolean onTouchEvent(final MotionEvent event){
        if(!mHasSoftButtons)
            return super.onTouchEvent(event);

        if(isEventInButton(mHomeButton, event)) {
            mHomeButton.onTouchEvent(event);
            return true;
        }
        if(isEventInButton(mMenuButton, event)) {
            mMenuButton.onTouchEvent(event);
            return true;
        }
        if(isEventInButton(mBackButton, event)) {
            mBackButton.onTouchEvent(event);
            return true;
        }
        if(isEventInButton(mSearchButton, event)) {
            mSearchButton.onTouchEvent(event);
            return true;
        }
        if(isEventInButton(mMenuButton, event)) {
            mQuickNaButton.onTouchEvent(event);
            return true;
        }

        return super.onTouchEvent(event);
    }

    HideButtonEnabler mHideButtonEnabler = new HideButtonEnabler();
    public class HideButtonEnabler implements Runnable {
        public void run(){
            Entry e=null;;

            if(mKnownCallers.isEmpty()){
                if(DEBUG) Slog.i(TAG, "HideButtonEnabler - mKnownCallers is empty - stopping to monitor active app");
                return;
            }

            RunningAppProcessInfo current=getForegroundApp();
            if(current!=null)
                e=getEntryByPid(current.pid);
            if(e!=null){
                ComponentName c=getActivityForApp(current);
                if(c==null || (c.compareTo(e.Activity)==0)){
                    if(DEBUG) Slog.i(TAG, "HideButtonEnabler - currentFgApp[NAME/PID/ACTIVITY]: " + current.processName +
                            "/" + current.pid + "/" + c.toShortString() + " Retrieved Entry[NAME/PID/ACTIVITY]:"
                            + e.ProcessInfo.processName + "/" + e.ProcessInfo.pid + "/" + e.Activity.toShortString());
                    onFullscreenAttempt();
                    return;
                }
            }

            mHandler.removeCallbacks(mHideButtonEnabler);
            mHandler.postDelayed(mHideButtonEnabler, 500);
        }

        private class Entry{
            public RunningAppProcessInfo ProcessInfo;
            public ComponentName Activity;

            Entry(RunningAppProcessInfo pProcessInfo, ComponentName pActivity){
                ProcessInfo=pProcessInfo;
                Activity=pActivity;
            }
        }
        private List <Entry> mKnownCallers=new LinkedList <Entry>();

        public void addFsCaller(RunningAppProcessInfo processInfo, ComponentName activity){
            Entry e=new Entry(processInfo, activity);
            if(!mKnownCallers.contains(e))
                mKnownCallers.add(e);
        }

        private Entry getEntryByPid(int pid){
            Iterator <Entry> i=mKnownCallers.iterator();
            Entry e;

            while(i.hasNext()){
                e=i.next();

                if(e.ProcessInfo.pid == pid)
                    return e;

                if(!isPidRunning(e.ProcessInfo.pid))
                    i.remove();
            }

            return null;
        }

        private boolean isPidRunning(int pid){
            if(mActivityManager==null)
                mActivityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);

            List <RunningAppProcessInfo> l = mActivityManager.getRunningAppProcesses();
            Iterator <RunningAppProcessInfo> i = l.iterator();
            RunningAppProcessInfo info;
            while(i.hasNext()){
                info = i.next();
                if(info.pid == pid)
                    return true;
            }

            return false;
        }
    }

    HideButtonDisabler mHideButtonDisabler = new HideButtonDisabler();
    public class HideButtonDisabler implements Runnable {
        private boolean mWasInactiveLastCall;

        public void run() {
            boolean appStillForeground=false;

            if(mFsCallerProcess==null){
                mFsCallerProcess=getForegroundApp();
                Slog.i(TAG, "Fullscreen Attempt ProcessName: " + (mFsCallerProcess==null ? "null" : mFsCallerProcess.processName));
                mFsCallerActivity=getActivityForApp(mFsCallerProcess);
                Slog.i(TAG, "Fullscreen Attempt Top Activity: " + (mFsCallerActivity==null ? "null" : mFsCallerActivity.flattenToShortString()));

                if(mFsCallerProcess!=null)
                    mHideButtonEnabler.addFsCaller(mFsCallerProcess, mFsCallerActivity);

                mWasInactiveLastCall=false;
            }

            if(isStillActive(mFsCallerProcess, mFsCallerActivity)){
                appStillForeground=true;
                mWasInactiveLastCall=false;
            }

            if(appStillForeground==false && mWasInactiveLastCall==false){
                mWasInactiveLastCall=true;
                mHandler.postDelayed(mHideButtonDisabler, 250);
                return;
            }

            if(appStillForeground)
                mHandler.postDelayed(mHideButtonDisabler, 500);
            else{
                mContext.sendBroadcast(mFsOffIntent);
                mFsCallerProcess=null;
                mHideButton.setVisibility(View.GONE);
                mSeperator5.setVisibility(View.GONE);
                updateSoftButtons();
                mHandler.removeCallbacks(mHideButtonEnabler);
                mHandler.removeCallbacks(mHideButtonDisabler);
                mHandler.postDelayed(mHideButtonEnabler, 500);
            }
        }
    };

    private boolean isStillActive(RunningAppProcessInfo process, ComponentName activity)
    {
        if(process==null)
            return false;

        RunningAppProcessInfo currentFg=getForegroundApp();
        ComponentName currentActivity=getActivityForApp(currentFg);

        if(currentFg==null){
            if(DEBUG) Slog.i(TAG, "isStillActive() returned no foreground activity. this is impossible and so ignored");
            return true;
        }

        if(currentFg!=null && currentFg.processName.equals(process.processName) &&
                (activity==null || currentActivity.compareTo(activity)==0))
            return true;

        Slog.i(TAG, "isStillActive returns false - CallerProcess: " + process.processName + " CurrentProcess: "
                + (currentFg==null ? "null" : currentFg.processName) + " CallerActivity:" + (activity==null ? "null" : activity.toString())
                + " CurrentActivity: " + (currentActivity==null ? "null" : currentActivity.toString()));
        return false;
    }

    private ComponentName getActivityForApp(RunningAppProcessInfo target){
        ComponentName result=null;
        ActivityManager.RunningTaskInfo info;

        if(target==null)
            return null;

        if(mActivityManager==null)
            mActivityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List <ActivityManager.RunningTaskInfo> l = mActivityManager.getRunningTasks(9999);
        Iterator <ActivityManager.RunningTaskInfo> i = l.iterator();

        while(i.hasNext()){
            info=i.next();
            if(target.processName.contains(info.baseActivity.getPackageName())){
                if(DEBUG) Slog.i(TAG, "getActivityForApp(" + target.processName + ") found the following activity (topActivity /// baseActivity): "
                        + info.topActivity.toString() + " /// " + info.baseActivity.toString());
                result=info.topActivity;
                break;
            }
        }

        return result;
    }

    private RunningAppProcessInfo getForegroundApp() {
        RunningAppProcessInfo result=null, info=null;

        if(mActivityManager==null)
            mActivityManager = (ActivityManager)mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List <RunningAppProcessInfo> l = mActivityManager.getRunningAppProcesses();
        Iterator <RunningAppProcessInfo> i = l.iterator();
        while(i.hasNext()){
            info = i.next();
            if(info.uid >= Process.FIRST_APPLICATION_UID && info.uid <= Process.LAST_APPLICATION_UID
                    && info.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                result=info;
                break;
            }
        }
        return result;
    }

    private void onFullscreenAttempt()
    {
        if(mShowQuickNa || mShowSearch || mShowBack || mShowMenu || mShowHome)
            mSeperator5.setVisibility(View.VISIBLE);

        mHideButton.setVisibility(View.VISIBLE);
        updateSoftButtons();

        mFsCallerProcess=null;
        mFsCallerActivity=null;
        mHandler.postDelayed(mHideButtonDisabler, 500);
    }

    private void updateSoftButtons() {
        if(!mHasSoftButtons)
            return;

        mIcons.removeView(mSoftButtons);
        mIcons.addView(mSoftButtons, mIsLeft ? 0 : mIcons.getChildCount());

        mEdgeLeft.setVisibility(mIsLeft ? View.GONE : View.VISIBLE);
        mEdgeRight.setVisibility(mIsLeft ? View.VISIBLE : View.GONE);

        mHomeButton.setVisibility(View.VISIBLE);
        mSeperator1.setVisibility(View.VISIBLE);
        mMenuButton.setVisibility(View.VISIBLE);
        mSeperator2.setVisibility(View.VISIBLE);
        mBackButton.setVisibility(View.VISIBLE);
        mSeperator3.setVisibility(View.VISIBLE);
        mSearchButton.setVisibility(View.VISIBLE);
        mSeperator4.setVisibility(View.VISIBLE);
        mQuickNaButton.setVisibility(View.VISIBLE);

        if(!mShowHome){
            mHomeButton.setVisibility(View.GONE);
            mSeperator1.setVisibility(View.GONE);
        }
        if(!mShowMenu){
            mMenuButton.setVisibility(View.GONE);
            mSeperator2.setVisibility(View.GONE);
        }
        if(!mShowBack){
            mBackButton.setVisibility(View.GONE);
            mSeperator3.setVisibility(View.GONE);
        }
        if(!mShowSearch){
            mSearchButton.setVisibility(View.GONE);
            mSeperator4.setVisibility(View.GONE);
        }
        if(!mShowQuickNa)
            mQuickNaButton.setVisibility(View.GONE);

        if(!mShowQuickNa)
            mSeperator4.setVisibility(View.GONE);
        if(!mShowQuickNa && !mShowSearch)
            mSeperator3.setVisibility(View.GONE);
        if(!mShowQuickNa && !mShowSearch && !mShowBack)
            mSeperator2.setVisibility(View.GONE);
        if(!mShowQuickNa && !mShowSearch && !mShowBack && !mShowMenu)
            mSeperator1.setVisibility(View.GONE);

        if(!mShowQuickNa && !mShowSearch && !mShowBack && !mShowMenu && !mShowHome && mHideButton.getVisibility()==View.GONE){
            mEdgeLeft.setVisibility(View.GONE);
            mEdgeRight.setVisibility(View.GONE);
        }

        if(mIsBottom){
            mEdgeLeft.setBackgroundResource(R.drawable.ic_statusbar_edge_right_bottom);
            mHomeButton.setBackgroundResource(R.drawable.ic_statusbar_home_bottom);
            mSeperator1.setBackgroundResource(R.drawable.ic_statusbar_sep_bottom);
            mMenuButton.setBackgroundResource(R.drawable.ic_statusbar_menu_bottom);
            mSeperator2.setBackgroundResource(R.drawable.ic_statusbar_sep_bottom);
            mBackButton.setBackgroundResource(R.drawable.ic_statusbar_back_bottom);
            mSeperator3.setBackgroundResource(R.drawable.ic_statusbar_sep_bottom);
            mSearchButton.setBackgroundResource(R.drawable.ic_statusbar_search_bottom);
            mSeperator4.setBackgroundResource(R.drawable.ic_statusbar_sep_bottom);
            mQuickNaButton.setBackgroundResource(R.drawable.ic_statusbar_na_up_bottom);
            mSeperator5.setBackgroundResource(R.drawable.ic_statusbar_sep_bottom);
            mHideButton.setBackgroundResource(R.drawable.ic_statusbar_hide_bottom);
            mEdgeRight.setBackgroundResource(R.drawable.ic_statusbar_edge_left_bottom);
        }else{
            mEdgeLeft.setBackgroundResource(R.drawable.ic_statusbar_edge_right_top);
            mHomeButton.setBackgroundResource(R.drawable.ic_statusbar_home_top);
            mSeperator1.setBackgroundResource(R.drawable.ic_statusbar_sep_top);
            mMenuButton.setBackgroundResource(R.drawable.ic_statusbar_menu_top);
            mSeperator2.setBackgroundResource(R.drawable.ic_statusbar_sep_top);
            mBackButton.setBackgroundResource(R.drawable.ic_statusbar_back_top);
            mSeperator3.setBackgroundResource(R.drawable.ic_statusbar_sep_top);
            mSearchButton.setBackgroundResource(R.drawable.ic_statusbar_search_top);
            mSeperator4.setBackgroundResource(R.drawable.ic_statusbar_sep_top);
            mQuickNaButton.setBackgroundResource(R.drawable.ic_statusbar_na_up_top);
            mSeperator5.setBackgroundResource(R.drawable.ic_statusbar_sep_top);
            mHideButton.setBackgroundResource(R.drawable.ic_statusbar_hide_top);
            mEdgeRight.setBackgroundResource(R.drawable.ic_statusbar_edge_left_top);
        }
    }

    public int getSoftButtonsWidth() {
        if(!mHasSoftButtons)
            return 0;

        int ret=0;
        if(mShowHome)
            ret+=mHomeButton.getWidth();
        if(mShowMenu)
            ret+=mMenuButton.getWidth()+mSeperator1.getWidth();
        if(mShowBack)
            ret+=mBackButton.getWidth()+mSeperator2.getWidth();
        if(mShowSearch)
            ret+=mSearchButton.getWidth()+mSeperator3.getWidth();
        if(mShowQuickNa)
            ret+=mQuickNaButton.getWidth()+mSeperator4.getWidth();
        if(mHideButton.getVisibility()==View.VISIBLE)
            ret+=mHideButton.getWidth()+mSeperator5.getWidth();
        if(ret>0)
            ret+=mEdgeLeft.getWidth();

        return ret;
    }

    private void runCustomApp(String uri) {
        if (uri != null) {
            try {
                Intent i = Intent.parseUri(uri, 0);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                getContext().startActivity(i);
            } catch (URISyntaxException e) {

            } catch (ActivityNotFoundException e) {

            }
        }
    }

    private void simulateKeypress(final int keyCode) {
        new Thread( new KeyEventInjector( keyCode ) ).start();
    }

    private class KeyEventInjector implements Runnable {
        private int keyCode;

        KeyEventInjector(final int keyCode) {
            this.keyCode = keyCode;
        }

        public void run() {
            try {
                if(! (IWindowManager.Stub
                    .asInterface(ServiceManager.getService("window")))
                         .injectKeyEvent(
                              new KeyEvent(KeyEvent.ACTION_DOWN, keyCode), true) ) {
                                   Slog.w(TAG, "Key down event not injected");
                                   return;
                              }
                if(! (IWindowManager.Stub
                    .asInterface(ServiceManager.getService("window")))
                         .injectKeyEvent(
                             new KeyEvent(KeyEvent.ACTION_UP, keyCode), true) ) {
                                  Slog.w(TAG, "Key up event not injected");
                             }
           } catch(RemoteException ex) {
               Slog.w(TAG, "Error injecting key event", ex);
           }
        }
    }
}
