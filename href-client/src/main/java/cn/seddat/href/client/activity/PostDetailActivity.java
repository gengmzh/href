/**
 * 
 */
package cn.seddat.href.client.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.ContentService;
import cn.seddat.href.client.service.Post;
import cn.seddat.href.client.service.ToastService;
import cn.seddat.href.client.service.User;

/**
 * @author mzhgeng
 * 
 */
public class PostDetailActivity extends Activity {

	private final String tag = PostDetailActivity.class.getSimpleName();
	private PostDetailTask postDetailTask;

	private boolean marked;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		this.setContentView(R.layout.post_detail);
		this.getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_back);
		// title
		TextView title = (TextView) this.findViewById(android.R.id.title);
		title.setText(R.string.page_label_post_detail);
		postDetailTask = new PostDetailTask();
		// show
		Post post = new Post();
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
		text = (TextView) findViewById(R.id.post_mark);
		text.setText(String.valueOf(post.getMark()));
		// TODO
		marked = false;
		postDetailTask.execute(post);
	}

	public void goBack(View view) {
		this.onBackPressed();
	}

	public void onMark(View view) {
		ImageView image = (ImageView) view;
		marked = !marked;
		image.setImageResource(marked ? R.drawable.menu_mark_on : R.drawable.menu_mark_off);
		ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
	}

	public void onSource(View view) {
		ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
	}

	public void onShare(View view) {
		ToastService.toast(this, "敬请期待", Toast.LENGTH_SHORT);
	}

	class PostDetailTask extends AsyncTask<Post, Integer, Post> {

		private ContentService postService;

		public PostDetailTask() {
			postService = new ContentService(PostDetailActivity.this);
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Post doInBackground(Post... params) {
			Post post = params.length > 0 ? params[0] : null;
			if (post != null && post.getId() != null && post.getId().length() > 0) {
				try {
					String ctt = postService.findPostContent(post.getId());
					post.setContent(ctt);
				} catch (Exception e) {
					post.remove(Post.COL_CONTENT);
					Log.e(tag, "find post by id failed", e);
				}
			}
			return post;
		}

		@Override
		protected void onPostExecute(Post post) {
			super.onPostExecute(post);
			if (post == null || post.getContent() == null) {
				ToastService.toast(PostDetailActivity.this, "网络不给力啊", Toast.LENGTH_SHORT);
				return;
			}
			if (post.getContent().length() > 0) {
				TextView text = (TextView) findViewById(R.id.post_content);
				text.setText(Html.fromHtml(post.getContent()));
			}
		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
	}

	@Override
	public void onBackPressed() {
		ToastService.cancel();
		super.onBackPressed();
	}

}
