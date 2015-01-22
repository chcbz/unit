package net.chcbz.util.web;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import net.chcbz.util.Helper;
import net.chcbz.util.secret.CommCodec;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;

@SuppressWarnings("unchecked")
public class WebHelper {
	public static final Logger logger = Logger.getLogger(WebHelper.class.getName());
	/**
	 * 把文本编码为Html代码
	 * 
	 * @param target
	 * @return 编码后的字符串
	 */
	public static String htmlEncode(String target) {
		StringBuffer stringbuffer = new StringBuffer();
		int j = target.length();
		for (int i = 0; i < j; i++) {
			char c = target.charAt(i);
			switch (c) {
			case 60:
				stringbuffer.append("&lt;");
				break;
			case 62:
				stringbuffer.append("&gt;");
				break;
			case 38:
				stringbuffer.append("&amp;");
				break;
			case 34:
				stringbuffer.append("&quot;");
				break;
			case 169:
				stringbuffer.append("&copy;");
				break;
			case 174:
				stringbuffer.append("&reg;");
				break;
			case 165:
				stringbuffer.append("&yen;");
				break;
			case 8364:
				stringbuffer.append("&euro;");
				break;
			case 8482:
				stringbuffer.append("&#153;");
				break;
			case 13:
				if (i < j - 1 && target.charAt(i + 1) == 10) {
					stringbuffer.append("<br>");
					i++;
				}
				break;
			case 32:
				if (i < j - 1 && target.charAt(i + 1) == ' ') {
					stringbuffer.append(" &nbsp;");
					i++;
					break;
				}
			default:
				stringbuffer.append(c);
				break;
			}
		}
		return new String(stringbuffer.toString());
	}
	
	/**
	 * 根据request获取表头信息
	 * @param request
	 * @return
	 */
	public static final String getRequestHeader(HttpServletRequest request){
		StringBuffer sb = new StringBuffer();
		sb.append(request.getMethod() + " " + request.getRequestURI() + "\r\n");
		Enumeration e = request.getHeaderNames();
		while(e.hasMoreElements())
		{
			String name = (String)e.nextElement();
			String value = request.getHeader(name);
			sb.append(name + " : " + value + "\r\n");
		}
		return sb.toString();
	}
	
	static class RouterPassAuth extends Authenticator {
		String name = null;
		String password = null;
		@Override
		public PasswordAuthentication getPasswordAuthentication() {
			return new PasswordAuthentication(name,
					password.toCharArray());
		}
		public RouterPassAuth(String name, String password) {
			this.name = name;
			this.password = password;
		}
	}
	
