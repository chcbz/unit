package net.chcbz.util.file;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;

public class RandomNumUtil {
	private ByteArrayInputStream image;
	private String validateCode;
	
	public RandomNumUtil(){
		createImage(85,20,115,4);
	}
	
	public static RandomNumUtil instance(){
		return new RandomNumUtil();
	}
	
	public ByteArrayInputStream getImage() {
		return image;
	}

	public String getValidateCode() {
		return validateCode;
	}

	/**
	 * 
	 * @param width 验证图片宽
	 * @param height 验证图片高
	 * @param fuzzy 验证图片模糊程度，即干扰条数
	 * @param num 验证码位数
	 */
	private void createImage(Integer width,Integer height,Integer fuzzy,Integer num){
		BufferedImage image = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
		Graphics g = image.createGraphics();
		Random random = new Random();
		g.setColor(getRandomColor(200,250));
		g.fillRect(0, 0, width, height);
		g.setFont(new Font("Times New Roman",Font.PLAIN,18));
		//随机生成干扰条
		g.setColor(getRandomColor(160,200));
		for(int i=0;i<fuzzy;i++){
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			int x1 = random.nextInt(12);
			int y1 = random.nextInt(12);
			g.drawLine(x, y, x+x1, y+y1);
		}
		//随机生成验证码
		String sRand = "";
		for(int i=0;i<num;i++){
			String rand = String.valueOf(random.nextInt(10));
			sRand +=rand;
			g.setColor(new Color(20+random.nextInt(110),20+random.nextInt(110),20+random.nextInt(110)));
			g.drawString(rand, width/num*i, height*3/4);
		}
		this.validateCode = sRand;
		g.dispose();
		//生成图像
		ByteArrayInputStream input = null;
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		try{
			ImageOutputStream imageOut = ImageIO.createImageOutputStream(output);
			ImageIO.write(image,"JPEG",imageOut);
			imageOut.close();
			input = new ByteArrayInputStream(output.toByteArray());
		}catch(IOException e){
			e.printStackTrace();
		}
		this.image = input;
	}
	/**
	 * 
	 * @param fc
	 * @param bc
	 * @return
	 */
	private Color getRandomColor(Integer fc,Integer bc){
		Random random = new Random();
		if(fc>255){
			fc = 255;
		}
		if(bc>255){
			bc = 255;
		}
		int r = fc+random.nextInt(bc-fc);
		int g = fc+random.nextInt(bc-fc);
		int b = fc+random.nextInt(bc-fc);
		return new Color(r,g,b);
	}
}
