/**
 * 
 */
package cn.seddat.href.client.service;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;
import cn.seddat.href.client.R;

/**
 * @author mzhgeng
 * 
 */
public class ContentService {

	private final String tag = ContentService.class.getSimpleName();
	private static final String api = "http://42.96.143.229";
	private static final String api_post = api + "/href/post";
	private static final String api_mark = api + "/href/mark";
	public static final String api_feedback = api + "/href/feedback";

	private final String defaultUserIcon = String.valueOf(R.drawable.default_user_icon);
	private DateFormat dateFormat;
	private Context context;
	private ContentResolver contentResolver;

	public ContentService(Context context) {
		dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		this.context = context;
		this.contentResolver = context.getContentResolver();
	}

	public List<Post> findPostByCache(long time, String item, int order, int limit) throws Exception {
		StringBuffer where = new StringBuffer();
		List<String> args = new ArrayList<String>();
		if (time > 0) {
			String comp = order == 0 ? ">" : "<";
			where.append(Post.COL_CREATE_TIME).append(comp).append("?");
			args.add(String.valueOf(time));
			if (item != null && item.length() > 0) {
				where.append(" or (").append(Post.COL_CREATE_TIME).append("=? and ");
				where.append(Post.COL_ID).append(comp).append("?)");
				args.add(String.valueOf(time));
				args.add(item);
			}
		}
		Cursor cursor = contentResolver.query(ContentProvider.CONTENT_POST, new String[] { Post.COL_ID,
				Post.COL_USER_ID, Post.COL_TITLE, Post.COL_SOURCE, Post.COL_COMPANY, Post.COL_CREATE_TIME,
				Post.COL_MARK, Post.COL_LIKE }, where.toString(), args.toArray(new String[args.size()]),
				Post.COL_CREATE_TIME + " desc, " + Post.COL_ID + " desc");
		List<Post> posts = new ArrayList<Post>();
		List<String> userIds = new ArrayList<String>();
		if (cursor != null) {
			if (limit <= 0) {
				limit = 20;
			}
			boolean hasNext = cursor.moveToFirst();
			while (hasNext) {
				Post post = new Post();
				post.setId(cursor.getString(cursor.getColumnIndex(Post.COL_ID)));
				post.setUserId(cursor.getString(cursor.getColumnIndex(Post.COL_USER_ID)));
				post.setTitle(cursor.getString(cursor.getColumnIndex(Post.COL_TITLE)));
				post.setSource(cursor.getString(cursor.getColumnIndex(Post.COL_SOURCE)));
				post.setCompany(cursor.getString(cursor.getColumnIndex(Post.COL_COMPANY)));
				post.setCreateTime(cursor.getLong(cursor.getColumnIndex(Post.COL_CREATE_TIME)));
				post.setMark(cursor.getLong(cursor.getColumnIndex(Post.COL_MARK)));
				post.put("pt", this.parseShowTime(post.getCreateTime()));
				posts.add(post);
				if (!userIds.contains(post.getUserId())) {
					userIds.add(post.getUserId());
				}
				hasNext = posts.size() < limit && cursor.moveToNext();
			}
			cursor.close();
		}
		if (!userIds.isEmpty()) {
			Map<String, User> users = this.findUserByCache(userIds);
			for (Post post : posts) {
				if (users.containsKey(post.getUserId())) {
					User user = users.get(post.getUserId());
					post.put(User.COL_NAME, user.getName());
					post.put(User.COL_ICON_URI, user.getIconUri());
					post.put(User.COL_ICON, user.getIcon());
				} else {
					post.put(User.COL_ICON, defaultUserIcon);// kidding me??
				}
			}
		}
		return posts;
	}

