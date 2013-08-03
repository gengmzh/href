/**
 * 
 */
package cn.seddat.zhiyu.crawler.cleaner;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import junit.framework.TestCase;
import cn.seddat.zhiyu.crawler.Config;
import cn.seddat.zhiyu.crawler.Post;
import cn.seddat.zhiyu.crawler.spider.ShuimuSpider;

/**
 * @author mzhgeng
 * 
 */
public class ShuimuCleanerTest extends TestCase {

	private ShuimuCleaner cleaner;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		cleaner = new ShuimuCleaner();
	}

	public void test_checkTile() throws Exception {
		String title = "gsfgg被取消在Career_Investment版的发文权限";
		Pattern p = Pattern.compile("取消[\\s\\S]*发文权限");
		Matcher m = p.matcher(title);
		Assert.assertTrue(m.find());
		System.out.println(m.group());

		System.out.println(title.matches("取消[\\s\\S]*发文权限"));
		System.out.println("[合集]fdsfsfds".startsWith("[合集]"));
	}

	public void test_cleanContent() throws Exception {
		Post p = new Post().setTitle("求问国核北京科学技术研究院有限公司待遇如何啊？")
				.setLink("http://www.newsmth.net/nForum/article/Career_PHD/209225").setSeed("shuimu")
				.setHost(Config.getInstance().getSpiderSeedHost("shuimu"));
		p.setContent("发信人: zhanyy (zhanyy), 信区: Career_PHD<br/>标&nbsp;&nbsp;题: 求问国核北京科学技术研究院有限公司待遇如何啊？<br/>发信站: 水木社区 (Mon Feb 18 15:57:14 2013), 站内<br/><br/>本人应届男博士一枚，刚收到国核北京科学技术研究院有限公司的offer，求问版上各位，这个单位待遇和发展前景如何呢？有了解的木有啊。<br/>--<br/><br/>※ 来源:・水木社区 <a target=\"_blank\" href=\"http://www.newsmth.net\">http://www.newsmth.net</a>・[FROM: 220.181.118.*]<br/>");

		cleaner.clean(p);

		Assert.assertTrue(p.getContent().startsWith("本人应届男博士一枚，"));
		Assert.assertTrue(p.getContent().endsWith("有了解的木有啊。<br/>"));
		System.out.println(p.getContent());
	}

	public void test_clean() throws Exception {
		ShuimuSpider spider = new ShuimuSpider();
		List<Post> pl = spider.crawl();
		pl = cleaner.clean(pl.toArray(new Post[pl.size()]));

		for (Post p : pl) {
			System.out.println(p.getLink());
			System.out.println(p.getContent());
			System.out.println();
		}
	}

}
