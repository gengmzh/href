/**
 * 
 */
package cn.seddat.zhiyu.crawler.cleaner;

import java.util.List;

import cn.seddat.zhiyu.crawler.Post;

/**
 * @author mzhgeng
 * 
 */
public interface Cleaner {

	public String getSeed();

	public List<Post> clean(Post... posts) throws Exception;

	public List<Post> clean(List<Post> posts) throws Exception;

}
