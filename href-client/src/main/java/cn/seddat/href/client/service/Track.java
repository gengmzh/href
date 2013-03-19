/**
 * 
 */
package cn.seddat.href.client.service;

import java.util.Date;

/**
 * @author mzhgeng
 * 
 */
public class Track extends Entity {

	public static final String COL_ACTION = "act";
	public static final String COL_VALUE = "val";
	public static final String COL_CACHE_TIME = "cht";

	public static final String ACTION_IMPRESS = "impress";
	public static final String ACTION_CLICK = "click";
	public static final String ACTION_MARK = "mark";

	public Track() {
	}

	public String getAction() {
		return get(COL_ACTION);
	}

	public Track setAction(String action) {
		put(COL_ACTION, action);
		return this;
	}

	public String getValue() {
		return get(COL_VALUE);
	}

	public Track setValue(String value) {
		put(COL_VALUE, value);
		return this;
	}

	public Date getCacheTime() {
		String time = get(COL_CACHE_TIME);
		if (time == null) {
			return null;
		}
		return new Date(Long.parseLong(time));
	}

	public Track setCacheTime(Date time) {
		put(COL_CACHE_TIME, String.valueOf(time.getTime()));
		return this;
	}

}