	/**
	 * 获取路由器的IP地址
	 * @param turl 路由器获取IP地址的URL,如果为空,则默认为:http://192.168.1.1:80/userRpm/StatusRpm.htm?Connect=连 接&wan=1
	 * @param name 路由器的登录用户名
	 * @param password 路由器的登录密码
	 * @return
	 * @throws Exception
	 */
	public static String getRouteIp(String turl, String name, String password) {
		if (turl == null || turl.equals("")) {
			turl = "http://192.168.1.1:80/userRpm/StatusRpm.htm?Connect=连 接&wan=1";
		}
		InputStream ins = null;
		try {
			Authenticator.setDefault(new RouterPassAuth(name, password));
			URL url = new URL(turl);
			ins = url.openConnection().getInputStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					ins));
			String str;
			boolean flag = false;
			StringBuffer wanPacket = new StringBuffer();
			int num = 3;
			while ((str = reader.readLine()) != null && num > 0) {
				if (str.contains("var wanPara = new Array(")) {
					flag = true;
				}
				if (flag) {
					wanPacket.append(str);
					num--;
				}
			}
			// 找出数据包中第一个匹配的IP,即为Ip
			Pattern p = Pattern.compile("\\d+\\.\\d+\\.\\d+\\.\\d+");
			Matcher m = p.matcher(wanPacket);
			if (m.find()) {
				return m.group();
			} else {
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static String getRouteIp(){
		return getRouteIp(null, "admin", "admin");
	}

	public boolean blockUpBlackUa(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		boolean ifBlock = false;
		String userAgent = request.getHeader("User-Agent").toLowerCase();
		String white_gateway[] = { "12.25.203.11", "202.108.235.154" };
		String black_userAgent[] = { "opera", "msie", "opmv-sdk", "m3gate", "up.browser" };
		String remote_ip = request.getRemoteAddr();
		for (int i = 0; i < black_userAgent.length; i++) {
			userAgent = userAgent.toLowerCase();
			if (userAgent.indexOf(black_userAgent[i]) > -1) {
				ifBlock = false;
				i = black_userAgent.length;

			} else
				continue;
		}
		for (int i = 0; i < white_gateway.length; i++) {

			if (remote_ip.equals(white_gateway[i])) {
				i = white_gateway.length;
				ifBlock = true;
			} else
				continue;
		}
		if (!ifBlock) {
			PrintWriter out = response.getWriter();
			out.println("");
			out.println("");
			out.println("");
			out.println("对不起，没有适配您的终端型号的内容。");
			out.println("梦网首页");
		}
		return ifBlock;
	}

	public static void getUserAgent(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException,
			NoSuchElementException {
		Enumeration enum_header = request.getHeaderNames();
		StringBuffer tmpStr = new StringBuffer("");
		String String_uaAll = null;
		String cur_header = "test";
		String userAgent = request.getHeader("User-Agent").toLowerCase();
		userAgent = userAgent.substring(0, userAgent.indexOf("/"));
		String path = "d:\\product\\view_cm\\webapps\\root\\mobile_ua\\"
				+ userAgent + ".txt";
		try {
			while (enum_header.hasMoreElements()) {
				cur_header = enum_header.nextElement().toString();
				if (cur_header == null || cur_header.equals("")) {
					new Exception("get the header's name as null!");
				}
				tmpStr.append(cur_header + "\n\r");
				Enumeration enum_value = request.getHeaders(cur_header);
				while (enum_value.hasMoreElements()) {
					tmpStr.append(enum_value.nextElement().toString() + "\n\r");
				}
			}
			tmpStr.append(request.getRemoteAddr() + "\n\r");
			tmpStr.append(request.getRemoteHost() + "\n\r");
			tmpStr.append(request.getProtocol() + "\n\r");
			String_uaAll = tmpStr.toString();
			FileWriter fw = new FileWriter(path, false);
			fw.write(String_uaAll);
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getIpAddr(HttpServletRequest request) {
		String ip = request.getHeader("x-forwarded-for");
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("WL-Proxy-Client-IP");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getHeader("MB-X-Forwarded-For");
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			ip = request.getRemoteAddr();
		}
		return ip;
	}
	
	public static final String urlEncode(String value) throws Exception
	{
		return URLEncoder.encode(value,"UTF-8");
	}
	public static final String urlDecode(String value) throws Exception
	{
		return URLDecoder.decode(value,"UTF-8");
	}
	
	protected static final String PARSE_FORM = "form";
	protected static final String PARSE_MULTI = "multi";
	protected static final String PARSE_MALFORM_POST = "malformpost";
	protected static final String REQATT_PARSE = "request.parse";
	protected static final String REQATT_ITEMS = "request.items";
	protected static final String REQATT_MALFORM_POST_ITEM = "request.malformpost.items";
    protected static final String REQATT_FORM_POST_ITEM = "request.form.items";

    public static final int AS_RUNTIME_UNKNOWN = 0;
	public static final int AS_RUNTIME_TOMCAT = 1;
	public static final int AS_RUNTIME_JETTY = 2;
	public static final String[] AS_RUNTIME_NAMES = new String[]{"未知WEB容器","Tomcat服务器","Jetty服务器"};
	public static final String getAsRuntimeName(HttpServletRequest request)
	{
		return AS_RUNTIME_NAMES[getAsRuntime(request)];
	}
	public static final int getAsRuntime(HttpServletRequest request)
	{
		int res = AS_RUNTIME_UNKNOWN;
		String request_class = request.getClass().getName();
		if(request_class.indexOf("catalina")>=0) res = AS_RUNTIME_TOMCAT;
		else if(request_class.indexOf("jetty")>=0) res = AS_RUNTIME_JETTY;
		return res;
	}
	
	protected static final String getParsed(HttpServletRequest request) throws Exception
	{//如果parse了，返回parse的类型，没有的话parse
//		String parse = (String)request.getAttribute(REQATT_PARSE);
//		if(parse==null || (parse.equals(PARSE_FORM)==false && parse.equals(PARSE_MULTI)==false && parse.equals(PARSE_MALFORM_POST)==false))
//		{
//			parse = parseRequest(request);//之前没有触发过parse,parse，有可能是fileupload(都要parse)
//			request.setAttribute(REQATT_PARSE,parse);
//		}
//		return parse;
        String parse = (String)request.getAttribute(REQATT_PARSE);//如果之前已经判断，根据之前的判断值
		if(parse==null || (parse.equals(PARSE_FORM)==false && parse.equals(PARSE_MULTI)==false && parse.equals(PARSE_MALFORM_POST)==false) ||
			(
				(parse.equals(PARSE_FORM)==true && request.getAttribute(REQATT_FORM_POST_ITEM)==null) ||
				(parse.equals(PARSE_MULTI)==true && request.getAttribute(REQATT_ITEMS)==null) ||
				(parse.equals(PARSE_MALFORM_POST)==true && request.getAttribute(REQATT_MALFORM_POST_ITEM)==null)
		    )
		  )
		{
			parse = parseRequest(request);
			request.setAttribute(REQATT_PARSE,parse);
		}
		return parse;
    }
	
	public static final boolean isMultipartContent(HttpServletRequest request) 
	{
		String contentType = request.getContentType();
		if(contentType!=null && contentType.toLowerCase().startsWith("multipart/")) return true; 
        return false;
    }
	
	protected static final String parseRequest(HttpServletRequest request) throws Exception
	{//返回forum || multi，request只需要parse一次
		String parse = PARSE_FORM;//默认是form方式
		if(isMultipartContent(request))
		{//file方式
			parse = PARSE_MULTI;
			FileItemFactory factory = new DiskFileItemFactory();
			ServletFileUpload upload = new ServletFileUpload(factory);
            String charset = request.getCharacterEncoding();
            if (charset != null) {
                upload.setHeaderEncoding(charset);
            }
            Iterator it = upload.parseRequest(request).iterator();
			Hashtable<String,FileItem> items = new Hashtable<String,FileItem>();
			while(it.hasNext())
			{
				FileItem item = (FileItem)it.next();
                items.put(item.getFieldName(),item);
			}
			request.setAttribute(REQATT_ITEMS,items);
		}
		else
		{//form方式，通过request.getParam来取得参数
			//但是要注意某些malformed post的情形，没有了content-type header,容器不能解释，导致getParam拿不到参数
			/*========header============
				connection : Keep-Alive
				host : 121.33.206.60
				accept : application/vnd.wap.wmlc;;Type=4365,application/vnd.wap.wmlc,application/vnd.wap.wmlscriptc,application/vnd.wap.multipart.related,application/vnd.wap.multipart.mixed,application/vnd.phonecom.mmc-wbxml,application/octet-stream,application/vnd.openwave.pp,text/plain,text/css,image/bmp,image/gif,image/jpeg,image/png,image/vnd.wap.wbmp,image/x-up-wpng,application/vnd.wap.sic,application/vnd.wap.slc,application/vnd.wap.coc,application/vnd.wap.xhtml+xml,application/xhtml+xml;;profile="http://www.wapforum.org/xhtml",text/html,application/smil,application/vnd.wap.mms-message,audio/imelody,audio/midi,text/x-imelody,audio/x-imelody,audio/x-midi,application/vnd.smaf,application/vnd.samsung.specific-image,image/gif,image/jpeg,image/png,image/vnd.wap.wbmp,text/vnd.sun.j2me.app-descriptor,application/java-archive,application/java,application/vnd.wap.wmlc;;Type=1108, /*
				accept-charset : utf-8
				accept-language : chinese, zh-cn, en
				content-length : 120
				cookie : CookieSupported=true
				via : HTTP/1.1 HESJ-PS-WAP1-GW15 (infoX-WISG, Huawei Technologies)
				encoding-version : 1.3
				========parameter============
				========content============
				action=send&content=%e5%a5%bd%e5%a5%bd%e7%9a%84&to_no=71240&from_no=2885&redir_path=member.msg.chat.jsp%3fopp_no%3d71240
			*/
			if(Helper.V(request.getContentType()).length()==0 && request.getMethod().toUpperCase().equals("POST") && request.getParameterMap().size()==0 && request.getContentLength()>0)
			{//malform post content
				parse = PARSE_MALFORM_POST;
				InputStream is = request.getInputStream();
				String content = new String(Helper.streamRead(is),"UTF-8").trim();
				String[] toks = content.split("&");
				HashMap<String,String> hm = new HashMap<String,String>();
				for(String tok : toks)
				{
					String name = tok;
					String value = "";
					int i = tok.indexOf('=');
					if(i>=0)
					{
						name = tok.substring(0,i);
						value = tok.substring(i+1);
					}
					name = WebHelper.urlDecode(name);
					value = WebHelper.urlDecode(value);
					hm.put(name,value);
				}
				request.setAttribute(REQATT_MALFORM_POST_ITEM,hm);
			}
			else
			{//form形式，不用容器的param分析器
				parse = PARSE_FORM;
				String content = Helper.V(request.getQueryString());
				if(request.getMethod().equalsIgnoreCase("POST"))
				{//POST有可能也有QUERY_STR
					String content2 = Helper.V(Helper.streamRead(request.getInputStream(),"UTF-8"));
					if(content.length()>0 && content2.length()>0) content = content + "&" + content2;
					else content = content + content2;
				}
				HashMap<String,List<String>> hm = new HashMap<String,List<String>>();
				parseFormContent(content,hm);
				request.setAttribute(REQATT_FORM_POST_ITEM,hm);
			}
		}
		return parse;
	}

    protected static final String[] ENCODING_GUESSING = new String[]{"UTF-8","UTF-16LE","UTF-16BE","GBK","BIG5"};
	protected static final String guessURLDecode(String src) throws Exception
	{
//		if(src==null || src.length()==0 || src.indexOf('%')==-1) return src;
		if(src==null || src.length()==0) return src;
		String res = null;
		double scr = Double.MIN_VALUE;
		for(String encoding : ENCODING_GUESSING)
		{
			String result = "";
			try
			{
				result = URLDecoder.decode(src,encoding);
			}
			catch(Exception e)
			{
				logger.info("error URLDecode src : " + src + "," + encoding);
				result = "";
			}
			double score = CommCodec.getGbkStrScore(result);
			if(score>scr)
			{
				res = result;
				scr = score;
			}
		}
		return res;
	}

    protected static final void parseFormContent(String input,HashMap<String,List<String>> hm) throws Exception
	{
		if(input==null || input.length()==0) return;
		String[] pairs = input.split("&");
		for(String pair : pairs)
		{
			if(pair==null || pair.length()==0) continue;
			String name = null;
			String value = null;
			int i = pair.indexOf('=');
			if(i>=0)
			{
				name=pair.substring(0,i);
				value=pair.substring(i+1);
			}
			else
			{
				name = pair;
				value = "";
			}
			name = guessURLDecode(name);
			value = guessURLDecode(value);
            if (hm.get(name) == null) {
                List<String> l = new ArrayList<String>();
                l.add(value);
                hm.put(name, l);
            } else {
                List<String> l = hm.get(name);
                l.add(value);
                hm.put(name, l);
            }
		}
	}
    protected static final FileItem getMultiItem(HttpServletRequest request,String name) throws Exception
	{
		Hashtable<String,FileItem> items = (Hashtable<String,FileItem>)request.getAttribute(REQATT_ITEMS);
		Helper.AssertNotNull(items);
		return items.get(name);
	}

	protected static boolean param_trans_enable = true;
	protected static String param_source_charset = "ISO-8859-1";
	protected static String param_target_charset = "GBK";
	public static final void setParamTrans(boolean enable,String source_charset,String target_charset)
	{
		param_trans_enable = enable;
		param_source_charset = source_charset;
		param_target_charset = target_charset;
	}

    protected static final String[] getStringsByFormItem(HttpServletRequest request,String name) throws Exception
	{//要进行可能的字符集转换
//		String res = request.getParameter(name);
//		if(res!=null && res.length()>0 && param_trans_enable)
//		{
//			if(!param_source_charset.equals(param_target_charset))
//				res = new String(res.getBytes(param_source_charset),param_target_charset);
//		}
//		return res;
        HashMap<String,List<String>> hm = (HashMap<String,List<String>>)request.getAttribute(REQATT_FORM_POST_ITEM);
		if(hm==null)
		{
			Enumeration e = request.getAttributeNames();
			while(e.hasMoreElements())
			{
				String nn = (String)e.nextElement();
				logger.info("req att : " + nn + " = " + request.getAttribute(nn));
			}

			Helper.E("hm REQATT_FORM_POST_ITEM is null");
		}
        List<String> l = hm.get(name);
        String[] res = null;
        if (l!=null) {
            res = (String[])l.toArray(new   String[0]);
        }
//        if(res!=null && res.length()>0) res=CommCodec.XMLEntityDecode(res);//wap用的情况下，纠正某些手机可能是XMLEntityEncode的情况
		return res;
    }

    protected static final String getStringByFormItem(HttpServletRequest request,String name) throws Exception
	{//要进行可能的字符集转换
//		String res = request.getParameter(name);
//		if(res!=null && res.length()>0 && param_trans_enable)
//		{
//			if(!param_source_charset.equals(param_target_charset))
//				res = new String(res.getBytes(param_source_charset),param_target_charset);
//		}
//		return res;
        HashMap<String,List<String>> hm = (HashMap<String,List<String>>)request.getAttribute(REQATT_FORM_POST_ITEM);
		if(hm==null)
		{
			Enumeration e = request.getAttributeNames();
			while(e.hasMoreElements())
			{
				String nn = (String)e.nextElement();
				logger.info("req att : " + nn + " = " + request.getAttribute(nn));
			}

			Helper.E("hm REQATT_FORM_POST_ITEM is null");
		}
        List<String> l = hm.get(name);
        String res = null;
        if (l!=null && l.size()>0) res = l.get(0);
        if(res!=null && res.length()>0) res=CommCodec.XMLEntityDecode(res);//wap用的情况下，纠正某些手机可能是XMLEntityEncode的情况
        return res;
    }
	protected static final String getStringByMalformItem(HttpServletRequest request,String name) throws Exception
	{
		HashMap hm = (HashMap)request.getAttribute(REQATT_MALFORM_POST_ITEM);
		if(hm==null) return null;
		String res = (String)hm.get(name);
		if(res!=null && res.length()>0) res=CommCodec.XMLEntityDecode(res);//wap用的情况下，纠正某些手机可能是XMLEntityEncode的情况
		return res;
	}
	protected static final String getStringByMalformItem(HttpServletRequest request,String name,String src_encoding,String target_encoding) throws Exception
	{
		HashMap hm = (HashMap)request.getAttribute(REQATT_MALFORM_POST_ITEM);
		if(hm==null) return null;
		String res = (String)hm.get(name);
		if(res!=null && res.length()>0) 
		{
			res = new String(res.getBytes(src_encoding),target_encoding);
			res=CommCodec.XMLEntityDecode(res);//wap用的情况下，纠正某些手机可能是XMLEntityEncode的情况
		}
		return res;
	}
	protected static final String getStringByFormItem(HttpServletRequest request,String name,String src_encoding,String target_encoding) throws Exception
	{//要进行可能的字符集转换
		String res = request.getParameter(name);
		if(res!=null && res.length()>0) 
		{
			res = new String(res.getBytes(src_encoding),target_encoding);
			//wap用的情况下，纠正某些手机可能是XMLEntityEncode的情况
			res = CommCodec.XMLEntityDecode(res);
		}
		return res;
	}
	protected static final String getStringByFileItem(HttpServletRequest request,String name) throws Exception
	{
		String res = null;
		FileItem item = getMultiItem(request,name);
		if(item!=null) res = item.getString();//进行可能的字符集转换
		return res; 
	}
	////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String getParamStr(HttpServletRequest request,String name) throws Exception
	{//返回参数值，如果是null代表没有post这个参数
		String parsed = getParsed(request); 
		if(parsed.equals(PARSE_FORM))
			return getStringByFormItem(request,name);
		else if(parsed.equals(PARSE_MULTI))
			return getStringByFileItem(request,name);
		else
			return getStringByMalformItem(request,name);
	}
	public static final String getParamStrSafe(HttpServletRequest request,String name) throws Exception
	{//返回参数值，如果没有post这个参数，返回""
		String res = getParamStr(request,name);
		return Helper.V(res);
	}
	public static final String getParamStrAssert(HttpServletRequest request,String name) throws Exception
	{//返回参数值，如果没有post这个参数，抛出异常
		String res = getParamStr(request,name);
		if(res==null) Helper.E("get assert pararm failed : " + name);
		return res;
	}
	public static final String getParamStrDefault(HttpServletRequest request,String name,String def) throws Exception
	{//返回参数值，如果没有post这个参数，返回def
		String res = getParamStr(request,name);
		if(res==null) res = def;
		return res;
	}
	public static final String getParamStrEmptyDefault(HttpServletRequest request,String name,String def) throws Exception
	{//返回参数值，如果没有post这个参数，返回def
		String res = getParamStr(request,name);
		if(res==null || res.length()==0) res = def;
		return res;
	}
	
	
	public static final String getMapStr(Map map,String name,String src_encoding,String target_encoding) throws Exception
	{
    	String[] values = (String[])map.get(name);
    	if(values==null || values.length==0 || values[0]==null) return null;
    	String value = values[0]; 
		value = new String(value.getBytes(src_encoding),target_encoding);
		//wap用的情况下，纠正某些手机可能是XMLEntityEncode的情况
		value = CommCodec.XMLEntityDecode(value);
    	return value;
	}
	public static final String getMapStrSafe(Map map,String name,String src_encoding,String target_encoding) throws Exception
	{
    	String value = getMapStr(map,name,src_encoding,target_encoding);
    	if(value==null) value = "";
    	return value;
	}
	public static final String getMapStrAssert(Map map,String name,String src_encoding,String target_encoding) throws Exception
	{
    	String value = getMapStr(map,name,src_encoding,target_encoding);
    	if(value==null) Helper.E("get assert map pararm failed : " + name);
    	return value;
	}
	public static final String getMapStrDefault(Map map,String name,String def,String src_encoding,String target_encoding) throws Exception
	{
    	String value = getMapStr(map,name,src_encoding,target_encoding);
    	if(value==null) value = def;
    	return value;
	}
	public static final String getMapStrEmptyDefault(Map map,String name,String def,String src_encoding,String target_encoding) throws Exception
	{
    	String value = getMapStr(map,name,src_encoding,target_encoding);
    	if(value==null || value.length()==0) value = def;
    	return value;
	}
	
	
	public static final String getParamStr(HttpServletRequest request,String name,String src_encoding,String target_encoding) throws Exception
	{//返回参数值，如果是null代表没有post这个参数
		String parsed = getParsed(request);  
		if(parsed.equals(PARSE_FORM))
			return getStringByFormItem(request,name,src_encoding,target_encoding);
		else if(parsed.equals(PARSE_MULTI))
			return getStringByFileItem(request,name);
		else
			return getStringByMalformItem(request,name,src_encoding,target_encoding);
	}
	public static final String getParamStrSafe(HttpServletRequest request,String name,String src_encoding,String target_encoding) throws Exception
	{//返回参数值，如果没有post这个参数，返回""
		String res = getParamStr(request,name,src_encoding,target_encoding);
		return Helper.V(res);
	}
	public static final String getParamStrAssert(HttpServletRequest request,String name,String src_encoding,String target_encoding) throws Exception
	{//返回参数值，如果没有post这个参数，抛出异常
		String res = getParamStr(request,name,src_encoding,target_encoding);
		if(res==null) Helper.E("get assert pararm failed : " + name);
		return res;
	}
	public static final String getParamStrDefault(HttpServletRequest request,String name,String def,String src_encoding,String target_encoding) throws Exception
	{//返回参数值，如果没有post这个参数，返回def
		String res = getParamStr(request,name,src_encoding,target_encoding);
		if(res==null) res = def;
		return res;
	}
	public static final String getParamStrEmptyDefault(HttpServletRequest request,String name,String def,String src_encoding,String target_encoding) throws Exception
	{//返回参数值，如果没有post这个参数，返回def
		String res = getParamStr(request,name,src_encoding,target_encoding);
		if(res==null || res.length()==0) res = def;
		return res;
	}
	
	
	
	
	

	public static final Integer getParamInt(HttpServletRequest request,String name) throws Exception
	{//返回参数值，如果没有post这个参数或有post但不是int，返回null
		Integer i = null;
		String res = null;
		String parsed = getParsed(request); 
		if(parsed.equals(PARSE_FORM))
			res = getStringByFormItem(request,name);
		else if(parsed.equals(PARSE_MULTI))	
			res = getStringByFileItem(request,name);
		else
			res = getStringByMalformItem(request,name);
		if(res!=null)
		{
			try
			{
				i = new Integer(Integer.parseInt(res));
			}
			catch(Exception e){}
		}
		return i;
	}
	public static final int getParamIntAssert(HttpServletRequest request,String name) throws Exception
	{//返回参数值，如果没有post这个参数，抛出异常
		Integer res = getParamInt(request,name);
		if(res==null) Helper.E("int param not post : " + name);
		return res.intValue();
	}
	public static final int getParamIntSafe(HttpServletRequest request,String name) throws Exception
	{//返回参数值，如果没有post这个参数，返回0
		Integer res = getParamInt(request,name);
		if(res==null) res = new Integer(0);
		return res.intValue();
	}
	public static final int getParamIntDefault(HttpServletRequest request,String name,int def) throws Exception
	{//返回参数值，如果没有post这个参数，返回def
		Integer res = getParamInt(request,name);
		if(res==null) res = new Integer(def);
		return res.intValue();
	}

	public static HashMap<String,String> getParamsByQueryStr(String query_str) throws Exception
	{//分析xxxx=xxx&xxxx=xxx结构字符串返回utf-8,decode后的hashmap,注意这个实现没有考虑同名变量的问题
		HashMap<String,String> res = new HashMap<String,String>();
		if(query_str==null) return res;
		for(String str : query_str.split("&"))
		{
			int i = str.indexOf('='); 
			if(i<0) res.put(WebHelper.urlDecode(str),"");
			else res.put(WebHelper.urlDecode(str.substring(0,i)),WebHelper.urlDecode(str.substring(i+1)));
		}
		return res;
	}
	
	
    public static Double getParamDouble(HttpServletRequest request,String name) throws Exception {
        Double d = null;
        String res = null;
        String parsed = getParsed(request);
        if (parsed.equals(PARSE_FORM))
            res = getStringByFormItem(request, name);
        else if (parsed.equals(PARSE_MULTI))
            res = getStringByFileItem(request, name);
        else
            res = getStringByMalformItem(request, name);
        if (res != null) {
            try {
                d = Double.parseDouble(res);
            }
            catch (Exception e) { }
        }
        return d;
	}
    public static double getParamDoubleAssert(HttpServletRequest request,String name) throws Exception {
		Double res = getParamDouble(request,name);
		if(res==null) Helper.E("double param not post : " + name);
		return res.doubleValue();
	}
	public static double getParamDoubleSafe(HttpServletRequest request,String name) throws Exception {
		Double res = getParamDouble(request,name);
		if(res==null) res = new Double(0);
		return res.doubleValue();
	}
	public static double getParamDoubleDefault(HttpServletRequest request,String name,int def) throws Exception {
		Double res = getParamDouble(request,name);
		if(res==null) res = new Double(def);
		return res.doubleValue();
	}



    public static final byte[] getParamFileContentAssert(HttpServletRequest request,String name) throws Exception
	{
		Helper.Assert(getParsed(request).equals(PARSE_MULTI));
		FileItem item = getMultiItem(request,name);
		Helper.AssertNotNull(item,"item not posted : " + name);
		Helper.Assert(item.isFormField()==false,"not a file field : " + name);
		return Helper.streamRead(item.getInputStream());
	}
	public static final int getParamFileSizeAssert(HttpServletRequest request,String name) throws Exception
	{
		Helper.Assert(getParsed(request).equals(PARSE_MULTI));
		FileItem item = getMultiItem(request,name);
		Helper.AssertNotNull(item,"item not posted : " + name);
		Helper.Assert(item.isFormField()==false,"not a file field : " + name);
		return (int)item.getSize();
	}
	public static final String getParamFileNameAssert(HttpServletRequest request,String name) throws Exception
	{
		Helper.Assert(getParsed(request).equals(PARSE_MULTI));
		FileItem item = getMultiItem(request,name);
		Helper.AssertNotNull(item,"item not posted : " + name);
		Helper.Assert(item.isFormField()==false,"not a file field : " + name);
		String file_name = Helper.V(item.getName());
		int i = file_name.lastIndexOf('\\');
		if(i<0) i = file_name.lastIndexOf('/');
		if(i>=0) file_name = file_name.substring(i+1);
		return file_name;
	}
	public static final String getParamFileTypeAssert(HttpServletRequest request,String name) throws Exception
	{
		Helper.Assert(getParsed(request).equals(PARSE_MULTI));
		FileItem item = getMultiItem(request,name);
		Helper.AssertNotNull(item,"item not posted : " + name);
		Helper.Assert(item.isFormField()==false,"not a file field : " + name);
		return Helper.V(item.getContentType());
	}
	public static final InputStream getParamFileStreamAssert(HttpServletRequest request,String name) throws Exception
	{
		Helper.Assert(getParsed(request).equals(PARSE_MULTI));
		FileItem item = getMultiItem(request,name);
		Helper.AssertNotNull(item,"item not posted : " + name);
		Helper.Assert(item.isFormField()==false,"not a file field : " + name);
		return item.getInputStream();
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////
	public static final String getRequestHostContext(HttpServletRequest request) throws Exception
	{
		return "http://" + getHeaderHostSafe(request) + request.getContextPath();
	}
	public static final String getHeaderXUpCallingLineSafe(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"X-Up-Calling-Line-ID");
	}
	public static final String getHeaderXUpCallingLineAdjustedSafe(HttpServletRequest request) throws Exception
	{
		String line_header = getHeaderXUpCallingLineSafe(request);
		return getLineIDByLineHeaderSafe(line_header);
	}
	public static final String getAdjustedLineIDByXUpCallingLineIDOrXNetworInfo(HttpServletRequest request) throws Exception
	{
		String line_id = getHeaderXUpCallingLineAdjustedSafe(request);
		if(line_id.length()==0) line_id = getHeaderXNetworkInfoAdjustedLineIDSafe(request);
		return line_id;
	}
	public static final String getHeaderXNetworkInfoAdjustedLineIDSafe(HttpServletRequest request) throws Exception
	{
		String network_info_header = getHeaderXNetworkInfo(request);
		//X-Network-info : GPRS,8615960720377,10.93.251.202,FZGGSN21BNK,unsecured
		String[] toks = network_info_header.split(",");
		if(toks.length>=2)
		{
			return getLineIDByLineHeaderSafe(toks[1]);
		}
		return "";
	}
	public static final String getHeaderXNetworkInfo(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"X-Network-info");
	}
	public static final String getLineIDByLineHeaderSafe(String line_header)
	{
		if(line_header.length()<11 ||Helper.strIsProductionOf(line_header,"+0123456789")==false) return "";
		return line_header.substring(line_header.length()-11);
	}
	public static final String getHeaderViaSafe(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"Via");
	}
	public static final String getHeaderXUpBearerTypeSafe(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"X-Up-Bearer-Type");
	}
	public static final String getHeaderXWapProfileSafe(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"X-Wap-Profile");
	}
	public static final String getHeaderAcceptCharsetSafe(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"Accept-Charset");
	}
	public static final String getHeaderRefererSafe(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"Referer");
	}
	public static final String getHeaderXForwardForSafe(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"X-Forwarded-For");
	}
	public static final String getHeaderUserAgentSafe(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"User-Agent");
	}
	public static final String getHeaderAcceptSafe(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"Accept");
	}
	public static final String getHeaderHostSafe(HttpServletRequest request) throws Exception
	{
		return getHeaderStrSafe(request,"Host");
	}
	public static final String getHeaderHostNameSafe(HttpServletRequest request) throws Exception
	{
		String host_name = getHeaderHostSafe(request);
		int i = host_name.lastIndexOf(':');
		if(i>=0) host_name = host_name.substring(0,i);
		return host_name;
	}
	public static final String getHeaderStr(HttpServletRequest request,String name) throws Exception
	{
		return request.getHeader(name);
	}
	public static final String getHeaderStrSafe(HttpServletRequest request,String name) throws Exception
	{
		String res = getHeaderStr(request,name);
		res = Helper.V(res);
		return res;
	}
	public static final String getHeaderStrAssert(HttpServletRequest request,String name) throws Exception
	{
		String res = getHeaderStr(request,name);
		if(res==null) Helper.E("get header assert failed : " + name);
		return res;
	}
	public static final String getHeaderStrDefault(HttpServletRequest request,String name,String def) throws Exception
	{
		String res = getHeaderStr(request,name);
		if(res==null) res = def;
		return res;
	}
	public static final String getHeaderStrEmptyDefault(HttpServletRequest request,String name,String def) throws Exception
	{
		String res = getHeaderStr(request,name);
		if(res==null || res.length()==0) res = def;
		return res;
	}
	
	
