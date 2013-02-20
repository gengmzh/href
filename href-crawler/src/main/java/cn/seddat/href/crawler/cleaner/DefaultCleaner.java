/**
 * 
 */
package cn.seddat.href.crawler.cleaner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.seddat.href.crawler.Post;
import cn.seddat.href.crawler.Source;

/**
 * @author mzhgeng
 * 
 */
public class DefaultCleaner implements Cleaner {

	private Map<String, Cleaner> cleaners;

	public DefaultCleaner() {
		cleaners = new HashMap<String, Cleaner>();
		cleaners.put(Source.SHUIMU.getName(), new ShuimuCleaner());
	}

	@Override
	public List<Post> clean(Post... posts) throws Exception {
		List<Post> result = new ArrayList<>();
		// classify
		Map<String, List<Post>> pm = new HashMap<String, List<Post>>();
		for (Post p : posts) {
			if (p.getSource() == null || p.getSource().isEmpty()) {
				continue;
			}
			if (!pm.containsKey(p.getSource())) {
				pm.put(p.getSource(), new ArrayList<Post>());
			}
			pm.get(p.getSource()).add(p);
		}
		// clean
		for (String source : pm.keySet()) {
			if (!cleaners.containsKey(source)) {
				continue;
			}
			List<Post> list = pm.get(source);
			result.addAll(cleaners.get(source).clean(list.toArray(new Post[list.size()])));
		}
		return result;
	}

}
