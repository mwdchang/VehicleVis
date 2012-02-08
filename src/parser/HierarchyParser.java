package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import util.DCUtil;

import datastore.Const;

///////////////////////////////////////////////////////////////////////////////// 
// This class parses the part/component file, assigns group relations
// and group ids, it prepares two text files for database upload
//  1) a group hierarchy structure
//  2) a part id to part name mapping
///////////////////////////////////////////////////////////////////////////////// 
public class HierarchyParser {
   
   public static void main(String args[]) {
      HierarchyParser hp = new HierarchyParser();
      try {
         hp.createDBTable();
      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   
   
   public HierarchyParser() {
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Create an intial database table layout  
   // Do not use this method after the db data has finalized
   ////////////////////////////////////////////////////////////////////////////////
   public int createDBTable() throws Exception {
      BufferedReader reader = new BufferedReader(new FileReader(Const.PART_FILE));       
      String line = "";
      
      int level;
      int currentLevel = 0;
      int parentGroupId = 0;
      int groupId = 0;
      
      groupTable.put(0, -1); // need a default
      
      while ( (line=reader.readLine()) != null) {
         if (line.startsWith("#")) continue;
         groupId ++;      
         level = getLevel(line);
         if (level - currentLevel == 1) {
            currentLevel++;   
            groupTable.put(groupId, parentGroupId);
            parsePart(line, groupId);
            parentGroupId = groupId;
         } else {
            while (level - currentLevel != 1) {
               currentLevel --;
               parentGroupId = groupTable.get(parentGroupId);
            }
            groupTable.put(groupId, parentGroupId);
            parsePart(line, groupId);
            parentGroupId = groupId;
            currentLevel = level;
         }
         
      }
  
      // Print
      BufferedWriter groupWriter = DCUtil.openWriter("group.txt");
      Enumeration<Integer> e1 = groupTable.keys();
      while (e1.hasMoreElements()) {
         Integer key = e1.nextElement();
         groupWriter.write(key + "\t" + groupTable.get(key) + "\r\n");
      }
      groupWriter.flush();
      groupWriter.close();
      
      
      BufferedWriter partWriter = DCUtil.openWriter("part.txt"); 
      Enumeration<Integer> e2 = partTable.keys();
      while (e2.hasMoreElements()) {
        Integer key = e2.nextElement();
        for (int i=0; i < partTable.get(key).size(); i++) {
           partWriter.write(key + "\t" + partTable.get(key).elementAt(i) + "\r\n");
        }
      }
      partWriter.flush();
      partWriter.close();
      return 0;
   }
   
   
   
   private void parsePart(String str, int groupId) {
      String txt = str.trim();
      String parts[] = txt.split(",");
      
      Vector<String> partGroup = new Vector<String>();
      for (int i=0; i < parts.length; i++) {
         String partname = parts[i];
         partname = partname.trim().replaceAll(" ", "_");
         System.out.println(partname);
         partGroup.add(partname);
         // The parts are not unique....so a part can have multiple parents
         // For example
         //    cylinder => engine_block
         //    cylinder => pump
      }
      partTable.put(groupId, partGroup);
      
   }
   
   // Get indentation level
   private int getLevel(String str) {
      int offset = 4; // Use 4 spaces
      int spCnt = 0;
      for (int i=0;i < str.length(); i++) {
         if (str.charAt(i) == ' ') spCnt ++;
         else break;
      }
      return spCnt/offset+1;
   }   
   
   public Hashtable<Integer, Integer> groupTable = new Hashtable<Integer, Integer>();   
   public Hashtable<Integer, Vector<String>> partTable  = new Hashtable<Integer, Vector<String>>();   
}
