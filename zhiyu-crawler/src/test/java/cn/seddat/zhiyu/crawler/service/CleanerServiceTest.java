/**
 * 
 */
package cn.seddat.zhiyu.crawler.service;

import java.io.File;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import junit.framework.Assert;
import junit.framework.TestCase;
import cn.seddat.zhiyu.crawler.Config;
import cn.seddat.zhiyu.crawler.Post;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * @author mzhgeng
 * 
 */
public class CleanerServiceTest extends TestCase {

	private BlockingQueue<Post> queue;
	private CleanerService postService;

	@Override
	protected void setUp() throws Exception {
		queue = new ArrayBlockingQueue<Post>(10);
		postService = new CleanerService(queue);
	}

	@Override
	protected void tearDown() throws Exception {
	}

	public void test_savePost() throws Exception {
		Post post = new Post().setTitle("【睿善科技】招聘 游戏策划").setContent("【睿善科技】招聘 游戏策划").setSeed("shuimu")
				.setHost(Config.getInstance().getSpiderSeedHost("shuimu")).setType("社会招聘").setCompany("睿善科技")
				.setAuthor("").setPubtime(new Date());
		postService.savePost(post);
		post.setLink("http://www.newsmth.net/nForum/article/Career_Upgrade/167768");
		postService.savePost(post);
		// find
		DBCollection postColl = MongoService.getInstance().getPostCollection();
		DBObject obj = postColl.findOne(new BasicDBObject("sl", post.getLink()));
		Assert.assertNotNull(obj);
		System.out.println(obj);
		// remove
		postColl.remove(obj);
	}

	public void test_saveUser() throws Exception {
		Post p = new Post().setSeed("shuimu").setHost(Config.getInstance().getSpiderSeedHost("shuimu"))
				.setAuthor("gengmzh");
		p.setTitle("【睿善科技】招聘 游戏策划").setContent("【睿善科技】招聘 游戏策划");
		p.setLink("http://www.newsmth.net/nForum/article/Career_Upgrade/167768");
		postService.saveUser(p);

		String uid = p.getAuthor();
		Assert.assertNotNull(uid);
		File icon = new File(Config.getInstance().getUserIconPath(), uid + ".png");
		Assert.assertTrue(icon.exists());
		System.out.println(icon);
	}

	public void test_execute() throws Exception {
		// put
		Post post = new Post().setTitle("【睿善科技】招聘 游戏策划").setContent("【睿善科技】招聘 游戏策划").setSeed("shuimu")
				.setHost(Config.getInstance().getSpiderSeedHost("shuimu")).setType("社会招聘").setCompany("睿善科技")
				.setAuthor("").setPubtime(new Date());
		post.setLink("http://www.newsmth.net/nForum/article/Career_Upgrade/167768");
		queue.put(post);
		// save
		ExecutorService executor = Executors.newFixedThreadPool(2);
		executor.submit(postService);
		Thread.sleep(5000);
		// find
		DBCollection postColl = MongoService.getInstance().getPostCollection();
		DBObject obj = postColl.findOne(new BasicDBObject("sl", post.getLink()));
		Assert.assertNotNull(obj);
		System.out.println(obj);
		// remove
		postColl.remove(obj);
		executor.shutdown();
	}

}
