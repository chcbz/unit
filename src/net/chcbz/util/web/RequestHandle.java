package net.chcbz.util.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@SuppressWarnings("unchecked")
public class RequestHandle {
	public static boolean hasItem(byte[] bts, String name){
		String str = new String(bts);
		int index = str.indexOf(name);
		if(index!=-1 && str.substring(index-6,index).equals("name=\"") && str.substring(index+name.length(),index+name.length()+1).equals("\"")){
			return true;
		}else{
			return false;
		}
	}
	
	public static ParameterRequestWrapper processRequestAddItem(HttpServletRequest request, Map<String,String> items){
		try{
			ParameterRequestWrapper prw = null;
			if(isMultipartContent(request))
			{//file方式
//				int len = request.getContentLength();
				ServletInputStream is = request.getInputStream();

				int bts;
				ByteArrayOutputStream bais = new ByteArrayOutputStream();
				while((bts=is.read())!=-1){
					bais.write(bts);
				}
				
				/*FileOutputStream fos = new FileOutputStream("D:\\out1.txt");
				int b;
				while((b=is.read())!=-1){
					fos.write(b);
				}
				fos.close();*/
				
//				byte[] btss = multipartAddItems(bais.toByteArray(), items);
				prw = new ParameterRequestWrapper(request,new BufferedServletInputStream(new ByteArrayInputStream(bais.toByteArray())));
				prw.setContentLength(bais.size());
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
				if(request.getContentType()!=null && request.getContentType().length()==0 && request.getMethod().toUpperCase().equals("POST") && request.getParameterMap().size()==0 && request.getContentLength()>0)
				{//malform post content
					/*InputStream is = request.getInputStream();
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
					}*/
				}
				else
				{//form形式，不用容器的param分析器
					HashMap m = new HashMap(request.getParameterMap());
					Iterator<String> iterator = items.keySet().iterator();
					while(iterator.hasNext()){
						String name = iterator.next();
						if(m.get(name)==null){
							m.put(name, items.get(name));
						}
					}
					prw = new ParameterRequestWrapper(request,m); 
				}
			}
			return prw;
		}catch(Exception e){
			return null;
		}
	}
	
	public static byte[] multipartAddItems(byte[] bts, Map<String,String> items){
		Map<String,String> newitems = new HashMap<String,String>();
		Iterator<String> iterator = items.keySet().iterator();
		while(iterator.hasNext()){
			String name = iterator.next();
			if(!hasItem(bts, name)){
				newitems.put(name, items.get(name));
			}
		}
		byte[] byteitem = oneItem(bts);
		Set<String> names = newitems.keySet();
		iterator = names.iterator();
		byte[][] bytes = new byte[names.size()][];
		int len = 0;
		for(int i=0;i<names.size();i++){
			String name = iterator.next();
			bytes[i] = changeItem(byteitem, name, newitems.get(name));
			len += bytes[i].length;
		}
		byte[] byts = new byte[len+bts.length];
		int index = 0;
		for(int i=0;i<bytes.length;i++){
			System.arraycopy(bytes[i], 0, byts, index, bytes[i].length);
			index += bytes[i].length;
		}
		System.arraycopy(bts, 0, byts, index, bts.length);
		return byts;
	}
	
	public static byte[] changeItem(byte[] bts, String name, String value){
		String newstr = new String(bts);
		int index = newstr.indexOf("name=\"")+6;
		String tag1 = newstr.substring(0,index);
		byte[] br = new byte[2];
		br[0] = 13;br[1] = 10;
		return (tag1+name+"\""+new String(br)+new String(br)+value+new String(br)).getBytes();
	}
	
	public static byte[] oneItem(byte[] bts){
		int br = 0;
		int headlen = 0;
		for(int i=0;i<bts.length;i++){
			byte b = bts[i];
			if(b==13 & bts[i+1]==10){
				br++;
			}
			if(br==4){
				headlen = i+2;
				break;
			}
		}
		byte[] newbyte = new byte[headlen];
		for(int i=0;i<headlen;i++){
			newbyte[i] = bts[i];
		}
		return newbyte;
	}
	
	public static final boolean isMultipartContent(HttpServletRequest request) 
	{
		String contentType = request.getContentType();
		if(contentType!=null && contentType.toLowerCase().startsWith("multipart/")) return true; 
        return false;
    }
	
	public static String getCookies(String cookieName, HttpServletRequest request){
		String cookieContent = "";
		Cookie[] cookies = request.getCookies();
		if(cookies!=null){
			for(int i=0;i<cookies.length;i++){
				Cookie cookie = cookies[i];
				if(cookieName.equals(cookie.getName())){
					cookieContent = cookie.getValue();
					break;
				}
			}
		}
		return cookieContent;
	}
}
