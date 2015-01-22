package net.chcbz.util.db;

public class DBHelper {
	/**
	 * mysql的字符串格式转换
	 * @param str
	 * @return
	 */
	public final static String mysql_varchar_escape(String str){
        if (str == null) str = "";
        str = str.replaceAll("'","''");
		str = str.replaceAll("\\\\","\\\\\\\\");
		return str;  
	}
}
