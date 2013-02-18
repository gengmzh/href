/**
 * 
 */
package cn.seddat.href.crawler.cleaner;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.seddat.href.crawler.Post;
import cn.seddat.href.crawler.Source;

/**
 * @author mzhgeng
 * 
 */
public class ShuimuCleaner implements Cleaner {

	private final Log log = LogFactory.getLog(ShuimuCleaner.class.getSimpleName());
	private Pattern titleCancelled;
	private Pattern contentHeader, contentTail;

	public ShuimuCleaner() {
		titleCancelled = Pattern.compile("取消[\\s\\S]*发文权限");
		contentHeader = Pattern
				.compile("发信人: [\\s\\S]*?, 信区: [\\s\\S]*?<br/>标&nbsp;&nbsp;题: [\\s\\S]*?<br/>发信站: [\\s\\S]*?<br/><br/>");
		contentTail = Pattern.compile("--<br/>[\\s\\S]*?来源:[\\s\\S]*?<br/>");
	}

	@Override
	public List<Post> clean(Post... posts) throws Exception {
		List<Post> pl = new ArrayList<Post>();
		for (Post p : posts) {
			if (!Source.SHUIMU.getName().equals(p.getSource())) {
				log.info("[Clean] source isn't shuimu, " + p);
				continue;
			}
			if (!this.checkTitle(p.getTitle())) {
				log.info("[Clean] title is invalid, " + p);
				continue;
			}
			if (!this.checkLink(p.getLink())) {
				log.info("[Clean] link is invalid, " + p);
				continue;
			}
			p.setContent(this.cleanConten(p.getContent()));
			pl.add(p);
		}
		return pl;
	}

	private boolean checkTitle(String title) throws Exception {
		if (title == null || title.isEmpty()) {
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

	private String cleanConten(String content) throws Exception {
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
