package util;

import java.sql.Statement;
import db.DBWrapper;

/////////////////////////////////////////////////////////////////////////////////
// By-pass the ugly DOS prompt interface to load table data
/////////////////////////////////////////////////////////////////////////////////
public class TableLoader {

   
   public static void main(String args[]) throws Exception {
//      SimpleDateFormat abc = new SimpleDateFormat("yyyymmdd");
//      SimpleDateFormat def = new SimpleDateFormat("yyyy/mm/dd");
//      System.out.println( def.format(abc.parse("20010109")));
//      TableLoader.loadData2Table("C:\\Users\\\\Daniel\\\\workspace\\\\projectv3\\\\cmp_clean.txt", "projectv3.cmp_clean", true);  
//      TableLoader.loadData2Table("C:\\Users\\Daniel\\workspace\\projectv3\\opt.txt", "projectv3.cmp_x_grp_clean", true);  
   }
   
   
    
   ////////////////////////////////////////////////////////////////////////////////
   // Load a tab delimited file into a MySQL database table
   //    filename  - path to the data file
   //    tableName - fully qualified table name in <db>.<table> format
   //    truncate  - whether to truncate the data before loading
   ////////////////////////////////////////////////////////////////////////////////
   public static int loadData2Table(String fileName, String tableName, boolean truncate) {
      long start = 0, end = 0;
      
      start = System.currentTimeMillis();
      System.out.println("Loading " + fileName + " to " + tableName);
      try {
         DBWrapper dbh = new DBWrapper();
         Statement stmt = null;
         
         if (truncate == true) {
            System.out.print("Truncating table...");
            stmt = dbh.conn.createStatement();
            stmt.execute("truncate table " + tableName);
            stmt.close();
            System.out.println("Done");
         }
         
         stmt = dbh.conn.createStatement();
         String cmd = "LOAD DATA INFILE " + quote(escape(fileName)) + " INTO TABLE " + tableName;
         stmt.executeUpdate(cmd);
         
      } catch (Exception e) {
         e.printStackTrace();   
         return 1;
      }
      end = System.currentTimeMillis();
      
      System.out.println("Done... (" + (end-start)/1000 + "s)");
      return 0;
   }
   
   
   public static String quote(String s) {
      return "\"" + s + "\"";  
   }
   
   public static String escape(String s) {
      return s.replace("\\", "\\\\");   
   }
   
   
}
