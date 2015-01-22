package net.chcbz.util.file;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

/**
 * 图片工具类，完成图片的截取
 * 
 * @author inc062977
 * 
 */
public class ImageHepler {
	/**
	 * 实现图像的等比缩放
	 * 
	 * @param source
	 *            原文件
	 * @param targetW
	 *            缩放后宽度
	 * @param targetH
	 *            缩放后高度
	 * @param proportion
	 *            是否等比缩放或等比缩放方式,
	 *            0为不等比, 
	 *            1为等比缩放并按比例小的一方缩放, 
	 *            2为等比缩放并按比例大的一方缩放
	 * @return
	 */
	private static BufferedImage resize(BufferedImage source, int targetW,
			int targetH, int proportion) {
		// targetW，targetH分别表示目标长和宽
		int type = source.getType();
		BufferedImage target = null;
		double sx = (double) targetW / source.getWidth();
		double sy = (double) targetH / source.getHeight();
		// 这里想实现在targetW，targetH范围内实现等比缩放
		if ((proportion==1 && sx > sy) || (proportion==2 && sx < sy)) {
			sx = sy;
			targetW = (int) (sx * source.getWidth());
		} else if((proportion==1 && sx < sy) || (proportion==2 && sx > sy)) {
			sy = sx;
			targetH = (int) (sy * source.getHeight());
		}
		if (type == BufferedImage.TYPE_CUSTOM) { // handmade
			ColorModel cm = source.getColorModel();
			WritableRaster raster = cm.createCompatibleWritableRaster(targetW,
					targetH);
			boolean alphaPremultiplied = cm.isAlphaPremultiplied();
			target = new BufferedImage(cm, raster, alphaPremultiplied, null);
		} else
			target = new BufferedImage(targetW, targetH, type);
		Graphics2D g = target.createGraphics();
		// smoother than exlax:
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
				RenderingHints.VALUE_INTERPOLATION_BICUBIC);
		g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
		g.dispose();
		return target;
	}

	/**
	 * 实现图像的等比缩放和缩放后的截取
	 * 
	 * @param inFilePath
	 *            要截取文件的路径
	 * @param outFilePath
	 *            截取后输出的路径
	 * @param width
	 *            要截取宽度
	 * @param hight
	 *            要截取的高度
	 * @param proportion 是否等比缩放
	 * @param due 后期处理,0为不处理,1为截图,2为补白
	 * @throws Exception
	 */

	@SuppressWarnings("static-access")
	public static void handleImage(String inFilePath, String outFilePath,
			int width, int height, boolean proportion, int due) throws Exception {
		File inFile = new File(inFilePath);
		String iFileName = System.getProperty("java.io.tmpdir")+"/"+inFile.getName();
		File file = new File(iFileName);
		FileHelper.copyFile(inFile, file);
		InputStream in = new FileInputStream(file);
		File saveFile = new File(outFilePath);
		File targetPath = saveFile.getParentFile();
		if(!targetPath.exists()){
			targetPath.mkdirs();
		}

		BufferedImage srcImage = ImageIO.read(in);
//		if (width > 0 || height > 0) {
			// 原图的大小
//			int sw = srcImage.getWidth();
//			int sh = srcImage.getHeight();
			// 如果原图像的大小小于要缩放的图像大小，直接将要缩放的图像复制过去
//			if (sw > width && sh > hight) {
//				srcImage = resize(srcImage, width, height, 1);
//			} else {
//				String fileName = saveFile.getName();
//				String formatName = fileName.substring(fileName
//						.lastIndexOf('.') + 1);
//				ImageIO.write(srcImage, formatName, saveFile);
//				return;
//			}
//		}
		String fileName = saveFile.getName();
		String formatName = fileName.substring(fileName.lastIndexOf('.') + 1);
		if(due==1){
			srcImage = resize(srcImage, width, height, 2);
			// 缩放后的图像的宽和高
			int w = srcImage.getWidth();
			int h = srcImage.getHeight();
			// 如果缩放后的图像和要求的图像宽度一样，就对缩放的图像的高度进行截取
			if (w == width) {
				// 计算X轴坐标
				int x = 0;
				int y = h / 2 - height / 2;
				saveSubImage(srcImage, new Rectangle(x, y, width, height), saveFile);
			}
			// 否则如果是缩放后的图像的高度和要求的图像高度一样，就对缩放后的图像的宽度进行截取
			else if (h == height) {
				// 计算X轴坐标
				int x = w / 2 - width / 2;
				int y = 0;
				saveSubImage(srcImage, new Rectangle(x, y, width, height), saveFile);
			}
		}else if(due==2){
			srcImage = resize(srcImage, width, height, 1);
			// 缩放后的图像的宽和高
			int w = srcImage.getWidth();
			int h = srcImage.getHeight();
			Image itemp = srcImage.getScaledInstance(w, h, srcImage.SCALE_SMOOTH); 
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB); 
			Graphics2D g = image.createGraphics();      
			g.setColor(Color.white);      
			g.fillRect(0, 0, width, height);      
			if (width == itemp.getWidth(null))      
				g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);      
			else     
				g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);      
			g.dispose();      
			itemp = image;
			ImageIO.write((BufferedImage) itemp, formatName, saveFile);  
		}else{
			srcImage = resize(srcImage, width, height, 1);
			ImageIO.write(srcImage, formatName, saveFile);
		}
		in.close();
	}

	/**
	 * 实现缩放后的截图
	 * 
	 * @param image
	 *            缩放后的图像
	 * @param subImageBounds
	 *            要截取的子图的范围
	 * @param subImageFile
	 *            要保存的文件
	 * @throws IOException
	 */
	private static void saveSubImage(BufferedImage image,
			Rectangle subImageBounds, File subImageFile) throws IOException {
		if (subImageBounds.x < 0 || subImageBounds.y < 0
				|| subImageBounds.width - subImageBounds.x > image.getWidth()
				|| subImageBounds.height - subImageBounds.y > image.getHeight()) {
			System.out.println("Bad   subimage   bounds");
			return;
		}
		BufferedImage subImage = image.getSubimage(subImageBounds.x,
				subImageBounds.y, subImageBounds.width, subImageBounds.height);
		String fileName = subImageFile.getName();
		String formatName = fileName.substring(fileName.lastIndexOf('.') + 1);
		ImageIO.write(subImage, formatName, subImageFile);
	}
	
	 /**
	 * 图像类型转换 GIF->JPG GIF->PNG PNG->JPG PNG->GIF(X)
	 */
	public static void convert(String source, String result) {
		try {
			File f = new File(source);
			f.canRead();
			f.canWrite();
			BufferedImage src = ImageIO.read(f);
			ImageIO.write(src, "JPG", new File(result));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 彩色转为黑白
	 * 
	 * @param source
	 * @param result
	 */
	public static void gray(String source, String result) {
		try {
			BufferedImage src = ImageIO.read(new File(source));
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			ColorConvertOp op = new ColorConvertOp(cs, null);
			src = op.filter(src, null);
			ImageIO.write(src, "JPEG", new File(result));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	 /**
	 * 把图片印刷到图片上
	 * 
	 * @param pressImg
	 *            -- 水印文件
	 * @param targetImg
	 *            -- 目标文件
	 * @param x
	 * @param y
	 */
	public final static void pressImage(String pressImg, String targetImg,
			int x, int y) {
		try {
			File _file = new File(targetImg);
			Image src = ImageIO.read(_file);
			int wideth = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(wideth, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = image.createGraphics();
			g.drawImage(src, 0, 0, wideth, height, null);

			// 水印文件
			File _filebiao = new File(pressImg);
			Image src_biao = ImageIO.read(_filebiao);
			int wideth_biao = src_biao.getWidth(null);
			int height_biao = src_biao.getHeight(null);
			g.drawImage(src_biao, wideth - wideth_biao - x, height
					- height_biao - y, wideth_biao, height_biao, null);
			// /
			g.dispose();
			FileOutputStream out = new FileOutputStream(targetImg);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(image);
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 打印文字水印图片
	 * 
	 * @param pressText
	 *            --文字
	 * @param targetImg
	 *            -- 目标图片
	 * @param fontName
	 *            -- 字体名
	 * @param fontStyle
	 *            -- 字体样式
	 * @param color
	 *            -- 字体颜色
	 * @param fontSize
	 *            -- 字体大小
	 * @param x
	 *            -- 偏移量
	 * @param y
	 */

	public static void pressText(String pressText, String targetImg,
			String fontName, int fontStyle, int color, int fontSize, int x,
			int y) {
		try {
			File _file = new File(targetImg);
			Image src = ImageIO.read(_file);
			int wideth = src.getWidth(null);
			int height = src.getHeight(null);
			BufferedImage image = new BufferedImage(wideth, height,
					BufferedImage.TYPE_INT_RGB);
			Graphics g = image.createGraphics();
			g.drawImage(src, 0, 0, wideth, height, null);
			// String s="www.qhd.com.cn";
			g.setColor(Color.RED);
			g.setFont(new Font(fontName, fontStyle, fontSize));

			g.drawString(pressText, wideth - fontSize - x, height - fontSize
					/ 2 - y);
			g.dispose();
			FileOutputStream out = new FileOutputStream(targetImg);
			JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(out);
			encoder.encode(image);
			out.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	/**
	 * 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
	 * @param imgFilePath 文件路径
	 * @return
	 */
	public static String imageToBase64(String imgFilePath) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		// 读取图片字节数组
		try {
			String suffix = imgFilePath.substring(imgFilePath.lastIndexOf(".")+1);
			BufferedImage bi = ImageIO.read(new File(imgFilePath));
			ImageIO.write(bi, suffix, bos);
			// 对字节数组Base64编码
			BASE64Encoder encoder = new BASE64Encoder();
			return encoder.encodeBuffer(bos.toByteArray());// 返回Base64编码过的字节数组字符串
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(bos!=null){
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	/**
	 * 对字节数组字符串进行Base64解码并生成图片
	 * @param imgStr 图片Base64字符串
	 * @param imgFilePath 图片路径
	 * @return
	 */
	public static boolean base64ToImage(String imgStr, String imgFilePath) {
		if (imgStr == null) // 图像数据为空
			return false;
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] bytes = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < bytes.length; ++i) {
				if (bytes[i] < 0) {// 调整异常数据
					bytes[i] += 256;
				}
			}
			// 生成jpeg图片
			OutputStream out = new FileOutputStream(imgFilePath);
			out.write(bytes);
			out.flush();
			out.close();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public static void main(String[] args){
		try {
			String fileName = "d:\\mifengcaiji.jpg";
			String descName = "d:\\1024768.jpg";
//			String iFileName = System.getProperty("java.io.tmpdir")+"已修.jpg";
//			FileHelper.copyFile(new File(fileName), new File(iFileName));
//			handleImage(fileName, descName,150,250, true, 0);
			
			System.out.println(imageToBase64("D:\\Richow\\project\\4ever330\\images\\hunli\\hunsha\\2R---LSR_9395.jpg"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}