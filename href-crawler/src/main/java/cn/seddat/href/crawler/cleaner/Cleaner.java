/**
 * 
 */
package cn.seddat.href.crawler.cleaner;

import java.util.List;

import cn.seddat.href.crawler.Post;

/**
 * @author mzhgeng
 * 
 */
public interface Cleaner {

	public List<Post> clean(Post... posts) throws Exception;

}
