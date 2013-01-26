/**
 * 
 */
package cn.seddat.href.crawler.cleaner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author mzhgeng
 * 
 */
public class ShuimuCleanerTest extends TestCase {

	public void test_matchTile() throws Exception {
		String title = "gsfgg被取消在Career_Investment版的发文权限";
		Pattern p = Pattern.compile("取消[\\s\\S]*发文权限");
		Matcher m = p.matcher(title);
		Assert.assertTrue(m.find());
		System.out.println(m.group());

		System.out.println(title.matches("取消[\\s\\S]*发文权限"));
	}

}
