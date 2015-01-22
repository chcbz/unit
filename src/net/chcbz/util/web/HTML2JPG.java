package net.chcbz.util.web;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

public class HTML2JPG extends JFrame {
	private static final long serialVersionUID = 5492130356893646985L;
	public HTML2JPG(String url,File file) throws Exception{
		JEditorPane editorPane = new JEditorPane();
		editorPane.setEditable(false);
		editorPane.setPage(url);
		JScrollPane jsp = new JScrollPane(editorPane);
		getContentPane().add(jsp);
		this.setLocation(0,0);
		this.setVisible(true);
		
		Thread.sleep(2*1000);
		
		setSize(10000,10000);
		pack();
		System.out.println(editorPane.getWidth()+"-------"+editorPane.getHeight());
		BufferedImage image = new BufferedImage(editorPane.getWidth(),editorPane.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics2D = image.createGraphics();
		editorPane.paint(graphics2D);
		
		BufferedImage image1 = resize(image,600,400);
		
		ImageIO.write(image1,"jpg",file);
		dispose();
	}
	
	public static BufferedImage resize(BufferedImage source,int targetW,int targetH){
		int type = source.getType();
		BufferedImage target = null;
		System.out.println(source.getWidth()+"----"+source.getHeight());
		double sx = (double)targetW/source.getWidth();
		double sy = (double)targetH/source.getHeight();
		if(sx>sy){
			sx = sy;
			targetW = (int)(sx*source.getWidth());
		}else{
			sy = sx;
			targetH = (int)(sy*source.getHeight());
		}
		System.out.println(targetW+"----"+targetH);
		if(type==BufferedImage.TYPE_CUSTOM){
			ColorModel cm = source.getColorModel();
			WritableRaster raster = cm.createCompatibleWritableRaster(targetW,targetH);
			boolean alphaPremultiplied = cm.isAlphaPremultiplied();
			target = new BufferedImage(cm,raster,alphaPremultiplied,null);
		}else{
			target = new BufferedImage(targetW,targetH,type);
		}
		Graphics2D g = target.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.drawRenderedImage(source, AffineTransform.getScaleInstance(sx, sy));
		g.dispose();
		return target;
	}
	
	public static void main(String[] args) throws Exception{
		new HTML2JPG("http://www.google.com/",new File("c:\\google.jpg"));
	}
}
