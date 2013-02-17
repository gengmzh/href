/**
 * 
 */
package cn.seddat.href.client.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author mzhgeng
 * 
 */
public class PostService {

	private String api = "http://42.96.143.229/href/post";
	private String userAgent = "HrefClient/0.1.0 Android";
	private DateFormat dateFormat;

	public PostService() {
		dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	}

	public List<Post> query(long startTime, long endTime) throws Exception {
		String args = null;
		if (startTime > 0) {
			args = "st=" + startTime;
		}
		if (endTime > 0) {
			if (args != null) {
				args += "&";
			}
			args += "et=" + endTime;
		}
		String url = api + (args != null ? "?" + args : "");
		// connect
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setRequestProperty("User-Agent", userAgent);
		conn.setRequestProperty("Content-Type", "text/html; charset=utf-8");
		conn.connect();
		// read
		ByteArrayOutputStream ous = new ByteArrayOutputStream();
		InputStream ins = conn.getInputStream();
		byte[] b = new byte[1024];
		int len = 0;
		while ((len = ins.read(b)) > -1) {
			ous.write(b, 0, len);
		}
		ins.close();
		conn.disconnect();
		String json = ous.toString();
		ous.close();
		// parse
		List<Post> posts = new ArrayList<Post>();
		JSONArray ja = new JSONArray(json);
		for (int i = 0; i < ja.length(); i++) {
			JSONObject jo = (JSONObject) ja.get(i);
			Post post = new Post();
			post.setTitle(jo.optString("ttl")).setContent(jo.optString("ctt"));
			post.setSource(jo.optString("sn")).setLink(jo.optString("sl"));
			post.setType(jo.optString("tp")).setCompany(jo.optString("com"));
			post.setAuthor(jo.optString("au"));
			long ct = this.parseTime(jo.optString("ct"));
			post.setCreateTime(ct).setShowTime(this.parseShowTime(ct));
			post.setPv(jo.optLong("pv")).setClick(jo.optLong("clk")).setMark(jo.optLong("mrk"));
			posts.add(post);
		}
		return posts;
	}

	private long parseTime(String time) {
		if (time == null || time.isEmpty()) {
			return 0;
		}
		try {
			Date date = dateFormat.parse(time);
			return date.getTime();
		} catch (ParseException e) {
			// ignore
		}
		return 0;
	}

	private String parseShowTime(long millis) {
		if (millis <= 0) {
			return "未知";
		}
		long second = (new Date().getTime() - millis) / 1000;
		if (second < 60 * 60) {// < 1 hour
			return second < 60 ? "1分钟内" : (second / 60) + "分钟前";
		} else if (second < 24 * 60 * 60) {// < 1 day
			return (second / (60 * 60)) + "小时前";
		} else if (second < 6 * 24 * 60 * 60) {// < 1 week
			return (second / (24 * 60 * 60)) + "天前";
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(millis);
			int day = cal.get(Calendar.HOUR_OF_DAY);
			int min = cal.get(Calendar.MINUTE);
			return (cal.get(Calendar.MONTH) + 1) + "月" + cal.get(Calendar.DAY_OF_MONTH) + "日 " + (day < 10 ? "0" : "")
					+ day + ":" + (min < 10 ? "0" : "") + min;
		}
	}

}
