package db;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Vector;

/////////////////////////////////////////////////////////////////////////////////
// A "query" object that encapsulates the 
// the data hierarchy that can be accessed by the user
/////////////////////////////////////////////////////////////////////////////////
public class QueryObj implements Serializable {
   
//   public static void main(String args[]) throws Exception {
//      DBWrapper dbh = new DBWrapper();
//      QueryObj obj = new QueryObj();
//      String sql = "select b.datea, a.groupid, b.mfr_txt, b.make_txt, b.model_txt " + 
//                   "from projectv3.cmp_x_grp_clean a " + 
//                   ",    projectv3.cmp_clean b " + 
//                   "where a.cmplid = b.cmplid";
//      long start = System.currentTimeMillis();
//      ResultSet rs = dbh.execute(sql, true);
//      int counter = 0;
//      while (rs.next()) {
//         String datea     = rs.getString(1);
//         String groupid   = rs.getString(2);
//         String mfr_txt   = rs.getString(3);
//         String make_txt  = rs.getString(4);
//         String model_txt = rs.getString(5);
//         
//         if (obj.get(mfr_txt) == null) {
//            obj.put(mfr_txt, new QueryObj());   
//         }
//         if (obj.get(mfr_txt).get(make_txt) == null) {
//            obj.get(mfr_txt).put(make_txt, new QueryObj());
//         }
//         if (obj.get(mfr_txt).get(make_txt).get(model_txt) == null) {
//            obj.get(mfr_txt).get(make_txt).put(model_txt, new QueryObj());
//         }
//         
//         // increment counter
//         obj.get(mfr_txt).get(make_txt).get(model_txt).count ++;
//         obj.get(mfr_txt).get(make_txt).count ++;
//         obj.get(mfr_txt).count ++;
//         
//         
//         counter++;
//         if (counter % 50000 == 0) System.out.println("Processed " + counter + " records");
//      }
//      System.out.println("Processed " + counter + " records");
//      
//      long end = System.currentTimeMillis();
//      System.out.println("Processing time is :" + (end-start));
//   }
   
   private static final long serialVersionUID = 7862764;
   
   public QueryObj() {
      count = 0;
      children = new Hashtable<String, QueryObj>();
   }
   
   
   public QueryObj get(String key) {
      return children.get(key);   
   }
   
   
   public void put(String key, QueryObj v) {
      children.put(key, v);   
   }
   
   
   
   /*
   public int getNumLeafChildren() {
      int result = 0;   
      if (this.children == null || this.children.size() == 0) {
         result = 1;
      } else {
         for (QueryObj q : this.children.values()) {
            result +=  q.getNumLeafChildren();  
         }
      }
      return result;
   }
   */

   
   /*
   public getTotalCnt() {
      int childrenCount = 0;
      
      Enumeration<String> keys = children.keys();
      while (keys.hasMoreElement()) {
         String key = keys.nextElement();
         childrenCount += 
      }
   }*/
   
   
  // For holding additional data
  //public Hashtable<Integer, Integer> lookup1 = new Hashtable<Integer, Integer>();
   
  // To hold a list of associated document ids
  public Vector<Integer> lookup2 = new Vector<Integer>();
   
   public int count;
   public String name;
   public Hashtable<String, QueryObj> children;
}
