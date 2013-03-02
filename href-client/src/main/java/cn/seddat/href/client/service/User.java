/**
 * 
 */
package cn.seddat.href.client.service;

import java.util.Date;

/**
 * @author mzhgeng
 * 
 */
public class User extends Entity {

	public static final String COL_ID = "Id";
	public static final String COL_NAME = "un";
	public static final String COL_ICON = "ui";
	public static final String COL_ICON_URI = "uiu";
	public static final String COL_CACHE_TIME = "cht";

	public User() {
	}

	public String getId() {
		return get(COL_ID);
	}

	public User setId(String id) {
		put(COL_ID, id);
		return this;
	}

	public String getName() {
		return get(COL_NAME);
	}

	public User setName(String name) {
		put(COL_NAME, name);
		return this;
	}

	public String getIconUri() {
		return get(COL_ICON_URI);
	}

	public User setIconUri(String uri) {
		put(COL_ICON_URI, uri);
		return this;
	}

	public String getIcon() {
		return get(COL_ICON);
	}

	public User setIcon(String icon) {
		put(COL_ICON, icon);
		return this;
	}

	public Date getCacheTime() {
		String time = get(COL_CACHE_TIME);
		if (time == null) {
			return null;
		}
		return new Date(Long.parseLong(time));
	}

	public User setCacheTime(Date time) {
		put(COL_CACHE_TIME, String.valueOf(time.getTime()));
		return this;
	}

}