	public List<Post> findMarkedPost(int limit) throws Exception {
		Cursor cursor = contentResolver.query(ContentProvider.CONTENT_POST, new String[] { Post.COL_ID,
				Post.COL_USER_ID, Post.COL_TITLE, Post.COL_SOURCE, Post.COL_COMPANY, Post.COL_CREATE_TIME,
				Post.COL_MARK }, Post.COL_LIKE + ">?", new String[] { "0" }, Post.COL_CREATE_TIME + " desc, "
				+ Post.COL_ID + " desc");
		List<Post> posts = new ArrayList<Post>();
		List<String> userIds = new ArrayList<String>();
		if (cursor != null) {
			boolean hasNext = cursor.moveToFirst();
			while (hasNext) {
				Post post = new Post();
				post.setId(cursor.getString(cursor.getColumnIndex(Post.COL_ID)));
				post.setUserId(cursor.getString(cursor.getColumnIndex(Post.COL_USER_ID)));
				post.setTitle(cursor.getString(cursor.getColumnIndex(Post.COL_TITLE)));
				post.setSource(cursor.getString(cursor.getColumnIndex(Post.COL_SOURCE)));
				post.setCompany(cursor.getString(cursor.getColumnIndex(Post.COL_COMPANY)));
				post.setCreateTime(cursor.getLong(cursor.getColumnIndex(Post.COL_CREATE_TIME)));
				post.setMark(cursor.getLong(cursor.getColumnIndex(Post.COL_MARK)));
				post.put("pt", this.parseShowTime(post.getCreateTime()));
				posts.add(post);
				if (!userIds.contains(post.getUserId())) {
					userIds.add(post.getUserId());
				}
				hasNext = cursor.moveToNext() && (limit <= 0 || posts.size() < limit);
			}
			cursor.close();
		}
		if (!userIds.isEmpty()) {
			Map<String, User> users = this.findUserByCache(userIds);
			for (Post post : posts) {
				if (users.containsKey(post.getUserId())) {
					User user = users.get(post.getUserId());
					post.put(User.COL_NAME, user.getName());
					post.put(User.COL_ICON_URI, user.getIconUri());
					post.put(User.COL_ICON, user.getIcon());
				} else {
					post.put(User.COL_ICON, defaultUserIcon);// kidding me??
				}
			}
		}
		return posts;
	}

	private Map<String, User> findUserByCache(List<String> ids) throws Exception {
		Map<String, User> result = new HashMap<String, User>();
		if (ids == null || ids.isEmpty()) {
			return result;
		}
		String where = User.COL_ID + " in(?";
		for (int i = 1; i < ids.size(); i++) {
			where += ",?";
		}
		where += ")";
		Cursor cursor = contentResolver.query(ContentProvider.CONTENT_USER, null, where,
				ids.toArray(new String[ids.size()]), null);
		if (cursor != null) {
			boolean hasNext = cursor.moveToFirst();
			while (hasNext) {
				User user = new User();
				user.setId(cursor.getString(cursor.getColumnIndex(User.COL_ID)));
				user.setName(cursor.getString(cursor.getColumnIndex(User.COL_NAME)));
				user.setIconUri(cursor.getString(cursor.getColumnIndex(User.COL_ICON_URI)));
				user.setIcon(defaultUserIcon);
				result.put(user.getId(), user);
				hasNext = cursor.moveToNext();
			}
			cursor.close();
		}
		return result;
	}

	public List<Post> findPostByServer(long time, String item, int order, int limit) throws Exception {
		// args
		HttpRequest.Parameter args = new HttpRequest.Parameter();
		if (time > 0) {
			args.set("time", String.valueOf(time));
		}
		if (item != null && item.length() == 0) {
			args.set("item", item);
		}
		if (order > 0) {
			args.set("order", String.valueOf(order));
		}
		args.set("limit", String.valueOf(limit <= 0 ? 20 : limit));
		// request
		HttpRequest request = new HttpRequest();
		String content = new String(request.request(api_post, args));
		// parse
		List<Post> posts = new ArrayList<Post>();
		JSONArray json = new JSONArray(content);
		for (int i = 0; i < json.length(); i++) {
			// post
			JSONObject jo = (JSONObject) json.get(i);
			Post post = new Post();
			post.setId(jo.optString("id", jo.optString("_id"))).setTitle(jo.optString("ttl"));
			post.setSource(jo.optString("sn")).setLink(jo.optString("sl"));
			post.setType(jo.optString("tp")).setCompany(jo.optString("com")).setUserId(jo.optString("uid"));
			try {
				post.setCreateTime(dateFormat.parse(jo.optString("ct")).getTime());
			} catch (ParseException e) {
			}
			post.setMark(jo.optLong("mrk"));
			post.setCacheTime(new Date());
			this.save(ContentProvider.CONTENT_POST, post.toValues(), Post.COL_ID + "=?", new String[] { post.getId() });
			// user
			User user = new User();
			user.setId(jo.optString("uid")).setName(jo.optString("au")).setIconUri(jo.optString("icon"));
			user.setCacheTime(new Date());
			this.save(ContentProvider.CONTENT_USER, user.toValues(), User.COL_ID + "=?", new String[] { user.getId() });
			// show
			post.put(User.COL_NAME, user.getName());
			post.put(User.COL_ICON_URI, user.getIconUri());
			post.put(User.COL_ICON, defaultUserIcon);
			post.put("pt", this.parseShowTime(post.getCreateTime()));
			posts.add(post);
		}
		return posts;
	}

