/**
 * 
 */
package cn.seddat.href.crawler.service;

import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

import junit.framework.Assert;
import junit.framework.TestCase;
import cn.seddat.href.crawler.CrawlerScheduler;
import cn.seddat.href.crawler.Post;

/**
 * @author mzhgeng
 * 
 */
public class PostServiceTest extends TestCase {

	private BlockingQueue<Post> queue;
	private DBCollection dbColl;
	private PostService postService;

	@Override
	protected void setUp() throws Exception {
		queue = new ArrayBlockingQueue<Post>(10);
		CrawlerScheduler crawlerScheduler = new CrawlerScheduler();
		dbColl = crawlerScheduler.getDatabase().getCollection("test");
		postService = new PostService(queue, dbColl);
	}

	@Override
	protected void tearDown() throws Exception {
		dbColl.getDB().getMongo().close();
	}

	public void testSave() throws Exception {
		Post post = new Post().setTitle("【睿善科技】招聘 游戏策划").setContent("【睿善科技】招聘 游戏策划").setSource("newsmth.net")
				.setType("社会招聘").setCompany("睿善科技").setAuthor("").setPubtime(new Date());
		postService.save(post);
		post.setLink("http://www.newsmth.net/nForum/article/Career_Upgrade/167768");
		postService.save(post);
		// find
		DBObject obj = dbColl.findOne(new BasicDBObject("sl", post.getLink()));
		Assert.assertNotNull(obj);
		System.out.println(obj);
		// remove
		dbColl.remove(obj);
	}

	public void testRun() throws Exception {
		// put
		Post post = new Post().setTitle("【睿善科技】招聘 游戏策划").setContent("【睿善科技】招聘 游戏策划").setSource("newsmth.net")
				.setType("社会招聘").setCompany("睿善科技").setAuthor("").setPubtime(new Date());
		post.setLink("http://www.newsmth.net/nForum/article/Career_Upgrade/167768");
		queue.put(post);
		// save
		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(postService);
		Thread.sleep(5000);
		// find
		DBObject obj = dbColl.findOne(new BasicDBObject("sl", post.getLink()));
		Assert.assertNotNull(obj);
		System.out.println(obj);
		// remove
		dbColl.remove(obj);
		executor.shutdown();
	}

}
