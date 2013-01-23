/**
 * 
 */
package cn.seddat.href.crawler.spider;

import java.util.List;

import cn.seddat.href.crawler.Post;

/**
 * @author mzhgeng
 * 
 */
public interface Spider {

	public List<Post> crawl() throws Exception;

}
