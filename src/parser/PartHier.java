package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import datastore.Const;

// Models component hierarchy for sets
// For example
//   <partA, partB, partC> ---child of---> <partD, partE> --- child of---> <part H> 
//   <partX, partY, partZ> ---/
public class PartHier {
      
   public static void main(String[] args) {
      PartHier head = new PartHier();
      head.parse(Const.PART_FILE);
      System.out.println("\n");
      
      System.out.println( head.search("hood") );
      System.out.println( head.search("oil_filter") );     
   }
   
   
   
   //public static void main(String[] args) {
      //PartHier brake = new PartHier("brake system", "brakes");
      //PartHier brakeband = new PartHier("brake band");      
      //PartHier p = new PartHier("car");
      //brake.setParent(p);
      //brakeband.setParent(brake);      
      //p.print();
      
      // Just testing...      
      /*
      PartHier carComponent = new PartHier();
      PartHier current;

      int currentLevel = 0;
      current = carComponent;
            
      try {
         BufferedReader brf =  new BufferedReader(new FileReader("C:\\Users\\Daniel\\workspace\\bbb"));
         String line = "";
         while ( (line = brf.readLine()) != null ) {
            int level = (line.indexOf("HAS PART:") - 10)/4 + 1;
            String txt[] = line.split(":");
            PartHier p = new PartHier(txt[1]);
        

            if (level - currentLevel == 1) {
               currentLevel ++;
               current.addChild(p);
            } else {
               while (level - currentLevel != 1 ){
                  currentLevel --;
                  current = current.parent;
               }
               current.addChild(p);
               currentLevel = level;
            }
            
            current = p;

         }        
         // Debug
         carComponent.print();
         
      } catch (Exception e) {
         e.printStackTrace(); 
      }
      */
            
//   }
   
   
   // Parse
   // Format : 
   //      HAS PART: <part1>, <part2>, <part3>
   //          HAS PART: <sub_part1>, <sub_part2>, <sub_part3>
   public void parse(String filename) {      
      PartHier current;
      int currentLevel = 0;
      current = this;
            
      try {
         BufferedReader brf =  new BufferedReader(new FileReader(filename));
         String line = "";
         while ( (line = brf.readLine()) != null ) {
            int level = (line.indexOf("HAS PART:") - 10)/4 + 1;
            String txt[] = line.split(":");
            PartHier p = new PartHier(txt[1].split(","));
        

            if (level - currentLevel == 1) {
               currentLevel ++;
               current.addChild(p);
            } else {
               while (level - currentLevel != 1 ){
                  currentLevel --;
                  current = current.parent;
               }
               current.addChild(p);
               currentLevel = level;
            }
            
            current = p;

         }        
         // Debug
         //this.print();
         
      } catch (Exception e) {
         e.printStackTrace(); 
      }      
   }
   
   

      
   public PartHier() {   
      part = new Hashtable<String, String>();
      parent = null;
      children = new Vector<PartHier>(0);
   }
   
   
   
   // Java 5.0+
   public PartHier(String... vars) {
      part = new Hashtable<String, String>();
      parent = null;
      children = new Vector<PartHier>(0);      
      for (String s: vars) { 
         s = s.trim();
         s = s.replaceAll(" ", "_");
         part.put(s, s);
      }
   }
   
   public int setParent(PartHier p) {
      this.parent = p;
      p.children.add(this);
      return 0;
   }
   
   public int addChild(PartHier p) {
      p.parent = this;
      this.children.add(p);
      return 0;
   }
   
   public void print() {
      recursivePrint(0);
   }
   
   public void recursivePrint(int lvl) {
      Set<String> s = part.keySet();
      for (int i=0; i < lvl; i++) {
         System.out.print(">");
      }
      
      System.out.println(s);
      int level = ++lvl;
      for (int i=0; i < children.size(); i++) {
         children.elementAt(i).recursivePrint(level);   
      }
   }
   
   
   public String toString() {
      return this.part.toString();
   }

   /*
   public int search(String str) {
      return part.contains(str) == true ? 1: 0;
   }
   */
   
   public PartHier search(String str) {
      PartHier result = null;
      if (this.part.containsKey(str)) {
         return this; 
      } else {         
         for (int i=0; i < children.size(); i++) {
            //System.out.println("Debug: Searching children");
            result =  children.elementAt(i).search( str );
            if (result != null) {
               break;
            }
         }
      }
      return result;
   }
      
   
   
   public Hashtable<String, String> part;   
   public PartHier parent;
   public Vector<PartHier> children; 
}



