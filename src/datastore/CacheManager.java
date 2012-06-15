package datastore;

import gui.DCPair;

import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import db.DBWrapper;
import db.DCDoc;
import db.DCDocComparator;
import db.DCTag;
import db.QueryObj;

import util.DCUtil;
import util.DWin;
import util.SerializeUtil;


/////////////////////////////////////////////////////////////////////////////////
// Singleton class to cache canned query result
// Cache the result sets into a vector of hashes to create a sliding windows effect
/////////////////////////////////////////////////////////////////////////////////
public class CacheManager {
   
   public static final short DATE_RANGE_DAY = 1;    // Massive ram required
   public static final short DATE_RANGE_MONTH = 2; 
   public static short dateRange = DATE_RANGE_MONTH;
   
   public static int timeLineStartYear = 1995;
   public static int timeLineEndYear   = 2011;
   
   public static boolean DEBUG = true;
//   public static boolean DEBUG = false;
   
//   public static void main(String argsp[]) {
//      //CacheManager.DEBUG = false;
//      CacheManager.instance();
//      CacheManager.instance().initSystem();
//   }
   
   
   public static String mfrFilter = null;
   public static String makeFilter = null;
   public static String modelFilter = null;
   public static String yearFilter = null;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Iteratively get the queryObj specified by params
   // It is assumed that params will be given in hierarchical order and 
   // that it is sane !!!
   ////////////////////////////////////////////////////////////////////////////////
   public QueryObj getQueryObj(int idx, int groupId, String ... params) {
      QueryObj root = queryTable.elementAt(idx).get(groupId);
      if (root == null) return root;
      
      for (int i=0; i < params.length; i++) {
         if (params[i] == null) break;
         root = root.children.get(params[i]);   
         if (root == null) break;
      }
      return root; 
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Iteratively get the queryObj at the filter level specified by params
   // It is assumed that params will be given in hierarchical order and 
   // that it is sane !!!
   ////////////////////////////////////////////////////////////////////////////////
   public QueryObj getQueryObjU(int idx, String ...params ) {
//      for (int i=0; i < params.length; i++) {
//         System.out.print(">>> : " + params[i] + " " );   
//      } System.out.println("");
      
      QueryObj root = queryTableU.elementAt(idx);   
      if (root == null) return root;
      
      for (int i=0; i < params.length; i++) {
         if (params[i] == null) break;
         root = root.children.get(params[i]);   
         if (root == null) break;
      }
      return root;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Fetches and returns the on disk cache status
   ////////////////////////////////////////////////////////////////////////////////
   public boolean readCache() {
      boolean done = false;
      
      try {
         System.err.println("Trying to resurrect seriarlized cache");
         
//         DCUtil.startTimer("Getting occurrence table...");
//         if(DEBUG) {
//            occurrenceTable = (Vector<Hashtable<Integer, Integer>>) SerializeUtil.objIn("debug_occurenceTable.ser");
//         } else {
//            occurrenceTable = (Vector<Hashtable<Integer, Integer>>) SerializeUtil.objIn("occurenceTable.ser");
//         } DCUtil.endTimer("Done");
         
         
         DCUtil.startTimer("Getting dateTable...");
         if (DEBUG) {
            dateTable = (Hashtable<String, Integer>) SerializeUtil.objIn("debug_dateTable.ser");
         } else {
            dateTable = (Hashtable<String, Integer>) SerializeUtil.objIn("dateTable.ser");
         } DCUtil.endTimer("Done");
         
         
         DCUtil.startTimer("Getting keyTable...");
         if (DEBUG) {
            keyTable = (Hashtable<Integer, String>) SerializeUtil.objIn("debug_keyTable.ser");
         } else {
            keyTable = (Hashtable<Integer, String>) SerializeUtil.objIn("keyTable.ser");
         } DCUtil.endTimer("Done");
         
         
         DCUtil.startTimer("Getting queryTable...");
         if (DEBUG) {
            queryTable = (Vector<Hashtable<Integer, QueryObj>>)SerializeUtil.objIn("debug_queryTable.ser");
         } else {
            queryTable = (Vector<Hashtable<Integer, QueryObj>>)SerializeUtil.objIn("queryTable.ser");
         } DCUtil.endTimer("Done");   
         System.gc();
         
         
         DCUtil.startTimer("Getting unique queryTable...");
         if (DEBUG) {
            queryTableU = (Vector<QueryObj>)SerializeUtil.objIn("debug_queryTableU.ser");
         } else {
            queryTableU = (Vector<QueryObj>)SerializeUtil.objIn("queryTableU.ser");
         } DCUtil.endTimer("Done");   
         System.gc();
         done = true;
      } catch (Exception e) {
         e.printStackTrace();
      }
      return done;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Initialize the system 
   ////////////////////////////////////////////////////////////////////////////////
   public void initSystem()  {
      long start = System.currentTimeMillis();
      try {
         initTimeLine(timeLineStartYear, timeLineEndYear);
         //initOccurrenceTable2();   
         initQueryTable();
         setDocumentTag();
System.out.println("Debugging");         
         initRelatedTable();
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
      long end = System.currentTimeMillis();
      
      System.out.println("Total time to bring up system : " + (end-start));
      System.out.flush();
      DWin.instance().msg("Total time to bring up cache : " + (end-start));
      
      
      // Attempt initialize the system from on disk cache
      /*
      if (readCache() == false)  {
         initTimeLine(timeLineStartYear, timeLineEndYear);
         try {
            initOccurrenceTable2();   
            initQueryTable();
         } catch (Exception e) {
            e.printStackTrace();
           System.exit(0);
         }
      }
      
      // Initialize the runtime cache
      setDocumentTag();
      initRelatedTable();
      */
   }
   
   
   
   
   /////////////////////////////////////////////////////////////////////////////////  
   // Default constructor
   /////////////////////////////////////////////////////////////////////////////////  
   protected CacheManager() {
      if (DEBUG == true) {
         timeLineStartYear = 1995;         
         timeLineEndYear   = 1998;         
      }
   }
   
   
   /////////////////////////////////////////////////////////////////////////////////  
   // Returns the singleton instance
   /////////////////////////////////////////////////////////////////////////////////  
   public static CacheManager instance() {
      if (inst == null) inst = new CacheManager();
      return inst;
   }
   
   
   
   
   
   /////////////////////////////////////////////////////////////////////////////////  
   // Get the index key given a specific date
   // returns null if not exist, assumes table is ready for use
   /////////////////////////////////////////////////////////////////////////////////  
   public Integer getDateKey(String dateStr) {
      if (dateRange == DATE_RANGE_MONTH) dateStr = dateStr.substring(0, 6);
      return dateTable.get(dateStr);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get the aggregated domain information, 
   // In this case, get the total for manufacture, make and model
   ////////////////////////////////////////////////////////////////////////////////
   public void initQueryTable() {
      HashSet<Integer> dupe = new HashSet<Integer>();
      
      try {
         DBWrapper dbh = new DBWrapper();
         long start = 0;
         long end = 0;
         ResultSet rs = null;
         int counter = 0;
         
         String sql = 
         "SELECT b.datea, a.groupid, b.mfr_txt, b.make_txt, b.model_txt, b.year_txt, b.cmplid " + 
         "FROM cmp_x_grp_clean a " + 
         ",    cmp_clean b " + 
         "WHERE a.cmplid = b.cmplid "  
         ;         
         if (mfrFilter != null) {
            sql += "AND b.mfr_txt in (" + mfrFilter + ") ";   
         }
         
         start = System.currentTimeMillis();
         rs    = dbh.execute(sql, true);
         while(rs.next()) {
            Date datea   = rs.getDate(1);
            int groupid   = rs.getInt(2);
            String mfr_txt   = rs.getString(3);
            String make_txt  = rs.getString(4);
            String model_txt = rs.getString(5);
            String year_txt = rs.getString(6);
            int cmplid = rs.getInt(7);
            Integer idx = this.getDateKey( DCUtil.formatDateYYYYMMDD( datea ));
            if (idx == null) continue;
            
            // Make sure the date table contains the proper keys
            Hashtable<Integer, QueryObj> t = queryTable.elementAt(idx);
            //TIntObjectHashMap<QueryObj> t = queryTable.elementAt(idx);
            if ( t.get( groupid ) == null) t.put(groupid, new QueryObj());
            
            // Make a query object based on the group - this is not a document unique query
            QueryObj q = t.get(groupid);
            if (q.get(mfr_txt) == null) q.put(mfr_txt, new QueryObj());
            if (q.get(mfr_txt).get(make_txt) == null) q.get(mfr_txt).put(make_txt, new QueryObj());
            if (q.get(mfr_txt).get(make_txt).get(model_txt)==null) q.get(mfr_txt).get(make_txt).put(model_txt, new QueryObj());
            if (q.get(mfr_txt).get(make_txt).get(model_txt).get(year_txt)==null) q.get(mfr_txt).get(make_txt).get(model_txt).put(year_txt, new QueryObj());
            
            q.get(mfr_txt).get(make_txt).get(model_txt).get(year_txt).count++;
            q.get(mfr_txt).get(make_txt).get(model_txt).count ++;
            q.get(mfr_txt).get(make_txt).count ++;
            q.get(mfr_txt).count ++;
            q.count ++;
            
            
            // Compile a list of document ids as cache
            if (! q.get(mfr_txt).get(make_txt).get(model_txt).get(year_txt).lookup2.contains(cmplid)) 
               q.get(mfr_txt).get(make_txt).get(model_txt).get(year_txt).lookup2.add(cmplid);
            if (! q.get(mfr_txt).get(make_txt).get(model_txt).lookup2.contains(cmplid))
               q.get(mfr_txt).get(make_txt).get(model_txt).lookup2.add(cmplid);
            if (! q.get(mfr_txt).get(make_txt).lookup2.contains(cmplid))  
               q.get(mfr_txt).get(make_txt).lookup2.add(cmplid);
            if (! q.get(mfr_txt).lookup2.contains(cmplid))   
               q.get(mfr_txt).lookup2.add(cmplid);
            if (! q.lookup2.contains(cmplid))
               q.lookup2.add(cmplid);
            
            // Make a query object based on complaint id - this should be a unique query
            if (dupe.contains(cmplid) == false) { 
               QueryObj u = queryTableU.elementAt(idx);
               if (u.get(mfr_txt) == null) u.put(mfr_txt, new QueryObj());
               if (u.get(mfr_txt).get(make_txt) == null) u.get(mfr_txt).put(make_txt, new QueryObj());
               if (u.get(mfr_txt).get(make_txt).get(model_txt)==null) u.get(mfr_txt).get(make_txt).put(model_txt, new QueryObj());
               if (u.get(mfr_txt).get(make_txt).get(model_txt).get(year_txt)==null) u.get(mfr_txt).get(make_txt).get(model_txt).put(year_txt, new QueryObj());
               
               u.get(mfr_txt).get(make_txt).get(model_txt).get(year_txt).count ++;
               u.get(mfr_txt).get(make_txt).get(model_txt).count ++;
               u.get(mfr_txt).get(make_txt).count ++;
               u.get(mfr_txt).count ++;
               u.count ++;
               dupe.add(cmplid);
            }
            
            
            counter++;
            if (counter % 30000 == 0) System.out.println("Processed " + counter + " records");
         }
         System.out.println("Processed " + counter + " records");
         end   = System.currentTimeMillis();
         
         /*
         if (DEBUG) {
            SerializeUtil.objOut(queryTable, "debug_queryTable.ser");
            SerializeUtil.objOut(queryTableU, "debug_queryTableU.ser");
         } else {
            SerializeUtil.objOut(queryTable, "queryTable.ser");
            SerializeUtil.objOut(queryTableU, "queryTableU.ser");
         }            
         */
         
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
      
      // Attemp to save some memory
      dupe.clear();
      dupe = null;
      System.gc();

   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get the tags for a given time period
   // Keyed by the complaint ID
   ////////////////////////////////////////////////////////////////////////////////
   public void setDocumentTag() {
      if (tagTable != null) {
         tagTable.clear();
      } else {
         tagTable = new Hashtable<Integer, Vector<DCTag>>();
      }
      
      DBWrapper dbh = new DBWrapper();
      try {
         long numTags = 0;
//         String sql =  "call get_document_tag(" +  "\"" + from + "\", \"" + to + "\")";   
         String sql = "select cmplid, groupId, start, end from cmp_x_grp";
         ResultSet rs = dbh.execute( sql, true );
         
         
         while (rs.next()) {
            numTags ++;
            int id = rs.getInt(1);
            short groupId = rs.getShort(2);
            short start = rs.getShort(3);
            short end = rs.getShort(4);
            
            
            if ( tagTable.get( id ) == null) {
               tagTable.put(id, new Vector<DCTag>(1));
            }
            tagTable.get(id).add(new DCTag(id, groupId, start, end));            
            
            /*
            if (numTags % 10000 == 0) {
               System.out.println("Num Tags : " + numTags);
            }*/
         }
         
         //System.out.println("Num Tags : " + numTags);
         System.out.println("Done creating tag table");
         rs.close();
         rs = null;
         dbh.cleanup();
         System.gc();
         
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
   }
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // For some reason select distinct is just bloody slow, I would debug it, but
   // it is just faster and as effective to do this in memory
   ////////////////////////////////////////////////////////////////////////////////
   public void initRelatedTable() {
      DBWrapper dbh = new DBWrapper(); 
      try {
         String sql = "select cmplid, groupId from cmp_x_grp_clean";
         ResultSet rs = dbh.execute(sql, true);
         while (rs.next()) {
            int cmplid = rs.getInt(1);
            int groupId = rs.getInt(2);
            
            Vector<Integer> v;
            if (relatedTable.get(cmplid) != null) {
               v = relatedTable.get(cmplid);   
            } else {
               v = new Vector<Integer>(1); 
            }
            v.add(groupId);
            relatedTable.put(cmplid, v);
         }
      } catch (Exception e) {
         e.printStackTrace();   
         System.exit(0);
      }
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Fetch document based on selected date range
   // Allows for single/multiple groups to be processed at once
   ////////////////////////////////////////////////////////////////////////////////
   public Vector<DCDoc> setDocumentDataAgg(String from, String to, int fromMonth, int toMonth, Vector<Integer> groupIds, int idx) {
      Vector<DCDoc> result = new Vector<DCDoc>();
      DBWrapper dbh = new DBWrapper();
      try {
         // Static clause
         //String sql = "SELECT a.cmplid, a.cdescr  " +   
         String sql = "SELECT a.cmplid, a.datea, a.mfr_txt, a.make_txt, a.model_txt, a.year_txt, a.cdescr " +
                      "FROM cmp_clean a " +
                      "WHERE a.datea >= '" + DCUtil.formatDateStr(from) + "' " +
                      "AND   a.datea <= '" + DCUtil.formatDateStr(to) + "' " +
                      "AND   MONTH(a.datea) >= " + (fromMonth+1) + " " +
                      "AND   MONTH(a.datea) <= " + (toMonth+1) + " ";
         
         // Dynamic clause filter
         if (SSM.useComparisonMode == true ) {
            sql += "AND ((" ;
            if (SSM.manufactureAttrib.selected == null) {
               sql += "1 = 1"; // Dummy pass thru   
            } else {
               if (SSM.manufactureAttrib.selected != null) 
                  sql += "a.mfr_txt = '" + SSM.manufactureAttrib.selected + "' ";
               if (SSM.makeAttrib.selected != null) 
                  sql += "AND a.make_txt = '" + SSM.makeAttrib.selected + "' ";
               if (SSM.modelAttrib.selected != null) 
                  sql += "AND a.model_txt = '" + SSM.modelAttrib.selected + "' ";
               if (SSM.yearAttrib.selected != null) 
                  sql += "AND a.year_txt = '" + SSM.yearAttrib.selected + "' ";
            }
            sql += " ) OR ( "; 
            if (SSM.c_manufactureAttrib.selected == null) {
               sql += "1 = 1"; // Dummy pass thru   
            } else {
               if (SSM.c_manufactureAttrib.selected != null) 
                  sql += "a.mfr_txt = '" + SSM.c_manufactureAttrib.selected + "' ";
               if (SSM.c_makeAttrib.selected != null) 
                  sql += "AND a.make_txt = '" + SSM.c_makeAttrib.selected + "' ";
               if (SSM.c_modelAttrib.selected != null) 
                  sql += "AND a.model_txt = '" + SSM.c_modelAttrib.selected + "' ";
               if (SSM.c_yearAttrib.selected != null) 
                  sql += "AND a.year_txt = '" + SSM.c_yearAttrib.selected + "' ";
            }
            sql += " )) ";
         } else {
            if (SSM.manufactureAttrib.selected != null) {
               sql += "AND a.mfr_txt = '" + SSM.manufactureAttrib.selected + "' ";
            }
            if (SSM.makeAttrib.selected != null) {
               sql += "AND a.make_txt = '" + SSM.makeAttrib.selected + "' ";
            }
            if (SSM.modelAttrib.selected != null) {
               sql += "AND a.model_txt = '" + SSM.modelAttrib.selected + "' ";
            }
            if (SSM.yearAttrib.selected != null) {
               sql += "AND a.year_txt = '" + SSM.yearAttrib.selected + "' ";
            }
         }
         
         // Work around to get dimensionalized view
         if (groupIds.size() == 0) {
             sql += "AND EXISTS ( " + 
                      "SELECT 1 FROM cmp_x_grp_clean b " + 
                      "WHERE a.cmplid = b.cmplid " +
                   ") ";
         } else {
            for (int i=0; i < groupIds.size(); i++) {
               sql += "AND EXISTS ( " + 
                         "SELECT 1 FROM cmp_x_grp_clean b " + 
                         "WHERE a.cmplid = b.cmplid " +
                         "AND b.groupId in " + DCUtil.makeInClause(HierarchyTable.instance().getAgg(groupIds.elementAt(i))) + " " +
                      ") ";
            }
         }
         
         sql += "limit " + idx + ", " + SSM.globalFetchSize;
         ResultSet rs = dbh.execute( sql, true );
         while (rs.next()) {
            int id  = rs.getInt(1);
            Date date = rs.getDate(2);
            String mfr = rs.getString(3);
            String make = rs.getString(4);
            String model = rs.getString(5);
            String year = rs.getString(6);
            String txt = rs.getString(7); 
            //result.add( new DCDoc(id, txt));
            result.add( new DCDoc(id, date, mfr, make, model, year, txt));
         }
         Collections.sort(result, new DCDocComparator<DCDoc>());         
         return result;
         
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
      return result; 
   }   
   
   public Vector<DCDoc> setDocumentData(String from, String to, int fromMonth, int toMonth, Vector<Integer> groupIds, int idx) {
      Vector<DCDoc> result = new Vector<DCDoc>();
      DBWrapper dbh = new DBWrapper();
      try {
         // Static clause
         //String sql = "SELECT a.cmplid, a.cdescr  " +   
         String sql = "SELECT a.cmplid, a.datea, a.mfr_txt, a.make_txt, a.model_txt, a.year_txt, a.cdescr " +
                      "FROM cmp_clean a " +
                      "WHERE a.datea >= '" + DCUtil.formatDateStr(from) + "' " +
                      "AND   a.datea <= '" + DCUtil.formatDateStr(to) + "' " +
                      "AND   MONTH(a.datea) >= " + (fromMonth+1) + " " +
                      "AND   MONTH(a.datea) <= " + (toMonth+1) + " ";
         
         // Dynamic clause filter
         if (SSM.useComparisonMode == true ) {
            sql += "AND ((" ;
            if (SSM.manufactureAttrib.selected == null) {
               sql += " 1=1 ";
            } else {
               if (SSM.manufactureAttrib.selected != null) 
                  sql += "a.mfr_txt = '" + SSM.manufactureAttrib.selected + "' ";
               if (SSM.makeAttrib.selected != null) 
                  sql += "AND a.make_txt = '" + SSM.makeAttrib.selected + "' ";
               if (SSM.modelAttrib.selected != null) 
                  sql += "AND a.model_txt = '" + SSM.modelAttrib.selected + "' ";
               if (SSM.yearAttrib.selected != null) 
                  sql += "AND a.year_txt = '" + SSM.yearAttrib.selected + "' ";
            }
            sql += " ) OR ( "; 
            if (SSM.c_manufactureAttrib.selected == null) {
               sql += "1=1";
            } else {
               if (SSM.c_manufactureAttrib.selected != null) 
                  sql += "a.mfr_txt = '" + SSM.c_manufactureAttrib.selected + "' ";
               if (SSM.c_makeAttrib.selected != null) 
                  sql += "AND a.make_txt = '" + SSM.c_makeAttrib.selected + "' ";
               if (SSM.c_modelAttrib.selected != null) 
                  sql += "AND a.model_txt = '" + SSM.c_modelAttrib.selected + "' ";
               if (SSM.c_yearAttrib.selected != null) 
                  sql += "AND a.year_txt = '" + SSM.c_yearAttrib.selected + "' ";
            }
            sql += " )) "; 
         } else {
            if (SSM.manufactureAttrib.selected != null) {
               sql += "AND a.mfr_txt = '" + SSM.manufactureAttrib.selected + "' ";
            }
            if (SSM.makeAttrib.selected != null) {
               sql += "AND a.make_txt = '" + SSM.makeAttrib.selected + "' ";
            }
            if (SSM.modelAttrib.selected != null) {
               sql += "AND a.model_txt = '" + SSM.modelAttrib.selected + "' ";
            }
            if (SSM.yearAttrib.selected != null) {
               sql += "AND a.year_txt = '" + SSM.yearAttrib.selected + "' ";
            }         
         }
         
         // Work around to get dimensionalized view
         if (groupIds.size() == 0) {
             sql += "AND EXISTS ( " + 
                      "SELECT 1 FROM cmp_x_grp_clean b " + 
                      "WHERE a.cmplid = b.cmplid " +
                   ") ";
         } else {
            for (int i=0; i < groupIds.size(); i++) {
               sql += "AND EXISTS ( " + 
                         "SELECT 1 FROM cmp_x_grp_clean b " + 
                         "WHERE a.cmplid = b.cmplid " +
                         "AND b.groupId = " + groupIds.elementAt(i) + 
                      ") ";
            }
         }
         
         sql += "limit " + idx + ", " + SSM.globalFetchSize;
         
         ResultSet rs = dbh.execute( sql, true );
         while (rs.next()) {
            int id  = rs.getInt(1);
            Date date = rs.getDate(2);
            String mfr = rs.getString(3);
            String make = rs.getString(4);
            String model = rs.getString(5);
            String year = rs.getString(6);
            String txt = rs.getString(7); 
            //result.add( new DCDoc(id, txt));
            result.add( new DCDoc(id, date, mfr, make, model, year, txt));
         }
         Collections.sort(result, new DCDocComparator<DCDoc>());         
         return result;
         
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
      
      return result; 
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get the group occurrence, including month range, with filtering
   ////////////////////////////////////////////////////////////////////////////////
   public Hashtable<Integer, Integer> getPartOccurrenceFilter(int startIdx, int endIdx, int fromMonth, int toMonth, String ... params) {
      Hashtable<Integer, Integer> result = new Hashtable<Integer, Integer>();
      for (int i = startIdx; i <= endIdx; i++) {
         String dateStr = this.getTimeByIndex(i);
         int m = Integer.parseInt(dateStr.substring(4,6)) - 1;
         
         if (m < fromMonth || m > toMonth) continue; // if month out of range skip
//System.out.println("Debug getPartOccurrence...");         
         synchronized(queryTable) {
         Enumeration<Integer> partEnum = queryTable.elementAt(i).keys();
         while (partEnum.hasMoreElements()) {
            int partID= partEnum.nextElement();
            int value     = this.getOcc(i, partID, params);
            if (result.get(partID) != null) {
               value += result.get(partID);   
            } 
            result.put(partID, value);
         }
         }
      }
      return result;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get aggregated group occurrence, including month range, with filtering
   ////////////////////////////////////////////////////////////////////////////////
   public Hashtable<Integer, Integer> getPartOccurrenceFilterAgg(int startIdx, int endIdx, int fromMonth, int toMonth, String ... params) {
      Hashtable<Integer, Integer> result = new Hashtable<Integer, Integer>();
      for (int i = startIdx; i <= endIdx; i++) {
         String dateStr = this.getTimeByIndex(i);
         int m = Integer.parseInt(dateStr.substring(4,6)) - 1;
         
         if (m < fromMonth || m > toMonth) continue; // if month out of range skip
         
//System.out.println("Debug getPartOccurrenceFilterAgg");         
         synchronized(queryTable) {
         Enumeration<Integer> partEnum = queryTable.elementAt(i).keys();
         while (partEnum.hasMoreElements()) {
            int partID= partEnum.nextElement();
            int value     = this.getOccAgg(i, partID, params);
            if (result.get(partID) != null) {
               value += result.get(partID);   
            } 
            result.put(partID, value);
         }
         }
      }
      return result;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get the monthly maximum based on what is currently selected
   // That is, the maximum is exactly the amount of co-occurring documents
   // of the selected parts with itself in any given month index
   ////////////////////////////////////////////////////////////////////////////////
   public Vector<Integer> getMonthMaximumSelected(String ... params) {
      Vector<Integer> result = new Vector<Integer>();
      for (int i=0; i < queryTable.size(); i++) result.add(0);   
      
      Vector<Integer> selectedGroup = new Vector<Integer>();
      selectedGroup.addAll(SSM.selectedGroup.values());
      
      for (int i=0; i < result.size(); i++) {
         int value = this.getCoOccurring(i, selectedGroup, selectedGroup, params);
         result.set(i, value);
      }
      return result;
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get the monthly maximum based on what is currently selected with aggregates
   // That is, the maximum is exactly the amount of co-occurring documents
   // of the selected parts with itself in any given month index
   ////////////////////////////////////////////////////////////////////////////////
   public Vector<Integer> getMonthMaximumSelectedAgg(String ... params) {
      Vector<Integer> result = new Vector<Integer>();
      for (int i=0; i < queryTable.size(); i++) result.add(0);   
      
      Vector<Integer> selectedGroup = HierarchyTable.instance().getAgg(SSM.selectedGroup);
      Vector<Integer> relatedGroup = new Vector<Integer>();
      relatedGroup.addAll(SSM.selectedGroup.values());
      
      for (int i=0; i < result.size(); i++) {
         int value = this.getCoOccurringAgg(i, selectedGroup, relatedGroup, params);
         result.set(i, value);
      }
      return result;
   }
   
   
   public Vector<Integer> getMonthMaximum(String ... params) {
      Vector<Integer> result = new Vector<Integer>();
      // Blank fill the entire time-line
      for (int i=0; i < queryTable.size(); i++) result.add(0);   
      
      // Find the maximum
      for (int i=0; i < result.size(); i++) {
         for (Integer part : queryTable.elementAt(i).keySet()) {
            int value = getOcc(i, part, params);
            if (value > result.elementAt(i)) result.set(i, value);
         }
      }
      return result;
   }
   public Vector<Integer> getMonthMaximumAgg(String ... params) {
      Vector<Integer> result = new Vector<Integer>();
      // Blank fill zeros
      for (int i=0; i < queryTable.size(); i++) result.add(0);
      
      // Find the maximum per agg group
      for (int i=0; i < result.size(); i++) {
         for (Integer part : queryTable.elementAt(i).keySet()) {
            int value = getOccAgg(i, part, params);
            if (value > result.elementAt(i)) result.set(i, value);
         }
      }
      return result; 
   }
   
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns the data for the year slider widget
   // Returns a vector, as we do not know ahead of time how many years are there
   ////////////////////////////////////////////////////////////////////////////////
   public int[] getFilterYearStatArray(String ...params) {
      Vector<DCPair> yV = getFilterYearlyStat(params);
      int result[] = new int[yV.size()]; 
      for (int i=0; i < yV.size(); i++) {
         result[i] = (int)yV.elementAt(i).value;         
      }
      return result;
   }
   public Vector<DCPair> getFilterYearlyStat(String ...params) {
      Vector<DCPair> result = new Vector<DCPair>();    
      int pyear = 0;
      DCPair pair = null;
      for (int i=0; i < queryTableU.size(); i++) {
         String dtStr = getTimeByIndex(i); 
         int year = Integer.parseInt(dtStr.substring(0,4));
         
         if (year != pyear) {
            pyear = year;
            if (pair != null) result.add(pair);
            pair = new DCPair(year+"", 0);
         }
         
         QueryObj query = this.getQueryObjU(i, params);
         if (query == null) continue;
         pair.value += query.count;
         
         
      } // end for i
      result.add(pair);
      
      return result;
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Controls the data for the month range slider
   // fromStr and toStr should be yearly based
   ////////////////////////////////////////////////////////////////////////////////
   public int[] getFilterMonthlyStat(String fromStr, String toStr, String ... params) {
      int result[] = new int[12];
      for (int i=0; i < 12; i++) {
         result[i] = 0;   
      }
      
      Integer keyStart = getDateKey(fromStr) == null ? 0 : getDateKey(fromStr);
      Integer keyEnd   = getDateKey(toStr) == null ? this.timeLineSize-1 : getDateKey(toStr);
      
      for (int i=keyStart; i <= keyEnd; i++) {
         String dtStr = getTimeByIndex(i); 
         int month = Integer.parseInt(dtStr.substring(4,6)) - 1;          
         
         QueryObj query = this.getQueryObjU(i, params);
         if (query == null) continue;
         result[month] += query.count;
         
        
      } // end for i
      return result;
   }
   
   
   
   public String getTimeByIndex(int i) {
      // Prevent max and min overflow
      if (i >= this.timeLineSize) return keyTable.get( this.timeLineSize-1);
      if (i < 0) return keyTable.get(0);
      
      return keyTable.get(i);         
   }
   
   
   public int getOcc(int index, int partID, String ... params) {
      QueryObj query = this.getQueryObj(index, partID, params);
      if (query == null) return 0;
      return query.count;
      
      /*
      QueryObj partQ = queryTable.elementAt(index).get(partID);
      if (partQ == null) return 0;
      
      if (model != null) {
         QueryObj manufactureQ = partQ.get(manufacture);
         if (manufactureQ == null) return 0;
         QueryObj makeQ = manufactureQ.get(make);
         if (makeQ == null) return 0;
         QueryObj modelQ = makeQ.get(model);
         if (modelQ == null) return 0;
         return modelQ.count;
      } else if (make != null) {
         QueryObj manufactureQ = partQ.get(manufacture);
         if (manufactureQ == null) return 0;
         QueryObj makeQ = manufactureQ.get(make);
         if (makeQ == null) return 0;
         return makeQ.count; 
      } else if (manufacture != null) {
         QueryObj manufactureQ = partQ.get(manufacture);
         if (manufactureQ == null) return 0;
         return manufactureQ.count;
      } else {
         //System.out.println( "getOCC " + partID + " " + partQ.count);
         return partQ.count;   
      }
      */
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get a filtered view of group occurrence aggregation
   ////////////////////////////////////////////////////////////////////////////////
   public int getOccAgg(int index, int partID, String ... params) {
      Vector<Integer> parts = HierarchyTable.instance().getAgg(partID);
      if (parts == null || parts.size() <= 0) {
         System.out.println("Hmmm...something weirid happened while unraveling parts hierarchy");   
         System.exit(0);
      }
      
      Vector<Integer> self = HierarchyTable.instance().getAgg(partID);
      Vector<Integer> tmp = new Vector<Integer>();
      tmp.add(partID);
      return this.getCoOccurringAgg(index, self, tmp, params);
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Initialize a continuous timeline 
   ////////////////////////////////////////////////////////////////////////////////
   public void initTimeLine(int startYear, int endYear) {
      System.out.println("Initialize time line from " + startYear + " to " + endYear);
      //occurrenceTable = new Vector<Hashtable<Integer, Integer>>();
      queryTable = new Vector<Hashtable<Integer, QueryObj>>();
      queryTableU = new Vector<QueryObj>();
      
      dateTable = new Hashtable<String, Integer>();
      keyTable = new Hashtable<Integer, String>();
      
      Calendar cal = new GregorianCalendar(startYear, 0, 1); // starts on Jan 01  
      
      int counter = 0;
      while(true) {
         int year = cal.get(Calendar.YEAR);
         int month = cal.get(Calendar.MONTH);
         int day   = cal.get(Calendar.DAY_OF_MONTH);
         
         // Check end date condition
         if (year > endYear) break;
         
         // Get the date string
         String dateStr = getDateString( cal.getTime() );
         
         if (dateRange == DATE_RANGE_MONTH) {
            dateStr = dateStr.substring(0, 6);    
            if (dateTable.get(dateStr) == null) {
               dateTable.put(dateStr, counter);
               keyTable.put(counter, dateStr);
               //occurrenceTable.add(new Hashtable<Integer, Integer>());
               queryTable.add( new Hashtable<Integer, QueryObj>());
               queryTableU.add( new QueryObj());
               
               cal.add(Calendar.MONTH, 1);
               counter ++;      
            }
         } else if (dateRange == DATE_RANGE_DAY) {
            dateTable.put(dateStr, counter);
            keyTable.put(counter, dateStr);
            //occurrenceTable.add(new Hashtable<Integer, Integer>());
            queryTable.add( new Hashtable<Integer, QueryObj>());
            queryTableU.add( new QueryObj());
            cal.add(Calendar.DAY_OF_MONTH, 1);
            counter ++;
         }
         
      }
      this.timeLineSize = queryTable.size();
      
      System.out.println("dateTable: " + dateTable.size());
      System.out.println("keyTable : " + keyTable.size());
      //System.out.println("occurrenceTable : " + occurrenceTable.size());
   }
   
   
   
   public String getDateString(Date d) {
      return DCUtil.formatDateYYYYMMDD(d);   
   }
   
   
   
   /////////////////////////////////////////////////////////////////////////////////   
   // Get related groups by a range of indexed dates
   /////////////////////////////////////////////////////////////////////////////////   
   public Vector<Integer> getRelatedGroup(int startIndex, int endIndex, int fromMonth, int toMonth, Vector<Integer> self, String ... params) {
      Vector<Integer> result = new Vector<Integer>(0);   
      
      for (int i=startIndex; i <= endIndex; i++) {
         String dateStr = this.getTimeByIndex(i);
         int m = Integer.parseInt(dateStr.substring(4,6)) - 1;
         if (m < fromMonth || m > toMonth) continue; // if month out of range skip         
         
         // Get result per time period
         Vector<Integer> t = getRelatedGroup(i, self, params);
         
         // Merge
         for (int j=0; j < t.size(); j++) {
            if (! result.contains(t.elementAt(j)))  
                  result.add(t.elementAt(j));
         }
      }
      return result;
   }
   
   /////////////////////////////////////////////////////////////////////////////////   
   // Get related groups by indexed date
   /////////////////////////////////////////////////////////////////////////////////   
   public Vector<Integer> getRelatedGroup(int idx, Vector<Integer> groupIds, String ... params) {
      Vector<Integer> result = new Vector<Integer>();
      
      for (Integer compId : queryTable.elementAt(idx).keySet()) {
         
         QueryObj query = this.getQueryObj(idx, compId, params);
         if (query == null) continue;
         
         
         // Check if this query object contains groupIds
         for (int i=0; i < query.lookup2.size(); i++) {
            if (relatedTable.get(query.lookup2.elementAt(i)).containsAll(groupIds))             
               if (! result.contains(compId)) result.add(compId);
         }
      }
      return result;
   }
   
   
   
  
   
   /////////////////////////////////////////////////////////////////////////////////   
   // Get co-occuring with strict AND clause
   // 0) Sanitize
   // 1) Get a unique list of documents for the indexed period
   // 2) Get the Xref 
   // 3) Check if the related are in the Xref
   /////////////////////////////////////////////////////////////////////////////////   
   public int getCoOccurring(int startIndex, int endIndex, int fromMonth, int toMonth, Vector<Integer> self, Vector<Integer> relatedIds, String ... params) {
      int r = 0;
      for (int i=startIndex; i <= endIndex; i++) {
         String dateStr = this.getTimeByIndex(i);
         int m = Integer.parseInt(dateStr.substring(4,6)) - 1;
         if (m < fromMonth || m > toMonth) continue; // if month out of range skip         
         r += getCoOccurring(i, self, relatedIds, params);
      }
      return r;
   }
   public int getCoOccurring(int idx, Vector<Integer> groupIds, Vector<Integer> relatedIds, String ... params) {
      int result = 0;
      QueryObj query;
      
      // 0) Check for 0's
      Hashtable<Integer, Integer> ids = new Hashtable<Integer, Integer>();
      for (int i=0; i < groupIds.size(); i++) {
         
         query = this.getQueryObj(idx, groupIds.elementAt(i), params);
         if (query == null) continue;
         
         
         // If the query contains all relatedIds then it is a co-occurrence
         for (int j=0; j < query.lookup2.size(); j++) {
            ids.put(query.lookup2.elementAt(j), query.lookup2.elementAt(j));   
         }
      } // end for i
      
      for (Integer cmplid : ids.keySet()) {
         if ( this.relatedTable.get(cmplid).containsAll(relatedIds)) result++;            
      }
      return result;
   }
   
   public int getCoOccurringAgg(int startIndex, int endIndex, int fromMonth, int toMonth, Vector<Integer> self, Vector<Integer> relatedIds, String ... params) {
      int r = 0;
      for (int i=startIndex; i <= endIndex; i++) {
         String dateStr = this.getTimeByIndex(i);
         int m = Integer.parseInt(dateStr.substring(4,6)) - 1;
         if (m < fromMonth || m > toMonth) continue; // if month out of range skip         
         r += getCoOccurringAgg(i, self, relatedIds, params);
      }
      return r;
   }
   
   
   public int getCoOccurringAgg(int idx, Vector<Integer> groupIds, Vector<Integer> relatedIds, String ... params) {
      int result = 0;
      QueryObj query;
      
      // 0) Check for 0's
      Hashtable<Integer, Integer> ids = new Hashtable<Integer, Integer>();
      for (int i=0; i < groupIds.size(); i++) {
         
         query = this.getQueryObj(idx, groupIds.elementAt(i), params);
         if (query == null) continue;
         
         // If the query contains all relatedIds then it is a co-occurrence
         for (int j=0; j < query.lookup2.size(); j++) {
            ids.put(query.lookup2.elementAt(j), query.lookup2.elementAt(j));   
         }
      } // end for i
      
      for (Integer cmplid : ids.keySet()) {
         boolean add = true;
         for (Integer pid: relatedIds) {
            // Because it is AGG mode, if matching any one of children or itself it counts as a match
            Vector<Integer> children = HierarchyTable.instance().getAgg(pid);
            int countDown = children.size();
            for (Integer cid: children) {
               if (relatedTable.get(cmplid).contains(cid)) {
                  countDown --;
                  break;
               }
            }
            if (countDown == children.size()) add = false;
            if (add == false) break;
         }
         if ( add ) result++;            
      }
      return result;
   }
      
   
   
   public int timeLineSize = 0;
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Date string to vector index lookup
   ////////////////////////////////////////////////////////////////////////////////
   public Hashtable<String, Integer> dateTable;
   
   //////////////////////////////////////////////////////////////////////////////// 
   // Vector index to date string lookup
   //////////////////////////////////////////////////////////////////////////////// 
   public Hashtable<Integer, String> keyTable; 
   
   
   //////////////////////////////////////////////////////////////////////////////// 
   // A list of lookup tables by time period. Look up absolute group occurrence
   //////////////////////////////////////////////////////////////////////////////// 
   //public Vector<Hashtable<Integer, Integer>> occurrenceTable;
   
   
   
   //////////////////////////////////////////////////////////////////////////////// 
   // A list of lookup objects. Nested lookup by partId->manufacture->make->model
   //////////////////////////////////////////////////////////////////////////////// 
   public Vector<Hashtable<Integer, QueryObj>> queryTable;
   
   //////////////////////////////////////////////////////////////////////////////// 
   // A list of lookup objects. Nested lookup by manufacture->make->model
   //////////////////////////////////////////////////////////////////////////////// 
   public Vector<QueryObj> queryTableU;
   
   //////////////////////////////////////////////////////////////////////////////// 
   // The current document list
   //////////////////////////////////////////////////////////////////////////////// 
   public Vector<DCDoc> docList = new Vector<DCDoc>();
   
   //////////////////////////////////////////////////////////////////////////////// 
   // The current part occurrence lookup. Can be either single or aggregated depending on state
   //////////////////////////////////////////////////////////////////////////////// 
   public Hashtable<Integer, Integer> groupOccurrence; 
   
    //////////////////////////////////////////////////////////////////////////////// 
   // The current part occurrence lookup. Can be either single or aggregated depending on state
   //////////////////////////////////////////////////////////////////////////////// 
   public Hashtable<Integer, Integer> c_groupOccurrence; 
  
   //////////////////////////////////////////////////////////////////////////////// 
   // Stores the monthly maximum for normal or aggregated, pending on the SSM switch
   //////////////////////////////////////////////////////////////////////////////// 
   public Vector<Integer> monthMaximum = new Vector<Integer>();
   
   //////////////////////////////////////////////////////////////////////////////// 
   // Stores the second monthly maximum for comparative mode
   //////////////////////////////////////////////////////////////////////////////// 
   public Vector<Integer> c_monthMaximum = new Vector<Integer>();
   
   //////////////////////////////////////////////////////////////////////////////// 
   // Tag table cache. Look up complaintId -> { tag1, tag2... }
   //////////////////////////////////////////////////////////////////////////////// 
   public Hashtable<Integer, Vector<DCTag>> tagTable = new Hashtable<Integer, Vector<DCTag>>();
   
   //////////////////////////////////////////////////////////////////////////////// 
   // Related lookup table. Stored complaintId -> { group1, gruop2...etc }
   //////////////////////////////////////////////////////////////////////////////// 
   public Hashtable<Integer, Vector<Integer>> relatedTable = new Hashtable<Integer, Vector<Integer>>();
   
   
   
   private static CacheManager inst = null;
   
   
   public int filterMonthData[] = null;
   public int filterYearData[]  = null;
   
//   public Vector<TIntObjectHashMap<QueryObj>> queryTable;
//   public Hashtable<Integer, Integer> filterGroupOccurrence;
//   public Hashtable<Integer, String> allDoc = new Hashtable<Integer, String>();
}
