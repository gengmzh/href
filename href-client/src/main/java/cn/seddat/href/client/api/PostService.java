/**
 * 
 */
package cn.seddat.href.client.api;

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
	private DateFormat dateFormat;

	public PostService() {
		dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	}

	public List<Post> query(long time, String item, int order) throws Exception {
		// args
		HttpRequest.Parameter args = new HttpRequest.Parameter();
		if (time > 0) {
			args.set("time", String.valueOf(time));
		}
		if (item != null && !item.isEmpty()) {
			args.set("item", item);
		}
		if (order > 0) {
			args.set("order", String.valueOf(order));
		}
		// request
		HttpRequest request = new HttpRequest();
		String content = request.request(api, args);
		// parse
		List<Post> posts = new ArrayList<Post>();
		JSONArray json = new JSONArray(content);
		for (int i = 0; i < json.length(); i++) {
			Post post = this.parsePost((JSONObject) json.get(i));
			posts.add(post);
		}
		return posts;
	}

	private Post parsePost(JSONObject jo) {
		Post post = new Post();
		post.setId(jo.optString("id", jo.optString("_id")));
		post.setTitle(jo.optString("ttl")).setContent(jo.optString("ctt"));
		post.setSource(jo.optString("sn")).setLink(jo.optString("sl"));
		post.setType(jo.optString("tp")).setCompany(jo.optString("com"));
		post.setUserId(jo.optString("uid")).setAuthor(jo.optString("au")).setIconUri(jo.optString("icon"));
		try {
			Date date = dateFormat.parse(jo.optString("ct"));
			post.setCreateTime(date.getTime());
		} catch (ParseException e) {
			// ignore
		}
		post.setShowTime(this.parseShowTime(post.getCreateTime()));
		post.setPv(jo.optLong("pv")).setClick(jo.optLong("clk")).setMark(jo.optLong("mrk"));
		return post;
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

	public Post findById(String id) throws Exception {
		if (id == null || id.isEmpty()) {
			return null;
		}
		// request
		HttpRequest request = new HttpRequest();
		String content = request.request(api + "/" + id, null);
		// parse
		return this.parsePost(new JSONObject(content));
	}

}
