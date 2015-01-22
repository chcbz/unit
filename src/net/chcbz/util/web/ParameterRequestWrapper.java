package net.chcbz.util.web;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

@SuppressWarnings("unchecked")
public class ParameterRequestWrapper extends HttpServletRequestWrapper {
	private Map params;
	private ServletInputStream sis;
	private int len;

	public ParameterRequestWrapper(HttpServletRequest request, Map newParams) {
		super(request);
		try {
			this.sis = request.getInputStream();
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.params = newParams;
	}
	
	public ParameterRequestWrapper(HttpServletRequest request, ServletInputStream newSis){
		super( request );
		this.sis = newSis;
		this.params = new HashMap();
	}

	public Map getParameterMap() {
		return params;
	}

	public Enumeration getParameterNames() {
		Vector l = new Vector(params.keySet());
		return l.elements();
	}

	public String[] getParameterValues(String name) {
		Object v = params.get(name);
		if (v == null) {
			return null;
		} else if (v instanceof String[]) {
			return (String[]) v;
		} else if (v instanceof String) {
			return new String[] { (String) v };
		} else {
			return new String[] { v.toString() };
		}
	}

	public String getParameter(String name) {
		Object v = params.get(name);
		if (v == null) {
			return null;
		} else if (v instanceof String[]) {
			String[] strArr = (String[]) v;
			if (strArr.length > 0) {
				return strArr[0];
			} else {
				return null;
			}
		} else if (v instanceof String) {
			return (String) v;
		} else {
			return v.toString();
		}
	}
	
	public String getQueryString(){
		String queryString = "";
		Set<String> set = params.keySet();
		Iterator<String> iterator = set.iterator();
		for(int i=0;i<set.size();i++){
			String name = iterator.next();
			Object v = params.get(name);
			if(v == null){
				break;
			}else if(v instanceof String[]){
				String[] value = (String[])v;
				queryString += name+"=";
				for(int j=0;j<value.length;j++){ 
					queryString += value[j];
					if(j!=value.length-1){
						queryString += ",";
					}
				}
			}else if(v instanceof String){
				queryString += name+"="+(String)v;
			}
			if(i!=set.size()-1){
				queryString += "&";
			}
		}
		return queryString;
	}
	
	public ServletInputStream getInputStream() throws IOException{
		return sis;
	}
	
	public int getContentLength(){
		return len;
	}
	
	public void setContentLength(int length){
		this.len = length;
	}
}