//	public static final String[] getParamStrs(HttpServletRequest request,String name) throws Exception
//	{
//		String[] res = request.getParameterValues(name);
//		return res;
//	}

    public static final String[] getParamStrs(HttpServletRequest request,String name) throws Exception
	{//返回参数值，如果是null代表没有post这个参数
		String parsed = getParsed(request);
		if(parsed.equals(PARSE_FORM))
			return getStringsByFormItem(request,name);
		else if(parsed.equals(PARSE_MULTI))
			return request.getParameterValues(name);
		else
			return request.getParameterValues(name);
	}

    public static final String[] getParamStrsSafe(HttpServletRequest request,String name) throws Exception
	{
		String[] res = getParamStrs(request,name);
		if(res==null) res = new String[0];
		return res;
	}
	public static final String[] getParamStrsAssert(HttpServletRequest request,String name) throws Exception
	{
		String[] res = getParamStrs(request,name);
		if(res==null) Helper.E("invalid post parameters " + name);
		return res;
	}
	public static final String[] getParamStrsDefault(HttpServletRequest request,String name,String[] def) throws Exception
	{
		String[] res = getParamStrs(request,name);
		if(res==null) Helper.E("invalid post parameters " + name);
		return res;
	}
	public static final int[] getParamInts(HttpServletRequest request,String name) throws Exception
	{
		String[] res = getParamStrs(request,name);
		if(res==null) return null;
		int[] res2 = new int[res.length];
		for(int i=0;i<res.length;i++)
		{
			try{res2[i] = Integer.parseInt(res[i]);}
			catch(Exception e){res2[i]=0;}
		}
		return res2;
	}
	public static final int[] getParamIntsSafe(HttpServletRequest request,String name) throws Exception
	{
		int[] res = getParamInts(request,name);
		if(res==null) res = new int[0];
		return res;
	}
	public static final int[] getParamIntsDefault(HttpServletRequest request,String name,int[] def) throws Exception
	{
		int[] res = getParamInts(request,name);
		if(res==null) res = def;
		return res;
	}
	
	public static final Integer getHeaderInt(HttpServletRequest request,String name) throws Exception
	{
		Integer i = null;
		String res = request.getHeader(name);
		if(res!=null)
		{
			try
			{
				i = new Integer(Integer.parseInt(res));
			}
			catch(Exception e){}
		}
		return i;
	}
	public static final int getHeaderIntSafe(HttpServletRequest request,String name) throws Exception
	{
		Integer res = getHeaderInt(request,name);
		if(res==null) res = new Integer(0);
		return res.intValue();
	}
	public static final int getHeaderIntAssert(HttpServletRequest request,String name) throws Exception
	{
		Integer res = getHeaderInt(request,name);
		if(res==null) Helper.E("get int header assert failed : " + name);
		return res.intValue();
	}
	public static final int getHeaderIntDefault(HttpServletRequest request,String name,int def) throws Exception
	{
		Integer res = getHeaderInt(request,name);
		if(res==null) res = new Integer(def);
		return res.intValue();
	}

	////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String getSessionStr(HttpSession session,String name) throws Exception
	{
		return (String)session.getAttribute(name);
	}
	public static final String getSessionStrSafe(HttpSession session,String name) throws Exception
	{
		String res = getSessionStr(session,name);
		res = Helper.V(res);
		return res;
	}
	public static final String getSessionStrAssert(HttpSession session,String name) throws Exception
	{
		String res = getSessionStr(session,name);
		if(res==null) Helper.E("get sess att assert failed : " + name);
		return res;
	}
	public static final String getSessionStrDefault(HttpSession session,String name,String def) throws Exception
	{
		String res = getSessionStr(session,name);
		if(res==null) res = def;
		return res;
	}
	public static final String getSessionStrEmptyDefault(HttpSession session,String name,String def) throws Exception
	{
		String res = getSessionStr(session,name);
		if(res==null || res.length()==0) res = def;
		return res;
	}
	
	public static final Integer getSessionInt(HttpSession session,String name) throws Exception
	{
		Integer i = null;
		String res = (String)session.getAttribute(name);
		if(res!=null)
		{
			try
			{
				i = new Integer(Integer.parseInt(res));
			}
			catch(Exception e){}
		}
		return i;
	}
	public static final int getSessionIntSafe(HttpSession session,String name) throws Exception
	{
		Integer res = getSessionInt(session,name);
		if(res==null) res = new Integer(0);
		return res.intValue();
	}
	public static final int getSessionIntAssert(HttpSession session,String name) throws Exception
	{
		Integer res = getSessionInt(session,name);
		if(res==null) Helper.E("get int header assert failed : " + name);
		return res.intValue();
	}
	public static final int getSessionIntDefault(HttpSession session,String name,int def) throws Exception
	{
		Integer res = getSessionInt(session,name);
		if(res==null) res = new Integer(def);
		return res.intValue();
	}
	
	
	///////////////////////////////////////////////////////////////////////////////////////////////////
	public static final void delCookie(HttpServletResponse response,String name)
	{
		Cookie cookie = new Cookie(name,"");
		cookie.setMaxAge(0);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	public static final void setCookieStrPermanent(HttpServletResponse response,String name,String value)
	{
		Cookie cookie = new Cookie(name,value);
		cookie.setMaxAge(60*60*24*30*3);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	public static final void setCookieStrTemporarily(HttpServletResponse response,String name,String value)
	{
		Cookie cookie = new Cookie(name,value);
		cookie.setMaxAge(-1);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	public static final void setCookieIntPermanent(HttpServletResponse response,String name,int value)
	{
		Cookie cookie = new Cookie(name,String.valueOf(value));
		cookie.setMaxAge(65535);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	public static final void setCookieIntTemporarily(HttpServletResponse response,String name,int value)
	{
		Cookie cookie = new Cookie(name,String.valueOf(value));
		cookie.setMaxAge(-1);
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	public static final String getCookieStr(HttpServletRequest request,String name) throws Exception
	{
		Cookie[] cookies = request.getCookies();
		if(cookies==null) return null;
		for(int i=0;i<cookies.length;i++)
		{
			Cookie cookie = cookies[i];
			if(cookie.getName().equals(name))
				return Helper.V(cookie.getValue());
		}
		return null;
	}
	public static final String getCookieStrSafe(HttpServletRequest request,String name) throws Exception
	{
		String res = getCookieStr(request,name);
		if(res==null) res = "";
		return res;
	}
	public static final String getCookieStrAssert(HttpServletRequest request,String name) throws Exception
	{
		String res = getCookieStr(request,name);
		if(res==null) Helper.E("get cookie assert failed : " + name);
		return res;		
	}
	public static final String getCookieStrDefault(HttpServletRequest request,String name,String def) throws Exception
	{
		String res = getCookieStr(request,name);
		if(res==null) res = def;
		return res;		
	}
	
	public static final Integer getCookieInt(HttpServletRequest request,String name) throws Exception
	{
		Integer i = null;
		String res = getCookieStr(request,name);
		if(res!=null)
		{
			try
			{
				i = new Integer(Integer.parseInt(res));
			}
			catch(Exception e){}
		}
		return i;
	}
	public static final int getCookieIntSafe(HttpServletRequest request,String name) throws Exception
	{
		Integer i = getCookieInt(request,name);
		if(i==null) i = new Integer(0);
		return i.intValue();
	}
	public static final int getCookieIntAssert(HttpServletRequest request,String name) throws Exception
	{
		Integer i = getCookieInt(request,name);
		if(i==null) Helper.E("get cookie int assert failed : " + name);
		return i.intValue();
	}
	public static final int getCookieIntDefault(HttpServletRequest request,String name,int def) throws Exception
	{
		Integer i = getCookieInt(request,name);
		if(i==null) i = new Integer(def);
		return i.intValue();
	}
	
	
	////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static final String getPersistStrAssert(HttpServletRequest request,String name,String sess_name) throws Exception
	{
		String res = getParamStr(request,name);
		HttpSession session = request.getSession();
		String att_name = sess_name + '.' + name; 
		if(res==null)
		{//没有投递，读取sess
			res = getSessionStr(session,att_name);
			if(res==null) 
				Helper.E("getPersistAssert failed ");
		}
		else
			session.setAttribute(att_name,res);
		return res;
	}
	public static final String getPersistStrSafe(HttpServletRequest request,String name,String sess_name) throws Exception
	{
		String res = getParamStr(request,name);
		HttpSession session = request.getSession();
		String att_name = sess_name + '.' + name; 
		if(res==null)
		{//没有投递，读取sess
			res = getSessionStr(session,att_name);
			if(res==null) res = "";
		}
		else
			session.setAttribute(att_name,res);
		return res;
	}
	public static final String getPersistStrDefault(HttpServletRequest request,String name,String sess_name,String def) throws Exception
	{
		String res = getParamStr(request,name);
		HttpSession session = request.getSession();
		String att_name = sess_name + '.' + name; 
		if(res==null)
		{//没有投递，读取sess
			res = getSessionStr(session,att_name);
			if(res==null)
			{
				res = def;
				session.setAttribute(att_name,res);
			}
		}
		else
			session.setAttribute(att_name,res);
		return res;
	}
	public static final String getPersistStrEmptyDefault(HttpServletRequest request,String name,String sess_name,String def) throws Exception
	{
		String res = getParamStr(request,name);
		HttpSession session = request.getSession();
		String att_name = sess_name + '.' + name; 
		if(res==null || res.length()==0)
		{//没有投递，读取sess
			res = getSessionStr(session,att_name);
			if(res==null || res.length()==0)
			{
				res = def;
			}
		}
		session.setAttribute(att_name,res);
		return res;
	}

	
	public static final int getPersistIntAssert(HttpServletRequest request,String name,String sess_name) throws Exception
	{
		String res = getParamStr(request,name);
		HttpSession session = request.getSession();
		String att_name = sess_name + '.' + name; 
		if(res==null)
		{//没有投递，读取sess
			res = getSessionStr(session,att_name);
			Helper.AssertNotEmpty(res,"getPersistAssert failed ");
		}
		try
		{
			int v = Integer.parseInt(res);
			session.setAttribute(att_name,res);
			return v;
		}
		catch(Exception e){Helper.E("invalid number format : " + res);return 0;}
	}
	public static final int getPersistIntSafe(HttpServletRequest request,String name,String sess_name) throws Exception
	{
		String res = getParamStr(request,name);
		HttpSession session = request.getSession();
		String att_name = sess_name + '.' + name; 
		if(res==null)
		{//没有投递，读取sess
			res = getSessionStr(session,att_name);
			if(res==null) res = "";
		}
		int v = 0;
		try{v = Integer.parseInt(res);}
		catch(Exception e){}
		session.setAttribute(att_name,String.valueOf(v));
		return v;
	}
	public static final int getPersistIntDefault(HttpServletRequest request,String name,String sess_name,int def) throws Exception
	{
		String res = getParamStr(request,name);
		HttpSession session = request.getSession();
		String att_name = sess_name + '.' + name; 
		if(res==null)
		{//没有投递，读取sess
			res = getSessionStr(session,att_name);
			if(res==null) res = String.valueOf(def);
		}
		int v = def;
		try{v = Integer.parseInt(res);}
		catch(Exception e){}
		session.setAttribute(att_name,String.valueOf(v));
		return v;
	}
	
	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
	
	public static final String escape(String str)
	{//escape <,>
		if(str==null || str.length()==1)
			return "";
		StringBuffer sb = new StringBuffer();
		int length = str.length();
		for(int i=0; i<length; i++)
		{
			char ch = str.charAt(i);
			if(ch=='<')sb.append("&lt;");
			else if(ch=='>')sb.append("&gt;");
			else sb.append(str.charAt(i));
		}
		return sb.toString();
	}
	
	 public static String createImageQueryString(){
	        StringBuffer sb = new StringBuffer();
	        SimpleDateFormat seqence=new SimpleDateFormat("MMddHHmm");
	        char[] host = {'C','N','.','M','B','O','O','K'};
	        String encode = seqence.format(new Date());
	        char[] cs = encode.toCharArray();
	        for(int index = 0;index < encode.length();index++){
	            cs[index] = (char)(cs[index] - 48+host[index]);
	        }
	        sb.append(cs);
	        return sb.toString();
	 }

	 public static String encodeImageQueryString(String src){
	        byte[] buff = src.getBytes();       
	        char[] result = new char[buff.length * 2];
	        int ri = 0;
	        int bi = 0;
	        for(;ri < result.length ;ri += 2, bi++){
	            result[ri] = (char) (((buff[bi] >> 4)  & 0x0F) + 97);
	            result[ri+1] = (char) ((buff[bi] & 0x0F) + 97);
	        }       
	        return new String(result);
	 }
	 
	 public static String getImageValidateString() {
		 return encodeImageQueryString(createImageQueryString());
	 }

	public static void main(String[] args) throws Exception{
		long times = new Date().getTime();
		System.out.println(htmlEncode("resgersg<br/>sergesrg"));
		long times2 = new Date().getTime();
		System.out.println(times2 - times);
		System.out.println(getRouteIp("http://192.168.1.1:80/userRpm/StatusRpm.htm?Connect=连 接&wan=1","admin","admin"));
	}
}
