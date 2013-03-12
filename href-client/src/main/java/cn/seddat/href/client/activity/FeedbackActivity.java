package cn.seddat.href.client.activity;

import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.ContentService;
import cn.seddat.href.client.service.HttpRequest;
import cn.seddat.href.client.service.ToastService;

public class FeedbackActivity extends Activity {

	private final String tag = FeedbackActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.feedback);
	}

	public void sendAdvice(View view) {
		EditText advice = (EditText) findViewById(R.id.feedback_advice);
		if (advice.getText().length() == 0) {
			advice.setHint(R.string.feedback_advice_error_message);
			advice.requestFocus();
			return;
		}
		// send
		HttpRequest.Parameter args = new HttpRequest.Parameter();
		args.set("feed", advice.getText().toString());
		RatingBar rating = (RatingBar) findViewById(R.id.feedback_rating);
		if (rating.getRating() > 0.0f) {
			args.set("rating", String.valueOf(rating.getRating()));
		}
		EditText email = (EditText) findViewById(R.id.feedback_email);
		if (email.getText().length() > 0) {
			args.set("email", email.getText().toString());
		}
		new FeedbackTask().execute(args);
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

	class FeedbackTask extends AsyncTask<HttpRequest.Parameter, Integer, Boolean> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(HttpRequest.Parameter... params) {
			if (params == null || params.length < 0) {
				return false;
			}
			try {
				HttpRequest http = new HttpRequest();
				byte[] result = http.request(ContentService.api_feedback, params[0]);
				JSONObject jo = new JSONObject(new String(result));
				return jo.optInt("code", 1) == 0;
			} catch (Exception ex) {
				Log.e(tag, "send feedback failed", ex);
				return false;
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result != null && result) {
				ToastService.toast(FeedbackActivity.this, "感谢您的宝贵意见", Toast.LENGTH_SHORT);
				onBackPressed();
			} else {
				ToastService.toast(FeedbackActivity.this, "网络不给力啊", Toast.LENGTH_SHORT);
			}
		}
	}

}
