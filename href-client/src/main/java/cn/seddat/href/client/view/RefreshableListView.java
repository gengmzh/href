/**
 * 
 */
package cn.seddat.href.client.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import cn.seddat.href.client.R;

/**
 * @author mzhgeng
 * 
 */
public class RefreshableListView extends LinearLayout implements OnTouchListener {

	private static final String KEY_REFRESHING_TIME = "refreshing-time";

	private static final int WHAT_HEADER_HEIGHT = 1;
	private static final int WHAT_REFRESHING_START = 2;
	private static final int WHAT_REFRESHING_DONE = 3;
	private static final int WHAT_LOADING_START = 4;
	private static final int WHAT_LOADING_DONE = 5;

	// private static final int DEFAULT_HEADER_VIEW_HEIGHT = 105; // 头部文件原本的高度
	private static final int AUTO_INCREMENTAL = 10; // 自增量，用于回弹

	private final String tag = RefreshableListView.class.getSimpleName();
	private View header;
	private TextView headerTitle;
	private TextView headerSubtitle;
	private ImageView headerArrow;
	private ProgressBar headerProgress;
	private ListView listView;
	private View footer;
	private TextView footerTitle;
	private ProgressBar footerProgress;

	private RefreshableListener refreshableListener;
	private List<Map<String, Object>> data;
	private SimpleAdapter adapter;

	private int headerHeightThreshold = 100;
	private int headerHeight; // 增量
	private float lasty;
	private boolean isBackTop; // 是否回推完成
	private boolean isRefreshing; // 是否下拉刷新中
	private boolean isLoading; // 是否获取更多中
	private boolean stopMove;

	public RefreshableListView(Context context) {
		super(context);
		this.init();
	}

