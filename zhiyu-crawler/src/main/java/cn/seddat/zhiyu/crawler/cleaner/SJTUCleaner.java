/**
 * 
 */
package cn.seddat.zhiyu.crawler.cleaner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.zhiyu.crawler.Post;

/**
 * 上交饮水思源站点信息清洗
 * 
 * @author gengmaozhang01
 * @since 2013-11-27 下午10:26:11
 */
public class SJTUCleaner implements Cleaner {

	private final Log log = LogFactory.getLog(SJTUCleaner.class.getSimpleName());

	private final String seed;
	private Pattern titleCancelled;
	private Pattern contentHeader, contentTail;

	public SJTUCleaner() {
		this.seed = "sjtu";
		titleCancelled = Pattern.compile("取消[\\s\\S]*发文权限");
		contentHeader = Pattern.compile("发信人: [\\s\\S]*?, 信区: [\\s\\S]*?标 题: [\\s\\S]*?发信站: [\\s\\S]*?\\)");
		contentTail = Pattern.compile("※ 来源:[\\s\\S]*?</pre>");
	}

	@Override
	public String getSeed() {
		return seed;
	}

	@Override
	public List<Post> clean(Post... posts) throws Exception {
		return this.clean(Arrays.asList(posts));
	}

	@Override
	public List<Post> clean(List<Post> posts) throws Exception {
		List<Post> pl = new ArrayList<Post>();
		for (Post p : posts) {
			if (!seed.equals(p.getSeed())) {
				log.info("source isn't " + seed + ", " + p);
				continue;
			}
			if (!this.checkTitle(p.getTitle())) {
				log.info("title is invalid, " + p);
				continue;
			}
			if (!this.checkLink(p.getLink())) {
				log.info("link is invalid, " + p);
				continue;
			}
			p.setContent(this.washContent(p.getContent()));
			pl.add(p);
		}
		return pl;
	}

	private boolean checkTitle(String title) throws Exception {
		if (title == null || title.isEmpty()) {
			return false;
		}
		if (title.startsWith("[合集]") || title.startsWith("版面积分")) {
			return false;
		}
		Matcher m = titleCancelled.matcher(title);
		if (m.find()) {
			return false;
		}
		return true;
	}

	private boolean checkLink(String link) throws Exception {
		if (link == null || link.isEmpty()) {
			return false;
		}
		return true;
	}

	private String washContent(String content) throws Exception {
		if (content == null || content.isEmpty()) {
			return "";
		}
		Matcher m = contentHeader.matcher(content);
		if (m.find()) {
			content = content.substring(m.end());
		}
		m = contentTail.matcher(content);
		if (m.find()) {
			content = content.substring(0, m.start());
		}
		return content;
	}

}
