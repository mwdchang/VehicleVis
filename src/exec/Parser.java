package exec;

import datastore.Const;

import parser.DependencyParser;
import parser.StatManager;

/* Entry point for NTFSA data parsing */
public class Parser {
   public Parser() {      
   }
   
   public static void main(String[] args) {
      DependencyParser p = new DependencyParser();   
      StatManager.instance();
      
      try {
         p.parse3(Const.DATA_FILE, 10, 10);
//         Hashtable table = StatManager.instance().partTable;
//         Enumeration<String> ee = table.keys();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
}
