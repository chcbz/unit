package net.chcbz.util.db;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.chcbz.util.Helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DBDao{
	private static final Logger logger = LoggerFactory.getLogger(DBDao.class);
	
	private DBBase db;
	private String driver;
	private String url;
	private String user;
	private String password;
	
	public static DBDao instance(String driver,String url,String user,String password){
		DBDao dd = new DBDao();
		dd.driver = driver;
		dd.url = url;
		dd.user = user;
		dd.password = password;
		return dd;
	}
	
	public final void close() {
		db.close();
	}

	public void saveOrUpdate(String tableName,Map<String,String> map){
		String columnName;
		db = DBBase.instance(driver,url,user,password);
		ResultSet rs = db.query("select COLUMN_NAME from information_schema.columns where table_name='"+tableName+"'"); 
		Map<String,String> realMap = new TreeMap<String,String>();
		try {
			while(rs.next()){
				columnName = rs.getString(1);
				if(map.get(columnName)!=null){
					realMap.put(columnName, map.get(columnName));
				}
			}
			map = realMap;
		} catch (SQLException e) {
			logger.error("SQLException",e);
		}
		if (map != null) {
			Iterator<String> iterator;
			String state = "save";
			iterator = map.keySet().iterator();
			Integer id = 0;

			for (int i = 0; i < map.size(); i++) {
				columnName = iterator.next();
				if (columnName.equals("id")) {
					if(Helper.isNumeric(map.get(columnName))){
						state = "update";
						id = Integer.valueOf(map.get(columnName).toString());
					}else{
						map.remove(columnName);
					}
					break;
				}
			}

			StringBuffer sql;
			if (state.equals("save")) {
				sql = new StringBuffer();
				sql.append("insert into " + tableName + "(");

				iterator = map.keySet().iterator();
				for (int i = 0; i < map.size(); i++) {
					if(i==map.size()-1){
						sql.append(iterator.next().toString() + ")");
					}else{
						sql.append(iterator.next().toString() + ",");
					}
				}
				sql.append(" values (");

				iterator = map.keySet().iterator();
				for (int i = 0; i < map.size(); i++) {
					String key = iterator.next().toString();
					if(i==map.size()-1){
						sql.append("'" + map.get(key).replaceAll("'", "&#39;") + "')");
					}else{
						sql.append("'" + map.get(key).replaceAll("'", "&#39;") + "',");
					}
				}
//				System.out.println(sql.toString());
				db.insert(sql.toString());
			} else {
				iterator = map.keySet().iterator();
				/*
				 * String sql1; for(int i=0;i<map.size();i++){ columnName =
				 * iterator.next().toString(); sql1 =
				 * "update "+tableName+" set "
				 * +columnName+"='"+map.get(columnName)+"' where id="+id;
				 * DBBase.instance().update(sql1); }
				 */
				sql = new StringBuffer();
				sql.append("update " + tableName + " set ");
				iterator = map.keySet().iterator();
				for (int i = 0; i < map.size(); i++) {
					columnName = iterator.next().toString();
					if (!columnName.equals("id")) {
						if(i==map.size()-1){
							sql.append(columnName + "='" + map.get(columnName).replaceAll("'", "&#39;")	+ "'");
						}else{
							sql.append(columnName + "='" + map.get(columnName).replaceAll("'", "&#39;")	+ "',");
						}
					}
				}
				sql.append(" where id=" + id);
				logger.info(sql.toString());
//				System.out.println(sql.toString());
				db.update(sql.toString());
			}
		}else{
			logger.error("数据表 "+tableName+" 不存在!");
		}
		db.close();
	}
	
	public List<Map<String,String>> findByExample(String tableName, Map<String,String> example){
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		List<String> columnNames = getColumnNames(tableName);
		
		Map<String,String> map = new TreeMap<String,String>();
		StringBuffer sql = new StringBuffer();
		
		if(example!=null && columnNames.size()!=0){
			sql.append("select * from "+tableName+" where ");
			Iterator<String> iterator = example.keySet().iterator();
			for(int i=0;i<example.size();i++){
				String key = iterator.next();
//				System.out.println(key);
				for(int j=0;j<columnNames.size();j++){
					if(key.equals(columnNames.get(j))){
						if(example.get(key)==null){
							sql.append(key+" is null and ");
						}else{
							sql.append(key+"='"+example.get(key)+"' and ");
						}
						break;
					}
				}
			}
			sql = sql.delete(sql.length()-5, sql.length());
//			System.out.println(sql.toString());
			db = DBBase.instance(driver,url,user,password);
			ResultSet rs = db.query(sql.toString());
			
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				while(rs.next()){
					map = new TreeMap<String,String>();
					for(int i=1;i<=rsmd.getColumnCount();i++){
						map.put(rsmd.getColumnName(i),rs.getString(i));
					}
					list.add(map);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			db.close();
		}
		return list;
	}

	public void delete(String tableName,Integer id){
		db = DBBase.instance(driver,url,user,password);
		ResultSet rs = db.query("show tables");
		try {
			while(rs.next()){
				if(rs.getString(1).equals(tableName)){
					String sql = "delete from "+tableName+" where id="+id;
					db.delete(sql);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
	}
	
	public Map<String,String> findById(String tableName,Integer id){
		db = DBBase.instance(driver,url,user,password);
		ResultSet rs = db.query("show tables");
		try {
			while(rs.next()){
				if(rs.getString(1).equals(tableName)){
					Map<String,String> map = new TreeMap<String,String>();
					String sql = "select * from "+tableName+" where id="+id;
//					db = DBBase.instance();
					ResultSet rs1 = db.query(sql);
					
					try {
						ResultSetMetaData rsmd = rs1.getMetaData();
						if(rs1.next()){
							for(int i=1;i<=rsmd.getColumnCount();i++){
								map.put(rsmd.getColumnName(i),rs1.getString(i));
							}
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					db.close();
					return map;
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		db.close();
		return null;
	}

	/**
	 * 根据属性查找符合条件的数据,并按orderBy排列,且实现分页
	 * @param tableName 表名
	 * @param example 共通属性
	 * @param orderBy 排列根据值
	 * @param isDesc 是否倒序
	 * @param currentPage 当前页码
	 * @param pageSize 每页显示数量
	 * @return
	 */
	public List<Map<String,String>> findByExampleOrder(String tableName, Map<String,String> example, String orderBy, boolean isDesc, Integer currentPage, Integer pageSize){
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		List<String> columnNames = getColumnNames(tableName);
		if(columnNames.size()!=0){
			boolean orderAvail = false;     //order属性是否存在
			for(int i=0;i<columnNames.size();i++){
//				System.out.println(orderBy+"="+columnNames.get(i));
				if(orderBy.equals(columnNames.get(i))){
					orderAvail = true;
					break;
				}
			}
			if(orderBy==null || orderBy.equals("") || !orderAvail){
				orderBy = columnNames.get(0);
			}
			String desc = "";
			if(isDesc){
				desc = "desc";
			}
			
			Map<String,String> map = new TreeMap<String,String>();
			StringBuffer sql = new StringBuffer();
			sql.append("select * from "+tableName);
			Integer firstS = (currentPage-1)*pageSize;
			
			if(example!=null){
				sql.append(" where ");
				Iterator<String> iterator = example.keySet().iterator();
				for(int i=0;i<example.size();i++){
					String key = iterator.next();
//					System.out.println(key);
					for(int j=0;j<columnNames.size();j++){
						if(key.equals(columnNames.get(j))){
							if(example.get(key)==null){
								sql.append(key+" is null and ");
							}else{
								sql.append(key+"='"+example.get(key)+"' and ");
							}
							break;
						}
					}
				}
				
				sql.append(orderBy);
				
				if(isDesc){
					sql.append("<");
				}else{
					sql.append(">");
				}
				sql.append("=(select "+orderBy+" from "+tableName+" where");
			
				iterator = example.keySet().iterator();
				for(int i=0;i<example.size();i++){
					String key = iterator.next();
//					System.out.println(key);
					for(int j=0;j<columnNames.size();j++){
						if(key.equals(columnNames.get(j))){
							if(example.get(key)==null){
								sql.append(" "+key+" is null and ");
							}else{
								sql.append(" "+key+"='"+example.get(key)+"' and ");
							}
							break;
						}
					}
				}
				sql = sql.delete(sql.length()-5, sql.length());
				sql.append(" order by "+orderBy+" "+desc);
				sql.append(" limit "+firstS+",1) order by "+orderBy+" "+desc+" limit "+pageSize);
			}else{
				sql.append(" order by "+orderBy+" "+desc+" limit "+firstS+","+pageSize);
			}
			
			logger.info(sql.toString());
//			System.out.println(sql.toString());
			db = DBBase.instance(driver,url,user,password);
			ResultSet rs = db.query(sql.toString());
			
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				while(rs.next()){
					map = new TreeMap<String,String>();
					for(int i=1;i<=rsmd.getColumnCount();i++){
						map.put(rsmd.getColumnName(i),rs.getString(i));
					}
					list.add(map);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			db.close();
			return list;
		}else{
			logger.error("数据表 "+tableName+" 不存在");
			return null;
		}
	}
	
	/**
	 * 根据属性查找符合条件的数据,并按orderBy排列
	 * @param tableName 表名
	 * @param example 共通属性
	 * @param orderBy 排列根据值
	 * @param isDesc 是否倒序
	 * @return
	 */
	public List<Map<String,String>> findByExampleOrder(String tableName, Map<String,String> example, String orderBy, boolean isDesc){
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		List<String> columnNames = getColumnNames(tableName);
		if(columnNames.size()!=0){
			Map<String,String> map = new TreeMap<String,String>();
			StringBuffer sql = new StringBuffer();
			sql.append("select * from "+tableName);
			
			if(example!=null){
				sql.append(" where ");
				Iterator<String> iterator = example.keySet().iterator();
				for(int i=0;i<example.size();i++){
					String key = iterator.next();
//					System.out.println(key);
					for(int j=0;j<columnNames.size();j++){
						if(key.equals(columnNames.get(j))){
							if(example.get(key)==null){
								sql.append(key+" is null and ");
							}else{
								sql.append(key+"='"+example.get(key)+"' and ");
							}
							break;
						}
					}
				}
				sql = sql.delete(sql.length()-5, sql.length());
			}
			
			boolean orderAvail = false;     //order属性是否存在
			for(int i=0;i<columnNames.size();i++){
//				System.out.println(orderBy+"="+columnNames.get(i));
				if(orderBy.equals(columnNames.get(i))){
					orderAvail = true;
					break;
				}
			}
			if(orderBy==null || orderBy.equals("") || !orderAvail){
				orderBy = columnNames.get(0);
			}
			sql.append(" order by "+orderBy);
			if(isDesc){
				sql.append(" desc");
			}
//			System.out.println(sql.toString());
			db = DBBase.instance(driver,url,user,password);
			ResultSet rs = db.query(sql.toString());
			
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				while(rs.next()){
					map = new TreeMap<String,String>();
					for(int i=1;i<=rsmd.getColumnCount();i++){
						map.put(rsmd.getColumnName(i),rs.getString(i));
					}
					list.add(map);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			db.close();
			return list;
		}else{
			logger.error("数据表 "+tableName+" 不存在");
			return null;
		}
	}
	
	/**
	 * 根据属性查找符合条件的数据,并随机显示
	 * @param tableName 表名
	 * @param example 共通属性
	 * @param randNum 需要随机显示的行数
	 * @return
	 */
	public List<Map<String,String>> findByExampleRand(String tableName, Map<String,String> example, Integer randNum){
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		List<String> columnNames = getColumnNames(tableName);
		if(columnNames.size()!=0){
			Map<String,String> map = new TreeMap<String,String>();
			StringBuffer sql = new StringBuffer();
			sql.append("select * from "+tableName);
			
			if(example!=null){
				sql.append(" where ");
				Iterator<String> iterator = example.keySet().iterator();
				for(int i=0;i<example.size();i++){
					String key = iterator.next();
//					System.out.println(key);
					for(int j=0;j<columnNames.size();j++){
						if(key.equals(columnNames.get(j))){
							if(example.get(key)==null){
								sql.append(key+" is null and ");
							}else{
								sql.append(key+"='"+example.get(key)+"' and ");
							}
							break;
						}
					}
				}
				sql = sql.delete(sql.length()-5, sql.length());
			}
			sql.append(" order by rand()");
			if(randNum!=null && randNum!=0){
				sql.append(" limit "+randNum);
			}
//			System.out.println(sql.toString());
			db = DBBase.instance(driver,url,user,password);
			ResultSet rs = db.query(sql.toString());
			
			try {
				ResultSetMetaData rsmd = rs.getMetaData();
				while(rs.next()){
					map = new TreeMap<String,String>();
					for(int i=1;i<=rsmd.getColumnCount();i++){
						map.put(rsmd.getColumnName(i),rs.getString(i));
					}
					list.add(map);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			db.close();
			return list;
		}else{
			logger.error("数据表 "+tableName+" 不存在");
			return null;
		}
	}
	
	private List<String> getColumnNames(String tableName){
		List<String> columnNames = null;
		db = DBBase.instance(driver,url,user,password);
		ResultSet rss = db.query("select COLUMN_NAME from information_schema.columns where table_name='"+tableName+"'");
		try {
			if(rss.next()){
				columnNames = new ArrayList<String>();
				columnNames.add(rss.getString(1));
			}
			while(rss.next()){
				columnNames.add(rss.getString(1));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
		return columnNames;
	}
	
	/**
	 * 根据属性查找符合条件的数据数
	 * @param tableName 表名
	 * @return
	 */
	public Integer sumPage(String tableName){
		return sumPage(tableName, null);
	}
	
	/**
	 * 根据属性查找符合条件的数据数
	 * @param tableName 表名
	 * @param example 共通属性
	 * @return
	 */
	public Integer sumPage(String tableName, Map<String,String> example){
		int sum = 0;
		List<String> columnNames = getColumnNames(tableName);
		if(columnNames.size()!=0){
			db = DBBase.instance(driver,url,user,password);
			StringBuffer sql = new StringBuffer();
			sql.append("select count(*) from "+tableName);
			if(example!=null){
				sql.append(" where ");
				Iterator<String> iterator = example.keySet().iterator();
				for(int i=0;i<example.size();i++){
					String key = iterator.next();
	//				System.out.println(key);
					for(int j=0;j<columnNames.size();j++){
						if(key.equals(columnNames.get(j))){
							if(example.get(key)==null){
								sql.append(key+" is null and ");
							}else{
								sql.append(key+"='"+example.get(key)+"' and ");
							}
							break;
						}
					}
				}
				sql = sql.delete(sql.length()-5, sql.length());
			}
	//		System.out.println(sql.toString());
			ResultSet rs = db.query(sql.toString());
			
			try {
				if(rs.next()){
					sum = rs.getInt(1);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
			db.close();
			return sum;
		}else{
			logger.error("数据表 "+tableName+" 不存在");
			return null;
		}
	}

	public List<Map<String, String>> findAll(String tableName) {
		return findAll(tableName, 0, 0);
	}
	
	public List<Map<String, String>> findAll(String tableName, Integer currentPage, Integer pageSize) {
		return findAll(tableName, currentPage, pageSize, null, false);
	}

	public List<Map<String, String>> findAll(String tableName,
			Integer currentPage, Integer pageSize,String orderBy, boolean desc) {
		db = DBBase.instance(driver,url,user,password);
		ResultSet rs = db.query("show tables");
		try {
			while(rs.next()){
				if(rs.getString(1).equals(tableName)){
					List<Map<String,String>> list = new ArrayList<Map<String,String>>();
					String sql = "select * from "+tableName;
					if(orderBy!=null){
						sql += " order by "+orderBy;
					}if(desc){
						sql += " desc";
					}
					if(currentPage!=null && currentPage>0 && pageSize!=null && pageSize>0){
						Integer firstS = (currentPage-1)*pageSize;
						sql += " limit "+firstS+","+pageSize;
					}
					ResultSet rs1 = db.query(sql);
					Map<String,String> map;

					ResultSetMetaData rsmd = rs1.getMetaData();
					while(rs1.next()){
						map = new TreeMap<String,String>();
						for(int i=1;i<=rsmd.getColumnCount();i++){
							map.put(rsmd.getColumnName(i),rs1.getString(i));
						}
						list.add(map);
					}
					db.close();
					return list;
				}
			}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		db.close();
		return null;
	}
	
	public List<Map<String,String>> findBySql(String sql){
		db = DBBase.instance(driver,url,user,password);
		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		ResultSet rs1 = db.query(sql);
		Map<String,String> map;

		ResultSetMetaData rsmd;
		try {
			rsmd = rs1.getMetaData();
			while(rs1.next()){
				map = new TreeMap<String,String>();
				for(int i=1;i<=rsmd.getColumnCount();i++){
					map.put(rsmd.getColumnName(i),rs1.getString(i));
				}
				list.add(map);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
		return list;
	}
	
	public Map<String,String> findOneRowBySql(String sql){
		db = DBBase.instance(driver,url,user,password);
		ResultSet rs = db.query(sql);
		Map<String,String> map = null;

		ResultSetMetaData rsmd;
		try {
			rsmd = rs.getMetaData();
			if(rs.next()){
				map = new TreeMap<String,String>();
				for(int i=1;i<=rsmd.getColumnCount();i++){
					map.put(rsmd.getColumnLabel(i),rs.getString(i));
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
		return map;
	}
	
	public void saveBySql(String sql){
		db = DBBase.instance(driver,url,user,password);
		db.update(sql);
		db.close();
	}
	
	public int getCountBySql(String sql) throws Exception{
		int count = 0;
		db = DBBase.instance(driver,url,user,password);
		ResultSet rs = db.query(convert_count_sql(sql));

		try {
			if(rs.next()){
				count = rs.getInt(1);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		db.close();
		return count;
	}
	
	//将普通查询sql转换成为计算返回行数的sql
	protected final static String convert_count_sql(String sql) throws Exception
	{
		//转换成小写
		String ss = sql.toLowerCase();
		
		//根据group by
		int i = ss.indexOf("group by");
		if(i>0)
		{
			return " select COUNT(*) as count from (" + sql + ") as t1 ";
		}
		else
		{
			int j = ss.indexOf(" from ");
			if(j==-1) Helper.E("invalid count sql : " + sql);
			int k = ss.indexOf(" order by ");
			if(k==-1) k = ss.length();
			return " select COUNT(*) as count " + sql.substring(j,k);
		}
	}
	public final static String mysql_varchar_escape(String str)
	{
        if (str == null) str = Helper.V(str);
        str = str.replaceAll("'","''");
		str = str.replaceAll("\\\\","\\\\\\\\");
		return str;  
	}
	public final static String mysql_like_escape(String str)
	{
		str = str.replaceAll("'","''");
		str = str.replaceAll("\\\\","\\\\\\\\");
		str = str.replaceAll("%","\\%");
		str = str.replaceAll("_","\\_");
		return str;
	}
	
	public static void main(String[] args){
		DBDao dbDao = DBDao.instance("com.mysql.jdbc.Driver","jdbc:mysql://127.0.0.1:3306/yijia?useUnicode=true&characterEncoding=utf-8","yijia","admin");
		List<Map<String,String>> list = dbDao.findAll("quotation");
		for(int i=0;i<list.size();i++){
			Map<String,String> map = list.get(i);
			System.out.println("<table>");
			System.out.println("<tr><th>序号</th><th>产品名称</th><th>图片</th><th>型号</th><th>规格W*D*H</th><th>材质说明</th><th>颜色</th><th>数量</th><th>单位</th><th>单价</th><th>总价格</th></tr>");
			System.out.println("<tr><td>"+map.get("id")+"</td><td>"+map.get("name")+"</td><td><img src=\"imagse/"+map.get("img")+"\"/></td><td>"+map.get("model")+"</td><td>"+map.get("standard")+"</td><td>"+map.get("vray")+"</td><td>"+map.get("color")+"</td><td>"+map.get("number")+"</td><td>"+map.get("unit")+"</td><td>"+map.get("price")+"</td><td>"+Integer.valueOf(map.get("number"))*Integer.valueOf(map.get("price"))+"</td></tr>");
			System.out.println("</table>");
		}
	}
}