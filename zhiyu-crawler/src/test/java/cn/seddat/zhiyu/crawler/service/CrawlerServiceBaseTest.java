/**
 * 
 */
package cn.seddat.zhiyu.crawler.service;

import junit.framework.TestCase;

/**
 * @author mzhgeng
 * 
 */
public class CrawlerServiceBaseTest extends TestCase {

	private CrawlerService crawler;

	@Override
	protected void setUp() throws Exception {
		crawler = new CrawlerService();
	}

	public void testStart() throws Exception {
		crawler.start();
		// find
		Thread.sleep(10000);
		crawler.shutdown(false);
	}

}
