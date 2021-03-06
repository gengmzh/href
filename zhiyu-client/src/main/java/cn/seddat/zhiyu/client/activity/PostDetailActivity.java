/**
 * 
 */
package cn.seddat.zhiyu.client.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.seddat.zhiyu.client.R;
import cn.seddat.zhiyu.client.service.ContentService;
import cn.seddat.zhiyu.client.service.Post;
import cn.seddat.zhiyu.client.service.ToastService;
import cn.seddat.zhiyu.client.service.Track;
import cn.seddat.zhiyu.client.service.TrackService;
import cn.seddat.zhiyu.client.service.User;

/**
 * @author mzhgeng
 * 
 */
public class PostDetailActivity extends Activity {

	private final String tag = PostDetailActivity.class.getSimpleName();
	private ContentService contentService;
	private PostDetailTask detailTask;
	private Post post;
	private ImageView markImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		this.setContentView(R.layout.post_detail);
		this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.title_back);
		// title
		TextView title = (TextView) this.findViewById(android.R.id.title);
		title.setText(R.string.page_label_post_detail);
		// show
		contentService = new ContentService(this);
		post = new Post();
		Bundle bundle = getIntent().getExtras();
		for (String key : bundle.keySet()) {
			post.put(key, bundle.getString(key));
		}
		TextView text = (TextView) findViewById(R.id.post_title);
		text.setText(post.getTitle());
		text = (TextView) findViewById(R.id.author_name);
		text.setText(post.get(User.COL_NAME));
		text = (TextView) findViewById(R.id.post_time);
		text.setText(post.get("pt"));
		text = (TextView) findViewById(R.id.post_company);
		if (post.getCompany() != null && post.getCompany().length() > 0) {
			text.setVisibility(View.VISIBLE);
			text.setText(post.getCompany());
		}
		text = (TextView) findViewById(R.id.post_source);
		text.setText(post.getSource());
		// text = (TextView) findViewById(R.id.post_mark);
		// text.setText(String.valueOf(post.getMark()));
		detailTask = new PostDetailTask();
		detailTask.execute(post);
		markImage = (ImageView) this.findViewById(R.id.action_mark);
	}

	public void goBack(View view) {
		this.onBackPressed();
	}

	public void onMark(View view) {
		new MarkPostTask().execute(post);
	}

	public void onRefresh(View view) {
		if (detailTask == null
				|| detailTask.getStatus() == AsyncTask.Status.FINISHED) {
			detailTask = new PostDetailTask();
		}
		if (detailTask.getStatus() == AsyncTask.Status.PENDING) {
			detailTask.execute(post);
		} else {
			ToastService.toast(this, "正在刷新", Toast.LENGTH_SHORT);
		}
	}

	public void onSource(View view) {
		String link = post.getLink();
		if (link == null || link.length() == 0) {
			ToastService.toast(this, "没有原文", Toast.LENGTH_SHORT);
			return;
		}
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
		this.startActivity(intent);
	}

	public void onShare(View view) {
		ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
	}

	class PostDetailTask extends AsyncTask<Post, Integer, Post> {

		@Override
		protected Post doInBackground(Post... params) {
			if (params == null || params.length < 1) {
				return null;
			}
			Post post = params[0];
			try {
				TrackService.cacheTrack(PostDetailActivity.this, new Track()
						.setAction(Track.ACTION_CLICK).setValue(post.getId()));
			} catch (Exception e) {
				Log.e(tag, "track click failed", e);
			}
			try {
				post = contentService.findPostDetail(post);
			} catch (Exception e) {
				// post.remove(Post.COL_CONTENT);
				post = null;
				Log.e(tag, "find post by id failed", e);
			}
			return post;
		}

		@Override
		protected void onPostExecute(Post post) {
			super.onPostExecute(post);
			if (post == null || post.getContent() == null) {
				ToastService.toast(PostDetailActivity.this, "网络不给力啊",
						Toast.LENGTH_SHORT);
				return;
			}
			if (post.getContent().length() > 0) {
				TextView text = (TextView) findViewById(R.id.post_content);
				text.setText(Html.fromHtml(post.getContent()));
			}
			if (post.isLiked()) {
				markImage.setImageResource(R.drawable.action_mark_on);
			}
		}

	}

	class MarkPostTask extends AsyncTask<Post, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(Post... params) {
			Post post = params != null && params.length > 0 ? params[0] : null;
			if (post == null) {
				return false;
			}
			try {
				TrackService.cacheTrack(PostDetailActivity.this, new Track()
						.setAction(Track.ACTION_MARK).setValue(post.getId()));
			} catch (Exception e) {
				Log.e(tag, "track click failed", e);
			}
			try {
				contentService.markPost(post, !post.isLiked());
			} catch (Exception e) {
				Log.e(tag, "mark post failed", e);
				return false;
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result == null || !result) {
				ToastService.toast(PostDetailActivity.this, "网络不给力啊",
						Toast.LENGTH_SHORT);
			} else {
				markImage
						.setImageResource(post.isLiked() ? R.drawable.action_mark_on
								: R.drawable.action_mark_off);
				ToastService.toast(PostDetailActivity.this,
						post.isLiked() ? "已添加收藏" : "已取消收藏", Toast.LENGTH_SHORT);
			}
		}
	}

	@Override
	public void onBackPressed() {
		ToastService.cancel();
		super.onBackPressed();
	}

}
