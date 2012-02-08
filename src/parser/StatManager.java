package parser;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.PriorityQueue;
import java.util.Vector;

import datastore.HierarchyTable;

// Singleton class to store statistics while parsing
// No logic here, just storing data for analysis at intermediate stages
public class StatManager {
   private static StatManager instance = null;
   
   public HierarchyParser ht;
   
   // Track the component that are in the part hierarchy
   //public Hashtable<PartHier, Integer> partTable;
   
   // Track how many components that 'may' be missed by the part hierarchy
   public Hashtable<String, Integer> missingPartTable; 
   
   // A flat dictionary for fast lookup 
   public Hashtable<String, String> dict;   
   
   // Replacement priority queue, based on decreasing length
   public PriorityQueue<String> priorityPart = new PriorityQueue<String>(500, new LengthComparator());
      
   // Current focused part/group
   //public static Integer currentGroup = null;
   
   public static void main(String[] args) {
      StatManager.instance();
   }
   
   protected StatManager() {    
      dict = new Hashtable<String, String>();
      //head = new PartHier();
      //partTable = new Hashtable<PartHier, Integer>();
      missingPartTable = new Hashtable<String, Integer>();
      
      
      // Just testing for now
      ht = new HierarchyParser();
      try {
         ht.createDBTable();
      } catch (Exception e) {}
      
      System.out.println("Creating priority part list...");
      Enumeration<Integer> e = ht.partTable.keys();
      while (e.hasMoreElements()) {
    	 Integer key = e.nextElement();
         Vector<String> v = ht.partTable.get(key);
         for (int i=0; i < v.size(); i++) {
            String word = v.get(i);
            dict.put(word.replaceAll("_", " "), word);
            priorityPart.add(word.replaceAll("_", " "));
         }
      }
      
      

     
      
      
//      head.parse(Const.PART_FILE);
      
      // Parse the file as a flat dictionary and a priority lookup queue
//      try {
//         BufferedReader brf =  new BufferedReader(new FileReader(Const.PART_FILE));
//         String line;
//         while ( (line = brf.readLine()) != null ) {
//            if (line.startsWith("#")) continue; // Comment and whatnots
//            
//            String txt[] = line.split(":");
//            String tokens[] = txt[1].split(",");
//            
//            for (int i=0; i < tokens.length; i++) {
//               // 'a b' ==> 'a_b'
//               dict.put(tokens[i].trim(), tokens[i].trim().replace(" ", "_"));
//               priorityPart.add(tokens[i].trim());
//            }            
//         } // End while         
//      } catch (Exception e) {
//         e.printStackTrace();
//         System.exit(0);
//      }
//      
//      // *** Debug : Just print out the static tables ***
//      System.out.println("=== Part Hierarchy ===");
//      head.print();
//      System.out.println("\n\n\n");
//      
//      System.out.println("=== Dictionary ===");
      Enumeration<String> e2 = dict.keys();
      while (e2.hasMoreElements()) {
         String key = e2.nextElement();
         //System.out.println(key + "-->" + dict.get(key).toString());
      }
      System.out.println("\n\n\n");
//      

   }
   

   // General cleaning/scrubbing
   public String cleanString(String str) {            
      PriorityQueue<String> queue = new PriorityQueue<String>(priorityPart);
      // Tag any known compound word by connecting them with an underscore, this should 
      // keep the parser output sane. For example avoiding any confusion from 'running shoes' or
      // 'steering wheel'
      while (queue.size() != 0) {
         String repStr = queue.remove();  
                  
         // \b is for checking word boundaries
         //str = str.replaceAll("\\b" + repStr + "\\b", "<" + dict.get(repStr) + ">");
         str = str.replaceAll("\\b" + repStr + "\\b", "" + dict.get(repStr) + "");
      }
                 
      str = str.replaceAll("\\(.\\*\\)", "");     // Remove stuff in brackets      
      str = str.replaceAll("\\s\\s*", " ");       // Remove extra spaces     
      str = str.replaceFirst("tt$", "");          // Remove weird ending string encodings
      str = str.replaceFirst("\\*ak.*$", "");     // Remove weird ending string encodings
      
      return str;
   }   
   
        
   public void print() {
      Enumeration<PartHier> e;
      Enumeration<String> e2;
      int count = 0;
      
      /*
      e = partTable.keys();
      count = 0;     
      System.out.println("\n\nOccurences of components found");      
      while(e.hasMoreElements()) {
         PartHier key = e.nextElement();
         count += partTable.get(key).intValue();
         System.out.println(key + " ==> "+  partTable.get(key));
      }
      System.out.println("== Total : " + count);
      System.out.println("");
      */
                       
            
      e2 = missingPartTable.keys();
      count = 0;     
      System.out.println("\n\nOccurences of possible components not found");      
      while(e2.hasMoreElements()) {
         String key = e2.nextElement();
         //System.out.println(key);
         count += missingPartTable.get(key).intValue();
         System.out.println(key + " ==> "+  missingPartTable.get(key));
      }      
      System.out.println("== Total : " + count);
      System.out.println(""); 
      
   }
   
   
   // Since we are always getting the parts from the dictionary, theoretically PartHier p will never be null
//   public int incrementPartCount(String name) {
//      PartHier p = head.search(name);
//      int num = partTable.get(p) == null ? 0 : partTable.get(p).intValue();
//      num ++;
//      partTable.put(p, Integer.valueOf(num));
//      return num;
//   }
//   
//   public int incrementPartCount(String name, int val) {
//      PartHier p = head.search(name);
//      int num = partTable.get(p) == null ? 0 : partTable.get(p).intValue();
//      num += val;
//      partTable.put(p, Integer.valueOf(num));
//      return num;
//   }
   
   
   public int incrementMissingPartCount(String name) {
      int num = missingPartTable.get(name) == null ? 0 : missingPartTable.get(name).intValue();
      num ++;
      missingPartTable.put(name, Integer.valueOf(num));
      return num;
   }
      
   public static StatManager instance() {
      if (instance == null) { instance = new StatManager(); }
      return instance;
   }
 
}
