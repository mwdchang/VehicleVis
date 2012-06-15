package db;

import java.sql.*;
import java.util.HashMap;
import java.util.Vector;

import datastore.SSM;


/* Wrapper for handling data base connection, assumes a single database */
public class DBWrapper {
   
   public static Connection conn = null;
   public static PreparedStatement pstmt = null;
   
   
   // Default constructor
   public DBWrapper() {
   	try {
	   	getConnection();
   	} catch (Exception e) {
   		e.printStackTrace();
   		System.exit(-1);
   	}
   	
   }
   
   public ResultSet execute(String sql) throws Exception {
      return execute(sql, false);   
   }
   
   
   public ResultSet execute(String sql, boolean useStream) throws Exception {
      System.err.print("Executing : " + sql);
      long startTime = System.currentTimeMillis();
      ResultSet rs = null;
      //Connection conn = getConnection();
      pstmt = conn.prepareStatement(sql);
      
      
      // HACK HACK HACK 
      // There seem to be a bug somewhere (JDBC? ) that, when fetching from a 
      // large table, the fetch gets buffered up and never gets released properly, 
      // causing severe memory leaks, setting the fetch size to a small value or 
      // Integer.MIN cause it to stream instead of buffer
      // See : http://benjchristensen.com/2008/05/27/mysql-jdbc-memory-usage-on-large-resultset/
      //
      // The smaller tables we want to buffer, the larger ones we will 
      // hack to use streaming methods
      if (useStream == true)
         pstmt.setFetchSize(Integer.MIN_VALUE);
      
      rs = pstmt.executeQuery();
      long endTime   = System.currentTimeMillis();
      System.err.println("...done (" + (endTime-startTime)+ ")");
      
      return rs;
   }
   
   
   // Return the query result as a Vector of HashMaps, note these are all converted
   // to String objects
   public Vector<HashMap<String, String>> runQuery(String sql) throws Exception {
      System.out.print("\n" + sql + "\n");
      Vector<HashMap<String, String>> map = new Vector<HashMap<String, String>>(); 
      //Connection conn = getConnection();
      PreparedStatement pstmt = conn.prepareStatement(sql);
      
      // Execute and retrieve the metadata
      ResultSet rs = pstmt.executeQuery();
      ResultSetMetaData rsmd = rs.getMetaData();
      
      // As far as I know, this is not standard, so it may be Column_Name for Column_Label
      // or neither, depending what is being retrieved... 
      // For example:
      //     select A as B from ... vs
      //     select A from ...
      // Hopefully this ain't as bad as Sybase ASE ...
      int columnCount = rsmd.getColumnCount();
      Vector<String> columnList = new Vector<String>();
      for (int i=0; i < columnCount; i++) {
         String cname  = rsmd.getColumnName(i+1);     // +1 because it likes to do things differently 
         String clabel = rsmd.getColumnLabel(i+1);    // +1 because it likes to do things differently 
         columnList.add( cname == null ? clabel:cname );
         //System.out.println(columnList.get(i).toString());         
      }
      
      // Loop through the result set
      while (rs != null && rs.next()) {
         HashMap<String, String> row = new HashMap<String, String>();
         for (int i=0; i < columnCount; i++) {
            row.put(columnList.get(i), rs.getString(i+1));
         }
         map.add(row);
      }
      
      // Clean up
      rs.close();
      conn.close();
      
      
      return map;
   }
   
   
   // Retrieves a connection object
   public Connection getConnection(String url, String username, String password) throws Exception {
      Class.forName("com.mysql.jdbc.Driver").newInstance();
      //Connection conn = null;
      try {
         conn = DriverManager.getConnection(url, username, password);   
      } catch (Exception e) {
         // Just pass it up
         e.printStackTrace();
         throw e;
      }
      conn.setTransactionIsolation(1);
      return conn;
   }
   
   public Connection getConnection() throws Exception {
      //return getConnection("jdbc:mysql://localhost/projectv3", "root", "root"); 
      return getConnection("jdbc:mysql://localhost/"+SSM.database, "root", "root"); 
   }
   
   
   // Clean up resources
   public void cleanup() {
      try {
         if (pstmt != null) {
           pstmt.close();
           pstmt = null;
         }
         if (conn != null) {
            conn.close();
            conn = null;
         }
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
   }
}
