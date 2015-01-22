package net.chcbz.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileHelper {
	private static final Logger logger = LoggerFactory.getLogger(FileHelper.class);
	/**
	 * 复制文件夹
	 * @param sourceDir 源文件夹的绝对路径
	 * @param targetDir 目标文件夹的绝对路径
	 * @throws IOException
	 */
	public static void copyDirectiory(String sourceDir, String targetDir)
			throws IOException {
		// 新建目标目录
		(new File(targetDir)).mkdirs();
		// 获取源文件夹当前下的文件或目录
		File fsourceDir = new File(sourceDir);
		if(!fsourceDir.exists()){
			fsourceDir.mkdirs();
		}
		File[] file = fsourceDir.listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(new File(targetDir)
						.getAbsolutePath()
						+ File.separator + file[i].getName());
				copyFile(sourceFile, targetFile);
			}
			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + "/" + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();
				copyDirectiory(dir1, dir2);
			}
		}
	}

	public static boolean copyFile(File sourceFile, File targetFile)
			throws IOException {
		if(!sourceFile.exists()){
			logger.error("原文件"+sourceFile.getAbsolutePath()+"不存在");
			return false;
		}
		if(sourceFile.compareTo(targetFile)!=0){
			try{
			// 新建文件输入流并对它进行缓冲
			FileInputStream input = new FileInputStream(sourceFile);
			BufferedInputStream inBuff = new BufferedInputStream(input);
	
			// 新建文件输出流并对它进行缓冲
			FileOutputStream output = new FileOutputStream(targetFile);
			BufferedOutputStream outBuff = new BufferedOutputStream(output);
	
			// 缓冲数组
			byte[] b = new byte[1024 * 5];
			int len;
			while ((len = inBuff.read(b)) != -1) {
				outBuff.write(b, 0, len);
			}
			// 刷新此缓冲的输出流
			outBuff.flush();
	
			// 关闭流
			inBuff.close();
			outBuff.close();
			output.close();
			input.close();
			}catch(FileNotFoundException e){
				logger.error("FileNotFoundException", e);
			}catch(IOException e){
				logger.error("IOException",e);
			}
			return true;
		}else{
			return false;
		}
	}
	
	/**
	 * 删除文件或文件夹
	 * @param path 文件或文件夹绝对路径
	 */
	public static void deleteFile(String path){
		File file = new File(path);
		if(file.isDirectory()){
			while(file.list().length!=0){
				if(!file.listFiles()[0].isDirectory()){
					file.listFiles()[0].delete();
				}else{
					deleteFile(path+"/"+file.list()[0]);
				}
			}
		}
		file.delete();
	}
}
