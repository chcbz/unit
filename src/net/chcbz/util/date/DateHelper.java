package net.chcbz.util.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateHelper {
	/**
	 * 获取两个日期之间周末最多的一天
	 * @param startDate 开始日期
	 * @param endDate 结束日期
	 * @return int[0]:星期六 int[1]:星期日 int[][0]:月 int[][1]:日
	 */
	public static int[][] mostWeekendDate(Date startDate, Date endDate){
		int[][][] list = weekendNumbers(startDate, endDate);
		int[][] date = new int[2][2];
		int tempNum = 0;
		for(int i=0;i<list.length;i++){
			for(int j=0;j<list[i].length;j++){
				for(int k=0;k<list[i][j].length;k++){
					if(list[i][j][k]>tempNum){
						date[k][0] = i+1;
						date[k][1] = k+1;
					}
				}
			}
		}
		return date;
	}
	/**
	 * 两个日期间的每个日期存在的周末数
	 * @param startDate 开始时间
	 * @param endDate 结束时间
	 * @return int[][][] 分别为月、日、星期六|星期日
	 */
	public static int[][][] weekendNumbers(Date startDate, Date endDate){
		int[][][] list = new int[12][31][2];
		Calendar calendar = Calendar.getInstance();
		while(startDate.getTime()<=endDate.getTime()){
			calendar.setTime(startDate);
			if(Calendar.SATURDAY==calendar.get(Calendar.DAY_OF_WEEK)){
				list[calendar.get(Calendar.MONTH)][calendar.get(Calendar.DAY_OF_MONTH)-1][0] += 1;
			}else if(Calendar.SUNDAY==calendar.get(Calendar.DAY_OF_WEEK)){
				list[calendar.get(Calendar.MONTH)][calendar.get(Calendar.DAY_OF_MONTH)-1][1] += 1;
			}
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			startDate = calendar.getTime();
		}
		return list;
	}
	
	/**
	 * 获取现在时间
	 * 
	 * @return 返回时间类型 yyyy-MM-dd HH:mm:ss
	 */
	public static Date getNowDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		ParsePosition pos = new ParsePosition(8);
		Date currentTime_2 = formatter.parse(dateString, pos);
		return currentTime_2;
	}

	public static String getStringDate() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取现在时间
	 * 
	 * @return 返回短时间字符串格式yyyy-MM-dd
	 */
	public static String getStringDateShort() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 获取时间 小时:分;秒 HH:mm:ss
	 * 
	 * @return
	 */
	public static String getTimeShort() {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		Date currentTime = new Date();
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 将长时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToDateLong(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 将长时间格式时间转换为字符串 yyyy-MM-dd HH:mm:ss
	 * 
	 * @param dateDate
	 * @return
	 */
	public static String dateToStrLong(java.util.Date dateDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	/**
	 * 将短时间格式时间转换为字符串 yyyy-MM-dd
	 * 
	 * @param dateDate
	 * @param k
	 * @return
	 */
	public static String dateToStr(java.util.Date dateDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		String dateString = formatter.format(dateDate);
		return dateString;
	}

	/**
	 * 将短时间格式字符串转换为时间 yyyy-MM-dd
	 * 
	 * @param strDate
	 * @return
	 */
	public static Date strToDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	/**
	 * 得到现在时间
	 * 
	 * @return
	 */
	public static Date getNow() {
		Date currentTime = new Date();
		return currentTime;
	}

	/**
	 * 提取一个月中的最后一天
	 * 
	 * @param day
	 * @return
	 */
	public static Date getLastDate(long day) {
		Date date = new Date();
		long date_3_hm = date.getTime() - 3600000 * 34 * day;
		Date date_3_hm_date = new Date(date_3_hm);
		return date_3_hm_date;
	}

	/**
	 * 得到现在时间
	 * 
	 * @return 字符串 yyyyMMdd HHmmss
	 */
	public static String getStringToday() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd HHmmss");
		String dateString = formatter.format(currentTime);
		return dateString;
	}

	/**
	 * 得到现在小时
	 */
	public static String getHour() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		String hour;
		hour = dateString.substring(11, 13);
		return hour;
	}

	/**
	 * 得到现在分钟
	 * 
	 * @return
	 */
	public static String getTime() {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = formatter.format(currentTime);
		String min;
		min = dateString.substring(14, 16);
		return min;
	}

	/**
	 * 根据用户传入的时间表示格式，返回当前时间的格式 如果是yyyyMMdd，注意字母y不能大写。
	 * 
	 * @param sformat
	 *            yyyyMMddhhmmss
	 * @return
	 */
	public static String getUserDate(String sformat) {
		Date currentTime = new Date();
		SimpleDateFormat formatter = new SimpleDateFormat(sformat);
		String dateString = formatter.format(currentTime);
		return dateString;
	}
	
	/**
	 * 判断时间date1是否在时间date2之前,时间格式 2005-4-21 16:16:34
	 * 
	 */
	public static boolean isDateBefore(String date1, String date2) {
		if((date2==null || date2.equals("")) && date1!=null){
			return false;
		}else if((date1==null || date1.equals("")) && date2!=null){
			return true;
		}else{
			try {
				DateFormat df = DateFormat.getDateTimeInstance();
				return df.parse(date1).before(df.parse(date2));
			} catch (ParseException e) {
				System.out.println("[SYS] " + e.getMessage());
				return false;
			}
		}
	}

	/**
	 * 判断当前时间是否在时间date2之前,时间格式 2005-4-21 16:16:34
	 */
	public static boolean isDateBefore(String date2) {
		try {
			Date date1 = new Date();
			DateFormat df = DateFormat.getDateTimeInstance();
			return date1.before(df.parse(date2));
		} catch (ParseException e) {
			System.out.println("[SYS] " + e.getMessage());
			return false;
		}
	}
	
	public static void main(String[] args) throws Exception{
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		int[][][] list = weekendNumbers(df.parse("2012-02-22"),df.parse("2052-02-22"));
		for(int i=0;i<list.length;i++){
			for(int j=0;j<list[i].length;j++){
				for(int k=0;k<list[i][j].length;k++){
					System.out.println((i+1)+"月"+(j+1)+"日: "+list[i][j][k]);
				}
			}
		}
		/*int[][] list = mostWeekendDate(df.parse("2012-02-22"),df.parse("2042-12-22"));
		System.out.println(list[0][0] + "-" + list[0][1] + "-" + list[1][0] + "-" + list[1][1]);*/
	}
}
