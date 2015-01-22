package net.chcbz.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * 测试FreeMarker.
 * 
 * @author scud
 * 
 */
@SuppressWarnings("unchecked")
public class FreeMarkerTemplateSupport {

	/**
	 * 生成静态文件.
	 * 
	 * @param templateFilePath
	 *            模板的绝对路径
	 * @param templateFileName
	 *            模板文件名,相对htmlskin路径,例如"/tpxw/view.ftl"
	 * @param propMap
	 *            用于处理模板的属性Object映射
	 * @param htmlFilePath
	 *            要生成的静态文件的路径,相对设置中的根路径,例如 "/tpxw/1/2005/4/"
	 * @param htmlFileName
	 *            要生成的文件名,例如 "1.htm"
	 */
	public static boolean geneHtmlFile(String templateFilePath,String templateFileName, Map propMap,
			String htmlFilePath, String htmlFileName) {
		try {
			Configuration freemarker_cfg = new Configuration();
            freemarker_cfg.setEncoding(Locale.CHINA, "UTF-8");
            freemarker_cfg.setDirectoryForTemplateLoading(new File(templateFilePath));
            freemarker_cfg.setObjectWrapper(new DefaultObjectWrapper()); 
			Template t = freemarker_cfg.getTemplate(templateFileName);
			// 如果根路径存在,则递归创建子目录
			creatDirs(htmlFilePath);

			File afile = new File(htmlFilePath + "/"
					+ htmlFileName);
			
			Writer out = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(afile),"utf-8"));
			t.process(propMap, out);
		} catch (TemplateException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public static String geneString(String templateFilePath,String templateFileName, Map propMap) {
		StringWriter sw = new StringWriter();
		try {
			Configuration freemarker_cfg = new Configuration();
            freemarker_cfg.setEncoding(Locale.CHINA, "utf-8");
            freemarker_cfg.setDirectoryForTemplateLoading(new File(templateFilePath));
            freemarker_cfg.setObjectWrapper(new DefaultObjectWrapper()); 
			Template t = freemarker_cfg.getTemplate(templateFileName);
			Writer out = new BufferedWriter(sw);
			t.process(propMap, out);
		} catch (TemplateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

//		String str = sw.toString();
//		str = str.replaceAll("/ims/userfiles/\\w+/", "");
		return sw.toString();
	}

	/**
	 * 创建多级目录
	 * 
	 * @param aParentDir
	 *            String
	 * @param aSubDir
	 *            以 / 开头
	 * @return boolean 是否成功
	 */
	public static boolean creatDirs(String aSubDir) {
		File aSubFile = new File(aSubDir);
		if (!aSubFile.exists()) {
			return aSubFile.mkdirs();
		} else {
			return true;
		}
	}
}