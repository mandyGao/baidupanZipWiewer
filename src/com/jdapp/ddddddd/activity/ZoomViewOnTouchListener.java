/*
 * Making Sense of Multitouch
 * http://www.firstdroid.com/2010/06/10/making-sense-of-multitouch/
 */

package com.jdapp.ddddddd.activity;

import android.content.Context;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.jdapp.ddddddd.App;
import com.sonyericsson.zoom.DynamicZoomControl;
import com.sonyericsson.zoom.ZoomState;

/**
 * Implemented OnTouchListener
 * @author Owner
 *
 */
public class ZoomViewOnTouchListener implements OnTouchListener {

	/** TAG */
	private static final String GESTURE_TAG = "Gesture";

	/** Invalid pointer id */
	private static final int INVALID_POINTER_ID = -1;

	/**
	 * Enum defining listener modes. Before the view is touched the listener is
	 * in the UNDEFINED mode. Once touch starts it can enter either one of the
	 * other two modes: If the user scrolls over the view the listener will
	 * enter PAN mode, if the user uses his two fingers the listener will enter
	 * ZOOM mode.
	 */
	private enum Mode {
		UNDEFINED, PAN_ZOOM, NONE
	}

	/** Current listener mode */
	private Mode mMode = Mode.UNDEFINED;

	/** Zoom control to manipulate */
	private DynamicZoomControl mZoomControl;

	/** Zoom view to manipulate */
	private View mZoomView;

	/** X-coordinate of previously handled touch event */
	private float mX;

	/** Y-coordinate of previously handled touch event */
	private float mY;

	/** X-coordinate of latest down event */
	private float mDownX;

	/** Y-coordinate of latest down event */
	private float mDownY;

	/** Currently moving pointer */
	private int mActivePointerId;

	/** Indicates whether the listener reacts to fling events or not. */
	private boolean mFlingable = true;

	/** Velocity tracker for touch events */
	private VelocityTracker mVelocityTracker;

	/** Detects gestures for events */
	private GestureDetector mGestureDetector;

	/** Detects multitouch transformation gestures for events */
	private ScaleGestureDetector mScaleDetector;

	/** Distance touch can wander before we think it's scrolling */
	private final int mScaledTouchSlop;

	/** Minimum velocity for fling */
	private final int mScaledMinimumFlingVelocity;

	/** Maximum velocity for fling */
	private final int mScaledMaximumFlingVelocity;

	/** Minimum distance for fling */
	private final int mScaledMinimumFlingDistance;

	public ZoomViewOnTouchListener(Context context) {
		mGestureDetector = new GestureDetector(context, new GestureListener());
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		mScaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
		mScaledMinimumFlingVelocity = ViewConfiguration.get(context).getScaledMinimumFlingVelocity();
		mScaledMaximumFlingVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
		mScaledMinimumFlingDistance = (int) (25* context.getResources().getDisplayMetrics().density);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mZoomView = v;
		// Let the GestureDetector inspect all events.
		mGestureDetector.onTouchEvent(event);
		// Let the ScaleGestureDetector inspect all events.
		mScaleDetector.onTouchEvent(event);

		final int action = event.getAction();

		if (mVelocityTracker == null) {
			mVelocityTracker = VelocityTracker.obtain();
		}
		mVelocityTracker.addMovement(event);

		switch (action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN: {
			Log.d(GESTURE_TAG, "ACTION_DOWN");

			mZoomControl.stopFling();

			final float x = event.getX();
			final float y = event.getY();

			mDownX = x;
			mDownY = y;
			mX = x;
			mY = y;

			// Save the ID of this pointer
			mActivePointerId = event.getPointerId(0);
			break;
		}
		case MotionEvent.ACTION_MOVE: {
			final int pointerIndex = event.findPointerIndex(mActivePointerId);
			final float x = event.getX(pointerIndex);
			final float y = event.getY(pointerIndex);

			if (mMode == Mode.NONE) {

			} else if (mMode == Mode.PAN_ZOOM) {
				if (!mScaleDetector.isInProgress()) {
				}

				final float dx = (x - mX) / v.getWidth();
				final float dy = (y - mY) / v.getHeight();

				mZoomControl.pan(-dx, -dy);
			} else {
				final float scrollX = mDownX - x;
				final float scrollY = mDownY - y;

				final float dist = FloatMath.sqrt(scrollX * scrollX + scrollY * scrollY);

				if (dist >= mScaledTouchSlop) {
					mMode = Mode.PAN_ZOOM;
				}
			}

			mX = x;
			mY = y;
			break;
		}
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP: {
			Log.d(GESTURE_TAG, action == MotionEvent.ACTION_UP ? "ACTION_UP" : "ACTION_CANCEL");

			if (mMode == Mode.NONE) {

			} else if (mMode == Mode.PAN_ZOOM) {
				mVelocityTracker.computeCurrentVelocity(1000, mScaledMaximumFlingVelocity);
				mZoomControl.startFling(-mVelocityTracker.getXVelocity() / v.getWidth(),
						-mVelocityTracker.getYVelocity() / v.getHeight());
			} else {
				mZoomControl.startFling(0, 0);
			}

			mVelocityTracker.recycle();
			mVelocityTracker = null;
			mActivePointerId = INVALID_POINTER_ID;
			mMode = Mode.UNDEFINED;
			break;
		}
		case MotionEvent.ACTION_POINTER_UP: {
			Log.d(GESTURE_TAG, "ACTION_POINTER_UP");

			final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			final int pointerId = event.getPointerId(pointerIndex);

			if (pointerId == mActivePointerId) {
				// This was our active pointer going up. Choose a new active
				// pointer and adjust accordingly.
				final int newPointerIndex = (pointerIndex == 0 ? 1 : 0);
				mX = event.getX(newPointerIndex);
				mY = event.getY(newPointerIndex);
				mActivePointerId = event.getPointerId(newPointerIndex);
			}
			break;
		}
		default: {
			Log.d(GESTURE_TAG, "OTHER");

			mVelocityTracker.recycle();
			mVelocityTracker = null;
			mMode = Mode.UNDEFINED;
			break;
		}
		}

		return true;
	}

