package cn.seddat.href.crawler;

import junit.framework.Assert;
import junit.framework.TestCase;

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

}
