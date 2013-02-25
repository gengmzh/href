/**
 * 
 */
package cn.seddat.href.crawler;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.PropertiesConfiguration;

/**
 * @author mzhgeng
 * 
 */
public class Config {

	private static Config instance;

	public static Config getInstance() throws Exception {
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

	private Config() throws Exception {
		config = new PropertiesConfiguration("crawler.properties");
	}

	public String getMongoUri() {
		return config.getString("mongo_uri");
	}

	// crawler
	public String[] getShuimuRss() {
		return config.getStringArray("crawler_shuimu_rss");
	}

	public long getPoliteTime() {
		return config.getLong("crawler_polite", 600);
	}

	// service
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
