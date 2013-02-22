/**
 * 
 */
package cn.seddat.href.client.activity;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
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
	private String[] from;
	private int[] to;

	public RefreshPostListener() {
		postService = new PostService();
		from = new String[] { "au", "pt", "ttl", "sn", "pv", "clk", "mrk" };
		to = new int[] { R.id.author_name, R.id.post_time, R.id.post_title, R.id.post_source, R.id.post_pv,
				R.id.post_click, R.id.post_mark };
	}

	@Override
	public ListAdapter onRefresh(RefreshableListView listView) throws Exception {
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
		if (adapter != null) {
			for (int i = 0; i < adapter.getCount(); i++) {
				posts.add((Post) adapter.getItem(i));
			}
		}
		return new SimpleAdapter(listView.getContext(), posts, R.layout.post_item, from, to);
	}

	@Override
	public ListAdapter onLoad(RefreshableListView listView) throws Exception {
		// last
		long time = 0;
		String item = null;
		ListAdapter adapter = listView.getListAdapter();
		if (adapter != null && adapter.getCount() > 0) {
			Post post = (Post) adapter.getItem(adapter.getCount() - 2);
			time = post.getCreateTime();
			item = post.getId();
		}
		// query
		List<Post> posts = postService.query(time, item, 1);
		if (adapter != null) {
			for (int i = adapter.getCount() - 1; i >= 0; i--) {
				posts.add(0, (Post) adapter.getItem(i));
			}
		}
		return new SimpleAdapter(listView.getContext(), posts, R.layout.post_item, from, to);
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
