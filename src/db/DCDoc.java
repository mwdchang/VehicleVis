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
   
   public int docId;
   public String txt;
   
   
   // Cache
   public Vector<String> tok = null;
   public Vector<String> tokClean = null;
   
}
