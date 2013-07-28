/**
 * 
 */
package cn.seddat.zhiyu.client.service;

import java.util.Date;

/**
 * @author mzhgeng
 * 
 */
public class Post extends Entity {

	public static final String COL_ID = "id";
	// public static final String COL_CHUNK = "chk";
	public static final String COL_USER_ID = "uid";
	public static final String COL_TITLE = "ttl";
	public static final String COL_CONTENT = "ctt";
	public static final String COL_SOURCE = "sn";
	public static final String COL_LINK = "sl";
	public static final String COL_TYPE = "tp";
	public static final String COL_COMPANY = "com";
	public static final String COL_ADDRESS = "addr";
	public static final String COL_CREATE_TIME = "ct";
	public static final String COL_MARK = "mrk";
	public static final String COL_LIKE = "like";
	public static final String COL_CACHE_TIME = "cht";

	public Post() {
		super();
	}

	public String getId() {
		return get(COL_ID);
	}

	public Post setId(String id) {
		put(COL_ID, id);
		return this;
	}

	// public int getChunk() {
	// String chunk = get(COL_CHUNK);
	// return chunk != null ? Integer.parseInt(chunk) : 0;
	// }
	//
	// public Post setChunk(int chunk) {
	// put(COL_CHUNK, String.valueOf(chunk));
	// return this;
	// }

	public String getUserId() {
		return get(COL_USER_ID);
	}

	public Post setUserId(String userId) {
		put(COL_USER_ID, userId);
		return this;
	}

	//
	// public String getUserName() {
	// return get(User.COL_NAME);
	// }
	//
	// public Post setUserName(String name) {
	// put(User.COL_NAME, name);
	// return this;
	// }
	//
	// public String getUserIcon() {
	// return get(User.COL_ICON);
	// }
	//
	// public Post setUserIcon(String icon) {
	// put(User.COL_ICON, icon);
	// return this;
	// }
	//
	// public String getUserIconUri() {
	// return get(User.COL_ICON_URI);
	// }
	//
	// public Post setUserIconUri(String uri) {
	// put(User.COL_ICON_URI, uri);
	// return this;
	// }

	public String getTitle() {
		return get(COL_TITLE);
	}

	public Post setTitle(String title) {
		put(COL_TITLE, title);
		return this;
	}

	public String getContent() {
		return get(COL_CONTENT);
	}

	public Post setContent(String content) {
		put(COL_CONTENT, content);
		return this;
	}

	public String getSource() {
		return get(COL_SOURCE);
	}

	public Post setSource(String source) {
		put(COL_SOURCE, source);
		return this;
	}

	public String getLink() {
		return get(COL_LINK);
	}

	public Post setLink(String link) {
		put(COL_LINK, link);
		return this;
	}

	public String getType() {
		return get(COL_TYPE);
	}

	public Post setType(String type) {
		put(COL_TYPE, type);
		return this;
	}

	public String getCompany() {
		return get(COL_COMPANY);
	}

	public Post setCompany(String company) {
		put(COL_COMPANY, company);
		return this;
	}

	public String getAddress() {
		return get(COL_ADDRESS);
	}

	public Post setAddress(String address) {
		put(COL_ADDRESS, address);
		return this;
	}

	public long getCreateTime() {
		return getLong(COL_CREATE_TIME, 0);
	}

	public Post setCreateTime(long createTime) {
		put(COL_CREATE_TIME, String.valueOf(createTime));
		return this;
	}

	// public String getShowTime() {
	// return get("pt");
	// }
	//
	// public Post setShowTime(String showTime) {
	// put("pt", showTime);
	// return this;
	// }

	// public long getPv() {
	// return getLong("pv", 0);
	// }
	//
	// public Post setPv(long pv) {
	// put("pv", String.valueOf(pv));
	// return this;
	// }
	//
	// public long getClick() {
	// return getLong("clk", 0);
	// }
	//
	// public Post setClick(long click) {
	// put("clk", String.valueOf(click));
	// return this;
	// }

	public long getMark() {
		return getLong(COL_MARK, 0);
	}

	public Post setMark(long mark) {
		put(COL_MARK, String.valueOf(mark));
		return this;
	}

	public boolean isLiked() {
		return getLong(COL_LIKE, 0) > 0;
	}

	public Post setLike(boolean like) {
		put(COL_LIKE, like ? "1" : "0");
		return this;
	}

	public Date getCacheTime() {
		String time = get(COL_CACHE_TIME);
		if (time == null) {
			return null;
		}
		return new Date(Long.parseLong(time));
	}

	public Post setCacheTime(Date time) {
		put(COL_CACHE_TIME, String.valueOf(time.getTime()));
		return this;
	}

}
