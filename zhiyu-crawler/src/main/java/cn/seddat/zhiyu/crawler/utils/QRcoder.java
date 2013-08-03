package cn.seddat.zhiyu.crawler.utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;

import jp.sourceforge.qrcode.QRCodeDecoder;
import jp.sourceforge.qrcode.data.QRCodeImage;

import com.swetake.util.Qrcode;

public class QRcoder {

	private static final Charset UTF_8 = Charset.forName("utf-8");

	public BufferedImage encode(String content) throws Exception {
		return this.encode(content, 7);
	}

	public BufferedImage encode(String content, int size) throws Exception {
		if (content == null || content.isEmpty() || content.length() > 800) {
			throw new IllegalAccessError("content is illegal");
		}
		// 获得内容的字节数组，设置编码格式
		byte[] data = content.getBytes(UTF_8);
		Qrcode qrcode = new Qrcode();
		// 设置二维码排错率，可选L(7%)、M(15%)、Q(25%)、H(30%)，排错率越高可存储的信息越少，但对二维码清晰度的要求越小
		qrcode.setQrcodeErrorCorrect('M');
		qrcode.setQrcodeEncodeMode('B');
		// 设置设置二维码尺寸，取值范围1-40，值越大尺寸越大，可存储的信息越大
		qrcode.setQrcodeVersion(size);
		// 图片尺寸
		int imgSize = 67 + 12 * (size - 1);
		BufferedImage img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
		Graphics2D graph = img.createGraphics();
		// 设置背景颜色
		graph.setBackground(Color.WHITE);
		// 设定图像颜色> BLACK
		graph.setColor(Color.BLACK);
		graph.clearRect(0, 0, imgSize, imgSize);
		// 设置偏移量，不设置可能导致解析出错
		int pixoff = 2;
		// 输出内容> 二维码
		boolean[][] codeOut = qrcode.calQrcode(data);
		for (int i = 0; i < codeOut.length; i++) {
			for (int j = 0; j < codeOut.length; j++) {
				if (codeOut[j][i]) {
					graph.fillRect(j * 3 + pixoff, i * 3 + pixoff, 3, 3);
				}
			}
		}
		graph.dispose();
		img.flush();
		return img;
	}

	public String decode(InputStream input) throws Exception {
		if (input == null) {
			throw new IllegalArgumentException("input is required");
		}
		BufferedImage img = ImageIO.read(input);
		QRCodeDecoder decoder = new QRCodeDecoder();
		byte[] data = decoder.decode(new QRcodeImage(img));
		return new String(data, UTF_8);
	}

	class QRcodeImage implements QRCodeImage {

		BufferedImage image;

		public QRcodeImage(BufferedImage source) {
			this.image = source;
		}

		public int getWidth() {
			return image.getWidth();
		}

		public int getHeight() {
			return image.getHeight();
		}

		public int getPixel(int x, int y) {
			return image.getRGB(x, y);

		}
	}

}