	/**
	 * Sets the zoom control to manipulate
	 * 
	 * @param control
	 *            Zoom control
	 */
	public void setZoomControl(DynamicZoomControl control) {
		mZoomControl = control;
	}

	public void setFlingable(boolean flingable) {
		mFlingable = flingable;
	}
	
	
	public static void popupMessage(Context context, CharSequence text) {
		Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
	}

	public boolean onSingleTap() {
		popupMessage(App.CONTEXT, "Single Tap");
		return false;
	}

	public void onNextPage() {
		popupMessage(App.CONTEXT, "Next Page");
	}

	public void onPrevPage() {
		popupMessage(App.CONTEXT, "Prev Page");
	}

	private final class ScaleListener extends SimpleOnScaleGestureListener {
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			final float factor = detector.getScaleFactor();
			final float zoom = mZoomControl.getZoomState().getZoom() * factor;
			final float px = mX / mZoomView.getWidth();
			final float py = mY / mZoomView.getHeight();

//			if (zoom > mZoomControl.getZoomState().getDefaultZoom()) {
			if (zoom >= 0.5 && zoom <= 16 ) {
				mZoomControl.getZoomState().setZoom(zoom);

				// TODO: Now just for Right-Top.
				float dx, dy;
				switch (mZoomControl.getZoomState().getAlignX()) {
				case Right:
					dx = (1 - px) - (1 - px) / factor;
					break;
				default:
					throw new UnsupportedOperationException("Now just for Right-Top.");
				}
				switch (mZoomControl.getZoomState().getAlignY()) {
				case Top:
					dy = py - py / factor;
					break;
				default:
					throw new UnsupportedOperationException("Now just for Right-Top.");
				}
				mZoomControl.pan(-dx, dy);
			}
			return true;
		}
	}

	private final class GestureListener extends SimpleOnGestureListener {
		/** Is bound to left edge */
		private boolean mIsEdgeLeft;

		/** Is bound to right edge */
		private boolean mIsEdgeRight;

		@Override
		public boolean onDown(MotionEvent e) {
			final ZoomState state = mZoomControl.getZoomState();

			if (mZoomControl.getPanMinX() == mZoomControl.getPanMaxX()) {
				mIsEdgeLeft = true;
				mIsEdgeRight = true;
			} else {
				mIsEdgeLeft = (state.getPanX() <= mZoomControl.getPanMinX());
				mIsEdgeRight = (state.getPanX() >= mZoomControl.getPanMaxX());
			}

			return true;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.v(GESTURE_TAG, "onSingleTapConfirmed()");
			return onSingleTap();
		}

		@Override
		public boolean onDoubleTap(MotionEvent e) {
			final ZoomState state = mZoomControl.getZoomState();

			if (state.getZoom() != state.getDefaultZoom()) {
				Log.i(GESTURE_TAG, "Double Tap");

				mZoomControl.getZoomState().setPanX(0);
				mZoomControl.getZoomState().setPanY(0);
				mZoomControl.getZoomState().setZoom(1f);
				mZoomControl.getZoomState().notifyObservers();
				return true;
			} else{
				Log.i(GESTURE_TAG, "Double Tap");

				mZoomControl.getZoomState().setPanX(0);
				mZoomControl.getZoomState().setPanY(0);
				mZoomControl.getZoomState().setZoom(1.8f);
				mZoomControl.getZoomState().notifyObservers();
				return true;
			}

		}

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
			Log.v(GESTURE_TAG, "Fling");

			final float scrollX = e2.getX() - e1.getX();
			// final float scrollY = e2.getY() - e1.getY();

			if (mFlingable && ((mIsEdgeLeft && scrollX > 0) || (mIsEdgeRight && scrollX < 0))
					&& Math.abs(velocityX) > mScaledMinimumFlingVelocity
					&& Math.abs(velocityX) > Math.abs(velocityY) * 2
					&& Math.abs(scrollX) > mScaledMinimumFlingDistance) {
				if (velocityX > 0) {
					Log.i(GESTURE_TAG, "Next Page");
					//onNextPage();
				} else {
					Log.i(GESTURE_TAG, "Prev Page");
					//onPrevPage();
				}

				//mMode = Mode.NONE;
				// mZoomControl.stopFling();
			}

			return false;
		}
	}
}
