/**
 * 
 */
package cn.seddat.href.client.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.Toast;
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.ContentService;
import cn.seddat.href.client.service.Post;
import cn.seddat.href.client.service.User;
import cn.seddat.href.client.view.RefreshableListView;
import cn.seddat.href.client.view.RefreshableListView.RefreshableListener;

/**
 * @author mzhgeng
 * 
 */
public class HrefActivity extends Activity implements RefreshableListener {

	private final String tag = HrefActivity.class.getSimpleName();
	private final String defaultUserIcon = String.valueOf(R.drawable.default_user_icon);
	private String appName;
	private ContentService contentService;
	private RefreshableListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		appName = getApplicationInfo().loadLabel(getPackageManager()).toString();
		// init
		contentService = new ContentService(this);
		List<Post> posts = null;
		try {
			posts = contentService.findPostByCache(0, null, 0);
		} catch (Exception e) {
			Log.e(tag, "find post by cache failed", e);
		}
		listView = (RefreshableListView) findViewById(R.id.post_list);
		listView.init(this, posts, R.layout.post_item, new String[] { "un", "ui", "com", "pt", "ttl", "sn", "mrk" },
				new int[] { R.id.author_name, R.id.author_icon, R.id.post_company, R.id.post_time, R.id.post_title,
						R.id.post_source, R.id.post_mark });
	}

	@Override
	protected void onStart() {
		super.onStart();
		// this.listView.refreshing();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private boolean refreshed = false;

	@Override
	public List<? extends Map<String, ?>> onRefresh(RefreshableListView listView) throws Exception {
		// args
		long time = 0;
		String item = null;
		// ListAdapter adapter = listView.getListAdapter();
		// if (adapter != null && adapter.getCount() > 0) {
		// Post post = (Post) adapter.getItem(0);
		// time = post.getCreateTime();
		// item = post.getId();
		// }
		// query
		Date cacheTime = new Date();
		List<Post> posts = contentService.findPostByServer(time, item, 0);
		contentService.clearCache(cacheTime);
		refreshed = true;
		return posts;
	}

	@Override
	public List<? extends Map<String, ?>> onLoad(RefreshableListView listView) throws Exception {
		// args
		long time = 0;
		String item = null;
		ListAdapter adapter = listView.getListAdapter();
		if (adapter != null && adapter.getCount() > 0) {
			Post post = (Post) adapter.getItem(adapter.getCount() - 1);
			time = post.getCreateTime();
			item = post.getId();
		}
		// query
		if (refreshed) {
			return contentService.findPostByServer(time, item, 1);
		} else {
			return contentService.findPostByCache(time, item, 1);
		}
	}

	@Override
	public void onItemClick(RefreshableListView listView, int position) throws Exception {
		ListAdapter adapter = listView.getListAdapter();
		Post post = (Post) adapter.getItem(position);
		Intent intent = new Intent(this, PostDetailActivity.class);
		for (String key : post.keySet()) {
			intent.putExtra(key, post.get(key));
		}
		this.startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(RefreshableListView listView, int position) throws Exception {
		return false;
	}

	@Override
	public void onDisplay(RefreshableListView listView, int firstVisible, int lastVisible) throws Exception {
		List<Post> posts = new ArrayList<Post>();
		List<String> uris = new ArrayList<String>();
		ListAdapter adapter = listView.getListAdapter();
		for (int i = firstVisible; i <= lastVisible; i++) {
			Post post = (Post) adapter.getItem(i);
			if (!defaultUserIcon.equals(post.get(User.COL_ICON))) {
				continue;
			}
			String uri = post.get(User.COL_ICON_URI);
			if (uri == null || uri.isEmpty()) {
				continue;
			}
			posts.add(post);
			uris.add(uri);
		}
		Map<String, String> icons = contentService.findUserIcon(uris);
		for (Post post : posts) {
			String uri = post.get(User.COL_ICON_URI);
			if (icons.containsKey(uri)) {
				post.put(User.COL_ICON, icons.get(uri));
			}
		}
	}

	private long backTime = 0;

	@Override
	public void onBackPressed() {
		long time = System.currentTimeMillis();
		if (time - backTime > 2000) {
			backTime = time;
			Toast.makeText(this, "再按一次退出" + appName, Toast.LENGTH_SHORT).show();
		} else {
			super.onBackPressed();
		}
	}

}
