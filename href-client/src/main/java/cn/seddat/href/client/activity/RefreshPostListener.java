/**
 * 
 */
package cn.seddat.href.client.activity;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.widget.ListAdapter;
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
		List<Post> posts = postService.query(time, item, 0);
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
		List<Post> posts = postService.query(time, item, 1);
		return posts;
	}

	@Override
	public void onClick(RefreshableListView listView, int position) throws Exception {
		Context context = listView.getContext();
		ListAdapter adapter = listView.getListAdapter();
		Post post = (Post) adapter.getItem(position);
		Intent intent = new Intent(context, PostDetailActivity.class);
		intent.putExtra(Intent.EXTRA_UID, post.getId());
		context.startActivity(intent);
	}

}
