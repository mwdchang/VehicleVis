package exec;

import datastore.Const;
import db.DBWrapper;
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
   public static void main(String args[]) {
      HierarchyParser hierarchyParser = new HierarchyParser();
      Normalizer normalizer = new Normalizer();
      KeywordParser keywordParser = new KeywordParser();
      
      int rc = 0;
      
      String fileDir = "C:\\Users\\Daniel\\VehicleVis\\";
      
      
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
         rc = TableLoader.loadData2Table(fileDir + "part.txt",  "projectv3.grp", true);   
         if (rc != 0) throw new Exception("Data load failed");
         
         rc = TableLoader.loadData2Table(fileDir + "group.txt", "projectv3.grp_hier", true);   
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
         normalizer.parse(Const.DATA_FILE, 0);   
         
         System.out.println("\nStarting database load...");
         rc = TableLoader.loadData2Table(fileDir + "cmp_clean.txt", "projectv3.cmp_clean", true);  
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
         rc = TableLoader.loadData2Table(fileDir + "new_cmp_x_grp.txt", "projectv3.cmp_x_grp", true);  
         if (rc != 0) throw new Exception("Data load failed");
         rc = TableLoader.loadData2Table(fileDir + "opt.txt", "projectv3.cmp_x_grp_clean", true);  
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
