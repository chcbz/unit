package net.chcbz.util.file;

import java.io.File;
import java.util.List;

import org.apache.commons.io.FilenameUtils;

public class CodeConven {

	private static String OldPath;
	private static String NewPath;

	public static boolean process() {
		int type = checkContentType();
		boolean status = false;
		if (type == 0) {
			status = processFLV(OldPath);// 直接将文件转为flv文件
		} else if (type == 1) {
			// String avifilepath = processAVI(type);
			// if (avifilepath == null)
			// return false;// avi文件没有得到
			// status = processFLV(avifilepath);// 将avi转为flv
			status = processRM();
		}
		return status;
	}

	private static int checkContentType() {
		String type = OldPath.substring(OldPath.lastIndexOf(".") + 1,
				OldPath.length()).toLowerCase();
		// ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
		if (type.equals("avi")) {
			return 0;
		} else if (type.equals("mpg")) {
			return 0;
		} else if (type.equals("wmv")) {
			return 0;
		} else if (type.equals("3gp")) {
			return 0;
		} else if (type.equals("mov")) {
			return 0;
		} else if (type.equals("mp4")) {
			return 0;
		} else if (type.equals("asf")) {
			return 0;
		} else if (type.equals("asx")) {
			return 0;
		} else if (type.equals("flv")) {
			return 0;
		}
		// 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等),
		// 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
		else if (type.equals("wmv9")) {
			return 1;
		} else if (type.equals("rm")) {
			return 1;
		} else if (type.equals("rmvb")) {
			return 1;
		}
		return 9;
	}

	private static boolean checkfile(String OldPath) {
		File file = new File(OldPath);
		if (!file.isFile()) {
			return false;
		}
		return true;
	}

	// 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等), 可以先用别的工具（mencoder）转换为avi(ffmpeg能解析的)格式.
	private static String processAVI(int type) {
		List<String> commend = new java.util.ArrayList<String>();
		commend.add("d:\\ffmpeg\\mencoder");
		commend.add(OldPath);
		commend.add("-oac");
		commend.add("lavc");
		commend.add("-lavcopts");
		commend.add("acodec=mp3:abitrate=64");
		commend.add("-ovc");
		commend.add("xvid");
		commend.add("-xvidencopts");
		commend.add("bitrate=600");
		commend.add("-of");
		commend.add("avi");
		commend.add("-o");
		commend.add(NewPath);
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			builder.start();
			return NewPath;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 对ffmpeg无法解析的文件格式(wmv9，rm，rmvb等), 用mencoder转换为FLV格式.
	private static boolean processRM() {
		List<String> commend = new java.util.ArrayList<String>();
		commend.add("mencoder");
		commend.add(OldPath);
		commend.add("-oac");
		commend.add("mp3lame");
		commend.add("-lameopts");
		commend.add("abr:br=56");
		commend.add("-lavcopts");
		commend
				.add("vcodec=flv:vbitrate=150:mbd=2:mv0:trell:v4mv:cbp:last_pred=3");
		commend.add("-ovc");
		commend.add("lavc");
		commend.add("-srate");
		commend.add("22050");
		commend.add("-of");
		commend.add("lavf");
		commend.add("-o");
		commend.add(NewPath);
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			builder.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	// ffmpeg能解析的格式：（asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv等）
	private static boolean processFLV(String oldfilepath) {

		if (!checkfile(OldPath)) {
			System.out.println(oldfilepath + " is not file");
			return false;
		}

		List<String> commend = new java.util.ArrayList<String>();
		commend.add("ffmpeg");
		commend.add("-i");
		commend.add(oldfilepath);
		commend.add("-y");
		commend.add("-ab");
		commend.add("32");
		commend.add("-ar");
		commend.add("22050");
		commend.add("-qscale");
		commend.add("6");
		// commend.add("-b");
		// commend.add("800000");
		// commend.add("-s");
		// commend.add("640*480");
		commend.add(NewPath);
		try {
			ProcessBuilder builder = new ProcessBuilder();
			builder.command(commend);
			builder.start();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public static boolean screenShot(String flvFilePath,String jpgPath){
		if(FilenameUtils.getExtension(flvFilePath).equals("flv")){
			List<String> commend = new java.util.ArrayList<String>();
			commend.add("ffmpeg");
			commend.add("-i");
			commend.add(flvFilePath);
			commend.add("-y");
			commend.add("-f");
			commend.add("image2");
			commend.add("-ss");
			commend.add("00:00:02");
			commend.add("-t");
			commend.add("0.001");
			commend.add("-s");
			commend.add("320x240");
			commend.add(jpgPath);
			try {
				ProcessBuilder builder = new ProcessBuilder();
				builder.command(commend);
				builder.start();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}else{
			return false;
		}
	}
	
	public static void main(String[] args){
		CodeConven cc= new CodeConven();
        String oldPath="/tmp/test.avi";
        String newPath="/tmp/test.flv";
        cc.setOldPath(oldPath);
        cc.setNewPath(newPath);
        if(cc.process()){
            System.out.println("conven ok");
        }
	}

	public String getOldPath() {
		return OldPath;
	}

	public void setOldPath(String oldPath) {
		OldPath = oldPath;
	}

	public String getNewPath() {
		return NewPath;
	}

	public void setNewPath(String newPath) {
		NewPath = newPath;
	}
}