/**
 * 
 */
package cn.seddat.zhiyu.crawler;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @author mzhgeng
 * 
 */
public class Config {

	private static Config instance;

	public static Config getInstance() {
		if (instance == null) {
			synchronized (Config.class) {
				if (instance == null) {
					instance = new Config();
				}
			}
		}
		return instance;
	}

	private Configuration config;

	private Config() {
		try {
			config = new PropertiesConfiguration("crawler.properties");
		} catch (ConfigurationException e) {
			throw new RuntimeException("load crawler.properties failed", e);
		}
	}

	// spider properties
	/**
	 * 获取爬虫礼貌间隔时间，单位毫秒
	 */
	public long getSpiderPolite() {
		return config.getLong("spider_polite", 600) * 1000;
	}

	public String getSpiderUserAgent() {
		return config.getString("spider_useragent", "ZHIYU Spider+");
	}

	public String[] getSpiderSeed() {
		return config.getStringArray("spider_seed");
	}

	public String getSpiderSeedHost(String seed) {
		return config.getString("spider_seed_host." + seed);
	}

	public String[] getSpiderSeedUrls(String seed) {
		return config.getStringArray("spider_seed_url." + seed);
	}

	public String getSpiderUserAgent(String seed) {
		return config.getString("spider_useragent." + seed, getSpiderUserAgent());
	}

	public String getSpiderCookie(String seed) {
		return config.getString("spider_cookie." + seed);
	}

	public long getSpiderPeriod(String seed) {
		return config.getLong("spider_period." + seed, 3600) * 1000;
	}

	public long getSipderInitialDelay(String seed) {
		return config.getLong("spider_initial_delay." + seed, 60) * 1000;
	}

	// service propeties

	public String getMongoUri() {
		return config.getString("mongo_uri");
	}

	public int getThreadPoolSize() {
		return config.getInt("threadpool_size", 3);
	}

	public int getQueueSize() {
		return config.getInt("queue_size", 1000);
	}

	public String getUserIconPath() {
		return config.getString("path_user_icon", "/tmp");
	}

}
