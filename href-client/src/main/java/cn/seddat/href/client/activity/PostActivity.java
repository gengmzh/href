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
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.ContentService;
import cn.seddat.href.client.service.Post;
import cn.seddat.href.client.service.User;
import cn.seddat.href.client.view.RefreshableListView;
import cn.seddat.href.client.view.RefreshableListView.RefreshResult;
import cn.seddat.href.client.view.RefreshableListView.RefreshableListener;

/**
 * @author mzhgeng
 * 
 */
public class PostActivity extends Activity implements RefreshableListener {

	private final String tag = PostActivity.class.getSimpleName();
	private final String defaultUserIcon = String.valueOf(R.drawable.default_user_icon);
	private final int limit = 20;
	private ContentService contentService;
	private RefreshableListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.post_list);
		// init
		contentService = new ContentService(this);
		List<Post> posts = null;
		try {
			posts = contentService.findPostByCache(0, null, 0, limit);
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
	public RefreshResult onRefresh(RefreshableListView listView) throws Exception {
		// args
		long time = 0;
		String item = null;
		ListAdapter adapter = listView.getListAdapter();
		if (adapter != null && adapter.getCount() > 0) {
			Post post = (Post) adapter.getItem(0);
			time = post.getCreateTime();
			item = post.getId();
		}
		// query
		RefreshResult result = new RefreshResult();
		Date cacheTime = new Date();
		List<Post> posts = contentService.findPostByServer(time, item, 0, limit);
		Log.i(tag, "[Refreshing] find " + posts.size() + " items");
		if (posts.size() < limit) {
			int count = Math.min(limit - posts.size(), adapter != null ? adapter.getCount() : 0);
			for (int i = 0; i < count; i++) {
				posts.add((Post) adapter.getItem(i));
			}
			refreshed = false;
		} else {
			contentService.clearCache(cacheTime);
			Log.i(tag, "[Refreshing] clear cache");
			refreshed = true;
		}
		result.setData(posts);
		return result;
	}

	@Override
	public RefreshResult onLoad(RefreshableListView listView) throws Exception {
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
		RefreshResult result = new RefreshResult();
		if (refreshed) {
			try {
				result.setData(contentService.findPostByServer(time, item, 1, limit));
				Log.i(tag, "[Loading] find " + result.getItemCount() + " items by server");
			} catch (Exception ex) {
				Log.i(tag, "loading by server failed", ex);
				result.setStatus(1).setMessage("网络不给力啊");
			}
		} else {
			try {
				result.setData(contentService.findPostByCache(time, item, 1, limit));
				Log.i(tag, "[Loading] find " + result.getItemCount() + " items by cache");
			} catch (Exception ex) {
				Log.i(tag, "loading by cache failed", ex);
				result.setStatus(1).setMessage("缓存加载失败");
			}
			if (result.getStatus() == 0 && result.getItemCount() == 0) {
				try {
					result.setData(contentService.findPostByServer(time, item, 1, limit));
					Log.i(tag, "[Loading] find " + result.getItemCount() + " items by server");
				} catch (Exception ex) {
					Log.i(tag, "loading by server failed", ex);
					result.setStatus(1).setMessage("网络不给力啊");
				}
			}
		}
		if (result.getStatus() == 0 && result.getItemCount() == 0) {
			result.setStatus(1).setMessage("没有了哟，亲");
		}
		return result;
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
			if (uri == null || uri.length() == 0) {
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
