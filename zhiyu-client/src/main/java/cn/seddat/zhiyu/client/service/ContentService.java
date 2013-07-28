/**
 * 
 */
package cn.seddat.zhiyu.client.service;

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
import android.provider.BaseColumns;
import cn.seddat.zhiyu.client.R;

/**
 * @author mzhgeng
 * 
 */
public class ContentService {

	// private final String tag = ContentService.class.getSimpleName();
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

	public List<Post> findPostByServer(long time, String item, int order, int limit) throws Exception {
		// args
		HttpRequest.Parameter args = TrackService.getTrackHeader(context);
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
		String content = new String(request.request(Config.getPostApi(), args));
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

	/**
	 * clear posts and users, if reserve marked posts, related users will be
	 * reserved too.
	 * 
	 * @param beforeTime
	 *            only remove posts and users before this time, if null delete
	 *            anyone
	 * @param reserveMarked
	 *            reserve marked posts and related users if true
	 * @return marked user ids
	 * @throws Exception
	 */
	public List<String> clearPostAndUser(Date beforeTime, boolean reserveMarked) throws Exception {
		// post
		StringBuffer where = new StringBuffer();
		List<String> args = new ArrayList<String>();
		if (beforeTime != null) {
			where.append(Post.COL_CACHE_TIME).append("<?");
			args.add(String.valueOf(beforeTime.getTime()));
		}
		if (reserveMarked) {
			if (where.length() > 0) {
				where.append(" and ");
			}
			where.append(Post.COL_LIKE).append("<=?");
			args.add("0");
		}
		contentResolver.delete(ContentProvider.CONTENT_POST, where.toString(), args.toArray(new String[args.size()]));
		// user
		where = new StringBuffer();
		args.clear();
		if (beforeTime != null) {
			where.append(User.COL_CACHE_TIME).append("<?");
			args.add(String.valueOf(beforeTime.getTime()));
		}
		if (reserveMarked) {
			Cursor cursor = contentResolver.query(ContentProvider.CONTENT_POST, new String[] { Post.COL_USER_ID },
					Post.COL_LIKE + ">?", new String[] { "0" }, null);
			if (cursor != null) {
				if (cursor.moveToFirst()) {
					if (where.length() > 0) {
						where.append(" and ");
					}
					where.append(User.COL_ID).append(" not in(?");
					String userId = cursor.getString(cursor.getColumnIndex(Post.COL_USER_ID));
					args.add(userId);
					while (cursor.moveToNext()) {
						userId = cursor.getString(cursor.getColumnIndex(Post.COL_USER_ID));
						if (!args.contains(userId)) {
							where.append(",?");
							args.add(userId);
						}
					}
					where.append(")");
				}
				cursor.close();
			}
		}
		contentResolver.delete(ContentProvider.CONTENT_USER, where.toString(), args.toArray(new String[args.size()]));
		// result
		if (beforeTime != null) {
			args.remove(0);
		}
		return args;
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
			HttpRequest.Parameter args = TrackService.getTrackHeader(context);
			HttpRequest request = new HttpRequest();
			byte[] bytes = request.request(Config.getPostApi() + "/" + post.getId(), args);
			JSONObject jo = new JSONObject(new String(bytes));
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

	public void markPost(Post post, boolean marked) throws Exception {
		if (post == null || post.getId() == null) {
			return;
		}
		post.setLike(marked).setMark(post.getMark() + (marked ? 1 : -1));
		ContentValues values = new ContentValues();
		values.put(Post.COL_LIKE, marked ? "1" : "0");
		values.put(Post.COL_MARK, String.valueOf(post.getMark()));
		this.save(ContentProvider.CONTENT_POST, values, Post.COL_ID + "=?", new String[] { post.getId() });
	}

}
