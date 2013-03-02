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
import java.util.List;
import java.util.Map;

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
	private final String api = "http://42.96.143.229";
	private final String api_host = "http://42.96.143.229/href/post";
	private final String defaultUserIcon = String.valueOf(R.drawable.default_user_icon);
	private DateFormat dateFormat;
	private Context context;
	private ContentResolver contentResolver;

	public ContentService(Context context) {
		dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
		this.context = context;
		this.contentResolver = context.getContentResolver();
	}

	public List<Post> findPostByCache(long time, String item, int order) throws Exception {
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
				Post.COL_MARK }, where.toString(), args.toArray(new String[args.size()]), Post.COL_CREATE_TIME
				+ " desc, " + Post.COL_ID + " desc");
		List<Post> posts = new ArrayList<Post>();
		List<String> userIds = new ArrayList<String>();
		if (cursor != null) {
			boolean hasNext = cursor.moveToFirst();
			while (hasNext) {
				Post post = this.parsePost(cursor);
				post.put("pt", this.parseShowTime(post.getCreateTime()));
				posts.add(post);
				if (!userIds.contains(post.getUserId())) {
					userIds.add(post.getUserId());
				}
				hasNext = posts.size() < 20 && cursor.moveToNext();
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

	private Post parsePost(Cursor cursor) {
		Post post = new Post();
		int index = cursor.getColumnIndex(Post.COL_ID);
		if (index > -1)
			post.setId(cursor.getString(index));
		index = cursor.getColumnIndex(Post.COL_USER_ID);
		if (index > -1)
			post.setUserId(cursor.getString(index));
		index = cursor.getColumnIndex(Post.COL_TITLE);
		if (index > -1)
			post.setTitle(cursor.getString(index));
		index = cursor.getColumnIndex(Post.COL_CONTENT);
		if (index > -1)
			post.setContent(cursor.getString(index));
		index = cursor.getColumnIndex(Post.COL_SOURCE);
		if (index > -1)
			post.setSource(cursor.getString(index));
		index = cursor.getColumnIndex(Post.COL_LINK);
		if (index > -1)
			post.setLink(cursor.getString(index));
		index = cursor.getColumnIndex(Post.COL_TYPE);
		if (index > -1)
			post.setType(cursor.getString(index));
		index = cursor.getColumnIndex(Post.COL_COMPANY);
		if (index > -1)
			post.setCompany(cursor.getString(index));
		index = cursor.getColumnIndex(Post.COL_CREATE_TIME);
		if (index > -1)
			post.setCreateTime(cursor.getLong(index));
		index = cursor.getColumnIndex(Post.COL_MARK);
		if (index > -1)
			post.setMark(cursor.getLong(index));
		return post;
	}

	public Map<String, User> findUserByCache(List<String> ids) throws Exception {
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

	public List<Post> findPostByServer(long time, String item, int order) throws Exception {
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
		// request
		HttpRequest request = new HttpRequest();
		String content = new String(request.request(api_host, args));
		// parse
		List<Post> posts = new ArrayList<Post>();
		JSONArray json = new JSONArray(content);
		for (int i = 0; i < json.length(); i++) {
			// post
			JSONObject jo = (JSONObject) json.get(i);
			Post post = this.parsePost(jo);
			this.save(ContentProvider.CONTENT_POST, post.toValues(), Post.COL_ID + "=?", new String[] { post.getId() });
			// user
			User user = this.parseUser(jo);
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

	private Post parsePost(JSONObject jo) {
		Post post = new Post();
		post.setId(jo.optString("id", jo.optString("_id")));
		String val = jo.optString("ttl");
		if (val.length() > 0) {
			post.setTitle(val);
		}
		val = jo.optString("ctt");
		if (val.length() > 0) {
			post.setContent(val);
		}
		val = jo.optString("sn");
		if (val.length() > 0) {
			post.setSource(val);
		}
		val = jo.optString("sl");
		if (val.length() > 0) {
			post.setLink(val);
		}
		val = jo.optString("tp");
		if (val.length() > 0) {
			post.setType(val);
		}
		val = jo.optString("com");
		if (val.length() > 0) {
			post.setCompany(val);
		}
		val = jo.optString("uid");
		if (val.length() > 0) {
			post.setUserId(val);
		}
		val = jo.optString("ct");
		if (val.length() > 0) {
			try {
				post.setCreateTime(dateFormat.parse(val).getTime());
			} catch (ParseException e) {
			}
		}
		long l = jo.optLong("mrk");
		if (l > 0) {
			post.setMark(l);
		}
		post.setCacheTime(new Date());
		return post;
	}

	private User parseUser(JSONObject jo) {
		User user = new User();
		String val = jo.optString("uid");
		if (val.length() > 0) {
			user.setId(val);
		}
		val = jo.optString("au");
		if (val.length() > 0) {
			user.setName(val);
		}
		val = jo.optString("icon");
		if (val.length() > 0) {
			user.setIconUri(val);
		}
		user.setCacheTime(new Date());
		return user;
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
		long millis = time.getTime();
		String[] args = new String[] { String.valueOf(millis) };
		contentResolver.delete(ContentProvider.CONTENT_POST, Post.COL_CACHE_TIME + "<?", args);
		contentResolver.delete(ContentProvider.CONTENT_USER, User.COL_CACHE_TIME + "<?", args);
		File cache = this.getCacheDir();
		File[] files = cache.listFiles(new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
		});
		for (File file : files) {
			if (file.lastModified() < millis) {
				file.delete();
			}
		}
	}

	public String findPostContent(String postId) throws Exception {
		if (postId == null || postId.length() == 0) {
			return null;
		}
		// cache
		String content = null;
		Cursor cursor = contentResolver.query(ContentProvider.CONTENT_POST, new String[] { Post.COL_CONTENT },
				Post.COL_ID + "=?", new String[] { postId }, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				content = cursor.getString(cursor.getColumnIndex(Post.COL_CONTENT));
			}
			cursor.close();
		}
		// server
		if (content == null || content.length() == 0) {
			HttpRequest request = new HttpRequest();
			String json = new String(request.request(api_host + "/" + postId, null));
			JSONObject jo = new JSONObject(json);
			content = jo.optString("ctt", null);
			if (content != null && content.length() > 0) {
				ContentValues values = new ContentValues();
				values.put(Post.COL_CONTENT, content);
				this.save(ContentProvider.CONTENT_POST, values, Post.COL_ID + "=?", new String[] { postId });
			}
		}
		return content;
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
					Log.w(tag, "find from cache " + file.getAbsolutePath());
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

}
