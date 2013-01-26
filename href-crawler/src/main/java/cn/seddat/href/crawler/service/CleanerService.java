/**
 * 
 */
package cn.seddat.href.crawler.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.seddat.href.crawler.Post;
import cn.seddat.href.crawler.Source;
import cn.seddat.href.crawler.cleaner.Cleaner;
import cn.seddat.href.crawler.cleaner.ShuimuCleaner;

/**
 * @author mzhgeng
 * 
 */
public class CleanerService {

	private Map<String, Cleaner> cleaners;

	public CleanerService() {
		cleaners = new HashMap<String, Cleaner>();
		cleaners.put(Source.SHUIMU.getName(), new ShuimuCleaner());
	}

	public List<Post> clean(Post... posts) throws Exception {
		List<Post> pl = new ArrayList<>();
		for (Post p : posts) {
			if (cleaners.containsKey(p.getSource())) {
				pl.addAll(cleaners.get(p.getSource()).clean(p));
			}
		}
		return pl;
	}

}
