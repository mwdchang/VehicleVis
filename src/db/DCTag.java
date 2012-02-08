package db;

import java.io.Serializable;


/////////////////////////////////////////////////////////////////////////////////
// This is an ORM model corresponding to our tagging of components
/////////////////////////////////////////////////////////////////////////////////
public class DCTag implements Serializable {
   
   private static final long serialVersionUID = 123456789;
   
   public DCTag(int _id, short _groupId, short _start, short _end) {
      id = _id;
      groupId = _groupId;
      start = _start;
      end = _end;
   }
   
   
   public boolean contains(int idx) {
      return idx >= start && idx <= end;
   }
   
   
   public int id;
   public int groupId;
   public short start;
   public short end;
}
