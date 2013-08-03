/**
 * 
 */
package cn.seddat.zhiyu.crawler.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author mzhgeng
 * 
 */
public class QRcoderTest extends TestCase {

	private QRcoder qrcoder;

	@Override
	protected void setUp() throws Exception {
		qrcoder = new QRcoder();
	}

	public void test_code() throws Exception {
		String data = "知遇\ngengmzh@gmail.com\nqq: 273090727";
		BufferedImage img = qrcoder.encode(data);
		ImageIO.write(img, "png", new File("e:\\default.png"));
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ImageIO.write(img, "png", out);
		ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
		out.close();
		System.out.println(in.available());
		String result = qrcoder.decode(in);
		in.close();

		Assert.assertEquals(data, result);
		System.out.println(result);
	}
}
