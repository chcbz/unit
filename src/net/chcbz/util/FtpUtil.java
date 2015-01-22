package net.chcbz.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpUtil {
	private static final Logger logger = LoggerFactory.getLogger(FtpUtil.class);
	private FTPClient ftpClient;
	public static final int BINARY_FILE_TYPE = FTP.BINARY_FILE_TYPE;
	public static final int ASCII_FILE_TYPE = FTP.ASCII_FILE_TYPE;
	
	/**
	 * 初始化FTP配置
	 * @param charset 系统编码,如"UTF-8"、"GB2312"
	 * @param sysType 系统类型,包括（"AS/400"、"TYPE: L8"、"MVS"、"NETWARE"、"WINDOWS"、"OS/2"、"OS/2"、"UNIX"、"VMS"）
	 * @param sysLanguageCode 系统语言,中文为"zh"
	 */
	public void initialClientConfig(String charset, String sysType, String sysLanguageCode){
		ftpClient = new FTPClient();
		ftpClient.setControlEncoding(charset);
		FTPClientConfig conf = new FTPClientConfig(sysType);
		conf.setServerLanguageCode(sysLanguageCode);
		ftpClient.configure(conf);
	}

	// path should not the path from root index
	// or some FTP server would go to root as '/'.
	public void connectServer(Map<String,String> ftpConfig) throws SocketException,
			IOException {
		String server = ftpConfig.get("server");
		int port = Integer.valueOf(ftpConfig.get("port"));
		String user = ftpConfig.get("user");
		String password = ftpConfig.get("password");
		String location = ftpConfig.get("location");
		connectServer(server, port, user, password, location);
	}

	public void connectServer(String server, int port, String user,
			String password, String path) throws SocketException, IOException {
		ftpClient.connect(server, port);
		logger.info("Connected to " + server + ".");
		logger.info(String.valueOf(ftpClient.getReplyCode()));
		ftpClient.login(user, password);
		// Path is the sub-path of the FTP path
		if (path.length() != 0) {
			ftpClient.changeWorkingDirectory(path);
		}
	}

	// FTP.BINARY_FILE_TYPE | FTP.ASCII_FILE_TYPE
	// Set transform type
	public void setFileType(int fileType) throws IOException {
		ftpClient.setFileType(fileType);
	}

	public void closeServer() throws IOException {
		if (ftpClient.isConnected()) {
			ftpClient.disconnect();
			logger.info("Close the Server!");
		}
	}

	// =======================================================================
	// == About directory =====
	// The following method using relative path better.
	// =======================================================================

	public boolean changeDirectory(String path) throws IOException {
		return ftpClient.changeWorkingDirectory(path);
	}

	public boolean createDirectory(String pathName) throws IOException {
		return ftpClient.makeDirectory(pathName);
	}
	
	public boolean createDirectorys(String path) throws IOException {
		try{
			String[] subPath = path.split("[/\\\\]+");
			String fPath = "/";
//			if(path.indexOf("/")!=0){
//				fPath = "/";
//			}
			for(int i=0;i<subPath.length;i++){
				if(!subPath[i].equals("")){
					fPath = fPath + subPath[i] + "/";
					createDirectory(fPath);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
		
		/*if(path.indexOf("/")!=-1 && path.lastIndexOf("/")!=0 && path.indexOf("/")!=path.length()-1){
			String fPath = path.substring(0,path.indexOf("/")+1);
			String ePath = path.substring(path.indexOf("/")+1);
			while(true){
				if(!existDirectory(fPath)){
//					System.out.println("fPath="+fPath);
					createDirectory(fPath);
				}
				fPath += ePath.substring(0,ePath.indexOf("/")+1);
				if(ePath.indexOf("/")==-1 && ePath.indexOf("/")==ePath.length()-1){
					break;
				}
				ePath = ePath.substring(path.indexOf("/")+1);
			}
			if(!existDirectory(fPath)){
//				System.out.println(ePath+"---------");
				createDirectory(ePath);
			}
			return true;
		}else{
			return createDirectory(path);
		}*/
	}

	public boolean removeDirectory(String path) throws IOException {
		return ftpClient.removeDirectory(path);
	}

	// delete all subDirectory and files.
	public boolean removeDirectory(String path, boolean isAll)
			throws IOException {

		if (!isAll) {
			return removeDirectory(path);
		}

		FTPFile[] ftpFileArr = ftpClient.listFiles(path);
		if (ftpFileArr == null || ftpFileArr.length == 0) {
			return removeDirectory(path);
		}
		//    
		for (FTPFile ftpFile : ftpFileArr) {
			String name = ftpFile.getName();
			if (ftpFile.isDirectory()) {
				if (!ftpFile.getName().equals(".")
						&& (!ftpFile.getName().equals(".."))) {
					logger.info("* [sD]Delete subPath [" + path + "/"
							+ name + "]");
					removeDirectory(path + "/" + name, true);
				}
			} else if (ftpFile.isFile()) {
				logger.info("* [sF]Delete file [" + path + "/" + name
						+ "]");
				deleteFile(path + "/" + name);
			} else if (ftpFile.isSymbolicLink()) {

			} else if (ftpFile.isUnknown()) {

			}
		}
		return ftpClient.removeDirectory(path);
	}

	// Check the path is exist; exist return true, else false.
	public boolean existDirectory(String path) throws IOException {
		boolean flag = false;
		FTPFile[] ftpFileArr = ftpClient.listFiles(path);
		if(ftpFileArr.length!=0){
			flag = true;
		}
//		for (FTPFile ftpFile : ftpFileArr) {
//			if (ftpFile.isDirectory()
//					&& ftpFile.getName().equalsIgnoreCase(path)) {
//				flag = true;
//				break;
//			}
//		}
		return flag;
	}

	// =======================================================================
	// == About file =====
	// Download and Upload file using
	// ftpUtil.setFileType(FtpUtil.BINARY_FILE_TYPE) better!
	// =======================================================================

	// #1. list & delete operation
	// Not contains directory
	public List<String> getFileList(String path) throws IOException {
		// listFiles return contains directory and file, it's FTPFile instance
		// listNames() contains directory, so using following to filer
		// directory.
		// String[] fileNameArr = ftpClient.listNames(path);
		FTPFile[] ftpFiles = ftpClient.listFiles(path);

		List<String> retList = new ArrayList<String>();
		if (ftpFiles == null || ftpFiles.length == 0) {
			return retList;
		}
		for (FTPFile ftpFile : ftpFiles) {
			if (ftpFile.isFile()) {
				retList.add(ftpFile.getName());
			}
		}
		return retList;
	}

	public boolean deleteFile(String pathName) throws IOException {
		return ftpClient.deleteFile(pathName);
	}

	// #2. upload to ftp server
	// InputStream <------> byte[] simple and See API

	public boolean uploadFile(String fileName, String newName)
			throws IOException {
		boolean flag = false;
		InputStream iStream = null;
		try {
			iStream = new FileInputStream(fileName);
			flag = ftpClient.storeFile(newName, iStream);
		} catch (IOException e) {
			flag = false;
			return flag;
		} finally {
			if (iStream != null) {
				iStream.close();
			}
		}
		return flag;
	}

	public boolean uploadFile(String fileName) throws IOException {
		return uploadFile(fileName, fileName);
	}

	public boolean uploadFile(InputStream iStream, String newName)
			throws IOException {
		boolean flag = false;
		try {
			// can execute [OutputStream storeFileStream(String remote)]
			// Above method return's value is the local file stream.
			flag = ftpClient.storeFile(newName, iStream);
		} catch (IOException e) {
			flag = false;
			return flag;
		} finally {
			if (iStream != null) {
				iStream.close();
			}
		}
		return flag;
	}
	
	/**
	 * 上传文件夹及所有子文件到FTP服务器
	 * @param localPath 本地路径
	 * @param remotePath 相对FTP服务器根目录的路径
	 * @return 是否上传成功
	 * @throws IOException
	 */
	public boolean uploadFolder(String localPath, String remotePath) throws IOException {
		boolean flag = false;
		
//		if(remotePath.lastIndexOf("/")==remotePath.length()-1){
//			remotePath = remotePath.substring(0,remotePath.length()-1);
//		}
		if(!existDirectory(remotePath)){
			createDirectorys(remotePath);
		}
		File localPathFile = new File(localPath);
		if(localPathFile.exists()){
			if(localPathFile.isDirectory()){
				File[] subFiles = localPathFile.listFiles();
				for(int i=0;i<localPathFile.list().length;i++){
					if(subFiles[i].isDirectory()){
						uploadFolder(subFiles[i].getAbsolutePath(),remotePath+"/"+subFiles[i].getName());
					}else{
						FileInputStream fis = new FileInputStream(subFiles[i]);
						logger.info(remotePath+"/"+subFiles[i].getName());
						uploadFile(fis,remotePath+"/"+subFiles[i].getName());
					}
				}
			}
		}
		return flag;
	}

	// #3. Down load

	public boolean download(String remoteFileName, String localFileName)
			throws IOException {
		boolean flag = false;
		File outfile = new File(localFileName);
		OutputStream oStream = null;
		try {
			oStream = new FileOutputStream(outfile);
			flag = ftpClient.retrieveFile(remoteFileName, oStream);
		} catch (IOException e) {
			flag = false;
			return flag;
		} finally {
			oStream.close();
		}
		return flag;
	}

	public InputStream downFile(String sourceFileName) throws IOException {
		return ftpClient.retrieveFileStream(sourceFileName);
	}
}