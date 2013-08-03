/**
 * 
 */
package cn.seddat.zhiyu.crawler.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author mzhgeng
 * 
 */
public class DateHelper {

	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd HH:mm:ss");

	/**
	 * @return formatted string by pattern "yyyyMMdd HH:mm:ss"
	 */
	public static String format(Date date) {
		if (date == null) {
			return "";
		}
		return DATE_FORMAT.format(date);
	}

}
