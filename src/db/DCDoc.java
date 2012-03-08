package db;

import java.util.Vector;

/////////////////////////////////////////////////////////////////////////////////
// Document representation
/////////////////////////////////////////////////////////////////////////////////
public class DCDoc {
   
   public DCDoc(int id, String t) {
      docId = id;
      txt   = t;
   }
   public DCDoc(int id, String d, String mfr, String make, String model, String year, String t) {
      this.docId = id;
      this.mfr = mfr;
      this.make = make;
      this.model = model;
      this.year = year;
      this.datea = d;
      this.txt = t;
   }
   
   public int docId;
   public String txt;
   
   // Domain Specific Stuff
   public String mfr;
   public String make;
   public String model;
   public String year;
   public String datea;
   
   
   // Cache
   public Vector<String> tok = null;
   public Vector<String> tokClean = null;
   
}
