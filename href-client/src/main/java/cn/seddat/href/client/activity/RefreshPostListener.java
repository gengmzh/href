/**
 * 
 */
package cn.seddat.href.client.activity;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ListAdapter;
import cn.seddat.href.client.R;
import cn.seddat.href.client.api.Post;
import cn.seddat.href.client.api.PostService;
import cn.seddat.href.client.view.RefreshableListView;

/**
 * @author mzhgeng
 * 
 */
public class RefreshPostListener implements RefreshableListView.RefreshableListener {

	private PostService postService;

	public RefreshPostListener() {
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
		String resId = String.valueOf(R.drawable.default_user_icon);
		for (Post post : posts) {
			post.setUserIcon(resId);
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
	public void onStopScrolling(RefreshableListView listView, int firstVisible, int lastVisible) throws Exception {
		Log.i(RefreshPostListener.class.getSimpleName(), "onStopScrolling");
	}

}