	private void save(Uri uri, ContentValues values, String where, String[] args) {
		if (uri == null || values == null || values.size() == 0) {
			return;
		}
		if (where != null && where.length() > 0) {
			Cursor cursor = contentResolver.query(uri, new String[] { BaseColumns._ID }, where, args, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					contentResolver.update(uri, values, where, args);
					cursor.close();
					return;
				} else {
					cursor.close();
				}
			}
		}
		contentResolver.insert(uri, values);
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

	public void clearCache(Date time) throws Exception {
		if (time == null) {
			return;
		}
		// post
		final long millis = time.getTime();
		contentResolver.delete(ContentProvider.CONTENT_POST, Post.COL_CACHE_TIME + "<? and " + Post.COL_LIKE + "<=?",
				new String[] { String.valueOf(millis), "0" });
		// user
		StringBuffer selection = new StringBuffer();
		Set<String> userIds = new HashSet<String>();
		Cursor cursor = contentResolver.query(ContentProvider.CONTENT_POST, new String[] { Post.COL_USER_ID },
				Post.COL_LIKE + ">?", new String[] { "0" }, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				selection.append("(?");
				String userId = cursor.getString(cursor.getColumnIndex(Post.COL_USER_ID));
				userIds.add(userId);
				while (cursor.moveToNext()) {
					userId = cursor.getString(cursor.getColumnIndex(Post.COL_USER_ID));
					if (userIds.add(userId)) {
						selection.append(",?");
					}
				}
				selection.append(")");
			}
			cursor.close();
		}
		String where = (selection.length() > 0 ? User.COL_ID + " not in" + selection + " and " : "")
				+ User.COL_CACHE_TIME + "<?";
		String[] args = userIds.toArray(new String[userIds.size() + 1]);
		args[args.length - 1] = String.valueOf(millis);
		contentResolver.delete(ContentProvider.CONTENT_USER, where, args);
		// icon
		File cache = this.getCacheDir();
		final Set<String> icons = new HashSet<String>();
		if (!userIds.isEmpty()) {
			where = User.COL_ID + " in " + selection;
			args = userIds.toArray(new String[userIds.size()]);
			cursor = contentResolver.query(ContentProvider.CONTENT_USER, new String[] { User.COL_ICON_URI }, where,
					args, null);
			if (cursor != null) {
				boolean hasNext = cursor.moveToFirst();
				while (hasNext) {
					String uri = cursor.getString(cursor.getColumnIndex(User.COL_ICON_URI));
					int index = uri.lastIndexOf("/");
					icons.add(index > 0 ? uri.substring(index + 1) : uri);
					hasNext = cursor.moveToNext();
				}
				cursor.close();
			}
		}
		File[] files = cache.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile() && file.lastModified() < millis && !icons.contains(file.getName());
			}
		});
		for (File file : files) {
			file.delete();
		}
	}

	public Post findPostDetail(Post post) throws Exception {
		if (post == null || post.getId() == null) {
			return null;
		}
		// cache
		Cursor cursor = contentResolver.query(ContentProvider.CONTENT_POST, new String[] { Post.COL_LINK,
				Post.COL_CONTENT, Post.COL_ADDRESS, Post.COL_LIKE }, Post.COL_ID + "=?", new String[] { post.getId() },
				null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				post.setLink(cursor.getString(cursor.getColumnIndex(Post.COL_LINK)));
				post.setContent(cursor.getString(cursor.getColumnIndex(Post.COL_CONTENT)));
				post.setAddress(cursor.getString(cursor.getColumnIndex(Post.COL_ADDRESS)));
				String like = cursor.getString(cursor.getColumnIndex(Post.COL_LIKE));
				post.setLike(like == null || like.length() == 0 ? false : Integer.valueOf(like) > 0);
			}
			cursor.close();
		}
		// server
		if (post.getContent() == null || post.getContent().length() == 0) {
			HttpRequest request = new HttpRequest();
			String json = new String(request.request(api_post + "/" + post.getId(), null));
			JSONObject jo = new JSONObject(json);
			post.setLink(jo.optString("sl", null)).setAddress(jo.optString("addr", null))
					.setContent(jo.optString("ctt", null));
			ContentValues values = new ContentValues();
			if (post.getLink() != null) {
				values.put(Post.COL_LINK, post.getLink());
			}
			if (post.getAddress() != null) {
				values.put(Post.COL_ADDRESS, post.getAddress());
			}
			if (post.getContent() != null) {
				values.put(Post.COL_CONTENT, post.getContent());
			}
			if (values.size() > 0) {
				this.save(ContentProvider.CONTENT_POST, values, Post.COL_ID + "=?", new String[] { post.getId() });
			}
		}
		return post;
	}

	public Map<String, String> findUserIcon(List<String> uris) throws Exception {
		// init cache
		File cache = this.getCacheDir();
		// find icon
		Map<String, String> result = new HashMap<String, String>();
		for (String uri : uris) {
			// cache
			int index = uri.lastIndexOf("/");
			String icon = index > 0 ? uri.substring(index + 1) : uri;
			File file = new File(cache, icon);
			if (file.exists()) {
				if (file.isFile()) {
					result.put(uri, file.getAbsolutePath());
					Log.i(tag, "find " + file.getName() + " by cache");
					continue;
				} else {
					this.delete(file);
					Log.w(tag, "delete illegal cache file " + file.getAbsolutePath());
				}
			}
			// server
			HttpRequest http = new HttpRequest();
			String url = api + (uri.startsWith("/") ? uri : "/" + uri);
			byte[] bytes = http.request(url, null);
			FileOutputStream out = new FileOutputStream(file);
			out.write(bytes);
			out.close();
			result.put(uri, file.getAbsolutePath());
			Log.i(tag, "find " + file.getName() + " by server");
		}
		return result;
	}

	private File getCacheDir() {
		File cache = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			cache = new File(Environment.getExternalStorageDirectory(), context.getPackageName());
			cache = new File(cache, "cache");
			cache.mkdirs();
		} else {
			cache = context.getCacheDir();
		}
		Log.i(tag, "using cache " + cache.getAbsolutePath());
		return cache;
	}

	private void delete(File file) {
		if (file == null || !file.exists()) {
			return;
		}
		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				this.delete(f);
			}
		}
		file.delete();
	}

	public void markPost(Post post, boolean marked) throws Exception {
		if (post == null || post.getId() == null) {
			return;
		}
		// server
		HttpRequest.Parameter args = new HttpRequest.Parameter();
		args.set("id", post.getId());
		if (marked == false) {
			args.set("mark", "false");
		}
		HttpRequest http = new HttpRequest();
		byte[] bytes = http.request(api_mark, args);
		JSONObject jo = new JSONObject(new String(bytes));
		if (jo.optInt("code", 1) != 0) {
			throw new Exception("mark post failed, " + jo.optString("message"));
		}
		// cache
		post.setLike(marked).setMark(post.getMark() + (marked ? 1 : -1));
		ContentValues values = new ContentValues();
		values.put(Post.COL_LIKE, marked ? "1" : "0");
		values.put(Post.COL_MARK, String.valueOf(post.getMark()));
		this.save(ContentProvider.CONTENT_POST, values, Post.COL_ID + "=?", new String[] { post.getId() });
	}

}
