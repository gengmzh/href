package cn.seddat.href.client.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;
import cn.seddat.href.client.R;

/**
 * @author mzhgeng
 * 
 */
public class SideslippingView extends ViewGroup {

	private final String tag = SideslippingView.class.getSimpleName();

	private final int SNAP_VELOCITY = 1000;
	private final int touchSlop;

	private ViewGroup menuView;
	private ViewGroup contentView;
	private Scroller mScroller;

	private int currentScreen;
	private float lastX, lastY;
	private float distance;
	private boolean isSideslipping;
	private VelocityTracker velocityTracker;

	public SideslippingView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public SideslippingView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mScroller = new Scroller(getContext());
		currentScreen = 1;
		touchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
		postDelayed(new Runnable() {
			@Override
			public void run() {
				scrollTo(findViewById(R.id.sideslipping_menu).getWidth(), 0);
			}
		}, 10);
	}

	public void init() {
		this.menuView = (ViewGroup) findViewById(R.id.sideslipping_menu);
		this.contentView = (ViewGroup) findViewById(R.id.sideslipping_content);
	}

	/**
	 * @return true if the side menu is showing, false if not
	 */
	public boolean isMenuShowing() {
		return currentScreen <= 0;
	}

	/**
	 * show the content view
	 * 
	 * @param view
	 */
	public void showContent(View view) {
		contentView.removeAllViews();
		contentView.addView(view);
		this.scrollRight();
	}

	@Override
	public void computeScroll() {
		if (mScroller.computeScrollOffset()) {
			scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		menuView.measure(menuView.getRight() - menuView.getLeft(), heightMeasureSpec);
		contentView.measure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		int childLeft = 0;
		for (int i = 0; i < getChildCount(); i++) {
			View child = getChildAt(i);
			if (child.getVisibility() != View.GONE) {
				int width = child.getMeasuredWidth();
				child.layout(childLeft, 0, childLeft + width, child.getMeasuredHeight());
				childLeft += width;
			}
		}
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final int action = ev.getAction();
		if (action == MotionEvent.ACTION_MOVE && isSideslipping) {
			return true;
		}
		final float x = ev.getX(), y = ev.getY();

		switch (action) {
		case MotionEvent.ACTION_DOWN:
			lastX = x;
			lastY = y;
			distance = 0;
			isSideslipping = !mScroller.isFinished();
			break;
		case MotionEvent.ACTION_MOVE:
			final int xDiff = (int) Math.abs(x - lastX);
			final int yDiff = (int) Math.abs(y - lastY);
			if (xDiff > touchSlop && xDiff > yDiff) {
				isSideslipping = true;
			}
			break;
		case MotionEvent.ACTION_CANCEL:
		case MotionEvent.ACTION_UP:
			isSideslipping = false;
			break;
		}
		return isSideslipping;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (!isSideslipping) {
			Log.w(tag, tag + " isn't sideslipping, are you kidding me??");
			return true;
		}
		if (velocityTracker == null) {
			velocityTracker = VelocityTracker.obtain();
		}
		velocityTracker.addMovement(ev);
		final int action = ev.getAction();
		final float x = ev.getX();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (!mScroller.isFinished()) {
				mScroller.abortAnimation();
			}
			lastX = x;
			distance = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			final int deltaX = (int) (lastX - x);
			lastX = x;
			if (deltaX < 0) {
				if (getScrollX() > 0) {
					int delta = Math.max(-getScrollX(), deltaX);
					scrollBy(delta, 0);
					distance += delta;
				}
			} else if (deltaX > 0) {
				final int availableToScroll = getChildAt(getChildCount() - 1).getRight() - getScrollX() - getWidth();
				if (availableToScroll > 0) {
					int delta = Math.min(availableToScroll, deltaX);
					scrollBy(delta, 0);
					distance += delta;
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			velocityTracker.computeCurrentVelocity(1000);
			int velocityX = (int) velocityTracker.getXVelocity();
			if (velocityX > SNAP_VELOCITY && currentScreen > 0) {
				snapToScreen(currentScreen - 1);
			} else if (velocityX < -SNAP_VELOCITY && currentScreen < getChildCount() - 1) {
				snapToScreen(currentScreen + 1);
			} else {
				int whichScreen = currentScreen;
				if (distance < 0) {
					if (currentScreen > 0) {
						int width = getChildAt(currentScreen - 1).getWidth();
						if (width / 5 + distance <= 0) {
							whichScreen = currentScreen - 1;
						}
					}
				} else if (distance > 0) {
					if (currentScreen < getChildCount() - 1) {
						int width = getChildAt(currentScreen).getWidth();
						if (width / 5 - distance <= 0) {
							whichScreen = currentScreen + 1;
						}
					}
				}
				snapToScreen(whichScreen);
			}
			velocityTracker.recycle();
			velocityTracker = null;
			isSideslipping = false;
			break;
		case MotionEvent.ACTION_CANCEL:
			isSideslipping = false;
		}
		return true;
	}

	protected void snapToScreen(int whichScreen) {
		int screen = Math.max(0, Math.min(whichScreen, getChildCount() - 1));
		if (screen == currentScreen) {
			return;
		}
		View focusedChild = getFocusedChild();
		if (focusedChild != null && focusedChild == getChildAt(currentScreen)) {
			focusedChild.clearFocus();
		}
		int delta = -getScrollX();
		for (int i = 0; i < screen; i++) {
			delta += getChildAt(i).getWidth();
		}
		mScroller.startScroll(getScrollX(), 0, delta, 0, Math.abs(delta) * 2);
		invalidate();
		currentScreen = screen;
		Log.i(tag, "current screen is " + currentScreen);
	}

	public void scrollLeft() {
		if (currentScreen > 0 && mScroller.isFinished()) {
			snapToScreen(currentScreen - 1);
		}
	}

	public void scrollRight() {
		if (currentScreen < getChildCount() - 1 && mScroller.isFinished()) {
			snapToScreen(currentScreen + 1);
		}
	}

}