package net.chcbz.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPOutputStream;

import net.chcbz.util.secret.CommCodec;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

@SuppressWarnings("unchecked")
public final class Helper {
	public static final Logger info_logger = Logger.getLogger("info_logger");
	public static final Logger moke_logger = Logger.getLogger("moke_logger");
	
    public static final void Log4jConfigLoggerDaillyRollingFile(String name,String file_path,boolean additivity)
	{
		Properties props = new Properties();
		if(name.equals("root")) props.put("log4j.rootLogger","DEBUG," + name + "_appender");
		else 
		{
			props.put("log4j.logger." + name,"DEBUG," + name + "_appender");
			props.put("log4j.additivity." + name,String.valueOf(additivity));
		}
		props.put("log4j.appender." + name + "_appender","org.apache.log4j.DailyRollingFileAppender");
		props.put("log4j.appender." + name + "_appender.File",file_path);
		props.put("log4j.appender." + name + "_appender.layout","org.apache.log4j.PatternLayout");
		props.put("log4j.appender." + name + "_appender.Append","true");
		props.put("log4j.appender." + name + "_appender.layout.ConversionPattern","%d %p %m%n");
		PropertyConfigurator.configure(props);
    }
    
    public static final void Log4jConfigLoggerConsole(String name,boolean additivity)
	{
		Properties props = new Properties();
		if(name.equals("root")) props.put("log4j.rootLogger","DEBUG," + name + "_appender");
		else 
		{
			props.put("log4j.logger." + name,"DEBUG," + name + "_appender");
			props.put("log4j.additivity." + name,String.valueOf(additivity));
		}
		props.put("log4j.appender." + name + "_appender","org.apache.log4j.ConsoleAppender");
		props.put("log4j.appender." + name + "_appender.layout","org.apache.log4j.PatternLayout");
		props.put("log4j.appender." + name + "_appender.layout.ConversionPattern","%d %p %m%n");
		PropertyConfigurator.configure(props);
    }
    
    public static final void Log4jConfigLoggerFile(String name,String file_path,boolean additivity)
	{
    	Properties props = new Properties();
		if(name.equals("root")) props.put("log4j.rootLogger","DEBUG," + name + "_appender");
		else 
		{
			props.put("log4j.logger." + name,"DEBUG," + name + "_appender");
			props.put("log4j.additivity." + name,String.valueOf(additivity));
		}
		props.put("log4j.appender." + name + "_appender","org.apache.log4j.FileAppender");
		props.put("log4j.appender." + name + "_appender.File",file_path);
		props.put("log4j.appender." + name + "_appender.layout","org.apache.log4j.PatternLayout");
		props.put("log4j.appender." + name + "_appender.layout.ConversionPattern","%d %p %m%n");
		PropertyConfigurator.configure(props);
    }
    
    public static final void Log4jConfigLoggerNull(String name,boolean additivity)
	{
    	Properties props = new Properties();
		if(name.equals("root")) props.put("log4j.rootLogger","DEBUG," + name + "_appender");
		else 
		{
			props.put("log4j.logger." + name,"DEBUG," + name + "_appender");
			props.put("log4j.additivity." + name,String.valueOf(additivity));
		}
		props.put("log4j.appender." + name + "_appender","org.apache.log4j.varia.NullAppender");
		PropertyConfigurator.configure(props);
    }
	
