package cn.seddat.zhiyu.crawler;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Unit test for simple App.
 */
public class ConfigTest extends TestCase {

	private Config config;

	@Override
	protected void setUp() throws Exception {
		config = Config.getInstance();
	}

	public void test_polite() throws Exception {
		long time = config.getSpiderPolite();
		Assert.assertTrue(time > 0);
		System.out.println(time);
		// Thread.sleep(time);
	}

	public void test_seed() throws Exception {
		String[] seeds = config.getSpiderSeed();
		Assert.assertTrue(seeds != null && seeds.length > 0);
		for (String seed : seeds) {
			System.out.println(seed);
		}
	}

	public void test_seed_url() throws Exception {
		String[] urls = config.getSpiderSeedUrls("shuimu");
		Assert.assertTrue(urls != null && urls.length > 0);
		for (String url : urls) {
			System.out.println(url);
		}
	}

	public void testLogging() throws Exception {
		System.out.println(System.getProperty("java.util.logging.config.file"));
		Log log = LogFactory.getLog(ConfigTest.class.getSimpleName());
		log.info("log format");
		log.error("test log format", new Exception("error\nexpcetion...."));
	}

}
