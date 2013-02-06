/**
 * 
 */
package cn.seddat.href.client.activity;

import java.util.List;

import android.content.Context;
import android.widget.ListAdapter;
import android.widget.SimpleAdapter;
import cn.seddat.href.client.R;
import cn.seddat.href.client.api.Post;
import cn.seddat.href.client.api.PostService;
import cn.seddat.href.client.view.OnRefreshListener;

/**
 * @author mzhgeng
 * 
 */
public class RefreshPostListener implements OnRefreshListener {

	private PostService postService;
	private String[] from;
	private int[] to;

	public RefreshPostListener() {
		postService = new PostService();
		from = new String[] { "au", "com", "pt", "ttl", "pv", "clk", "mrk" };
		to = new int[] { R.id.author_name, R.id.post_company, R.id.post_time, R.id.post_title, R.id.post_pv,
				R.id.post_click, R.id.post_mark };
	}

	@Override
	public ListAdapter onRefresh(Context context, Object firstItem) throws Exception {
		Post item = (Post) firstItem;
		long st = (item != null ? item.getCreateTime() : 0);
		List<Post> posts = postService.query(st, 0);
		return new SimpleAdapter(context, posts, R.layout.post_item, from, to);
	}

	@Override
	public ListAdapter onLoadMore(Context context, Object lastItem) throws Exception {
		Post item = (Post) lastItem;
		long et = (item != null ? item.getCreateTime() : 0);
		List<Post> posts = postService.query(0, et);
		return new SimpleAdapter(context, posts, R.layout.post_item, from, to);
	}

}
