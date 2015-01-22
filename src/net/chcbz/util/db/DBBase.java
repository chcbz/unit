package net.chcbz.util.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DBBase {
	private static DBBase db;
	private Connection conn;
	private Statement stmt;
	private ResultSet rs;
	
	private String driver;
	private String url;
	private String user;
	private String password;
	
	private void getConnection(){
		try {
			Class.forName(this.driver);
			conn = DriverManager.getConnection(this.url,this.user,this.password);
			conn.setAutoCommit(false);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch(SQLException e1){
			e1.printStackTrace();
		}
	}
	
	public void close(){
		if(rs  !=  null){  
		      try{
		        rs.close(); 
		        rs = null; 
		      }catch(Exception  e){}  
		  }  
		  if(stmt  !=  null){  
		      try{ 
		          stmt.close(); 
		          stmt = null; 
		      }catch(Exception  e){}  
		  }  
		  if(conn  !=  null){
		      try{ 
		    	conn.commit();
		        conn.close(); 
		        conn = null; 
		      }catch(Exception  e){}  
		  }
	}
	
	public static DBBase instance(String driver,String url,String user,String password){
		db = new DBBase();
		db.driver = driver;
		db.url = url;
		db.user = user;
		db.password = password;
		db.getConnection();
		return db;
	}
	
	public ResultSet query(String sql){
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return rs;
	}
	public void update(String sql){
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public void delete(String sql){
		try {
			stmt = conn.createStatement();
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	public boolean insert(String sql){
		int i = 0;
		try {
			stmt = conn.createStatement();
			i = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(i!=1){
			return false;
		}
		return true;
	}
	public int excuteDelete(String sql){
		int count = 0;
		try {
			stmt = conn.createStatement();
			count = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}
	public int exectueUpdate(String sql){
		int count = 0 ;
		try {
			stmt = conn.createStatement();
			count = stmt.executeUpdate(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return count;
	}
}
