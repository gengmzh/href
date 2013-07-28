/**
 * 
 */
package cn.seddat.zhiyu.client.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import cn.seddat.zhiyu.client.R;
import cn.seddat.zhiyu.client.service.CacheService;
import cn.seddat.zhiyu.client.service.ContentService;
import cn.seddat.zhiyu.client.service.Post;
import cn.seddat.zhiyu.client.service.ToastService;
import cn.seddat.zhiyu.client.service.User;

/**
 * @author mzhgeng
 * 
 */
public class PostMarkActivity extends Activity implements AdapterView.OnItemClickListener, AbsListView.OnScrollListener {

	private final String tag = PostMarkActivity.class.getSimpleName();
	private final String defaultUserIcon = String.valueOf(R.drawable.default_user_icon);
	private final int limit = 1000;
	private ContentService contentService;
	private CacheService cacheService;

	private List<Post> posts;
	private SimpleAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_mark);
		// init
		contentService = new ContentService(this);
		cacheService = new CacheService(this);
		posts = new ArrayList<Post>();
		adapter = new SimpleAdapter(this, posts, R.layout.post_item, new String[] { "un", "ui", "com", "pt", "ttl",
				"sn" }, new int[] { R.id.author_name, R.id.author_icon, R.id.post_company, R.id.post_time,
				R.id.post_title, R.id.post_source });
		ListView listView = (ListView) findViewById(R.id.post_list);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		List<Post> pl = null;
		try {
			pl = contentService.findMarkedPost(limit);
		} catch (Exception e) {
			Log.e(tag, "find post by cache failed", e);
		}
		if (pl != null) {
			posts.clear();
			if (!pl.isEmpty()) {
				posts.addAll(pl);
			} else {
				ToastService.toast(this, "没有收藏记录", Toast.LENGTH_SHORT);
			}
			adapter.notifyDataSetChanged();
			if (!posts.isEmpty()) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							Log.e(tag, "displaying thread interrupted", e);
						}
						onScrollStateChanged((ListView) findViewById(R.id.post_list),
								AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
					}
				}).start();
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Post post = (Post) adapter.getItem(position);
		Intent intent = new Intent(this, PostDetailActivity.class);
		for (String key : post.keySet()) {
			intent.putExtra(key, post.get(key));
		}
		this.startActivity(intent);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
		if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
			ListView list = (ListView) view;
			int first = Math.max(list.getFirstVisiblePosition(), list.getHeaderViewsCount());
			int last = Math.min(list.getLastVisiblePosition(), view.getCount() - list.getFooterViewsCount() - 1);
			new DisplayTask().execute(first, last);
		}
	}

	class DisplayTask extends AsyncTask<Integer, Integer, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			if (params == null || params.length < 2) {
				return false;
			}
			List<String> uris = new ArrayList<String>();
			for (int i = params[0]; i <= params[1]; i++) {
				Post post = (Post) adapter.getItem(i);
				if (!defaultUserIcon.equals(post.get(User.COL_ICON))) {
					continue;
				}
				String uri = post.get(User.COL_ICON_URI);
				if (uri != null && uri.length() > 0) {
					uris.add(uri);
				}
			}
			if (uris.isEmpty()) {
				return false;
			}
			Map<String, String> icons = null;
			try {
				icons = cacheService.findUserIcon(uris);
			} catch (Exception e) {
				Log.e(tag, "find user icon failed", e);
			}
			if (icons == null || icons.isEmpty()) {
				return false;
			}
			for (int i = params[0]; i <= params[1]; i++) {
				Post post = (Post) adapter.getItem(i);
				String uri = post.get(User.COL_ICON_URI);
				if (icons.containsKey(uri)) {
					post.put(User.COL_ICON, icons.get(uri));
				}
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	@Override
	public void onBackPressed() {
		Activity parent = getParent();
		if (parent != null) {
			parent.onBackPressed();
		} else {
			super.onBackPressed();
		}
	}

}
