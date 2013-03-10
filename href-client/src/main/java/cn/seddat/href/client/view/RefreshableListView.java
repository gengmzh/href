/**
 * 
 */
package cn.seddat.href.client.view;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.HeaderViewListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.ToastService;

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
	private static final int WHAT_STOPSCROLLING_DONE = 6;

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

	private int headerHeightThreshold = 72;
	private int headerHeight; // 增量
	private float lasty;
	private boolean isBackTop; // 是否回推完成
	private boolean isRefreshing; // 是否下拉刷新中
	private boolean isLoading; // 是否获取更多中
	private boolean isScrolling;

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
		DefaultListViewListener listener = new DefaultListViewListener();
		header.getViewTreeObserver().addOnPreDrawListener(listener);
		listView.setOnItemClickListener(listener);
		listView.setOnItemLongClickListener(listener);
		listView.setOnScrollListener(listener);
		listView.setOnTouchListener(this);
		Log.i(tag, "init " + RefreshableListView.class.getSimpleName() + " done");
	}

	private class DefaultListViewListener implements ViewTreeObserver.OnPreDrawListener,
			AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, AbsListView.OnScrollListener {

		@Override
		public boolean onPreDraw() {
			headerHeightThreshold = header.getMeasuredHeight();
			Log.i(tag, "default header height: " + headerHeightThreshold);
			headerHeightThreshold += headerHeightThreshold / 10;
			Log.i(tag, "header height threshold: " + headerHeightThreshold);
			header.getViewTreeObserver().removeOnPreDrawListener(this);
			return true;
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			if (refreshableListener != null) {
				if (!isScrolling && position < listView.getCount() - listView.getFooterViewsCount()) {
					try {
						refreshableListener.onItemClick(RefreshableListView.this, position);
					} catch (Exception e) {
						Log.e(tag, "click item failed", e);
					}
				}
			} else {
				Log.e(tag, RefreshableListener.class.getSimpleName() + " is null");
			}
		}

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			if (refreshableListener != null) {
				if (!isScrolling && position < listView.getCount() - listView.getFooterViewsCount()) {
					try {
						return refreshableListener.onItemLongClick(RefreshableListView.this, position);
					} catch (Exception e) {
						Log.e(tag, "click item failed", e);
					}
				}
			} else {
				Log.e(tag, RefreshableListener.class.getSimpleName() + " is null");
			}
			return false;
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
				if (refreshableListener != null) {
					try {
						int last = view.getLastVisiblePosition();
						last = Math.min(last, listView.getCount() - 1 - listView.getFooterViewsCount());
						displaying(view.getFirstVisiblePosition(), last);
					} catch (Exception e) {
						Log.e(tag, "invoke scroll idle event failed", e);
					}
				}
			}
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		}

	}

	@SuppressWarnings("unchecked")
	public void init(RefreshableListener listener, List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		refreshableListener = listener;
		this.data = new ArrayList<Map<String, Object>>();
		if (data != null && !data.isEmpty()) {
			this.data.addAll((List<Map<String, Object>>) data);
		}
		adapter = new SimpleAdapter(getContext(), this.data, resource, from, to);
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
		isScrolling = false;
		Log.i(tag, "isScrolling " + isScrolling);
		return false;
	}

	private boolean onPressMove(ListView view, MotionEvent event) {
		if (isRefreshing || isLoading) {
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
		if (delta != 0) {
			isScrolling = true;
		}
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
		Log.i(tag, "isScrolling " + isScrolling);
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
			public void run() {
				RefreshResult result = null;
				try {
					result = refreshableListener.onRefresh(RefreshableListView.this);
					if (result == null) {
						result = new RefreshResult();
						result.setStatus(1).setMessage("列表刷新失败");
					}
				} catch (Exception e) {
					result = new RefreshResult();
					result.setStatus(1).setMessage("网络不给力啊");
					Log.e(tag, "refreshing failed", e);
				}
				Log.i(tag, "[Refreshing] find " + result.getItemCount() + " items");
				Message msg = internalHandler.obtainMessage(WHAT_REFRESHING_DONE);
				msg.obj = result;
				msg.sendToTarget();
			};
		};
		th.start();
	}

	private void postRefreshing(RefreshResult result) {
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
		if (result.getStatus() != 0) {
			ToastService.toast(getContext(), result.getMessage(), Toast.LENGTH_LONG);
		} else {
			data.clear();
			data.addAll(result.getItems());
			adapter.notifyDataSetChanged();
		}
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
			public void run() {
				RefreshResult result = null;
				try {
					result = refreshableListener.onLoad(RefreshableListView.this);
					if (result == null) {
						result = new RefreshResult();
						result.setStatus(1).setMessage("列表加载失败");
					}
				} catch (Exception e) {
					result = new RefreshResult();
					result.setStatus(1).setMessage("网络不给力啊");
					Log.e(tag, "loading failed", e);
				}
				Log.i(tag, "[Loading] find " + result.getItemCount() + " items");
				Message msg = internalHandler.obtainMessage(WHAT_LOADING_DONE);
				msg.obj = result;
				msg.sendToTarget();
			};
		};
		th.start();
	}

	private void postLoading(RefreshResult result) {
		if (!isLoading) {
			return;
		}
		isLoading = false;
		footerProgress.setVisibility(View.GONE);
		if (result.getStatus() != 0) {
			footerTitle.setText(result.getMessage());
		} else {
			footerTitle.setText("加载更多");
			data.addAll(result.getItems());
			adapter.notifyDataSetChanged();
		}
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

	private void displaying() {
		Thread th = new Thread() {
			public void run() {
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					Log.e(tag, "displaying thread interrupted", e);
				}
				int i = 0;
				while (i++ < 5) {
					int first = listView.getFirstVisiblePosition(), last = listView.getLastVisiblePosition();
					if (first < last) {
						displaying(first, last);
						break;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Log.e(tag, "displaying thread interrupted", e);
						break;
					}
				}
			}
		};
		th.start();
	}

	private void displaying(final int firstVisible, final int lastVisible) {
		Log.i(tag, "[displaying] " + firstVisible + " ~ " + lastVisible);
		Thread th = new Thread() {
			public void run() {
				if (refreshableListener != null) {
					try {
						refreshableListener.onDisplay(RefreshableListView.this, firstVisible, lastVisible);
					} catch (Exception e) {
						Log.e(tag, "displaying failed", e);
					}
				} else {
					Log.e(tag, RefreshableListener.class.getSimpleName() + " is null");
				}
				Log.i(tag, "[displaying] find user icon");
				internalHandler.sendEmptyMessage(WHAT_STOPSCROLLING_DONE);
			};
		};
		th.start();
	}

	private void postDisplaying() {
		adapter.notifyDataSetChanged();
		Log.i(tag, "[displaying] done");
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
				RefreshResult result = (RefreshResult) msg.obj;
				postRefreshing(result);
				showFooter();
				displaying();
				break;
			}
			case WHAT_LOADING_START: {
				loading();
				break;
			}
			case WHAT_LOADING_DONE: {
				RefreshResult result = (RefreshResult) msg.obj;
				postLoading(result);
				break;
			}
			case WHAT_STOPSCROLLING_DONE: {
				postDisplaying();
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

		public RefreshResult onRefresh(RefreshableListView listView) throws Exception;

		public RefreshResult onLoad(RefreshableListView listView) throws Exception;

		public void onItemClick(RefreshableListView listView, int position) throws Exception;

		public boolean onItemLongClick(RefreshableListView listView, int position) throws Exception;

		public void onDisplay(RefreshableListView listView, int firstVisible, int lastVisible) throws Exception;

	}

	public static class RefreshResult {

		private int status;
		private String message;
		private List<? extends Map<String, ?>> data;

		public RefreshResult() {
			super();
		}

		public int getStatus() {
			return status;
		}

		public RefreshResult setStatus(int status) {
			this.status = status;
			return this;
		}

		public String getMessage() {
			return message;
		}

		public RefreshResult setMessage(String message) {
			this.message = message;
			return this;
		}

		public List<? extends Map<String, ?>> getData() {
			return data;
		}

		public RefreshResult setData(List<? extends Map<String, ?>> data) {
			this.data = data;
			return this;
		}

		public int getItemCount() {
			return data != null ? data.size() : 0;
		}

		@SuppressWarnings("unchecked")
		public List<Map<String, Object>> getItems() {
			if (data == null) {
				return Collections.emptyList();
			}
			return (List<Map<String, Object>>) data;
		}

	}

}
