package gui;

public class DCPair {
   
   public DCPair(String k, double v) {
      key = k;
      value = v; 
   }
   
   
   public String toString() {
      return "[" + key + "->" + value + "]";    
   }
   
   public String key;
   public double value;
   
}
