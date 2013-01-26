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
	private Pattern invalidTitle;

	public ShuimuCleaner() {
		invalidTitle = Pattern.compile("取消[\\s\\S]*发文权限");
	}

	@Override
	public List<Post> clean(Post... posts) throws Exception {
		List<Post> pl = new ArrayList<Post>();
		for (Post p : posts) {
			if (p.getTitle() == null || p.getTitle().isEmpty() || p.getLink() == null || p.getLink().isEmpty()) {
				log.info("[Clean] title/link is null, " + p);
				continue;
			}
			if (!Source.SHUIMU.getName().equals(p.getSource())) {
				log.info("[Clean] source isn't shuimu, " + p);
				continue;
			}
			// title
			Matcher m = invalidTitle.matcher(p.getTitle());
			if (m.find()) {
				log.info("[Clean] title is invalid, " + p);
				continue;
			}
			pl.add(p);
		}
		return pl;
	}

}