	public static final String fileRead(String filepath,String encoding) throws Exception
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(filepath);
			return streamRead(fis,encoding);
		}
		finally
		{
			if(fis!=null) try{fis.close();}catch(Exception e){}
			fis = null;
		}
	}
	
	public static final byte[] fileRead(String filepath,int bufferlen) throws Exception
	{
		FileInputStream fis = null;
		try
		{
			fis = new FileInputStream(filepath);
			return streamRead(fis,bufferlen);
		}
		finally
		{
			if(fis!=null) try{fis.close();}catch(Exception e){}
			fis = null;
		}
	}
	
	public static final void fileWrite(String filepath,byte[] bts) throws Exception
	{
		Helper.fileAssertDirs(filepath);
		FileOutputStream fos = null;
		try
		{
			File filedes = new File(filepath);
			if(filedes.exists()) filedes.delete();
			filedes = null;
			fos = new FileOutputStream(filepath);
			fos.write(bts);
		}
		finally
		{
			if(fos!=null) fos.close();
			fos = null;
		}
	}
	
	public static final byte[] fileRead(File file,int start,int end) throws Exception
	{
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(file,"r");
			byte[] bts = new byte[end-start];
			raf.seek(start);
			if(raf.read(bts)!=bts.length) Helper.E("invalid file offset");
			return bts;
		}
		finally
		{
			if(raf!=null) raf.close();
			raf = null;
		}
	}
	
	public static final void fileGzip(String srcfilepath,String desfilepath) throws Exception
	{
		FileInputStream fis = null;
		FileOutputStream fos = null;
		GZIPOutputStream zos = null;
		try
		{
			File filedes = new File(desfilepath);
			if(filedes.exists()) filedes.delete();
			filedes = null;
			fis = new FileInputStream(srcfilepath);
			fos = new FileOutputStream(desfilepath);
			zos = new GZIPOutputStream(fos);
			zos.write(streamReadBuffered(fis,10240));
		}
		finally
		{
			if(zos!=null) zos.close();
			if(fos!=null) fos.close();
			if(fis!=null) fis.close();
			zos = null;
			fos = null;
			fis = null;
		}
	}
	
	public static final void fileDirAssert(String filepath)
	{
		int i = filepath.lastIndexOf('/');
		if(i==-1) i = filepath.lastIndexOf('\\');
		if(i==-1) return;
		String dirpath = filepath.substring(0,i+1);
		File dir = new File(dirpath);
		if(dir.isDirectory() && dir.exists()==false) dir.mkdirs();
	}
	
	public static final String[] strRegExtract(String src,String regexp)
	{//按照正则表达式分析攫取字符串
		 Pattern p = Pattern.compile(regexp,Pattern.DOTALL);
		 Matcher m = p.matcher(src);
		 if(m.matches()==false) return null;
		 int group_count = m.groupCount();
		 String[] res = new String[group_count];
		 for(int i=1;i<group_count+1;i++) res[i-1] = m.group(i);
		 return res;
	}
	
	public static final String[] strRegExtractIgnoreCased(String src,String regexp)
	{//按照正则表达式分析攫取字符串
		 Pattern p = Pattern.compile(regexp,Pattern.CASE_INSENSITIVE|Pattern.DOTALL);
		 Matcher m = p.matcher(src);
		 if(m.matches()==false) return null;
		 int group_count = m.groupCount();
		 String[] res = new String[group_count];
		 for(int i=1;i<group_count+1;i++) res[i-1] = m.group(i);
		 return res;
	}
	
	public static final boolean strRegMatch(String src,String regexp)
	{
		Pattern p = Pattern.compile(regexp);
		Matcher m = p.matcher(src);
		return m.matches();
	}
	
	protected static final char TOK_DELM_CHAR = ',';
	public static final int tokenContains(String tok,int val)
	{
		return tokenContains(tok,String.valueOf(val));
	}
	public static final String tokenPut(String tok,int val)
	{
		return tokenPut(tok,String.valueOf(val));
	}
	public static final String tokenDelete(String tok,int val)
	{
		return tokenDelete(tok,String.valueOf(val));
	}
	public static final int[] tokenDecode(String tok)
	{
		StringTokenizer st = new StringTokenizer(tok,String.valueOf(TOK_DELM_CHAR));
		List<Integer> list = new ArrayList<Integer>();
		while(st.hasMoreTokens())
		{
			String t = st.nextToken();
			if(t.length()==0) continue;
			try{list.add(Integer.parseInt(t));}
			catch(Exception e){}
		}
		return Helper.listIntToArray(list);
	}
	public static final String tokenEncode(int[] vals)
	{
		StringBuffer sb = new StringBuffer();
		for(int v:vals)
		{
			if(sb.length()>0) sb.append(',');
			sb.append(String.valueOf(v));
		}
		return sb.toString();
	}
	public static final int tokenContains(String tok,String val)
	{//逗号分隔的token管理封装
		int j = 0;
		int toklen = tok.length();
		int len = val.length();
		while(true)
		{
			int i= tok.indexOf(val,j);
			if(i<0) return -1;
			if(i>0 && tok.charAt(i-1)!=TOK_DELM_CHAR)
			{
				j = i+len;
				continue;
			}
			if(i<toklen-len && tok.charAt(i+len)!=TOK_DELM_CHAR)
			{
				j = j+len;
				continue;
			}
			return i;
		}
	}
	public static final String tokenPut(String tok,String val)
	{//逗号分隔的token管理封装
		if(tok.length()==0) tok = val;
		else if(tokenContains(tok,val)==-1) tok += ',' + val;
		return tok;
	}
	public static final String tokenDelete(String tok,String val)
	{//逗号分隔的token管理封装
		int i = tokenContains(tok,val);
		int toklen = tok.length();
		int vallen = val.length();
		String res = null;
		if(i<0)
		{
			res = tok;
		}
		else if(i==0)
		{
			if(i==toklen-vallen) res = "";
			else res = tok.substring(i+vallen+1);
		}
		else if(i==toklen-vallen)
		{
			if(i==0) res = "";
			else res = tok.substring(0,i-1);
		}
		else
		{
			res = tok.substring(0,i-1) + TOK_DELM_CHAR + tok.substring(i+vallen+1);
		}
		return res;
	}
	
	public static final char[] tokens = {'、','，',',','。','.',';','；',' ','…'};
	public static List<String> tokenContains(String src)throws Exception{//多种字符的分隔ling
		src = V(src);
		List<String> splitList = new ArrayList<String>();
		int begin = 0 ;
		for (int i = 0; i < src.length(); i++) {
			char s = src.charAt(i);
			for (int j = 0; j < tokens.length; j++) {
				if(s==tokens[j]){
					if(i-begin>0)
						splitList.add(src.substring(begin,i));
					begin = i + 1;
					break;
				}
			}
		}
		if(begin<src.length()) splitList.add(src.substring(begin));
		return splitList;
	}
	
	public static final int intRangeJudge(int standard,int range,int value) throws Exception
	{//浮动范围判断
		Helper.Assert(range>=0);
		if(value<standard-range) return -1;
		else if(value>standard+range) return 1;
		return 0;
	}
	
	public static final List<Integer> arrayIntToList(int[] arr)
	{
		List<Integer> list = new ArrayList<Integer>();
		for(int v:arr) list.add(v);
		return list;
	}
	public static final List<Integer> arrayIntToListReverse(int[] arr)
	{
		List<Integer> list = new ArrayList<Integer>();
		int len = arr.length;
		for(int i=len-1; i>=0; i--) list.add(arr[i]);
		return list;
	}
	public static final List<String> arrayStrToList(String[] arr)
	{
		List<String> list = new ArrayList<String>();
		for(String v:arr) list.add(v);
		return list;
	}
	public static final int[] listIntToArray(List<Integer> list)
	{
		int[] res = new int[list.size()];
		int len = list.size();
		for(int i=0;i<len;i++) res[i] = list.get(i).intValue();
		return res;
	}
	public static final Integer[] listIntToIntArray(List<Integer> list)
	{
		Integer[] res = new Integer[list.size()];
		int len = list.size();
		for(int i=0;i<len;i++) res[i] = list.get(i);
		return res;
	}
	public static final String[] listStrToArray(List<String> list)
	{
		String[] res = new String[list.size()];
		int len = list.size();
		for(int i=0;i<len;i++) res[i] = list.get(i);
		return res;
	}
	
	public static final List<Integer> listStrToListInt(List<String> list,boolean ignoreNotInt) {
		List<Integer> intList = new ArrayList<Integer>();
		for ( String item : list ) {
			try{
				intList.add(Integer.parseInt(item));
			}catch(Exception ex){ if(!ignoreNotInt)intList.add(0); }
		}
		return intList;
	}
	
	public static final <T> T[] listToArray(List<T> list,Class<T> elem_class)
	{
		int len = list.size();
		Object array = Array.newInstance(elem_class,len);
		for (int i=0;i<len;i++) Array.set(array,i,list.get(i));
		return (T[])array;
	}
	
	public static final String strEscapeRegExp(String src)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<src.length();i++)
		{
			char ch = src.charAt(i);
			switch(ch)
			{
				case '$':
				case '^':
				case '{':
				case '}':
				case '(':
				case ')':
				case '[':
				case ']':
				case '-':
				case '?':
				case '+':
				case '*':
				case '\\':
				case '.':
					sb.append('\\').append(ch);
					break;
				default:
					sb.append(ch);
					break;
			}
		}
		return sb.toString();
	}
	
	public static final <V> List<V> listRandPick(List<V> list,int count)
	{//从list中随机取count个出来,有可能返回个个数少于count
		int size = list.size(); 
		if(size>0)
		{
			if(count>size) count = size;
			int offset_range = size-count;
			int rand = 0;
			if(offset_range>0) rand = randSafeInt(offset_range);
			return list.subList(rand,rand+count);
		}
		return new ArrayList<V>();
	}
	public static final <V> List<V> listRandPick1(List<V> list,int count){ //增大随机性,一定返回count个
		int size = list.size();
		List<V> pickList = new ArrayList<V>();
		Set<Integer> alreadyIndex = new HashSet<Integer>();
		if(size>0){
			if(count>size) count = size;
			while(alreadyIndex.size() < count){
				int index = randSafeInt(size);
				if( alreadyIndex.contains(Integer.valueOf(index)) ) continue;
				pickList.add(list.get(index));
				alreadyIndex.add(Integer.valueOf(index));
			}
		}
		return pickList;
	}
	public static <V> V listRandPickOne(List<V> list)
	{//从list中随机取count个出来,有可能返回个个数少于count
		int size = list.size();
		int rand = randSafeInt(size);
		return list.get(rand);
	}

	public static final <V> void arrayRandomResort(V[] arr)
	{//按随机顺序打乱输入的数组，用于不重复随机抽取
		int len = arr.length;
		for(int i=0;i<len;i++)
		{
			int j = randSafeInt(len);
			if(i==j) continue;
			V tmp = arr[i];
			arr[i] = arr[j];
			arr[j] = tmp;
		}
	}
     public static <V> List <V> listRandPickSubList(List<V> list, int count, List<V> subList) {
        List<V> tmpList = new java.util.LinkedList<V>(list);
        try {
            int size = tmpList.size();
            if (size > 0) {
                int rand = randSafeInt(size);
                subList.add(tmpList.get(rand));
                tmpList.remove(rand);
            }
            count --;
            if (count > 0) listRandPickSubList(tmpList, count, subList);
        } catch (Exception e) {
            logger.fatal(e);
        }

        return subList;
    }
     
     public static <T> List<T> listRandPick2(List<T> l, int count){
    	 if(l == null) throw new NullPointerException();
    	 List<T> tmpList = new ArrayList<T>(l);
    	 if(l.size() <= count) return tmpList;
    	 List<T> newList = new ArrayList<T>();
    	 int rand = 0;
    	 for(int i = 0; i < count; i++) {
    		 rand = randSafeInt(tmpList.size());
    		 newList.add(tmpList.remove(rand));
    	 }
    	 return newList;
     }

    public static <V> List<V> listRandPick(List<V> list,int count,int limit)
	{//从list的前limit个中，随机取count个出来,有可能返回个数少于count
		int size = list.size();
		if(size>0)
		{
			if(limit>size) limit=size;
			if(count>limit) count = limit;
			int offset_range = limit-count;
			int rand = 0;
			if(offset_range>0) rand = randSafeInt(offset_range);
			return list.subList(rand,rand+count);
		}
		return new ArrayList<V>();
	}
	
	public static final String getCurrentThreadStackTrace()
	{
		StackTraceElement[] stes = Thread.currentThread().getStackTrace();
		
		StringBuffer sb = new StringBuffer("stack trace begin ===\r\n");
		for(int i=0;i<stes.length;i++)
		{
			StackTraceElement ste = stes[i];
			sb.append("+" + ste.getClassName() + "," + ste.getMethodName() + ":" + ste.getFileName() + "[" + ste.getLineNumber() + "]\r\n"); 
		}
		sb.append("=====================\r\n");
		return sb.toString();
	}

	
	
	public static final String getWelcomeString()
	{
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		String welcome = "欢迎";
		if (hour < 6) welcome = "凌晨好";
		else if (hour < 9)  welcome = "早上好";
		else if (hour < 12) welcome = "上午好";
		else if (hour < 14) welcome = "中午好";
		else if (hour < 17) welcome = "下午好";
		else if (hour < 19) welcome = "傍晚好";
		else welcome = "晚上好";
		return welcome;
	}

	public static final String strReplace(String content,String[] tokens)
	{
		int len = tokens.length; 
		if(len%2==1) Helper.RE("Helper.strReplace : invalid tokens " + tokens);
		for(int i=0;i<len;i+=2)
		{
			String find = tokens[i];
			if(find==null || find.length()==0) continue;
			int head = content.indexOf(find,0);
			if(head<0) continue;
			String replace = Helper.V(tokens[i+1]);
			StringBuffer sb = new StringBuffer();
			sb.append(content,0,head);
			sb.append(replace);
			int findlen = find.length();
			head += findlen;
			while(true)
			{
				int tail=content.indexOf(find,head);
				if(tail<0) break;
				sb.append(content,head,tail);
				sb.append(replace);
				head=tail+findlen;
			}
			sb.append(content,head,content.length());
			content = sb.toString();
		}
		return content;
	}
	
	public static final double strToDouble(String str,double def)
	{
		if(str==null || str.length()==0) return def;
		try{return Double.parseDouble(str);}
		catch(Exception e){return def;}
	}
	public static final float strToFloat(String str,float def)
	{
		if(str==null || str.length()==0) return def;
		try{return Float.parseFloat(str);}
		catch(Exception e){return def;}
	}
	public static final int strToInt(String str,int radix,int def)
	{
		if(str==null || str.length()==0) return def;
		try{return Integer.parseInt(str,radix);}
		catch(Exception e){return def;}
	}
	public static final int strToInt(String str,int def)
	{
		if(str==null || str.length()==0) return def;
		try{return Integer.parseInt(str);}
		catch(Exception e){return def;}
	}
	public static final int strToIntGuess(String str,int invalid_return,int empty_return)
	{//#efef00,00d0ff,07877,0x
		if(str==null || str.length()==0) return empty_return;
		int radix = 10;
		if(str.startsWith("#"))
		{
			radix = 16;
			str = str.substring(1);
			if(str.length()==0) return empty_return;
			if(Helper.strIsProductionOf(str,"0123456789abcdefABCDEF")==false) return invalid_return;
		}
		else if(str.startsWith("0x"))
		{
			radix = 16;
			str = str.substring(2);
			if(str.length()==0) return empty_return;
			if(Helper.strIsProductionOf(str,"0123456789abcdefABCDEF")==false) return invalid_return;
		}
		else
		{
			if(Helper.strIsProductionOf(str,"0123456789abcdefABCDEF")==false) return invalid_return;
			if(Helper.strHasToken(str,"abcdefABCDEF")) radix = 16;
		}
		try
		{
			return Integer.parseInt(str,radix);
		}
		catch(Exception e)
		{
			return invalid_return;
		}
	}
	public static final boolean strHasToken(String str,String toks)
	{
		int len = toks.length();
		for(int i=0;i<len;i++) if(str.indexOf(toks.charAt(i))>=0) return true;
		return false;
	}
	public static final long strToLong(String str,int radix,int def)
	{
		if(str==null || str.length()==0) return def;
		try{return Long.parseLong(str,radix);}
		catch(Exception e){return def;}
	}
	public static final long strToLong(String str,int def)
	{
		if(str==null || str.length()==0) return def;
		try{return Long.parseLong(str);}
		catch(Exception e){return def;}
	}
	public static final boolean strIsInt(String str,boolean empty_str_return)
	{
		if(str==null || str.length()==0) return empty_str_return;
		try{Integer.parseInt(str);}
		catch(Exception e){return false;}
		return true;
	}
	public static final boolean strIsLongInt(String str,boolean empty_str_return)
	{
		if(str==null || str.length()==0) return empty_str_return;
		try{Long.parseLong(str);}
		catch(Exception e){return false;}
		return true;
	}
	
	public static final Integer strToInteger(String str,Integer empty_str_return)
	{
		Integer res = empty_str_return;
		if(str!=null && str.length()>0)
		{
			try{res = new Integer(Integer.parseInt(str));}
			catch(Exception e){return null;}
		}
		return res;
	}
	
	public static final String intToLexicalPad0(int val,int wide)
	{
		String res = String.valueOf(val);
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<wide-res.length();i++)
			sb.append('0');
		sb.append(res);
		return sb.toString();
	}
	
	public static final void fileAssertDirs(String file_path) throws Exception
	{
		int i = file_path.lastIndexOf('/');
		if(i==-1) i = file_path.lastIndexOf('\\'); 
		if(i>=0) file_path = file_path.substring(0,i+1);
		File file = new File(file_path);
		if(file.exists()){if(file.isFile()) Helper.E("is file :" + file_path);}
		else if(file.mkdirs()==false) {Helper.E("fail mkdir:" + file_path);}
	}
	public static final void fileRenameDir(String from_path,String to_path) throws Exception
	{
		File from_file = new File(from_path);
		if(from_file.exists()==false || from_file.isDirectory()==false) return;
		File to_file = new File(to_path);
		if(to_file.exists()) return;
		from_file.renameTo(to_file);
	}
	public static final void fileCreateDir(String dir_path) throws Exception
	{
		File file = new File(dir_path);
		if(file.exists()) return;
		file.mkdirs();
	}
	public static final void fileDeleteTree(String path) throws Exception
	{
		File file = new File(path);
		if(file.exists()==false) return;
		if(file.isFile()) {file.delete();return;}
		//目录
		for(File subfile : file.listFiles())
		{
			if(subfile.isFile()) subfile.delete();
			else fileDeleteTree(subfile);
		}
		file.delete();
	}
	public static final void fileDeleteTree(File file) throws Exception
	{
		if(file.exists()==false) return;
		if(file.isFile()){file.delete();return;}
		//目录
		for(File subfile : file.listFiles())
		{
			if(subfile.isFile()) subfile.delete();
			else fileDeleteTree(subfile);
		}
		file.delete();
	}
	
	//int[0] position int[1]=contained char,return null not contains
	public static final int[] strContains(String src,String toks)
	{
		int len = src.length();
		for(int i=0;i<len;i++)
		{
			char ch = src.charAt(i);
			int ii = toks.indexOf(ch);
			if(ii>=0)
			{
				return new int[]{i,ii};
			}
		}
		return null;
	}
	
	
	public static final boolean checkMobileNumber(String mobile_no)
	{
		if(mobile_no==null || mobile_no.length()!=11 ) return false;
		if((!mobile_no.startsWith("13")) && (!mobile_no.startsWith("15")) && (!mobile_no.startsWith("10"))
                && (!mobile_no.startsWith("18")) && (!mobile_no.startsWith("14"))) return false;
		if(Helper.strIsProductionOf(mobile_no,"0123456789")==false) return false;
		return true;
	}
	


	public static final boolean strContainedInArray(String value,String[] values)
	{
		for(int i=0;i<values.length;i++)
			if(values[i].equals(value)) return true;
		return false;
	}
	
	public static final Properties propsLoad(Class<?> cls,String path) throws Exception
	{
		InputStream is = cls.getResourceAsStream(path);
		if(is==null) Helper.E("can't get props file : " + path);
		Properties props = new Properties();
		props.load(is);
		is.close();
		return props;
	}
	
	//www.mbook.cn:80 -> socketaddr,不合格返回null
	public static SocketAddress str2sockaddr(String str)
	{
		try
		{
			if(str==null) return null;
			int i = str.indexOf(':');
			if(i==-1) return null;
			String host = str.substring(0,i);
			int port = Integer.parseInt(str.substring(i+1));
			return new InetSocketAddress(host,port);
		}
		catch(Exception e)
		{
			return null;
		}
	}
	
	public static String randStringArray(String[] str,int[] factor)
	{
		return str[randIntArray(factor)];
	}
	public static int randIntArray(int[] factor)
	{
		int total = 0;
		int i = 0;
		for(i=0;i<factor.length;i++) total+=factor[i];
		int value = randSafeInt(total);
		int cp = 0;
		for(i=factor.length-1;i>=0;i--)
		{
			cp += factor[i];
			if(value<cp) break;
		}
		return i;
	}
	
	public static <E> void randList(List<E> dest, List<E> src, int count){
        if (dest == null || src == null) return;
        if (src.size() <= count)
            for (int i = 0; i < src.size(); ++i) {
                dest.add(src.get(i));
            }
        else {
            int index = 0;
            for (int i = 0; i < count; ++i) {
                index = randSafeInt(src.size());
                dest.add(src.get(index));
                src.remove(index);
            }
        }
	}
	
	//从一个list中随机抽取N条记录
	public static <V> List <V> getListItems(List<V> list, int num) {		
		List<V> temp_list = list;
		List<V> return_list = new ArrayList<V>();
		java.util.Random random = new java.util.Random();
		for (int i = 0; i < num; i++) {			 
			if (temp_list != null && temp_list.size() > 0) {
				// 在列表中产生一个随机索引
				int arrIndex = random.nextInt(temp_list.size());
				return_list.add(temp_list.get(arrIndex));
				// 然后删掉此索引的列表项
				temp_list.remove(arrIndex);

				if (temp_list.size() == num) {
					return temp_list;
				}
			} else {
				break;
			}
		}
		return return_list;
	}
	
	public static int testHttp(String test_url,int test_timeout)
	{
		try
		{
			java.net.URL url = new java.net.URL(test_url);
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection)url.openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setConnectTimeout(test_timeout*1000);
			return conn.getResponseCode();	
		}
		catch(Exception e){return -1;}
	}
	public static int getHttpURLSize(String get_url,int test_timeout)
	{
		try
		{
			java.net.URL url = new java.net.URL(get_url);
			java.net.HttpURLConnection conn = (java.net.HttpURLConnection)url.openConnection();
			conn.setInstanceFollowRedirects(false);
			conn.setConnectTimeout(test_timeout*1000);
			return conn.getContentLength();
		}
		catch(Exception e){return 0;}
	}

	
	public static String propUpdateLine(String props_str,String key,String value) throws Exception
	{
		BufferedReader br = new BufferedReader(new StringReader(props_str));
		String line;
		StringBuffer sb = new StringBuffer();
		while((line = br.readLine())!=null)
		{
			if(line.startsWith(key + "="))
				sb.append(key + "=" + value + "\r\n");
			else
				sb.append(line + "\r\n");
		}
		return sb.toString();
	}


    public static String propUpdateLine2(String props_str,String key,String value) throws Exception {
		BufferedReader br = new BufferedReader(new StringReader(props_str));
		String line;
		StringBuffer sb = new StringBuffer();
        boolean keyExistFlag = false;
        while((line = br.readLine())!=null) {
			if(line.startsWith(key + "=")) {
                keyExistFlag = true;
                sb.append(key + "=" + value + "\r\n");
            } else sb.append(line + "\r\n");
		}
        if (!keyExistFlag) sb.append(key + "=" + value + "\r\n");
        return sb.toString();
	}
    public static Properties propsFromString(String str) throws Exception
	{
		Properties props = new Properties();
		InputStream is = new ByteArrayInputStream(str.getBytes("ISO-8859-1"));
		props.load(is);
		return props;
	}
	public static String propsToString(Properties props) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		props.store(baos,"");
		return baos.toString("ISO-8859-1");
	}
	public static List<String> propsKeysSort(Properties props) throws Exception
	{
		List<String> list = new ArrayList<String>();
		Enumeration<?> e = props.keys();
		while(e.hasMoreElements()) list.add((String)e.nextElement());
		Collections.sort(list);
		return list;
	}
	public static List<String> propsKeysSortReverse(Properties props) throws Exception
	{
		List<String> list = new ArrayList<String>();
		Enumeration<?> e = props.keys();
		while(e.hasMoreElements()) list.add((String)e.nextElement());
		Collections.sort(list);
		Collections.reverse(list);
		return list;
	}

	public static String[] propsGetAssertStringArray(Properties props,String name) throws Exception
	{
		String value = props.getProperty(name);
		Helper.AssertNotEmpty(value);
		return value.split(",");
	}
	public static int[] propsGetAssertIntArray(Properties props,String name) throws Exception
	{
		String value = props.getProperty(name);
		Helper.AssertNotEmpty(value);
		String[] toks = value.split(",");
		int[] res = new int[toks.length];
		for(int i=0;i<res.length;i++)
			res[i] = Integer.parseInt(toks[i]);
		return res;
	}
	
	
	//////////////////////////////////////////////////////////////////////////////////////////
	public static final Calendar datetimeStrParse(String datetime_str)
	{//如果是无效date_str,返回null
		int[] res = new int[6];
		StringTokenizer stk = new StringTokenizer(datetime_str,"- :.");
		
		if(stk.hasMoreTokens()==false) return null;
		res[0] = Helper.strToInt(stk.nextToken(),0);
		if(res[0]<1) return null;
		
		if(stk.hasMoreTokens()==false) return null;
		res[1] = Helper.strToInt(stk.nextToken(),0);
		if(res[1]<1 || res[1]>12) return null;
		
		if(stk.hasMoreTokens()==false) return null;
		res[2] = Helper.strToInt(stk.nextToken(),0);
		if(res[2]<1 || res[2]>31) return null;
		
		if(stk.hasMoreTokens()==false) return null;
		res[3] = Helper.strToInt(stk.nextToken(),-1);
		if(res[3]<0 || res[3]>23) return null;
		
		if(stk.hasMoreTokens()==false) return null;
		res[4] = Helper.strToInt(stk.nextToken(),-1);
		if(res[4]<0 || res[4]>59) return null;
		
		if(stk.hasMoreTokens()==false) return null;
		res[5] = Helper.strToInt(stk.nextToken(),-1);
		if(res[5]<0 || res[5]>59) return null;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(res[0],res[1]-1,res[2],res[3],res[4],res[5]);
		return calendar;		
	}
	public static final long datetimeStrSub(String datetime_str1,String datetime_str2) throws Exception
	{//
		Calendar datetime1 = datetimeStrParse(datetime_str1);
		Calendar datetime2 = datetimeStrParse(datetime_str2);
		if(datetime1==null || datetime2==null) Helper.E("invald date format : " + datetime_str1 + "," + datetime_str2);
		long diff = datetime1.getTimeInMillis() - datetime2.getTimeInMillis();
		return diff/1000;
	}
	public static final Calendar dateStrParse(String date_str)
	{//如果是无效date_str,返回null
		int[] res = new int[3];
		StringTokenizer stk = new StringTokenizer(date_str,"-");
		
		if(stk.hasMoreTokens()==false) return null;
		res[0] = Helper.strToInt(stk.nextToken(),0);
		if(res[0]<1) return null;
		
		if(stk.hasMoreTokens()==false) return null;
		res[1] = Helper.strToInt(stk.nextToken(),0);
		if(res[1]<1 || res[1]>12) return null;
		
		if(stk.hasMoreTokens()==false) return null;
		res[2] = Helper.strToInt(stk.nextToken(),0);
		if(res[2]<1 || res[2]>31) return null;
		
		Calendar calendar = Calendar.getInstance();
		calendar.set(res[0],res[1]-1,res[2],0,0,0);
		return calendar;
	}
	public static final String dateStrFromCalendar(Calendar calendar)
	{
		StringBuffer sb = new StringBuffer();
		sb.append(calendar.get(Calendar.YEAR));
		sb.append('-');
		int month = calendar.get(Calendar.MONTH) + 1;
		if(month<10) sb.append('0');
		sb.append(month);
		sb.append('-');
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		if(day<10) sb.append('0');
		sb.append(day);
		return sb.toString();
	}
	public static final String dateTimeStrFromCalendar(Calendar calendar)
	{
		StringBuffer sb = new StringBuffer(Helper.dateStrFromCalendar(calendar));
		sb.append(' ');
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		if(hour < 10)sb.append('0');
		sb.append(hour);
		sb.append(':');
		int minute = calendar.get(Calendar.MINUTE);
		if(minute < 10)sb.append('0');
		sb.append(minute);
		sb.append(':');
		int second = calendar.get(Calendar.SECOND);
		if(second < 10)sb.append('0');
		sb.append(second);
		return sb.toString();
	}
	public static final boolean dateStrEquals(String date_str1,String date_str2)
	{
		Calendar date1 = dateStrParse(date_str1);
		Calendar date2 = dateStrParse(date_str2);
		return date1.equals(date2);
	}
	public static final boolean dateStrLessThan(String date_str1,String date_str2)
	{
		Calendar date1 = dateStrParse(date_str1);
		Calendar date2 = dateStrParse(date_str2);
		return date1.before(date2);
	}
	public static final boolean dateStrGreaterThan(String date_str1,String date_str2)
	{
		Calendar date1 = dateStrParse(date_str1);
		Calendar date2 = dateStrParse(date_str2);
		return date1.after(date2);
	}
	
	public static final String dateToStr(Date date)
	{//将date类型转换为"2007-06-16"
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH)+1;
		int day = cal.get(Calendar.DAY_OF_MONTH);
		return String.valueOf(year) + "-" + (month<10 ? "0" : "") + String.valueOf(month) + "-" + (day<10 ? "0" : "") + String.valueOf(day);
	}
	public static final String dateStrAddDay(String date_str,int diff) throws Exception
	{
		Calendar calendar = dateStrParse(date_str);
		if(calendar==null) Helper.E("invalid date_str : " + date_str);
		calendar.add(Calendar.DATE,diff);
		return dateStrFromCalendar(calendar);
	}

    public static String dateStrConvert3(String datestr)
	{//如果是今日，只显示时间，如果不是今日，显示日期
    	if(datestr==null || datestr.isEmpty()) return "";
    	if ( datestr.length()==21 ) datestr = datestr.substring(0,datestr.length()-2);
    	
		StringBuffer res1 = new StringBuffer();
		StringBuffer res2 = new StringBuffer();
		StringTokenizer tk = new StringTokenizer(datestr,"-: .");
		if(tk.hasMoreTokens()==false) return "";
		tk.nextToken();//年
		if(tk.hasMoreTokens()==false) return "";
		res1.append(tk.nextToken());//月
		if(tk.hasMoreTokens()==false) return "";
		res1.append("-").append(tk.nextToken());//日
		
		if(tk.hasMoreTokens()==false) return "";
		res2.append(tk.nextToken());//时
		if(tk.hasMoreTokens()==false) return "";
		res2.append(":").append(tk.nextToken());//分
		
		if(res1.toString().equals(Helper.getTodayStrCompat()))
			return res2.toString();
		return res1.toString();
	}
    /**
     * @deprecated replaced by dateStrConvert6();
     */
    public static String dateStrConvert5(String datestr)
	{//如果是今日，只显示时间，如果不是今日，显示日期
    	if(datestr==null || datestr.isEmpty()) return "";
    	if ( datestr.length()==21 ) datestr = datestr.substring(0,datestr.length()-2);
		StringBuffer res1 = new StringBuffer();
		StringBuffer res2 = new StringBuffer();
		StringTokenizer tk = new StringTokenizer(datestr,"-: .");
		if(tk.hasMoreTokens()==false) return "";
        String year = tk.nextToken();
        if (!year.equals(getThisYearStr())) res1.append(year.substring(2,4)).append("-");//年
        if(tk.hasMoreTokens()==false) return "";
		res1.append(tk.nextToken());//月
		if(tk.hasMoreTokens()==false) return "";
		res1.append("-").append(tk.nextToken());//日

		if(tk.hasMoreTokens()==false) return "";
		res2.append(tk.nextToken());//时
		if(tk.hasMoreTokens()==false) return "";
		res2.append(":").append(tk.nextToken());//分

		if(res1.toString().equals(Helper.getTodayStrCompat()))
			return res2.toString();
		return res1.toString();
	}
    
    //如果是今日，只显示时间，如果不是今日，显示日期
    //日期格式必须为"yyyy-MM-dd HH:mm:ss.S"
    public static String dateStrConvert6 (String dateStr) throws Exception {
    	Calendar calNow = Calendar.getInstance();
    	Calendar cal = Calendar.getInstance();
    	cal.setTime(sdf.parse(dateStr));
    	if (cal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH) && cal.get(Calendar.DATE) == calNow.get(Calendar.DATE))
    		return dateStr.substring(11, 16);
    	else
    		return dateStr.substring(0, 10);
    }
    
    //当天显示时间,当年显示x月x日,不同年显示x年x月x日
    public static String dateStrConvert7 (String dateStr) throws Exception {
    	Calendar calNow = Calendar.getInstance();
    	Calendar cal = Calendar.getInstance();
    	if (dateStr.indexOf(".") <= 0) {
    		dateStr = new StringBuilder(dateStr).append(".0").toString();
    	}
    	cal.setTime(sdf.parse(dateStr));
    	if (cal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH) && cal.get(Calendar.DATE) == calNow.get(Calendar.DATE))
    		return dateStr.substring(11, 16);
    	else {
    		if (cal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)) {
    			return sdf5.format(cal.getTime());
    		} else {
    			return sdf3.format(cal.getTime());
    		}
    	}
    }
    
    public static String dateStrConvert7Ext (String dateStr) throws Exception {
    	Calendar calNow = Calendar.getInstance();
    	Calendar cal = Calendar.getInstance();
    	if (dateStr.indexOf(".") <= 0) {
    		dateStr = new StringBuilder(dateStr).append(".0").toString();
    	}
    	cal.setTime(sdf.parse(dateStr));
    	if (cal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == calNow.get(Calendar.MONTH) && cal.get(Calendar.DATE) == calNow.get(Calendar.DATE))
    		return dateStr.substring(11, 16);
    	else {
    		if (cal.get(Calendar.YEAR) == calNow.get(Calendar.YEAR)) {
    			return sdf6.format(cal.getTime());
    		} else {
    			return sdf2.format(cal.getTime());
    		}
    	}
    }

    /**
     * @deprecated	-- use datestr.substring(5, 16) instead
     */
    public static String dateStrConvert(String datestr)
	{//'12-20 22:22'
    	if(datestr==null || datestr.isEmpty()) return "";
    	if ( datestr.length()==21 ) datestr = datestr.substring(0,datestr.length()-2);
    	
		StringBuffer res = new StringBuffer();
		StringTokenizer tk = new StringTokenizer(datestr,"-: .");
		if(tk.hasMoreTokens()==false) return res.toString();
		tk.nextToken();
		if(tk.hasMoreTokens()==false) return res.toString();
		res.append(tk.nextToken());
		if(tk.hasMoreTokens()==false) return res.toString();
		res.append("-").append(tk.nextToken());
		if(tk.hasMoreTokens()==false) return res.toString();
		res.append(" ").append(tk.nextToken());
		if(tk.hasMoreTokens()==false) return res.toString();
		res.append(":").append(tk.nextToken());
		return res.toString();
	}
	public static String dateStrConvert2(String dtstr)
	{//返回'2006-10-10'
		if(dtstr==null || dtstr.isEmpty()) return "";
    	if ( dtstr.length()==21 ) dtstr = dtstr.substring(0,dtstr.length()-2);
    	
		StringBuffer res = new StringBuffer();
		StringTokenizer tk = new StringTokenizer(dtstr,"-: .");
		if(tk.hasMoreTokens()==false) return "";
		res.append(tk.nextToken());//年
		if(tk.hasMoreTokens()==false) return "";
		res.append("-").append(tk.nextToken());//月
		if(tk.hasMoreTokens()==false) return "";
		res.append("-").append(tk.nextToken());//日
		return res.toString();
	}

	public static String dateStrConvert4(String dtstr)
	{
		if(dtstr==null || dtstr.isEmpty()) return "";
    	if ( dtstr.length()==21 ) dtstr = dtstr.substring(0,dtstr.length()-2);
    	
		StringBuffer res = new StringBuffer();
		StringTokenizer tk = new StringTokenizer(dtstr,"-: .");
		if(tk.hasMoreTokens()==false) return "";
		res.append(tk.nextToken().substring(2,4));//年
		if(tk.hasMoreTokens()==false) return "";
		res.append(".").append(tk.nextToken());//月
		if(tk.hasMoreTokens()==false) return "";
		res.append(".").append(tk.nextToken());//日
		return res.toString();
	}
	
	public final static short DATETIME_PRECISION_SECOND = 3;//秒
	public final static short DATETIME_PRECISION_MINUTE = 4;//分
	public final static short DATETIME_PRECISION_HOUR = 5;//时
	public final static short DATETIME_PRECISION_DAY = 6;//天
	public final static short DATETIME_PRECISION_MONTH = 7;//月
	
	public static String dateStrConvert6(String dbStr, short precision) {
		StringBuffer res = new StringBuffer();
		StringTokenizer tk = new StringTokenizer(dbStr,"-: .");

		if(tk.hasMoreTokens()==false)
			return "";
		res.append(tk.nextToken().substring(0,4));//年
		if(precision <= DATETIME_PRECISION_MONTH) {
			if(tk.hasMoreTokens()==false) return "";
			res.append("-").append(tk.nextToken());//月
		}
		if(precision <= DATETIME_PRECISION_DAY) {
			if(tk.hasMoreTokens()==false) return "";
			res.append("-").append(tk.nextToken());//日			
		}
		if(precision <= DATETIME_PRECISION_HOUR) {
			if(tk.hasMoreTokens()==false) return "";
			res.append(" ").append(tk.nextToken());//时
		}
		if(precision <= DATETIME_PRECISION_MINUTE) {
			if(tk.hasMoreTokens()==false) return "";
			res.append(":").append(tk.nextToken());//分
		}
		if(precision <= DATETIME_PRECISION_SECOND) {
			if(tk.hasMoreTokens()==false) return "";
			res.append(":").append(tk.nextToken());//秒
		}
		return res.toString();
	}
	
	
	public static int intArraySum(int[] arr)
	{
		int total = 0;
		for(int i=0;i<arr.length;i++) total+=arr[i];
		return total;
	}
	public static long longArraySum(long[] arr)
	{
		long total = 0;
		for(int i=0;i<arr.length;i++) total+=arr[i];
		return total;
	}
	
	
	public static String getHostIP()
	{
		try
		{
			return InetAddress.getLocalHost().getHostAddress();
		}
		catch(Exception e)
		{
			return "";
		}
	}
	
	public static void addHashMapCount(HashMap<String,Integer> map,String name)
	{
		Integer value = map.get(name);
		if(value==null) value=new Integer(0);
		value = new Integer(value.intValue() + 1);
		map.put(name,value);
	}
	public static void addHashMapValue(HashMap<String,Integer> map,String name,int v)
	{
		Integer value = map.get(name);
		if(value==null) value=new Integer(0);
		value = new Integer(value.intValue() + v);
		map.put(name,value);
	}
	
	public static void addHashCount(Hashtable<String,Integer> ht,String name)
	{
		Integer value = ht.get(name);
		if(value==null) value=new Integer(0);
		value = new Integer(value.intValue() + 1);
		ht.put(name,value);
	}
	public static void addHashValue(Hashtable<String,Integer> ht,String name,int v)
	{
		Integer value = ht.get(name);
		if(value==null) value=new Integer(0);
		value = new Integer(value.intValue() + v);
		ht.put(name,value);
	}
	
	public static String getTodayStr()
	{
		Date d = new Date();
		return sdf2.format(d);
	}
	
	//返回 yyyy-mm-dd 格式
	public static String getDataTimeStr(String data_time)
	{
		String date = null;
           try {
			date = sdf2.format(sdf2.parse(data_time)).toString();
			
		} catch (ParseException e) {
			e.printStackTrace();
		}	
		return date;
	}

    public static String getThisYearStr()
	{
		Date d = new Date();
		DateFormat fmt = new SimpleDateFormat("yyyy");
		return fmt.format(d);
	}

    public static String getTodayStrCompat()
	{
		Date d = new Date();
		DateFormat fmt = new SimpleDateFormat("MM-dd");
		return fmt.format(d);
	}
	public static String getNowStr()
	{
		Date d = new Date();
		return sdf1.format(d);
	}
	public static String getNowStrCompat()
	{
		Date d = new Date();
		DateFormat fmt = new SimpleDateFormat("MM-dd HH:mm"); 
		return fmt.format(d);
	}
	
	//得到h m s total
	public static int gethmstotal()
	{
		Date d = new Date();
		DateFormat fmt = new SimpleDateFormat("HH:mm:ss"); 

		String time = fmt.format(d);

		int h = Integer.parseInt(time.substring(0, 2));
		int m = Integer.parseInt(time.substring(3, 5));
		int s = Integer.parseInt(time.substring(6, 8));
		int total = h * 3600 + m * 60 + s;
		return total;
	}
	
	public static String getStrForDate(Date date)
	{
		return sdf1.format(date);
	}
	public static String getNowCompat()
	{//返回(月月-日日 ([星期]几) 时:分)
		Date d = new Date();
		DateFormat fmt = new SimpleDateFormat("MM-dd(E)HH:mm", Locale.SIMPLIFIED_CHINESE); // 并不是所有系统都是中文的
		String temp = fmt.format(d);
		return temp.substring(0, temp.indexOf("(")+1)+temp.substring(temp.indexOf(")")-1);
	}
	public static String getNowStrCompat(String date)
	{
		try
		{
			DateFormat df = DateFormat.getDateTimeInstance();
			Date d = df.parse(date);
			java.text.DateFormat fmt = new java.text.SimpleDateFormat("MM-dd HH:mm");
			return fmt.format(d);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
	public static String getDateStrCompat(String date,String format)
	{
		try
		{
			DateFormat df = DateFormat.getDateTimeInstance();
			Date d = df.parse(date);
			java.text.DateFormat fmt = new java.text.SimpleDateFormat(format);
			return fmt.format(d);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return "";
		}
	}
	
	public static String[] ObjectArrayToStringArray(Object[] array)
	{
		int len = array.length;
		String[] res = new String[len];
		for(int i=0;i<len;i++) res[i]=(String)(array[i]);
		return res;
	}
	
	public static String  getStringSafe(String value){return V(value);}
	public static String  getStringAssert(String value) throws Exception {if(value==null||value.length()==0) Helper.E("getStringAssert Failed : " + value);return value;}
	public static String  getStringAssert(String name,String value) throws Exception {if(value==null||value.length()==0) Helper.E("getStringAssert Failed : " + name + "=" + value);return value;}
	public static String  getStringDefault(String value,String def){if(value==null||value.length()==0) return def;return value;}
	public static int     getIntSafe(String value){if(value==null) return 0;try{return Integer.parseInt(value);}catch(Exception e){return 0;}}
	public static int     getIntAssert(String value) throws Exception {if(value==null) Helper.E("getIntAssert Failed : " + value);try{return Integer.parseInt(value);}catch(Exception e){Helper.E("getIntAssert Failed : " + value);return 0;}}
	public static int     getIntAssert(String name,String value) throws Exception {if(value==null) Helper.E("getIntAssert Failed : " + name + "=" + value);try{return Integer.parseInt(value);}catch(Exception e){Helper.E(" parse getIntAssert Failed : " + name + "=" + value);return 0;}}
	public static int     getIntDefault(String value,int def){if(value==null) return def;try{return Integer.parseInt(value);}catch(Exception e){return def;}}
	public static boolean getBooleanSafe(String value){if(value==null||value.length()==0) return false;return Boolean.parseBoolean(value);}
	public static boolean getBooleanAssert(String value) throws Exception {if(value==null||(!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))) Helper.E("getBooleanAssert Failed : " + value);return Boolean.parseBoolean(value);}
	public static boolean getBooleanAssert(String name,String value) throws Exception {if(value==null||(!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))) Helper.E("getBooleanAssert Failed : " + name + "=" + value);return Boolean.parseBoolean(value);}
	public static boolean getBooleanDefault(String value,boolean def){if(value==null||(!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false"))) return def;return Boolean.parseBoolean(value);}
	
	public static int getIntProperty(Properties props,String id,int def)
	{
		String value = props.getProperty(id);
		if(Helper.strIsEmpty(value)) return def;
		try
		{
			return Integer.parseInt(value);
		}
		catch(Exception e)
		{
			return def;
		}
	}
	public static boolean getBooleanProperty(Properties props,String name,boolean def)
	{
		String value = props.getProperty(name,"" + def);
		if(value.equalsIgnoreCase("true")) return true;
		else if(value.equalsIgnoreCase("false")) return false;
		return def;
	}
	
	
	//从class_path按encoding按读取properties文件，分析并构建返回properties
	public static Properties propGetByClassPath(Class<?> loader,String class_path,String encoding) throws Exception
	{
		Properties props = new Properties();
		InputStream ins = loader.getResourceAsStream(class_path);
		Helper.AssertNotNull(ins,"Can't get class resource file : " + class_path);
		String temp = CommCodec.UnicodeEscapeEncode(streamRead(ins,encoding));
		ByteArrayInputStream bais = new ByteArrayInputStream(temp.getBytes("ISO-8859-1")); 
		props.load(bais);
		return props;
	}
	
	//将inputstream读取成为string返回
	public static String streamRead(InputStream is,String encoding) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		Reader rd = new InputStreamReader(is,encoding);
		char[] buf=new char[1024];
		while(true)
		{
			int size = rd.read(buf);
			if(size==-1) break;
			sb.append(buf,0,size);
		}
		return sb.toString();
	}
	//将inputstream读取成为byte[]返回
	public static byte[] streamRead(InputStream is,int buffer_size) throws Exception
	{
		byte[] bts = new byte[buffer_size];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int len = 0;
		while((len=is.read(bts))!=-1) baos.write(bts,0,len);
		return baos.toByteArray();
	}
	public static byte[] streamRead(InputStream is) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int bt = 0;
		while((bt=is.read())!=-1) baos.write(bt);
		baos.flush();
		return baos.toByteArray();
	}
	public static byte[] streamReadBuffered(InputStream is,int buffer_size) throws Exception
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf=new byte[buffer_size];
		while(true)
		{
			int size = is.read(buf);
			if(size==-1) break;
			baos.write(buf,0,size);
		}
		baos.flush();
		return baos.toByteArray();
	}
	
	//读取inputstream,写到outputstream
	public static int streamTransfer(InputStream is,OutputStream os) throws Exception
	{
		AssertNotNull(is);
		AssertNotNull(os);
		int len = 0;
		byte[] buf=new byte[1024];
		while(true)
		{
			int size = is.read(buf);
			if(size==-1) break;
			os.write(buf,0,size);
			len += size;
		}
		os.flush();
		return len;
	}
	public static final int streamTransferBuffered(InputStream is,OutputStream os,int buffer_size) throws Exception
	{
		int len = 0;
		byte[] buf=new byte[buffer_size];
		while(true)
		{
			int size = is.read(buf);
			if(size==-1) break;
			os.write(buf,0,size);
			len += size;
		}
		os.flush();
		return len;
	}
	public static final int streamCount(InputStream is) throws Exception
	{
		int res = 0;
		int len = 0;
		while((len=is.available())>0)
		{
			res += len;
			is.skip(len);
		}
		return res;
	}
	
	
	public static long last_seed = 0;
	public static Random rand = new Random();
	public synchronized static String genRandoomString(String toks,int len)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<len;i++) sb.append(toks.charAt(randSafeInt3(toks.length())));
		return sb.toString();
	}
	
	public synchronized static String genRandoomStringSafe(String toks,int len) {
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<len;i++) sb.append(toks.charAt(randSafeInt3(toks.length())));
		return sb.toString();
	}
	
	public static int randSafeInt3(int range)
	{
		Random rd = new Random();
		rd.setSeed(System.currentTimeMillis() + rd.toString().hashCode());
		return rd.nextInt(range);
	}
	
	
	public static final String randHourTime(int hour)
	{
		int min = Helper.randSafeInt(60);
		int sec = Helper.randSafeInt(60);
		return (hour<10 ? "0" + String.valueOf(hour) : String.valueOf(hour)) + ":" + (min<10 ? "0" + String.valueOf(min) : String.valueOf(min)) + ":" + (sec<10 ? "0" + String.valueOf(sec) : String.valueOf(sec));
	}
	public static final String randChinaMobileNo()
	{//随机生成mobile_no
		String[] starts = new String[]{"139","138","137","136","135","134","159","158"};
		int[] start_factors = new int[]{20,20,18,15,16,14,8,9};
		String start = Helper.randStringArray(starts,start_factors);
		return start + Helper.genRandoomString("0123456789",8);
	}
	public static final int[] intArrayMultiple(int[] v,float f)
	{
		int[] res = new int[v.length];
		for(int i=0;i<v.length;i++) res[i] = (int)(v[i]*f);
		return res;
	}
	public static final int[] intArrayMinus(int[] v1,int[] v2)
	{
		if(v1.length!=v2.length) Helper.RE("RE");
		int[] res = new int[v1.length];
		for(int i=0;i<v1.length;i++) res[i] = v1[i]-v2[i];
		return res;
	}
	public static final int[] intFactorWave(int wave,int[] factors)
	{//随机将factor按wave振幅振动
		int[] res = new int[factors.length];
		for(int i=0;i<factors.length;i++) res[i] = factors[i] + (Helper.randSafeInt(wave)-wave);
		return res;
	}
	public static final int[] intFactorBalance(int total,int[] factors)
	{
		int[] res = new int[factors.length];
		int total_factors = Helper.intArraySum(factors);
		for(int i=0;i<factors.length;i++)
		{
			if(i==factors.length-1) res[i] = total - Helper.intArraySum(res);
			else res[i] = (int)((((long)factors[i])*total)/total_factors);
		}
		return res;
	}
	
	public static final int randMultiple(float factor)
	{
		//randMutiple(0) 永远都返回0
		//randMutiple(0.5) 有一半机会返回0，有一半机会返回1
		//randMutiple(1) 永远都返回1
		//randMutiple(1.2) 有0.8一半机会返回1，有0.2机会返回2
		int i = (int)factor;
		int j = (int)((factor-i)*1000);
		if(Helper.randSafeInt(1000)<j) i++;
		return i;
	}
	public static int randSafeInt(int range)
	{
		if(range<=0) return 0;
		long seed = System.currentTimeMillis();
		if(seed!=last_seed){last_seed=seed;rand.setSeed(seed);}
		return rand.nextInt(range);
	}
	
	public static int randSafeInt() {
		rand.setSeed(System.currentTimeMillis());
		return rand.nextInt();
	}
	
	public static int randSafeInt(int min,int max)
	{
		return min + randSafeInt(max-min);
	}
	
	public static int randSafeInt2(int range)
	{
		if(range<=0) return 0;
		long seed = System.currentTimeMillis();
		Random rd = new Random(seed);
		return rd.nextInt(range);
	}
	
	
	public static final int LINUX_PLATFORM = 1;
	public static final int WINDOWS_PLATFORM = 2;
	public static int getRuntimePlatform()
	{
		String os = System.getProperty("os.name");
		if(os!=null && os.toLowerCase().indexOf("windows")>=0) 
			return WINDOWS_PLATFORM;
		else 
			return LINUX_PLATFORM;
	}
	public static String getRuntimePlatform2()
	{
		String os = System.getProperty("os.name");
		if(os!=null && os.toLowerCase().indexOf("windows")>=0)
			return "windows";
		else 
			return "linux";
	}
	

	//如果value不是int格式，返回min,如果小于min,返回min,大于max，返回max,如果min>max抛出异常
	public static int roundToNearestInt(String value,int min,int max) throws Exception
	{
		if(min>max) Helper.E("min > max int roundToNearestInt");
		if(value==null) return min; 
		int res = min;
		try{res = Integer.parseInt(value);}catch(Exception e){}
		if(res<min) res = min;
		if(res>max) res = max;
		return res;
	}
	public static int roundToNearestInt(int value,int min,int max) throws Exception
	{
		if(min>max) Helper.E("min > max int roundToNearestInt");
		int res = value;
		if(res<min) res = min;
		if(res>max) res = max;
		return res;
	}
	public static int roundToInt(String value,int def)
	{
		if(strIsEmpty(value)) return def;
		try{return Integer.parseInt(value);}
		catch(Exception e){return def;}
	}
	
	
	public static void AssertNotEmpty(String str,String msg) throws Exception
	{
		if(str==null || str.length()==0) E(msg);
	}
	public static void AssertNotEmpty(String str) throws Exception
	{
		if(str==null || str.length()==0) E("assert not empty failed :" + str);
	}
	public static void Assert(boolean condition,String msg) throws Exception
	{
		if(condition==false) E(msg);
	}
	public static void Assert(boolean condition) throws Exception
	{
		if(condition==false) E("assert failed");
	}
	public static void AssertNotNull(Object obj,String msg) throws Exception
	{
		if(obj==null) E(msg);
	}
	public static void AssertNotNull(Object obj) throws Exception
	{
		if(obj==null) E("Assert not null failed");
	}
	
	public static void Check(boolean condition,String msg)
	{
		if(condition==false) logger.info("CheckFailed: " + msg);
	}
	public static void CheckNull(Object obj,String msg)
	{
		if(obj!=null) logger.info("CheckFailed: " + msg);
	}
	public static void CheckNotNull(Object obj,String msg)
	{
		if(obj==null) logger.info("CheckFailed: " + msg);
	}
	
	
	public static Logger logger = Logger.getLogger(Helper.class.getName());
	
	public static Object classLoad(String class_name)
	{
		try
		{
			if(class_name==null) return null;
			return Class.forName(class_name).newInstance();
		}
		catch(NoClassDefFoundError e)
		{
			logger.info("NoClassDefFoundError : " + class_name + " : ",e);
			return null;
		}
		catch(Exception e)
		{
			logger.info("can't find class : " + class_name + " : ",e);
			return null;
		}
	}
	
	
	public static String strRightTrim(String str,String toks)
	{
		if(str==null) return null;
		while(true)
		{
			if(str.length()==0) break;
			if(toks.indexOf(str.charAt(str.length()-1))==-1) break;
			str = str.substring(0,str.length()-1);
		}
		return str;
	}
	public static String strLeftTrim(String str,String toks)
	{
		if(str==null) return null;
		while(true)
		{
			if(str.length()==0) break;
			if(toks.indexOf(str.charAt(0))==-1) break;
			str = str.substring(1);
		}
		return str;
	}
	//去除str两边含有toks的字符 strTrim( 'dsdfsdf"fsd?fd"" , "') ==>  dsdfsdf"fsd?fd 
	public static String strTrim(String str,String toks)
	{
		str = strLeftTrim(str,toks);
		str = strRightTrim(str,toks);
		return str;
	}
	
	public static final String WIDE_CHARS = "＜＞？，．／；＇：＂［］｛｝｀１２３４５６７８９０－＝＼～！＠＃￥％＾＆＊（）＿＋｜　ａｂｃｄｅｆｇｈｉｊｋｌｍｎｏｐｑｒｓｔｕｖｗｘｙｚＡＢＣＤＥＦＧＨＩＪＫＬＭＮＯＰＱＲＳＴＵＶＷＸＹＺ";
	public static final String NARR_CHARS = "<>?,./;':\"[]{}`1234567890-=\\~!@#$%^&*()_+| abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
	
	public static final String WIDE_SYMBS = "＜＞？，．／；＇：＂［］｛｝｀－＝＼～！＠＃￥％＾＆＊（）＿＋｜　";
	public static final String NARR_SYMBS = "<>?,./;':\"[]{}`-=\\~!@#$%^&*()_+| ";
	
	public static final String strToWideSymbols(String src)
	{
		StringBuffer sb = new StringBuffer();
		int len = src.length();
		for(int i=0;i<len;i++)
		{
			char ch = src.charAt(i);
			int index = NARR_SYMBS.indexOf(ch);
			if(index>=0) sb.append(WIDE_SYMBS.charAt(index));
			else sb.append(ch);
		}
		return sb.toString();
	}
	public static final String strToNarrowSymbols(String src)
	{
		StringBuffer sb = new StringBuffer();
		int len = src.length();
		for(int i=0;i<len;i++)
		{
			char ch = src.charAt(i);
			int index = WIDE_SYMBS.indexOf(ch);
			if(index>=0) sb.append(NARR_SYMBS.charAt(index));
			else sb.append(ch);
		}
		return sb.toString();
	}
	
	public static final String strToWideChar(String src)
	{
		StringBuffer sb = new StringBuffer();
		int len = src.length();
		for(int i=0;i<len;i++)
		{
			char ch = src.charAt(i);
			int index = NARR_CHARS.indexOf(ch);
			if(index>=0) sb.append(WIDE_CHARS.charAt(index));
			else sb.append(ch);
		}
		return sb.toString();
	}
	public static final String strToNarrowChar(String src)
	{
		StringBuffer sb = new StringBuffer();
		int len = src.length();
		for(int i=0;i<len;i++)
		{
			char ch = src.charAt(i);
			int index = WIDE_CHARS.indexOf(ch);
			if(index>=0) sb.append(NARR_CHARS.charAt(index));
			else sb.append(ch);
		}
		return sb.toString();
	}
	public static final String strTrimByRegExp(String src,String[] regexps) throws Exception
	{
		String res = src;
		int len = regexps.length;
		for(int i=0;i<len;i++) src.replaceAll(regexps[i],"");
		return res;
	}
	
	public static boolean strSafeEquals(String str,String str2,boolean case_sensitive)
	{
		if(str==null) return false;
		if(str2==null) return false;
		if(case_sensitive)
			return str.equals(str2);
		else 
			return str.equalsIgnoreCase(str2);
	}
	public static String strLastToken(String str,String token,boolean token_include)
	{
		int i = str.lastIndexOf(token);
		if(i==-1) return str;
		
		if(token_include)
			return str.substring(i);
		else
			return str.substring(i+token.length());
	}
	public static boolean strIsDateStr(String str)
	{
    	try
    	{
    		sdf2.parse(str);
    		return true;
    	}catch(Exception e){return false;}	
	}
	public static boolean strIsDateStr(String str,boolean empty_str_return)
	{
		if(str==null || str.length()==0) return empty_str_return;
    	try
    	{
    		sdf2.parse(str);
    		return true;
    	}catch(Exception e){return false;}	
	}
	public static Date strGetDateTime(String str) throws Exception
	{
		return sdf1.parse(str);
	}
	public static Date strGetDate(String str) throws Exception
	{
    	return sdf2.parse(str);
	}
	
	
	//
	public static String floatWelformat(float f)
	{
		String s = Float.toString(f);
		if(s.endsWith(".0")) s = s.substring(0,s.length()-2);
		return s;
	}
	
	public static String strDeleteChars(String src,String chars)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<src.length();i++)
		{
			char c = src.charAt(i);
			if(chars.indexOf(c)>=0) continue;
			sb.append(c);
		}
		return sb.toString();
	}
	
	//返回重复的elem的n次
	public static String strRepeat(String elem,int times,String delm)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<times;i++)
		{
			if(i>0) sb.append(delm);
			sb.append(elem);
		}
		return sb.toString();
	}
	
	//返回字符串的左边n个字符，如果字符串没有n个的长度，直接返回该字符串
	public static String strLeft(String str,int length) throws Exception
	{
		AssertNotNull(str);
		if(str.length()>length) str = str.substring(0,length) + "...";
		return str;
	}
	public static String strLeftBlank(String str,int length) throws Exception{
		AssertNotNull(str);
		if(str.length()>length) str = str.substring(0,length) ;
		return str;
	}
	//返回字符串左边n长度的字符，英文为1，中文为2
	public static String strLeftCW(String str,int len) throws Exception
	{
		StringBuffer sb = new StringBuffer();
		int acc = 0;
		for(int i=0;i<str.length();i++)
		{
			char c = str.charAt(i);
			if(c>255) acc+=2;
			else acc++;
			if(acc>len) break;
			sb.append(c);
		}
		return sb.toString();
	}
	
	
	public static boolean strIsNotEmpty(String str)
	{
		return !strIsEmpty(str);
	}
	public static boolean strIsEmpty(String str)
	{
		if(str==null || str.length()==0) return true;
		return false;
	}
	public static boolean strIsProductionOf(String str,String toks)
	{
		for(int i=0;i<str.length();i++)
		{
			char c = str.charAt(i);
			if(toks.indexOf(c)==-1)
				return false;
		}
		return true;
	}
	
	//如果str==null 或 str=="" 或 数字格式不正确，返回def
	public static int safeInt(String str,int def)
	{
		if(str==null || str.length()==0) return def;
		try
		{
			return Integer.parseInt(str);
		}
		catch(Exception e)
		{
			return def;
		}
	}
	//如果str==null 或 str=="" 返回def
	public static String safeString(String str,String def)
	{
		if(str==null||str.length()==0) return def;
		return str;
	}

	//安全将字符串转换为int,如果str==null或str==""或不能转换为int,返回def
	public static int safeString2Int(String str,int def)
	{
		int res;
		str = safeString(str,""+def);
		try{res = Integer.parseInt(str);}
		catch(Exception e){res = def;}
		return res;
	}
	
	//List的分页接口
	//page_desc为int[3],其中
	//int[0]为页数(从1开始数)
	//int[2] 为总共的人数
	//int[3] 为总共的项目数
	public static Iterator<Object> ListPageDivide(List<Object> list,int item_per_page,String page_no,int[] page_desc) throws Exception
	{
		List<Object> res = new ArrayList<Object>();
		
		
		int total_item_count = list.size();
		int total_page_count = (total_item_count-1)/item_per_page + 1;

		int i_page_no = safeString2Int(page_no,1);
		if(i_page_no<=0) i_page_no = 1;
		else if(i_page_no>total_page_count) i_page_no = total_page_count;
		
		if(page_desc==null || page_desc.length<3) Helper.E("output param page_desc invalid ");
		page_desc[0] = i_page_no;//count from 1
		page_desc[1] = total_page_count;
		page_desc[2] = total_item_count;

		int offset = item_per_page*(i_page_no-1);//count from 0
		for(int i=offset;i<list.size()&&i<offset+item_per_page;i++)
		{
			res.add(list.get(i));
		}
		
		return res.iterator();
	}
	
	//取得系统启动目录
	public static String getUserDir() 
	{
		return System.getProperty("user.dir");
	}
	//flag : 0 folders & files  1 files   2 folders 
	public static final int GET_ALL = 0;
	public static final int GET_FILES = 1;
	public static final int GET_DIRS = 2;
	public static List<String> getSubFiles(String dir,int flag,boolean filter) throws Exception
	{
		File file = new File(dir);
		if(file==null) E(dir + " not found!");
		List<String> res = new ArrayList<String>();
		File[] subfiles = file.listFiles();
		if(subfiles==null) return res;
		for(int i=0;i<subfiles.length;i++)
		{
			File f = subfiles[i];
			if(flag==GET_FILES && f.isDirectory()) continue;
			if(flag==GET_DIRS && f.isFile()) continue;
			if(filter)
			{
				if(f.getName().equalsIgnoreCase("CVS")) continue;
				if(f.getName().equalsIgnoreCase("Thumbs.db")) continue;
			}
			res.add(subfiles[i].getName());
		}
		return res;
	}
	//////////// exception hander services ////////////////
	public static void RE(int msg)
	{
		if(true) throw new RuntimeException("int: " + msg);
	}
	public static void RE(String msg) 
	{
		if (true) throw new RuntimeException(msg);
	}
	public static void RE(Object msg) 
	{
		if (true) throw new RuntimeException(msg.toString());
	}
	public static void E(int msg) throws Exception 
	{
		if (true) throw new Exception("int: " + msg);
	}
	public static void E(String msg) throws Exception 
	{
		if (true) throw new Exception(msg);
	}
	public static void E(Object msg) throws Exception 
	{
		if (true) throw new Exception(msg.toString());
	}
	public static String V(String s) 
	{
		if (s == null) return "";
		return s;
	}

	/////////// string util ////////////////////////////////
	public static String trimChWidth(String s, String encoding,
			int width_in_byte) throws Exception {
		if (s == null) return s;
		int i = 1;
		for (; i <= s.length(); i++)
		{
			String n = s.substring(0, i);
			if (n.getBytes(encoding).length > width_in_byte) break;
		}
		return s.substring(0, i - 1);
	}

	//返回字符串的宽度的字符串（英文字宽的倍数）
	public static String trimChWidth(String s, int width) {
		if (s == null) return s;
		StringBuffer sb = new StringBuffer();
		int j = 0;
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c > 255) j += 2;//中文
			else j++;
			if (j >= width)
			{
				//
				char cc = sb.charAt(sb.length() - 1);
				if (cc > 255) return sb.substring(0, sb.length() - 1) + "..";
				else return sb.substring(0, sb.length() - 2) + "..";
			}
			sb.append(c);
		}
		return sb.toString();
	}
	
	
	
	public static String getStrAttributeDefault(Hashtable<String,String> ht,String name,String def)
	{
		if(ht==null) return def;
		String value = ht.get(name); 
		if(value==null) return def;
		return value;
	}
	public static String getStrAttributeAssert(Hashtable<String,String> ht,String name) throws Exception
	{
		AssertNotNull(ht);
		String value = ht.get(name);
		AssertNotEmpty(value,"empty attribute : " + name);
		return value;
	}
	
	public static int getIntAttributeDefault(Hashtable<String,String> ht,String name,int def)
	{
		if(ht==null) return def;
		String value = ht.get(name); 
		if(value==null) return def;
		try{return Integer.parseInt(value);}
		catch(Exception e){return def;}
	}
	public static int getIntAttributeAssert(Hashtable<String,String> ht,String name) throws Exception
	{
		AssertNotNull(ht);
		String value = ht.get(name);
		AssertNotEmpty(value,"empty attribute : " + name);
		return Integer.parseInt(value);
	}
	
	
    public static String getStrEntryDefault(Map<String,String[]> map,String name,String def) throws Exception
    {
    	if(map==null) return def;
    	String[] values = map.get(name);
    	if(values==null || values.length!=1 || values[0]==null) return def;
    	return values[0];
    }
    public static String getStrEntryAssert(Map<String,String[]> map,String name) throws Exception
    {
    	AssertNotNull(map);
    	String[] values = map.get(name);
    	if(values==null || values.length!=1 || values[0]==null) Helper.E("get entry failed : " + name);
    	return values[0];
    }
    
    public static String getStrEntryDefault(Map<String,String[]> map,String name,String def,String enc1,String enc2) throws Exception
    {
    	if(map==null) return def;
    	String[] values = map.get(name);
    	if(values==null || values.length!=1 || values[0]==null) return def;
    	return new String(values[0].getBytes(enc1),enc2);
    }
    public static String getStrEntryAssert(Map<String,String[]> map,String name,String enc1,String enc2) throws Exception
    {
    	AssertNotNull(map);
    	String[] values = map.get(name);
    	if(values==null || values.length!=1 || values[0]==null) Helper.E("get entry failed : " + name);
    	return new String(values[0].getBytes(enc1),enc2);
    }
    
    
    
    public static int getIntEntryDefault(Map<String,String[]> map,String name,int def) throws Exception
    {
    	if(map==null) return def;
    	String[] values = map.get(name);
    	if(values==null || values.length!=1 || values[0]==null) return def;
    	try{return Integer.parseInt(values[0]);}
    	catch(Exception e){return def;}
    }
    public static int getIntEntryAssert(Map<String,String[]> map,String name) throws Exception
    {
    	AssertNotNull(map);
    	String[] values = (String[])map.get(name);
    	if(values==null || values.length!=1 || values[0]==null) Helper.E("get entry failed : " + name);
    	return Integer.parseInt(values[0]);
    }
    
    public static String buildIn(List<String> strs) throws Exception{
		StringBuilder str = new StringBuilder();
		str.append("(");
		for(int i=0; i<strs.size(); i++){
			if(i!=0){
				str.append(",");
				str.append(strs.get(i));
			}
			else {
				str.append(strs.get(i));
			}
		}
		str.append(")");
		return str.toString();
	}
    
    public final static String[] getArrayLinkByStrLink(String url) throws Exception
    {
    	if(url.indexOf("&amp;") >= 0)
    		throw new Exception("unsupport seprater(&amp;)");
    	int ind_1 = url.indexOf("?");
    	if(ind_1 < 0)
    		return new String[]{url};
    	String href_link = url.substring(0, ind_1);
    	String[] path_info = url.substring(ind_1 + 1, url.length()).split("&");
		HashMap<String, String> params = new HashMap<String, String>();
		for(String var : path_info)
		{
			String[] p = var.split("=");
			if(p[0].length() == 0)
				continue;
			params.put(p[0], p.length > 1 ? p[1] : "");
		}
		String[] result = new String[1 + 2 * params.size()];
		result[0] = href_link;
		Iterator<String> it = params.keySet().iterator();
		int flag = 1;
		while(it.hasNext()){
			String key = it.next();
			result[flag++] = key;
			result[flag++] = params.get(key);
		}
    	return result;
    }
    
    //匹配半角和全角空格
    private static Pattern illegalMsgPattern = Pattern.compile("(?:\\s|　)+");
    private static Pattern illegalMsgFilterPattern = Pattern.compile("(?:mbook\\.cn|nbshu\\.com|read01\\.com|du007\\.com|5iread\\.com\\.cn|myeshu\\.net|mvip\\.cn)+");
    public static boolean containsIllegalMessage(String message) {
    	if(message == null || message.trim().equals(""))
    		return false;
    	String[] illegalSuffixes = new String[] {".com", ".cn", ".com.cn", ".net", ".net.cn", ".org", ".tv", ".cc", ".im", ".biz", ".name", ".mobi", ".tw", ".hk", ".org.cn", ".us", ".au", ".jp"};//".info",  cancel by ysp
		String[] alphabet1 = new String[] {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z","a",".",".",".",".","c"};
		String[] alphabet2 = new String[] {"ａ","ｂ","ｃ","ｄ","ｅ","ｆ","ｇ","ｈ","ｉ","ｊ","ｋ","ｌ","ｍ","ｎ","ｏ","ｐ","ｑ","ｒ","ｓ","ｔ","ｕ","ｖ","ｗ","ｘ","ｙ","ｚ","@","。","点","\uff0e", "、","℃"};
		
		Matcher m = illegalMsgPattern.matcher(message);
		message = m.replaceAll("").toLowerCase();
		for(int i = 0; i < alphabet2.length; ++i) 
			message = message.replaceAll(alphabet2[i], alphabet1[i]);

		m = illegalMsgFilterPattern.matcher(message);
		message = m.replaceAll("");

		for(int i = 0; i < illegalSuffixes.length; ++i) {
			if(message.indexOf(illegalSuffixes[i]) != -1) 
				return true;
		}
		return false;
    }
    
    static Pattern p = Pattern.compile("");
	static Matcher m = p.matcher("");
    public static boolean isMobileNo(String s) {
    	if(s == null || s.trim().equals(""))
    		return false;
    	String regex = "^1[3,4,5,6,8]\\d{9}$";
    	m.usePattern(Pattern.compile(regex));
		m.reset(s);
		return m.matches();
    }
    
    //SimpleDateFormat.parse not thread safe,sdf shouldn't be public
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
    public static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy年MM月dd日");
    public static SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
    public static SimpleDateFormat sdf5 = new SimpleDateFormat("MM月dd日");
    public static SimpleDateFormat sdf6 = new SimpleDateFormat("MM-dd");
    public static String getFunnyDate(String mysqlDateTime) throws Exception{
    	if (mysqlDateTime == null || mysqlDateTime.isEmpty()) return "";
    	long current = System.currentTimeMillis();
    	long ori = sdf.parse(mysqlDateTime).getTime();
    	int spanInMinute = (int)((current - ori) / 1000 / 60);
    	String funnyDateStr = "";
    	if(spanInMinute < 60) funnyDateStr = spanInMinute + "分钟前";
    	else if(spanInMinute < 1440) funnyDateStr = (spanInMinute / 60) + "小时前";
    	else if(spanInMinute < 10080) funnyDateStr = (spanInMinute / 60 / 24) + "天前";
    	else funnyDateStr = mysqlDateTime.substring(2, 10);
    	return funnyDateStr;
    }
    
    //////////////////////////length format////////////////////////////
    protected final static String[] ZERO_ARRAY = new String[]{
    	"", "0", "00", "000" ,"0000", "00000", "000000", "0000000", "00000000", "000000000", "0000000000"
    };
    
    /**
     * 
     * @param src 如果src的长度大于len，就直接返回src，src.length<Short.MAX_VALUE
     * @param len 0<=len<=10+src.length()
     * @return
     */
    public final static String formatStrLength(String src, int len) {
    	if(src == null)
    		return ZERO_ARRAY[len - 1];
    	int length = src.trim().length();
    	if(length >= len)
    		return src;
    	else return ZERO_ARRAY[len - length] + src.trim();
    }
    
    public final static String formatIntLength(int src, int len) {
    	return formatStrLength(String.valueOf(src), len);
    }
    
    public static final int compareDateTime(Date currentDateTime, String strDateTime) throws Exception{
    	return currentDateTime.compareTo(sdf.parse(strDateTime));
    }
    
    public static final String getDateStrConvert3(long millisseconds){
		try{
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(millisseconds);
			
			return dateStrConvert3(sdf1.format(calendar.getTime()));
		}catch (Exception e){}
    	return "";
	}
    
    public static int max(int[] arr) {
    	int index = 0;
    	for(int i = 1; i < arr.length; i++) {
    		index = arr[index] >= arr[i] ? index : i;
    	}
    	return arr[index];
    }
    
    public final static int max(String[] arr) {
    	int intArr[] = strArrToIntArr(arr);
    	int index = 0;
    	for(int i = 1; i < intArr.length; i++) {
    		index = intArr[index] >= intArr[i] ? index : i;
    	}
    	return intArr[index];
    }
    
    public static enum SortingOrder{
    	ASCENDING, DESCENDING
    }
    //so sorry that this is less efficient than Arrays.sort()
    public final static void sort(int[] arr, SortingOrder so) {
    	if(arr == null || arr.length <= 1)
    		return;
    	if(so == SortingOrder.ASCENDING) {
	    	for(int i = 0; i < arr.length - 1; i++) 
				for(int j = i; j < arr.length - 1; j++) {
					if(arr[i] > arr[j + 1]) {
		    			arr[i] ^= arr[j + 1];
		    			arr[j + 1] ^= arr[i]; 
		    			arr[i] ^= arr[j + 1];
		    		}
				}
    	}else if(so == SortingOrder.DESCENDING) {
    		for(int i = 0; i < arr.length - 1; i++) 
    			for(int j = i; j < arr.length - 1; j++) {
    				if(arr[i] < arr[j + 1]) {
    	    			arr[i] ^= arr[j + 1];
    	    			arr[j + 1] ^= arr[i]; 
    	    			arr[i] ^= arr[j + 1];
    	    		}
    			}
        }
    }
    
    public final static int[] strArrToIntArr(String[] strArr) throws NumberFormatException{
    	if(strArr == null) return null;
    	if(strArr.length == 0) return new int[0];
    	int intArr[] = new int[strArr.length];
    	for(int i = 0; i < strArr.length; i++) {
    		intArr[i] = Integer.parseInt(strArr[i]);
    	}
    	return intArr;
    }
    
	public static <K, V> Map.Entry<K, V>[] getSortedHashtableByValue(Map<K, V> map) {  
        Set<Map.Entry<K, V>> set = map.entrySet();  
        Map.Entry<K, V>[] entries = (Map.Entry<K, V>[]) set.toArray(new Map.Entry[set.size()]);  
        Arrays.sort(entries, new Comparator<Map.Entry<K, V>>() {  
            public int compare(Map.Entry<K, V> obj1, Map.Entry<K, V> obj2) {  
                Long key1 = Long.valueOf(((Map.Entry<K, V>) obj1).getValue().toString());  
                Long key2 = Long.valueOf(((Map.Entry<K, V>) obj2).getValue().toString());  
                return key1.compareTo(key2);  
            }  
        });  
        return entries;  
    } 
    
    public final static boolean delFileOnWap4 (String fileName) throws Exception {
    	HttpURLConnection conn = null;
    	InputStream is = null;
    	try {
    		String f = "/home/mbook/webapps/res/" + fileName;
	    	URL url = new URL("http://192.168.0.100/wap/nonsess/del.resource.on.wap.4.jsp?pwd=itismbook&f=" + f);
	        conn = (HttpURLConnection)url.openConnection();
	        conn.connect();
	        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	        	is = conn.getInputStream();	//ensures the request be sent
	        	return true;
	        }   
    	} catch (Exception e) {  
    		//do nothing
    	} finally {
    		if (is != null) is.close();
    		if (conn != null)
    			conn.disconnect();
    	}
    	return false;
    }
    
    //timeout in milliseconds
    public final static boolean sendHttpRequest (String urlStr, int timeout) throws Exception {
    	HttpURLConnection conn = null;
    	InputStream is = null;
    	try {
	    	URL url = new URL(urlStr);
	        conn = (HttpURLConnection)url.openConnection();
	        conn.setConnectTimeout(timeout);
	        conn.connect();
	        
	        if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
	        	is = conn.getInputStream();	//ensures the request be sent
	        	return true;
	        }
    	} catch (Exception e) {  
    		//do nothing
    	} finally {
    		if (is != null) is.close();
    		if (conn != null)
    			conn.disconnect();
    	}
    	return false;
    }
    
    //get url content as string with trim,timeout in milliseconds
    public final static String getHttpURLContent (String urlStr, int timeout) throws Exception {
    	HttpURLConnection conn = null;
    	InputStream is = null;
    	Reader reader = null;
    	BufferedReader br = null;
    	try {
    		conn = (HttpURLConnection) new URL(urlStr).openConnection();
    		conn.setConnectTimeout(timeout);
    		is = conn.getInputStream();
    		reader = new InputStreamReader(is);
    		br = new BufferedReader(reader);
    		StringBuilder sb = new StringBuilder(500);
    		String line = null;
    		while ((line = br.readLine()) != null) {
    			sb.append(line.trim());
    		}
    		return sb.toString();
    	} finally {
    		if (br != null) br.close();
    		if (reader != null) reader.close();
    		if (is != null) is.close();
    		if (conn != null) conn.disconnect();
    	}
    }
    
    //正则表达式判断是否是数字
    public final static boolean isNumeric(String str) {
    	if(str!=null && !str.equals("")){
			return str.matches("^[0-9]*$");
		}
		return false;
	}
    
    public static boolean isNumeric(Object o) {
		if (o != null){
			String s = o.toString();
			return isNumeric(s);
		}
		return false;
	}
    
    //返回显示多少个(length)字符
    public final static String getStr(String content,int length,int sb_start,int sb_end)
    {
    	String total_content = "";
    	if(content.length()>length)
    	{
    		total_content=content.substring(sb_start, sb_end) + "…";
    	}else if(content.length()<=length)
    	{
    	    total_content = content;	
    	}
    	return total_content;
    }
    
	public static String sqlEncode(String target){
		StringBuffer stringbuffer = new StringBuffer();
		int j = target.length();
		for (int i = 0; i < j; i++) {
			char c = target.charAt(i);
			switch (c) {
			case 92:
				stringbuffer.append("\\\\");
				break;
			case 39:
				stringbuffer.append("\\'");
				break;
			default:
				stringbuffer.append(c);
				break;
			}
		}
		return new String(stringbuffer.toString());
	}
	
	public static String sqlDecode(String target){
		StringBuffer stringbuffer = new StringBuffer();
		int j = target.length();
		for (int i = 0; i < j; i++) {
			char c = target.charAt(i);
			switch (c) {
			case 34:
				stringbuffer.append("'");
				break;
			default:
				stringbuffer.append(c);
				break;
			}
		}
		return new String(stringbuffer.toString());
	}
	
	public static Integer vInt(String s){
		if(isNumeric(s)){
			return Integer.valueOf(s);
		}else{
			return 0;
		}
	}
	
	public static Integer vIntDefault(String s, int def){
		if(isNumeric(s)){
			return Integer.valueOf(s);
		}else{
			return def;
		}
	}
	
	public static Integer vInt(Object o){
		if(isNumeric(o)){
			return (Integer)o;
		}else{
			return 0;
		}
	}
	
	public static Integer vIntDefault(Object o, int def){
		if(isNumeric(o)){
			return (Integer)o;
		}else{
			return def;
		}
	}
	
	public static String vString(String s){
		if(s == null){
			return "";
		}else{
			return s;
		}
	}
	
	public static String vStringDefault(String s, String def){
		if(s == null){
			return def;
		}else{
			return s;
		}
	}
	
	public static String vString(Object o){
		if(o == null){
			return "";
		}else{
			return o.toString();
		}
	}
	
	public static String vStringDefault(Object o, String def){
		if(o == null){
			return def;
		}else{
			return o.toString();
		}
	}
	
	/**
	 * 验证输入的邮箱格式是否符合
	 * 
	 * @param email
	 * @return 是否合法
	 */
	public static boolean emailFormat(String email) {
		boolean tag = true;
		final String pattern1 = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
		final Pattern pattern = Pattern.compile(pattern1);
		final Matcher mat = pattern.matcher(email);
		if (!mat.find()) {
			tag = false;
		}
		return tag;
	}
	
	public static List<?> cleanListDuplicate(List<?> tlist){
		ArrayList<Object> list = new ArrayList<Object>();
		for(int i=0;i<tlist.size();i++){
		    for(int y=i+1;y<tlist.size();y++){
		        if(tlist.get(i)!=tlist.get(y)){
		        	list.add(tlist.get(i));
		        }
		    }
		} 
		return list;
	}
	
	// Mapping table from 6-bit nibbles to Base64 characters.
	private static char[]	map1	= new char[64];
	static
	{
		int i = 0;
		for (char c = 'A'; c <= 'Z'; c++)
			map1[i++] = c;
		for (char c = 'a'; c <= 'z'; c++)
			map1[i++] = c;
		for (char c = '0'; c <= '9'; c++)
			map1[i++] = c;
		map1[i++] = '+';
		map1[i++] = '/';
	}
	// Mapping table from Base64 characters to 6-bit nibbles.
	private static byte[]	map2	= new byte[128];
	static
	{
		for (int i = 0; i < map2.length; i++)
			map2[i] = -1;
		for (int i = 0; i < 64; i++)
			map2[map1[i]] = (byte) i;
	}

	public static String Base64Encode(byte[] in) 
	{
		int iLen = in.length;
		int oDataLen = (iLen * 4 + 2) / 3; // output length without padding
		int oLen = ((iLen + 2) / 3) * 4; // output length including padding
		char[] out = new char[oLen];
		int ip = 0;
		int op = 0;
		while (ip < iLen)
		{
			int i0 = in[ip++] & 0xff;
			int i1 = ip < iLen ? in[ip++] & 0xff : 0;
			int i2 = ip < iLen ? in[ip++] & 0xff : 0;
			int o0 = i0 >>> 2;
			int o1 = ((i0 & 3) << 4) | (i1 >>> 4);
			int o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
			int o3 = i2 & 0x3F;
			out[op++] = map1[o0];
			out[op++] = map1[o1];
			out[op] = op < oDataLen ? map1[o2] : '=';
			op++;
			out[op] = op < oDataLen ? map1[o3] : '=';
			op++;
		}
		return new String(out);
	}

	public static byte[] Base64Decode(String s) 
	{
		char[] in = s.toCharArray();
		int iLen = in.length;
		if (iLen % 4 != 0) throw new IllegalArgumentException(
				"Length of Base64 encoded input string is not a multiple of 4.");
		while (iLen > 0 && in[iLen - 1] == '=')
			iLen--;
		int oLen = (iLen * 3) / 4;
		byte[] out = new byte[oLen];
		int ip = 0;
		int op = 0;
		while (ip < iLen)
		{
			int i0 = in[ip++];
			int i1 = in[ip++];
			int i2 = ip < iLen ? in[ip++] : 'A';
			int i3 = ip < iLen ? in[ip++] : 'A';
			if (i0 > 127 || i1 > 127 || i2 > 127 || i3 > 127) throw new IllegalArgumentException(
					"Illegal character in Base64 encoded data.");
			int b0 = map2[i0];
			int b1 = map2[i1];
			int b2 = map2[i2];
			int b3 = map2[i3];
			if (b0 < 0 || b1 < 0 || b2 < 0 || b3 < 0) throw new IllegalArgumentException(
					"Illegal character in Base64 encoded data.");
			int o0 = (b0 << 2) | (b1 >>> 4);
			int o1 = ((b1 & 0xf) << 4) | (b2 >>> 2);
			int o2 = ((b2 & 3) << 6) | b3;
			out[op++] = (byte) o0;
			if (op < oLen) out[op++] = (byte) o1;
			if (op < oLen) out[op++] = (byte) o2;
		}
		return out;
	}
	
	public static void main(String[] args){
		cleanListDuplicate(new ArrayList<String>());
	}
}