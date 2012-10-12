package datastore;

import java.sql.ResultSet;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import db.DBWrapper;

/////////////////////////////////////////////////////////////////////////////////
// This class contains the hierarchy parts and information
/////////////////////////////////////////////////////////////////////////////////
public class HierarchyTable {
   
   public static void main(String args[]) {
      HierarchyTable ht = new HierarchyTable();
      try {
         //ht.createDBTable();
         ht.initFromDB();
      } catch (Exception e) {
         e.printStackTrace(); 
      }
      /* 
      System.out.println(ht.partTable.get(10));
      System.out.println(ht.partTable.get(ht.groupTable.get(10)));
      */
      
      System.out.println( HierarchyTable.instance().getAgg(25));
   }
   
   public static HierarchyTable instance() {
      if (inst == null) inst = new HierarchyTable();
      return inst;
   }
   
   
   
   protected HierarchyTable() {
      try {
         initFromDB();   
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println("Failed to initialize component hierarchy");   
         System.exit(0);
      }
   }
   
   
   
   public void createExtraLookupTables() {
   }
   
   
   // Initialize the contents from DB 
   public void initFromDB() throws Exception {
      // Clear out any existing data first
      groupTable.clear();
      partTable.clear();
      
      // Copies the database tables into the data structure
      try  {
         DBWrapper dbh = new DBWrapper();         
         String sqlStr = "";  
         ResultSet rs = null;
         
         // Get the group hierarchy
         sqlStr = "select groupId, parentId from " + SSM.database + ".grp_hier";
         rs = dbh.execute(sqlStr);
         while(rs != null && rs.next()) {
            Integer key = rs.getInt("groupId");
            Integer val = rs.getInt("parentId");
            groupTable.put(key, val);
            
            if (childTable.get(val) == null) {
               Vector<Integer> l = new Vector<Integer>();
               l.addElement(key);
               childTable.put(val, l);
            } else {
               childTable.get(val).addElement(key);
            }
         }
         rs.close();
         
         // Get the group part names
         sqlStr = "select groupId, name from " + SSM.database + ".grp order by groupId asc";
         rs = dbh.execute(sqlStr);
         while(rs != null && rs.next()) {
            Integer key = rs.getInt("groupId");
            String  val = rs.getString("name").trim().replaceAll("\\n", "");
            if (partTable.get(key) != null) {
               partTable.get(key).add(val);
            } else {
               Vector<String> v = new Vector<String>();
               v.add(val);
               partTable.put(key, v);
            }
         }
         rs.close();
         
      } catch (Exception e) { e.printStackTrace();}
      
   }
   
   
   
   
   // Returns the groupId(s) associated with the name
   // Note: May have more than one
   public Vector<Integer> getGroupId(String partname) {
	   Vector<Integer> result = new Vector<Integer>();
	   Enumeration<Integer> em = partTable.keys();
	   while (em.hasMoreElements()) {
		   Integer key = em.nextElement();
		   if (partTable.get(key).contains(partname)) {
			   result.add(key);
		   }
	   }
	   return result;
   }
   
   
   // Get the parentId
   public Integer getParentId(Integer i) {
	   return groupTable.get(i);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Recursively add up the children
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
   public Vector<Integer> getAgg(Hashtable<Integer, Integer> p) {
      Vector<Integer> result = new Vector<Integer>();
      for (Integer key : p.keySet()) {
         Vector<Integer> children = childTable.get(key);
         if (children != null && children.size() > 0) {
            for (int i=0; i < children.size(); i++) {
               result.addAll( getAgg( children.elementAt(i)) );   
            }
         }
         result.add(key);
      }
      return result;      
   }
   public Vector<Integer> getAgg(Integer p) {
      Vector<Integer> result = new Vector<Integer>();
      Vector<Integer> children = childTable.get(p);
      
      if (children != null && children.size() > 0) {
         for (int i=0; i < children.size(); i++) {
            result.addAll( getAgg( children.elementAt(i)) );   
         }
      }
      result.add(p);
      return result;
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Tables to access hierarchy information
   //   groupTable - used to access parent nodes
   //   childTable - used to access child nodes
   ////////////////////////////////////////////////////////////////////////////////
   public Hashtable<Integer, Integer> groupTable = new Hashtable<Integer, Integer>();
   public Hashtable<Integer, Vector<Integer>> childTable = new Hashtable<Integer, Vector<Integer>>();
   
   public Hashtable<Integer, Vector<String>> partTable  = new Hashtable<Integer, Vector<String>>();
   
   
   
   private static HierarchyTable inst;
   
}
