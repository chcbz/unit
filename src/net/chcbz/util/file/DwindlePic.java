package net.chcbz.util.file;

import java.applet.Applet;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageEncoder;
//缩略图类，  
//本java类能将jpg图片文件，进行等比或非等比的大小转换。  
//具体使用方法  
//s_pic(大图片路径,生成小图片路径,大图片文件名,生成小图片文名,生成小图片宽度,生成小图片高度,是否等比缩放(默认为true))  
public class DwindlePic {
	
	private static final Logger logger = LoggerFactory.getLogger(DwindlePic.class);
  
    private static String InputDir; //输入图路径  
    private static String OutputDir; //输出图路径  
    private static String InputFileName; //输入图文件名  
    private static String OutputFileName; //输出图文件名  
    private static int OutputWidth = 80; //默认输出图片宽  
    private static int OutputHeight = 80; //默认输出图片高  
    int rate = 0;  
    private static boolean proportion = true; //是否等比缩放标记(默认为等比缩放)  
  
    public DwindlePic() {  
        //初始化变量  
        InputDir = "";  
        OutputDir = "";  
        InputFileName = "";  
        OutputFileName = "";  
        OutputWidth = 80;  
        OutputHeight = 80;  
        rate = 0;  
    }  
  
    private static boolean s_pic() {  
//        BufferedImage image;  
//        String NewFileName;  
//建立输出文件对象  
        File file = new File(OutputDir,OutputFileName);  
        FileOutputStream tempout = null;  
        try {  
            tempout = new FileOutputStream(file);  
        } catch (Exception ex) {  
            System.out.println(ex.toString());  
        }  
        Image img = null;  
        Toolkit tk = Toolkit.getDefaultToolkit();
        Applet app = new Applet();  
        MediaTracker mt = new MediaTracker(app);  
        try {  
        	System.out.println(new File(InputDir,InputFileName).getAbsolutePath());
            img = tk.getImage(new File(InputDir,InputFileName).getAbsolutePath());  
            mt.addImage(img, 0);  
            mt.waitForID(0);  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
  
        if (img.getWidth(null) == -1) {  
//            System.out.println(" can't read,retry!" + "<BR>");  
            return false;  
        } else {  
            int new_w;  
            int new_h;  
            if (DwindlePic.proportion == true) { //判断是否是等比缩放.  
//为等比缩放计算输出的图片宽度及高度  
                double rate1 = ((double) img.getWidth(null)) / (double) OutputWidth +  
                        0.1;  
                double rate2 = ((double) img.getHeight(null)) / (double) OutputHeight +  
                        0.1;  
                double rate = rate1 > rate2 ? rate1 : rate2;  
                new_w = (int) (((double) img.getWidth(null)) / rate);  
                new_h = (int) (((double) img.getHeight(null)) / rate);  
            } else {  
                new_w = OutputWidth; //输出的图片宽度  
                new_h = OutputHeight; //输出的图片高度  
            }  
            BufferedImage buffImg = new BufferedImage(new_w, new_h,  
                    BufferedImage.TYPE_INT_RGB);  
  
            Graphics g = buffImg.createGraphics();  
  
            g.setColor(Color.white);  
            g.fillRect(0, 0, new_w, new_h);  
  
            g.drawImage(img, 0, 0, new_w, new_h, null);  
            g.dispose();  
  
            JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(tempout);  
            try {  
                encoder.encode(buffImg);  
                tempout.close();  
            } catch (IOException ex) {  
                System.out.println(ex.toString());  
            }  
        }  
        return true;  
    }  
  
    public static boolean s_pic(String InputDir, String OutputDir, String InputFileName,  
            String OutputFileName) {  
//输入图路径  
    	DwindlePic.InputDir = InputDir;  
//输出图路径  
    	DwindlePic.OutputDir = OutputDir;  
//输入图文件名  
    	DwindlePic.InputFileName = InputFileName;  
//输出图文件名  
    	DwindlePic.OutputFileName = OutputFileName;  
        return s_pic();  
    } 
    
    /**
     * 图片缩放
     * @param dir 图片文件所在目录的绝对路径
     * @param fileName 图片文件名（包括后缀）
     * @param width 处理后的图片宽度
     * @param height 处理后的图片高度
     * @param gp 是否等比缩放
     * @return 是否处理成功
     */
    public static boolean s_pic(String dir, String fileName, int width, int height, boolean gp) {  
    	String tmpDir = System.getProperty("java.io.tmpdir");
    	File tmpFile = new File((new File(tmpDir).getAbsolutePath())+"/"+fileName);
    	File srcFile = new File((new File(dir)).getAbsolutePath()+"/"+fileName);
    	try {
			FileHelper.copyFile(srcFile, tmpFile);
//			srcFile.delete();
		} catch (IOException e) {
			logger.error("IOException",e);
			e.printStackTrace();
		}
		DwindlePic.InputDir = tmpDir;
		DwindlePic.OutputDir = dir;
		DwindlePic.InputFileName = fileName;
		DwindlePic.OutputFileName = fileName;
		setW_H(width, height);
        DwindlePic.proportion = gp;
       return s_pic();
    }
  
    public static boolean s_pic(String InputDir, String OutputDir, String InputFileName,  
            String OutputFileName, int width, int height, boolean gp) {  
//输入图路径  
    	DwindlePic.InputDir = InputDir;  
//输出图路径  
    	DwindlePic.OutputDir = OutputDir;  
//输入图文件名  
    	DwindlePic.InputFileName = InputFileName;  
//输出图文件名  
    	DwindlePic.OutputFileName = OutputFileName;  
//设置图片长宽  
        setW_H(width, height);  
//是否是等比缩放 标记  
        DwindlePic.proportion = gp;  
        return s_pic();  
    }  
  
    private static void setW_H(int width, int height) {  
    	DwindlePic.OutputWidth = width;  
    	DwindlePic.OutputHeight = height;  
    }  
  
    public static void main(String[] a) {  
        //s_pic(大图片路径,生成小图片路径,大图片文件名,生成小图片文名,生成小图片宽度,生成小图片高度)  
//        DwindlePic mypic = new DwindlePic();  
        System.out.println(
        		s_pic("C:\\workspace\\project\\.metadata\\.me_tcat\\webapps\\ims\\template\\dazhong\\src\\images","fi.png", 600, 600, true));
    }  
} 