package cn.seddat.href.crawler;

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

	public void testConfig() throws Exception {
		String[] shuimu = config.getShuimuRss();
		Assert.assertTrue(shuimu != null && shuimu.length > 0);
		for (String source : shuimu) {
			System.out.println(source);
		}
	}

	public void testLogging() throws Exception {
		System.out.println(System.getProperty("java.util.logging.config.file"));
		Log log = LogFactory.getLog(ConfigTest.class.getSimpleName());
		log.info("log format");
		log.error("test log format", new Exception("error\nexpcetion...."));
	}

}
