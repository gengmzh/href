/**
 * 
 */
package cn.seddat.href.client.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;
import android.widget.ListAdapter;
import cn.seddat.href.client.R;
import cn.seddat.href.client.api.Post;
import cn.seddat.href.client.api.PostService;
import cn.seddat.href.client.api.User;
import cn.seddat.href.client.api.UserService;
import cn.seddat.href.client.view.RefreshableListView;

/**
 * @author mzhgeng
 * 
 */
public class RefreshPostListener implements RefreshableListView.RefreshableListener {

	private final String tag = RefreshPostListener.class.getSimpleName();
	private UserService userService;
	private PostService postService;
	private final String defaultUserIcon = String.valueOf(R.drawable.default_user_icon);

	public RefreshPostListener() {
		userService = new UserService();
		postService = new PostService();
	}

	@Override
	public List<? extends Map<String, ?>> onRefresh(RefreshableListView listView) throws Exception {
		// first
		long time = 0;
		String item = null;
		ListAdapter adapter = listView.getListAdapter();
		if (adapter != null && adapter.getCount() > 0) {
			Post post = (Post) adapter.getItem(0);
			time = post.getCreateTime();
			item = post.getId();
		}
		// query
		List<Post> posts = this.findPost(time, item, 0);
		return posts;
	}

	private List<Post> findPost(long time, String item, int order) throws Exception {
		List<Post> posts = postService.query(time, item, order);
		for (Post post : posts) {
			post.put(User.COL_ICON, defaultUserIcon);
		}
		return posts;
	}

	@Override
	public List<? extends Map<String, ?>> onLoad(RefreshableListView listView) throws Exception {
		// last
		long time = 0;
		String item = null;
		ListAdapter adapter = listView.getListAdapter();
		if (adapter != null && adapter.getCount() > 0) {
			Post post = (Post) adapter.getItem(adapter.getCount() - 1);
			time = post.getCreateTime();
			item = post.getId();
		}
		// query
		List<Post> posts = this.findPost(time, item, 1);
		return posts;
	}

	@Override
	public void onItemClick(RefreshableListView listView, int position) throws Exception {
		Context context = listView.getContext();
		ListAdapter adapter = listView.getListAdapter();
		Post post = (Post) adapter.getItem(position);
		Intent intent = new Intent(context, PostDetailActivity.class);
		intent.putExtra(Intent.EXTRA_UID, post.getId());
		context.startActivity(intent);
	}

	@Override
	public boolean onItemLongClick(RefreshableListView listView, int position) throws Exception {
		return false;
	}

	@Override
	public void onDisplay(RefreshableListView listView, int firstVisible, int lastVisible) throws Exception {
		// cache
		File cache = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			cache = new File(Environment.getExternalStorageDirectory(), listView.getContext().getPackageName());
			cache = new File(cache, "cache");
			cache.mkdirs();
		} else {
			cache = listView.getContext().getCacheDir();
		}
		Log.i(tag, "using cache " + cache.getAbsolutePath());
		// icon
		ListAdapter adapter = listView.getListAdapter();
		lastVisible = Math.min(lastVisible, adapter.getCount());
		for (int i = firstVisible; i <= lastVisible; i++) {
			Post post = (Post) adapter.getItem(i);
			String iconUri = post.get(User.COL_ICON_URI);
			if (!defaultUserIcon.equals(post.get(User.COL_ICON)) || iconUri == null || iconUri.isEmpty()) {
				continue;
			}
			// find cache
			int index = iconUri.lastIndexOf("/");
			String icon = index > 0 ? iconUri.substring(index + 1) : iconUri;
			File file = new File(cache, icon);
			if (file.exists()) {
				if (file.isFile()) {
					post.put(User.COL_ICON, file.getAbsolutePath());
					continue;
				} else {
					this.delete(file);
					Log.w(tag, "delete illegal cache file " + file.getAbsolutePath());
				}
			}
			// find server
			byte[] bytes = null;
			try {
				bytes = userService.getUserIcon(iconUri);
			} catch (Exception ex) {
				Log.w(icon, "fetch user icon failed", ex);
			}
			if (bytes != null && bytes.length > 0) {
				FileOutputStream out = new FileOutputStream(file);
				out.write(bytes);
				out.close();
				post.put(User.COL_ICON, file.getAbsolutePath());
			}
		}
	}

	private void delete(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				this.delete(f);
			}
		}
		file.delete();
	}

}
