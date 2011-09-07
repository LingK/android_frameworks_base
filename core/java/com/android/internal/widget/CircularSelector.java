/*
 * Copyright (C) John Weyrauch
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
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;

import com.android.internal.R;

public class CircularSelector extends View {

	private String TAG = "CircularSelector";
	private static final boolean DBG = false;
	private static final boolean IDBG = false;
	private static final boolean TDBG = false;
    private static final boolean VISUAL_DEBUG = false;
	private int mOrientation;
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;

	final Matrix mBgMatrix = new Matrix();
	private Paint mPaint = new Paint();
	Bitmap mPortraitCircle;
	Bitmap mLandscapeCircle;
	Bitmap mLockIcon;
	Bitmap mLockIconBackGround;

	private int mLockX, mLockY;
	private boolean mIsTouchInCircle = false;
	private float mDensity;
	private OnCircularSelectorTriggerListener mCircularTriggerListener;
	private int  mGrabbedState = OnCircularSelectorTriggerListener.ICON_GRABBED_STATE_NONE;
	Boolean mSlideisSensitive = false;
	 
	public CircularSelector(Context context) {
		this(context,null);
	}

	public CircularSelector(Context context, AttributeSet attrs) {
		super(context,attrs);

	    TypedArray a =
	        context.obtainStyledAttributes(attrs, R.styleable.CircularSelector);
	        mOrientation = a.getInt(R.styleable.CircularSelector_orientation, VERTICAL);
	        a.recycle();
	        initializeUI();
	}
	
	private void initializeUI(){
		mPortraitCircle = getBitmapFor(R.drawable.lock_ic_circle_port);
		mLandscapeCircle =  getBitmapFor(R.drawable.lock_ic_land_phosphrous);
      	mLockIcon = getBitmapFor(R.drawable.lock_ic_lock_move);
		mLockIconBackGround = getBitmapFor(R.drawable.lock_ic_lock_bg);
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
            if(isVertical() ? isYUnderArc(width/2, eventY, eventX, height, width) : isYWithinCircle(width/3, eventY, eventX, height, width) || TDBG){
			if (DBG) log("touch-down within the arc or circle");
			setLockXY(eventX, eventY);
		 	mIsTouchInCircle = true;
			setGrabbedState(OnCircularSelectorTriggerListener.ICON_GRABBED_STATE_GRABBED);
		    	invalidate();
            	}
		else{
			if (DBG) log("touch-down outside the arc or circle");
			reset();
			invalidate();
		}
            break;
        case MotionEvent.ACTION_MOVE:
            if (DBG) log("touch-move");
            if((isVertical() ? isYUnderArc(width/2, eventY, eventX, height, width) : isYWithinCircle(width/3, eventY, eventX, height, width) || TDBG) ){
            	if (DBG) log("touch-move within the arc or circle");
            	setLockXY(eventX, eventY);
            	if(mSlideisSensitive)mIsTouchInCircle = true;
            	setGrabbedState(OnCircularSelectorTriggerListener.ICON_GRABBED_STATE_GRABBED);
            	invalidate();
            }
            else if(mIsTouchInCircle == true){
            	dispatchTriggerEvent(OnCircularSelectorTriggerListener.LOCK_ICON_TRIGGERED);
            	reset();
            	invalidate();
            }
            else{
            	reset();
            	invalidate();
            }
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
	
	@Override 
	public void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		  if (IDBG) log("Redrawing the view");
          final int width = getWidth();
          final int height = getHeight();
          final int halfWidth = width/2;
          final int halfHeight = height/2;
          if (DBG) log("The width of the view is " + width + " and the hieght of the veiw is " + height );

          if (VISUAL_DEBUG) {
			  if (IDBG) log("Debugging the widget visibly");
              mPaint.setColor(0xffff0000);
              mPaint.setStyle(Paint.Style.STROKE);
              canvas.drawRect(0, 0, width, height , mPaint);
              
              if (isVertical()) {
                canvas.drawCircle(halfWidth, height, halfWidth, mPaint);
              } else {
            	canvas.drawCircle(halfWidth, halfHeight, width/3, mPaint);
              }
          }

          if(isVertical()){
        	  canvas.drawBitmap(mPortraitCircle,  0, height-mPortraitCircle.getHeight(), mPaint);
        	  canvas.drawBitmap(mLockIconBackGround,  (width-mLockIconBackGround.getWidth())/2, (height-mLockIconBackGround.getHeight()), mPaint);
          }
          else{
        	  canvas.drawBitmap(mLandscapeCircle, (width-mLandscapeCircle.getWidth())/2, (height-mLandscapeCircle.getHeight())/2, mPaint);
        	  canvas.drawBitmap(mLockIconBackGround,  (width-mLockIconBackGround.getWidth())/2, (height-mLockIconBackGround.getHeight())/2, mPaint);
          }

          if(mIsTouchInCircle){
        	  canvas.drawBitmap(mLockIcon,  mLockX-(mLockIcon.getWidth()/2), mLockY - mLockIcon.getHeight()/2, mPaint);
          }
          else{
        	  if(isVertical()){
	             canvas.drawBitmap(mLockIcon,  (width-mLockIcon.getWidth())/2, (height-mLockIcon.getHeight()), mPaint);
	     	  }else{
	     		 canvas.drawBitmap(mLockIcon,  (width - mLockIcon.getWidth())/2, (height - mLockIcon.getHeight())/2, mPaint);
	    	  }
          }
	    return;
	}
	
    @Override 
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		  if (IDBG) log("Measuring the demensions of the view");

		  final int length = isVertical() ?
                  MeasureSpec.getSize(widthMeasureSpec) :
                  MeasureSpec.getSize(heightMeasureSpec);

      	  final int height = (isVertical() ?
                      (MeasureSpec.getSize(heightMeasureSpec)/5)*2 :
                      MeasureSpec.getSize(widthMeasureSpec)/2);

		   if (DBG) log("The demensions of the view is length:" + length + " and height: " + height );

           if (isVertical()) {
               setMeasuredDimension(length, height);
           } else {
               setMeasuredDimension(height, length);
           }
       }

    private boolean isYUnderArc(int innerRadius, int y, int x, int height, int width) {
    	int CartesianX = width/2; // The x point directly in the middle of the view
    	int CartesianY = height;  // The Y point, at the bottom of the view
    	int YRadiusUnderArc = innerRadius;
    	int CartesianShiftTouchX;
    	int CartesianShiftTouchY;

    	if(x < CartesianX)
    		CartesianShiftTouchX = CartesianX - x;
    	else
    		CartesianShiftTouchX = x - CartesianX;

    	if(y < CartesianY)
    		CartesianShiftTouchY = CartesianX - y;
    	else
    		CartesianShiftTouchY = y - CartesianY;

    	int YTouchRadius = (int) Math.sqrt((CartesianShiftTouchX*CartesianShiftTouchX) + (CartesianShiftTouchY*CartesianShiftTouchY));

    	if(YTouchRadius > YRadiusUnderArc)
    		return false;
    	else 
    		return true;
    }
    
    private boolean isYWithinCircle(int innerRadius, int y, int x, int height, int width) {
    	int YRadiusUnderArc = innerRadius;
      	int CartesianX = width/2; // The x point directly in the middle of the view
    	int CartesianY = height/2;  // The Y point directly in the middle of the view
    	int CartesianShiftTouchX;
    	int CartesianShiftTouchY;

    	if(x > CartesianX)
    		CartesianShiftTouchX = CartesianX - x;
    	else
    		 CartesianShiftTouchX = x - CartesianX;

    	if(y > CartesianY)
    		CartesianShiftTouchY = CartesianX - y;
    	else
    		 CartesianShiftTouchY = y - CartesianY;

    	int YTouchRadius = (int) Math.sqrt((CartesianShiftTouchX*CartesianShiftTouchX) + (CartesianShiftTouchY*CartesianShiftTouchY));

    	if(YTouchRadius > YRadiusUnderArc)
    		return false;
    	else 
    		return true;
    }
    
    private void setLockXY(int eventX, int eventY){
    	mLockX = eventX;
    	mLockY = eventY;
    }
    
    public interface OnCircularSelectorTriggerListener{
    	static final int ICON_GRABBED_STATE_NONE = 0;
    	static final int ICON_GRABBED_STATE_GRABBED = 1;
    	static final int LOCK_ICON_TRIGGERED = 10;
    	public void OnCircularSelectorGrabbedStateChanged(View v, int GrabState);
    	public void onCircularSelectorTrigger(View v, int Trigger);
    }
    
    public void setOnCircularSelectorTriggerListener(OnCircularSelectorTriggerListener l) {
    	if (DBG) log("Setting the listners");
    	this.mCircularTriggerListener = l;
    }
    
    private void setGrabbedState(int newState) {
        if (newState != mGrabbedState) {
            mGrabbedState = newState;

            if (mCircularTriggerListener != null) {
                mCircularTriggerListener.OnCircularSelectorGrabbedStateChanged(this, mGrabbedState);
            }
        }
    }
    
    private void dispatchTriggerEvent(int whichTrigger) {
    	if (IDBG) log("Dispatching a trigered event");

        if (mCircularTriggerListener != null) {
            mCircularTriggerListener.onCircularSelectorTrigger(this, whichTrigger);
        }
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

    private void reset(){
    	setGrabbedState(OnCircularSelectorTriggerListener.ICON_GRABBED_STATE_NONE);
    	mIsTouchInCircle = false;
    }
    
    public void setSlide(boolean SlideAll){
    	mSlideisSensitive = SlideAll;
    }   
}
