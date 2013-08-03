/**
 * 
 */
package cn.seddat.zhiyu.crawler.spider;

import java.util.List;

import cn.seddat.zhiyu.crawler.Post;

/**
 * @author mzhgeng
 * 
 */
public interface Spider {

	public String getSeed();

	public List<Post> crawl() throws Exception;

}
