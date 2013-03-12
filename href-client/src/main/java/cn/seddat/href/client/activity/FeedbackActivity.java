package cn.seddat.href.client.activity;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Toast;
import cn.seddat.href.client.R;
import cn.seddat.href.client.service.ToastService;

public class FeedbackActivity extends Activity {

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
		RatingBar rating = (RatingBar) findViewById(R.id.feedback_rating);
		EditText email = (EditText) findViewById(R.id.feedback_email);
		// TODO
		ToastService.toast(this, rating.getRating() + "\n" + email.getText() + "\n" + advice.getText(),
				Toast.LENGTH_SHORT);
		// reset
		advice.setText("");
		advice.setHint(R.string.feedback_advice_hint);
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