	public RefreshableListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.init();
	}

	private void init() {
		this.setOrientation(LinearLayout.VERTICAL);
		// header
		header = LayoutInflater.from(getContext()).inflate(R.layout.refreshable_header, null);
		headerTitle = (TextView) header.findViewById(R.id.refreshable_title);
		headerSubtitle = (TextView) header.findViewById(R.id.refreshable_subtitle);
		headerArrow = (ImageView) header.findViewById(R.id.refreshable_arrow);
		headerProgress = (ProgressBar) header.findViewById(R.id.refreshable_progress);
		addView(header, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		headerSubtitle.setText(this.getRefreshingTime());
		// list
		listView = new ListView(getContext());
		addView(listView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		// footer
		footer = LayoutInflater.from(getContext()).inflate(R.layout.refreshable_footer, null);
		footerTitle = (TextView) footer.findViewById(R.id.refreshable_title);
		footerProgress = (ProgressBar) footer.findViewById(R.id.refreshable_progress);
		// event
		listView.setOnTouchListener(this);
		listView.setOnItemClickListener(new ClickItemListener());
		header.getViewTreeObserver().addOnPreDrawListener(new MeasureHeaderHeight());
		Log.i(tag, "init " + RefreshableListView.class.getSimpleName() + " done");
	}

	private class MeasureHeaderHeight implements ViewTreeObserver.OnPreDrawListener {
		@Override
		public boolean onPreDraw() {
			headerHeightThreshold = header.getMeasuredHeight();
			Log.i(tag, "default header height: " + headerHeightThreshold);
			headerHeightThreshold += headerHeightThreshold / 10;
			Log.i(tag, "header height threshold: " + headerHeightThreshold);
			header.getViewTreeObserver().removeOnPreDrawListener(this);
			return true;
		}
	}

	private class ClickItemListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (refreshableListener != null) {
				try {
					refreshableListener.onClick(RefreshableListView.this, position);
				} catch (Exception e) {
					Log.e(tag, "click item failed", e);
				}
			} else {
				Log.e(tag, RefreshableListener.class.getSimpleName() + " is null");
			}
		}
	}

	public void init(RefreshableListener listener, int resource, String[] from, int[] to) {
		refreshableListener = listener;
		data = new ArrayList<Map<String, Object>>();
		adapter = new SimpleAdapter(getContext(), data, resource, from, to);
		listView.setAdapter(adapter);
		this.refreshing();
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (!ListView.class.isInstance(v)) {
			Log.w(tag, "View " + v.getClass().getName() + " isn't " + ListView.class.getSimpleName());
			return false;
		}
		ListView view = (ListView) v;
		boolean handled = false;
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			handled = this.onPressDown(view, event);
			break;
		case MotionEvent.ACTION_MOVE:
			handled = this.onPressMove(view, event);
			break;
		case MotionEvent.ACTION_UP:
			handled = this.onPressUp(view, event);
			break;
		}
		return handled;
	}

	private boolean onPressDown(ListView view, MotionEvent event) {
		lasty = event.getRawY();
		isBackTop = false;
		stopMove = isRefreshing || isLoading;
		return false;
	}

	private boolean onPressMove(ListView view, MotionEvent event) {
		if (stopMove) {
			return true;
		}
		int childCount = listView.getChildCount();
		if (childCount == 0) {
			return false;
		}
		float y = event.getRawY();
		// 下拉后再回推到顶端时，不用处理
		if (isBackTop) {
			lasty = y;
			return true;
		}
		float delta = y - lasty;
		int incr = (int) Math.ceil(delta / 2);
		// 下拉
		if (header.getLayoutParams().height > 0 && delta < 0) {
			headerHeight = Math.max(headerHeight + incr, 0);
			this.setHeaderHeight(headerHeight);
			if (headerHeight <= 0) {
				isBackTop = true;
			}
			lasty = y;
			return true;
		}
		// 触顶
		final int firstTop = listView.getChildAt(0).getTop();
		final int listPadding = listView.getListPaddingTop();
		if (listView.getFirstVisiblePosition() <= 0 && firstTop >= listPadding && delta > 0) {
			headerHeight += incr;
			if (headerHeight >= 0) {
				setHeaderHeight(headerHeight);
			}
			lasty = y;
			return true;
		}
		// 触底
		final int bottom = listView.getChildAt(childCount - 1).getBottom();
		final int height = listView.getHeight() - listView.getPaddingBottom();
		if (listView.getLastVisiblePosition() == listView.getCount() - 1 && bottom <= height && delta < 0) {
			if (isFullScreen()) {
				internalHandler.sendEmptyMessage(WHAT_LOADING_START);
				lasty = y;
				return true;
			}
		}
		return false;
	}

	private boolean onPressUp(ListView view, MotionEvent event) {
		float y = event.getRawY();
		boolean handled = false;
		if (header.getLayoutParams().height > 0) {
			Timer timer = new Timer(true);
			timer.scheduleAtFixedRate(new RollUpHeaderTask(), 0, 10);
			lasty = y;
			handled = true;
		}
		return handled;
	}

	private void refreshing() {
		if (isRefreshing) {
			return;
		}
		Log.i(tag, "[Refreshing] starts...");
		isRefreshing = true;
		headerTitle.setText("正在刷新");
		headerArrow.clearAnimation();
		headerArrow.setVisibility(View.GONE);
		headerProgress.setVisibility(View.VISIBLE);
		header.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
		header.setLayoutParams(header.getLayoutParams());
		// header.setVisibility(View.VISIBLE);
		Thread th = new Thread() {
			@SuppressWarnings("unchecked")
			public void run() {
				List<Map<String, Object>> items = null;
				if (refreshableListener != null) {
					try {
						items = (List<Map<String, Object>>) refreshableListener.onRefresh(RefreshableListView.this);
						data.addAll(0, items);
					} catch (Exception e) {
						Log.e(tag, "refreshing failed", e);
					}
				} else {
					Log.e(tag, RefreshableListener.class.getSimpleName() + " is null");
				}
				Log.i(tag, "[Refreshing] find " + (items != null ? items.size() : 0) + " items");
				internalHandler.sendEmptyMessage(WHAT_REFRESHING_DONE);
			};
		};
		th.start();
	}

	private void postRefreshing() {
		if (!isRefreshing) {
			return;
		}
		isRefreshing = false;
		TextView title = (TextView) header.findViewById(R.id.refreshable_title);
		title.setText("下拉刷新");
		ImageView arrow = (ImageView) header.findViewById(R.id.refreshable_arrow);
		arrow.setVisibility(View.VISIBLE);
		ProgressBar progress = (ProgressBar) header.findViewById(R.id.refreshable_progress);
		progress.setVisibility(View.GONE);
		header.getLayoutParams().height = 0;
		header.setLayoutParams(header.getLayoutParams());
		headerHeight = 0;
		// header.setVisibility(View.GONE);
		adapter.notifyDataSetChanged();
		this.saveRefreshingTime();
		headerSubtitle.setText(this.getRefreshingTime());
		Log.i(tag, "[Refreshing] done");
	}

	private String getRefreshingTime() {
		SharedPreferences pref = this.getPreference();
		long millis = pref.getLong(KEY_REFRESHING_TIME, 0);
		if (millis <= 0) {
			return "上次刷新：未知";
		}
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(millis);
		int day = cal.get(Calendar.HOUR_OF_DAY);
		int min = cal.get(Calendar.MINUTE);
		return "上次刷新：" + (cal.get(Calendar.MONTH) + 1) + "月" + cal.get(Calendar.DAY_OF_MONTH) + "日 "
				+ (day < 10 ? "0" : "") + day + ":" + (min < 10 ? "0" : "") + min;
	}

	private void saveRefreshingTime() {
		SharedPreferences pref = this.getPreference();
		Editor editor = pref.edit();
		editor.putLong(KEY_REFRESHING_TIME, new Date().getTime());
		editor.commit();
		Log.i(tag, "save refreshing time: " + pref.getLong(KEY_REFRESHING_TIME, 0));
	}

	private SharedPreferences getPreference() {
		SharedPreferences pref = getContext().getSharedPreferences(RefreshableListView.class.getSimpleName(),
				Context.MODE_PRIVATE);
		return pref;
	}

	private void setHeaderHeight(int height) {
		// height
		headerHeight = height;
		header.getLayoutParams().height = height;
		header.setLayoutParams(header.getLayoutParams());
		// status
		String ttl = headerTitle.getText().toString();
		if (header.getLayoutParams().height >= headerHeightThreshold) {
			if (!"松开刷新".equals(ttl)) {
				headerTitle.setText("松开刷新");
				RotateAnimation anim = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				anim.setDuration(250);
				anim.setFillAfter(true);
				headerArrow.startAnimation(anim);
			}
		} else {
			if (!"下拉刷新".equals(ttl)) {
				headerTitle.setText("下拉刷新");
				RotateAnimation anim = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF, 0.5f,
						Animation.RELATIVE_TO_SELF, 0.5f);
				anim.setDuration(250);
				anim.setFillAfter(true);
				headerArrow.startAnimation(anim);
			}
		}
	}

	private class RollUpHeaderTask extends TimerTask {

		private boolean refreshing = false;
		private int minHeight;

		public RollUpHeaderTask() {
			refreshing = headerHeight >= headerHeightThreshold;
			minHeight = refreshing ? headerHeightThreshold : 0;
		}

		@Override
		public void run() {
			headerHeight = Math.max(headerHeight - AUTO_INCREMENTAL, minHeight);
			internalHandler.sendEmptyMessage(WHAT_HEADER_HEIGHT);
			if (headerHeight == minHeight) {
				if (refreshing) {
					internalHandler.sendEmptyMessage(WHAT_REFRESHING_START);
				}
				cancel();
			}
		}
	}

	private void loading() {
		if (isLoading) {
			return;
		}
		Log.i(tag, "[Loading] starts...");
		isLoading = true;
		footerTitle.setText("正在加载...");
		footerProgress.setVisibility(View.VISIBLE);
		Thread th = new Thread() {
			@SuppressWarnings("unchecked")
			public void run() {
				List<Map<String, Object>> items = null;
				if (refreshableListener != null) {
					try {
						items = (List<Map<String, Object>>) refreshableListener.onLoad(RefreshableListView.this);
						data.addAll(items);
					} catch (Exception e) {
						Log.e(tag, "refreshing failed", e);
					}
				} else {
					Log.e(tag, RefreshableListener.class.getSimpleName() + " is null");
				}
				Log.i(tag, "[Loading] find " + (items != null ? items.size() : 0) + " items");
				internalHandler.sendEmptyMessage(WHAT_LOADING_DONE);
			};
		};
		th.start();
	}

	private void postLoading() {
		if (!isLoading) {
			return;
		}
		isLoading = false;
		footerTitle.setText("加载更多");
		footerProgress.setVisibility(View.GONE);
		adapter.notifyDataSetChanged();
		Log.i(tag, "[Loading] done");
	}

	private void showFooter() {
		if (listView.getFooterViewsCount() == 0 && isFullScreen()) {
			listView.addFooterView(footer);
			listView.setAdapter(listView.getAdapter());
		}
	}

	private boolean isFullScreen() {
		int total = listView.getCount();
		int visible = listView.getLastVisiblePosition() - listView.getFirstVisiblePosition() + 1;
		return visible < total;
	}

	private Handler internalHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case WHAT_HEADER_HEIGHT: {
				setHeaderHeight(headerHeight);
				break;
			}
			case WHAT_REFRESHING_START: {
				refreshing();
				break;
			}
			case WHAT_REFRESHING_DONE: {
				postRefreshing();
				showFooter();
				break;
			}
			case WHAT_LOADING_START: {
				loading();
				break;
			}
			case WHAT_LOADING_DONE: {
				postLoading();
				break;
			}
			}
		}

	};

	public ListAdapter getListAdapter() {
		ListAdapter adapter = listView.getAdapter();
		if (HeaderViewListAdapter.class.isInstance(adapter)) {
			return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
		}
		return adapter;
	}

	public boolean isFooterShown() {
		return listView.getFooterViewsCount() > 0;
	}

	public interface RefreshableListener {

		public List<? extends Map<String, ?>> onRefresh(RefreshableListView listView) throws Exception;

		public List<? extends Map<String, ?>> onLoad(RefreshableListView listView) throws Exception;

		public void onClick(RefreshableListView listView, int position) throws Exception;

	}

}
