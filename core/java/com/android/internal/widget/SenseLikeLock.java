/*
* Copyright (C) 2011 John Weyrauch
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

package com.android.internal.widget;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;

import com.android.internal.R;
import com.android.internal.view.FastBitmapDrawable;

public class SenseLikeLock extends View {

	private String TAG = "SenseLikeLock";
	private static final boolean DBG = true;
	private static final boolean IDBG = DBG;
	private static final boolean TDBG = false;
    private static final boolean VISUAL_DEBUG = false;
    private Animation mUnlockAnimation;

	private int mOrientation;
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	final Matrix mBgMatrix = new Matrix();
	private Paint mPaint = new Paint();
	   
	Bitmap mLowerBackground;
	Bitmap mShortcutsBackground;
	Bitmap mLockIcon;
	Bitmap mLockAppIcon;
	Bitmap mShortCutOne;
	Bitmap mShortCutTwo;
	Bitmap mShortCutThree;
	Bitmap mShortCutFour;
	   
	private float mShortCutHeight;
	private int mLockX, mLockY;
	private boolean mIsTouchInCircle = false;
	private boolean mUsingShortcuts = false;
	private boolean mTriggering = false;
	private Canvas mCanvas;
	private float mDensity;
	   
	private OnSenseLikeSelectorTriggerListener mSenseLikeTriggerListener;
	private int  mGrabbedState = OnSenseLikeSelectorTriggerListener.ICON_GRABBED_STATE_NONE;
	private float mDensityScaleFactor = 1;
	private int mShortCutSelected;

    Boolean mUseShortcutOne = false;
    Boolean mUseShortcutTwo = false;
    Boolean mUseShortcutThree = false;
    private Boolean mUseShortcutFour = false;
    private Boolean mIsInRingMode = false;
	
	private enum mSelected {
		LOCK(1),
		SHORTCUT(2);
		private final double value;

		mSelected(int i){
			this.value = i;
		}
	}

	public SenseLikeLock(Context context) {
		this(context,null);
	}

	public SenseLikeLock(Context context, AttributeSet attrs) {
		super(context,attrs);
		
		TypedArray a =
	        context.obtainStyledAttributes(attrs, R.styleable.SenseLikeSelector);
	        mOrientation = a.getInt(R.styleable.SenseLikeSelector_orientation, VERTICAL);
	        Resources r = getResources();
	        mDensity = r.getDisplayMetrics().density;
	        int densityDpi = r.getDisplayMetrics().densityDpi;

	        if(densityDpi <= 240 && densityDpi >= 180)
	            mDensityScaleFactor=(float)(240.0 / densityDpi);
	        if(densityDpi <= 160 && densityDpi >= 120)
	            mDensityScaleFactor=(float)(160.0 / densityDpi);

	        a.recycle();
	        initializeUI();
	}
	
	@Override 
	public boolean onTouchEvent(MotionEvent event){
		super.onTouchEvent(event);
		
		final int height = getHeight();
		final int width  = getWidth();
		final int action = event.getAction();
		final int eventX = (int) event.getX();    
        final int eventY = (int) event.getY();
        if (DBG) log("x -" + eventX + " y -" + eventY);
    	                
		switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (DBG) log("touch-down");
			    setLockXY(eventX, eventY);
			
			    if (whichShortcutSelected()) {
				    setGrabbedState(OnSenseLikeSelectorTriggerListener.ICON_SHORTCUT_GRABBED_STATE_GRABBED);
				    invalidate();
				    mUsingShortcuts = true;
			    } else {
				    mIsTouchInCircle = true;
				    setGrabbedState(OnSenseLikeSelectorTriggerListener.ICON_GRABBED_STATE_GRABBED);
				    invalidate();
			    }
                break;

            case MotionEvent.ACTION_MOVE:
                if (DBG) log("touch-move");
                setLockXY(eventX, eventY);

                if (mUsingShortcuts) {
                	int i = OnSenseLikeSelectorTriggerListener.LOCK_ICON_SHORTCUT_ONE_TRIGGERED;
                	int ar[] = {(width - mLockIcon.getWidth())/2, (height -(2*(mLockIcon.getHeight()/3))) };
                
                    if(isShortTriggered( eventX, eventY)) {
                    	Log.d(TAG, "Shortcut Triggered");
                    
                    	switch(this.mShortCutSelected) {
                        	case 1:
                        		dispatchTriggerEvent(OnSenseLikeSelectorTriggerListener.LOCK_ICON_SHORTCUT_ONE_TRIGGERED);
                        		reset();
                        		break;
                        	case 2:
                        		dispatchTriggerEvent(OnSenseLikeSelectorTriggerListener.LOCK_ICON_SHORTCUT_TWO_TRIGGERED);
                        		reset();
                        		break;
                        	case 3:
                        		dispatchTriggerEvent(OnSenseLikeSelectorTriggerListener.LOCK_ICON_SHORTCUT_THREE_TRIGGERED);
                        		reset();
                        		break;
                        	case 4:
                        		dispatchTriggerEvent(OnSenseLikeSelectorTriggerListener.LOCK_ICON_SHORTCUT_FOUR_TRIGGERED);
                        		reset();
                        		break;
                    	}
                    }
                } else {
                    if(isLockIconTriggered()) {
                	    reset();
                	    mTriggering = true;
                	}
                }
		        invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (DBG) log("touch-up");
                reset();
                invalidate();
                break;
            case MotionEvent.ACTION_CANCEL:
                if (DBG) log("touch-cancel");
                reset();
                invalidate();
        }
		
		return true;
	}

	private boolean isLockIconTriggered() {
		int padding = 15;
		int width = getWidth();
		int heighth = getHeight();

		if((mLockX >= (width - padding) || mLockX <= padding ) ){
    		Log.d(TAG, "Dispatching horizontal lock trigger event");
    		dispatchTriggerEvent(OnSenseLikeSelectorTriggerListener.LOCK_ICON_TRIGGERED);
    		return true;
    	}

    	if( (mLockY <= (heighth/4)) || (mLockY >= (heighth - padding )) ){
    		Log.d(TAG, "Dispatching vertical lock trigger event");
    		dispatchTriggerEvent(OnSenseLikeSelectorTriggerListener.LOCK_ICON_TRIGGERED);
    		return true;
    	}

    	return false;
	}

	private boolean isShortTriggered(int x, int y) {
		int width = this.getWidth()/2;
		int height = this.getHeight() - (this.mLockIcon.getHeight()/4);
		int radius = this.mShortcutsBackground.getWidth()/2;
		int CartesianShiftTouchX;
    	int CartesianShiftTouchY; 
    	
    	if(x < width)
    		CartesianShiftTouchX = width - x;
    	else
    		 CartesianShiftTouchX = x - width;
    	
    	if(y < height)
    		CartesianShiftTouchY = height - y;
    	else
    		 CartesianShiftTouchY = y - height;
    	
    	int YTouchRadius = (int) Math.sqrt((CartesianShiftTouchX*CartesianShiftTouchX) + (CartesianShiftTouchY*CartesianShiftTouchY));
    	
    	if(YTouchRadius > radius)
    		return false;
    	else 
    		return true;
	}

	private boolean whichShortcutSelected() {
		Log.d(TAG, "Figuring out which shortcut");
        if (DBG) log("x -" + mLockX + " y -" + mLockY);
		
		if((mLockY > this.mShortCutHeight) && (mLockY < (this.mShortCutHeight + this.mShortcutsBackground.getHeight())) ){
			int width = this.getWidth()/2;
			int padding = this.mShortcutsBackground.getWidth()/2;
			Log.d(TAG, "Touch within shortcut bar");
			
			if(mLockX >  width - (padding*6) && mLockX < width - (padding*4)){
				mShortCutSelected = 1;
				Log.d(TAG, "Shortcut one");

				if(mUseShortcutOne)
                    return true;
                else
                    return false;
            }

			if(mLockX >  width - (padding*3) && mLockX < width - (padding)){
				mShortCutSelected = 2;
				Log.d(TAG, "Shortcut two");
				
                if(mUseShortcutTwo)
                    return true;
                else
                    return false;
			}

			if(mLockX <  width + (padding*3) && mLockX > width + (padding)){
				mShortCutSelected = 3;
				Log.d(TAG, "Shortcut three");
				
            	if(mUseShortcutThree)
                    return true;
                else
                    return false;
			}

			if(mLockX <  width + (padding*6) && mLockX > width + (padding*4)){
				mShortCutSelected = 4;
				Log.d(TAG, "Shortcut four");
				
                if(mUseShortcutFour)
                    return true;
                else
                    return false;
			}
			
			Log.d(TAG, "No shortcut selected");
			return false;
		}

		Log.d(TAG, "No touch in shortcut bar");
	    return false;
	}
	
	@Override 
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		mCanvas = canvas;
		if (IDBG) log("Redrawing the view");
        final int width = getWidth();
        final int height = getHeight();
        final int halfWidth = width/2;
        final int halfHeight = height/2;
        mShortCutHeight = height - (this.mLockIcon.getHeight()/2) - this.mLowerBackground.getHeight();
        int padding = this.mShortcutsBackground.getWidth()/2;
        if (DBG) log("The width of the view is " + width + " and the hieght of the veiw is " + height );

        if (VISUAL_DEBUG) {
		    if (IDBG) log("Debugging the widget visibly");
            mPaint.setColor(0xffff0000);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(0, 0, width-1, height-1 , mPaint);
              
            float h = height - (this.mLockIcon.getHeight()/2) - this.mLowerBackground.getHeight()/2;              
            canvas.drawRect(0, mShortCutHeight , width, mShortCutHeight + this.mShortcutsBackground.getHeight() , mPaint);
            canvas.drawLine(halfWidth, height, halfWidth, 0, mPaint);
        }

        canvas.drawBitmap(this.mLowerBackground,  0, (height -(this.mLowerBackground.getHeight()) ), mPaint);

        if (mIsTouchInCircle && !mIsInRingMode) {	
            mLockIcon = getBitmapFor(R.drawable.sense_ring_on_unlock);
            canvas.drawBitmap(mLockIcon,  mLockX-(mLockIcon.getWidth()/2), mLockY - mLockIcon.getHeight()/2, mPaint);	
        } else if (mUsingShortcuts) {
        	Log.d(TAG, "Shorcut bar drawing without moving ring");
        	canvas.drawBitmap(mLockAppIcon,  (width - mLockIcon.getWidth())/2, (height -(2*(mLockIcon.getHeight()/3))), mPaint);
            drawShorts(canvas, halfWidth, padding);  
        } else {
        	Log.d(TAG, "Shorcut bar drawing with moving ring");
            if(!mUseShortcutOne)drawShortOne(canvas, halfWidth - (padding*6), mShortCutHeight);
            if(!mUseShortcutTwo)drawShortTwo(canvas, halfWidth - (padding*3), mShortCutHeight);
            if(!mUseShortcutThree)drawShortThree(canvas, halfWidth + (padding), mShortCutHeight);
            if(!mUseShortcutFour)drawShortFour(canvas, halfWidth + (padding*4), mShortCutHeight);
            canvas.drawBitmap(mLockIcon,  (width/2)-(mLockIcon.getWidth()/2), (height -(mLockIcon.getHeight()/3)), mPaint);
        }

		return;
	}

	private void doUnlockAnimation() {
		Log.d(TAG, "dounlockanimation");
		this.mUnlockAnimation = new ScaleAnimation(1,0,1,0);
		this.mUnlockAnimation.setDuration(1000L);
		this.mUnlockAnimation.setInterpolator(new AccelerateInterpolator());
		mUnlockAnimation.setRepeatCount(0);
		this.startAnimation(mUnlockAnimation);	
	}

	private void drawShorts(Canvas canvas, int halfWidth, int padding) {
        switch(mShortCutSelected) {
	        case 1 : { 
	          Log.d(TAG, "Drawing shorcut new position");
              if(!mUseShortcutOne)drawMovableShort(canvas, 1, mShortcutsBackground.getWidth()/2 );
              if(!mUseShortcutTwo)drawShortTwo(canvas, halfWidth - (padding*3), mShortCutHeight);
              if(!mUseShortcutThree)drawShortThree(canvas, halfWidth + (padding), mShortCutHeight);
              if(!mUseShortcutFour)drawShortFour(canvas, halfWidth + (padding*4), mShortCutHeight);
	          break;
	         }
           case 2 : {
             if(!mUseShortcutOne)drawMovableShort(canvas, 2, mShortcutsBackground.getWidth()/2);
             if(!mUseShortcutTwo)drawShortOne(canvas, halfWidth - (padding*6), mShortCutHeight);
             if(!mUseShortcutThree)drawShortThree(canvas, halfWidth + (padding), mShortCutHeight);
             if(!mUseShortcutFour)drawShortFour(canvas, halfWidth + (padding*4), mShortCutHeight);
            }
      	    break;
          case 3 : {
             if(!mUseShortcutOne)drawMovableShort(canvas, 3, mShortcutsBackground.getWidth()/2);
             if(!mUseShortcutTwo)drawShortOne(canvas, halfWidth - (padding*6), mShortCutHeight);
             if(!mUseShortcutThree)drawShortTwo(canvas, halfWidth - (padding*3), mShortCutHeight);
             if(!mUseShortcutFour)drawShortFour(canvas, halfWidth + (padding*4), mShortCutHeight);
            }
      	    break;
          case 4 : {
             if(!mUseShortcutOne) drawMovableShort(canvas, 4, mShortcutsBackground.getWidth()/2);
             if(!mUseShortcutTwo)drawShortOne(canvas, halfWidth - (padding*6), mShortCutHeight);
             if(!mUseShortcutThree)drawShortTwo(canvas, halfWidth - (padding*3), mShortCutHeight);
             if(!mUseShortcutFour)drawShortThree(canvas, halfWidth + (padding), mShortCutHeight);
            }
      	    break;
        }
	}

	public void drawMovableShort(Canvas canvas, int whichshort, int offset) {
		switch (whichshort) {
		    case 1:
			     drawShortOne(canvas, mLockX - offset, mLockY- offset);
			     break;
		    case 2:
			     drawShortTwo(canvas, mLockX- offset, mLockY- offset);
			     break;
		    case 3:
			     drawShortThree(canvas, mLockX- offset, mLockY- offset);
			     break;
		    case 4:
			     drawShortFour(canvas, mLockX - offset, mLockY- offset);
			     break;
		}	
	}

	private void drawShortOne(Canvas canvas, float postx, float posty) {
        canvas.drawBitmap(this.mShortcutsBackground,  postx, posty, mPaint);
        canvas.drawBitmap(this.mShortCutOne, postx, posty, mPaint);
	}

	private void drawShortTwo(Canvas canvas, float postx, float posty) {
	    canvas.drawBitmap(this.mShortcutsBackground,   postx, posty, mPaint);
        canvas.drawBitmap(this.mShortCutTwo,  postx, posty, mPaint);
	}

	private void drawShortThree(Canvas canvas, float postx, float posty) {
        canvas.drawBitmap(this.mShortcutsBackground,   postx, posty, mPaint);
        canvas.drawBitmap(this.mShortCutThree,   postx, posty, mPaint);
	}

	private void drawShortFour(Canvas canvas, float postx, float posty) {
	    canvas.drawBitmap(this.mShortcutsBackground,   postx, posty, mPaint);
        canvas.drawBitmap(this.mShortCutFour,  postx, posty, mPaint);
	}
	
    @Override 
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (IDBG) log("Measuring the demensions of the view");

		final int length = isVertical() ?
            MeasureSpec.getSize(widthMeasureSpec) :
            MeasureSpec.getSize(heightMeasureSpec);

        final int height = (isVertical() ?
            (MeasureSpec.getSize(heightMeasureSpec)) :
            MeasureSpec.getSize(widthMeasureSpec)/2);

		if (DBG) log("The demensions of the view is length:" + length + " and height: " + height );
           if (isVertical()) {
               setMeasuredDimension(length, height);
           } else {
               setMeasuredDimension(height, length);
           }
    }

    private void initializeUI(){
    	Log.d(TAG, "Initializing user interface");
    	mLockIcon = getBitmapFor(R.drawable.sense_ring);
    	mLowerBackground = getBitmapFor(R.drawable.sense_panel);
    	mShortcutsBackground = getBitmapFor(R.drawable.app_bg);
    	mLockAppIcon = getBitmapFor(R.drawable.sense_ring_appready);
    }

    private void setLockXY(int eventX, int eventY) {
    	mLockX = eventX;
    	mLockY = eventY;
    }
    
    public interface OnSenseLikeSelectorTriggerListener {
    	static final int ICON_GRABBED_STATE_NONE = 0;
    	static final int ICON_GRABBED_STATE_GRABBED = 1;
    	static final int ICON_SHORTCUT_GRABBED_STATE_GRABBED = 2;
    	static final int LOCK_ICON_TRIGGERED = 10;
    	static final int LOCK_ICON_SHORTCUT_ONE_TRIGGERED   = 11;
    	static final int LOCK_ICON_SHORTCUT_TWO_TRIGGERED   = 12;
    	static final int LOCK_ICON_SHORTCUT_THREE_TRIGGERED = 13;
    	static final int LOCK_ICON_SHORTCUT_FOUR_TRIGGERED  = 14;
    	public void OnSenseLikeSelectorGrabbedStateChanged(View v, int GrabState);
    	public void onSenseLikeSelectorTrigger(View v, int Trigger);	
    }
    
    public void setOnSenseLikeSelectorTriggerListener(OnSenseLikeSelectorTriggerListener l) {
        if (DBG) log("Setting the listeners");
    	this.mSenseLikeTriggerListener = l;
    }
    
    private void setGrabbedState(int newState) {
        if (newState != mGrabbedState) {
            mGrabbedState = newState;

            if (mSenseLikeTriggerListener != null) {
                mSenseLikeTriggerListener.OnSenseLikeSelectorGrabbedStateChanged(this, mGrabbedState);
            }
        }
    }
    
    private void dispatchTriggerEvent(int whichTrigger) {
        if (IDBG) log("Dispatching a trigered event");
        doUnlockAnimation();
        mSenseLikeTriggerListener.onSenseLikeSelectorTrigger(this, whichTrigger);
    }
    
    private boolean isVertical() {
        return (mOrientation == VERTICAL);
    }
    
    private void log(String msg) {
	    Log.d(TAG, msg);
	}

    private Bitmap getBitmapFor(int resId) {
        return BitmapFactory.decodeResource(getContext().getResources(), resId);
    }

    private Bitmap getBitmapFromDrawable(Drawable icon) {
        Log.d(TAG, "Decoding drawable to bitmap");

        if (icon instanceof BitmapDrawable) {
            return((BitmapDrawable)icon).getBitmap();
        } else {
            return null;
        }
    }

    private void reset(){
    	setGrabbedState(OnSenseLikeSelectorTriggerListener.ICON_GRABBED_STATE_NONE);
    	mIsTouchInCircle = false;
    	mUsingShortcuts = false;
    	mTriggering = false;
    	this.mLockX = 0;
    	this.mLockY = 0;
    }
    
    public void setShortCutsDrawables(Drawable FarLeft, Drawable Left, Drawable Right, Drawable FarRight) {
        log("Setting the icon One");
        if(FarLeft != null)mShortCutOne = getBitmapFromDrawable(FarLeft);

        if(mShortCutOne != null)
           mUseShortcutOne = true;
        else
           mUseShortcutOne = false;

        log("Setting the icon Two");
        if(FarLeft != null)mShortCutTwo = getBitmapFromDrawable(Left);

        if(mShortCutTwo != null)
           mUseShortcutTwo = true;
        else
           mUseShortcutTwo = false;

        log("Setting the icon Three");
        if(FarLeft != null)mShortCutThree = getBitmapFromDrawable(Right);

        if(mShortCutThree != null)
           mUseShortcutThree = true;
        else
           mUseShortcutThree = false;

        log("Setting the icon Four");
        if(FarLeft != null)mShortCutFour = getBitmapFromDrawable(FarRight);

        if(mShortCutFour != null)
           mUseShortcutFour = true;
        else
           mUseShortcutFour = false;
    }
    
    public Intent[] setDefaultIntents(){
    	Intent intent = new Intent(Intent.ACTION_DIAL); 
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	Intent[] i = {new Intent(Intent.ACTION_DIAL).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),new Intent(),new Intent(),new Intent()};
    	return i;
    }
    
    public void setToTwoShortcuts(boolean UseOnlyTwoShortcuts){
    	if(UseOnlyTwoShortcuts == true)Log.d(TAG, "Using only two shortcuts");
    	mUseShortcutTwo = UseOnlyTwoShortcuts;
    }
}
