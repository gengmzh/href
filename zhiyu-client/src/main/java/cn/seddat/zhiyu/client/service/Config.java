/**
 * 
 */
package cn.seddat.zhiyu.client.service;

/**
 * @author mzhgeng
 * 
 */
public class Config {

	private static final String api = "http://42.96.143.229";
	private static final String api_post = api + "/href/post";
	private static final String api_mark = api + "/href/mark";
	private static final String api_feedback = api + "/href/feedback";
	private static final String api_track = api + "/href/track";
	private static final String splash_image = "splash.png";

	public static final String getBaseApi() {
		return api;
	}

	public static final String getPostApi() {
		return api_post;
	}

	public static final String getMarkApi() {
		return api_mark;
	}

	public static final String getFeedbackApi() {
		return api_feedback;
	}

	public static final String getTrackApi() {
		return api_track;
	}

	public static final String getSplashImageName() {
		return splash_image;
	}

}
