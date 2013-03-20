/**
 * 
 */
package cn.seddat.href.crawler;

import junit.framework.TestCase;

/**
 * @author mzhgeng
 * 
 */
public class CrawlerSchedulerBaseTest extends TestCase {

	private CrawlerScheduler scheduler;

	@Override
	protected void setUp() throws Exception {
		scheduler = new CrawlerScheduler();
	}

	public void testStart() throws Exception {
		scheduler.start();
		// find
		Thread.sleep(10000);
		scheduler.shutdown(false);
	}

}
