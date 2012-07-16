package exec;

import java.util.Vector;

import datastore.Const;
import datastore.SSM;
import parser.HierarchyParser;
import parser.KeywordParser;
import parser.Normalizer;
import util.TableLoader;


/////////////////////////////////////////////////////////////////////////////////
// This class (re)populates the project's database
// Note there is a dependency among the sections and it is best to run this
// program in its entirety.
//
/////////////////////////////////////////////////////////////////////////////////
public class InitDB {
   
   public static String getTable(String s) {
      return SSM.database + "." + s;   
   }
   
   public static void main(String args[]) {
      HierarchyParser hierarchyParser = new HierarchyParser();
      Normalizer normalizer = new Normalizer();
      KeywordParser keywordParser = new KeywordParser();
      
      int rc = 0;
      
      String fileDir = "C:\\Users\\Daniel\\VehicleVis\\";
      
      
      // Build a white list here - anything outside of the
      // white list will not get parsed into the system
      boolean useReplacement = true;
      Vector<String> whiteList = new Vector<String>();
      whiteList.add("GENERAL MOTORS CORP.");
      whiteList.add("FORD MOTOR COMPANY");
      whiteList.add("DAIMLERCHRYSLER CORPORATION");
      whiteList.add("TOYOTA MOTOR CORPORATION");
      
      /*
      whiteList.add("FORD MOTOR COMPANY");                       
      whiteList.add("DAIMLERCHRYSLER CORPORATION");              
      whiteList.add("TOYOTA MOTOR CORPORATION");                 
      whiteList.add("HONDA (AMERICAN HONDA MOTOR CO.)");         
      whiteList.add("NISSAN NORTH AMERICA, INC.");               
      whiteList.add("VOLKSWAGEN OF AMERICA, INC");               
      whiteList.add("CHRYSLER GROUP LLC");                       
      whiteList.add("HYUNDAI MOTOR COMPANY");                    
      whiteList.add("MAZDA NORTH AMERICAN OPERATIONS");          
      whiteList.add("MITSUBISHI MOTORS NORTH AMERICA, INC.");   
      whiteList.add("CHRYSLER LLC"); 
      */
      
      
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // 0) Create table schema ?
      ////////////////////////////////////////////////////////////////////////////////
      
      ////////////////////////////////////////////////////////////////////////////////
      // 1) Parse and load the part hierarchy file
      ////////////////////////////////////////////////////////////////////////////////
      try {
         System.out.println("\n\nStarting phase 1");
         hierarchyParser.createDBTable();
         System.out.println("\nStarting database load...");
         rc = TableLoader.loadData2Table(fileDir + "part.txt",  InitDB.getTable("grp"), true);   
         if (rc != 0) throw new Exception("Data load failed");
         
         rc = TableLoader.loadData2Table(fileDir + "group.txt", InitDB.getTable("grp_hier"), true);   
         if (rc != 0) throw new Exception("Data load failed");
      } catch (Exception e) {
         e.printStackTrace();   
         System.exit(0);
      }
      
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // 2) Normalize text data records
      ////////////////////////////////////////////////////////////////////////////////
      try {
         System.out.println("\n\nStarting phase 2");
         //normalizer.parse(Const.DATA_FILE, 0, null);   
         normalizer.parse(Const.DATA_FILE, 0, whiteList, useReplacement);   
         
         System.out.println("\nStarting database load...");
         rc = TableLoader.loadData2Table(fileDir + "cmp_clean.txt", InitDB.getTable("cmp_clean"), true);  
         if (rc != 0) throw new Exception("Data load failed");
      } catch (Exception e) {
         e.printStackTrace();   
         System.exit(0);
      }
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // 3) Run keywords and component extractions
      ////////////////////////////////////////////////////////////////////////////////
      try {
         System.out.println("\n\nStarting phase 3");
         keywordParser.parseKeyword();
         
         System.out.println("\nStarting database load...");
         rc = TableLoader.loadData2Table(fileDir + "new_cmp_x_grp.txt", InitDB.getTable("cmp_x_grp"), true);  
         if (rc != 0) throw new Exception("Data load failed");
         rc = TableLoader.loadData2Table(fileDir + "opt.txt", InitDB.getTable("cmp_x_grp_clean"), true);  
         if (rc != 0) throw new Exception("Data load failed");
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // 4) Create optimized cache from existing tables
      ////////////////////////////////////////////////////////////////////////////////
      
   }
}
