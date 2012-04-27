package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.Hashtable;

/////////////////////////////////////////////////////////////////////////////////
// Cheap and hacky serialization to speed up development  
//  --> Serialize data query to file format to save connection queries
/////////////////////////////////////////////////////////////////////////////////
public class SerializeUtil {
   
   
   
//   public static void main(String args[]) {
//      Hashtable<String, String> blarg = new Hashtable<String, String>();   
//      blarg.put("KeyOne", "ValueOne");
//      blarg.put("KeyTwo", "ValueTwo");
//      
//      SerializeUtil.objOut(blarg, "blarg.ser");
//      
//      Hashtable<String, String> read = new Hashtable<String, String>();
//      read = (Hashtable<String, String>)SerializeUtil.objIn("blarg.ser");
//      
//      System.out.println("read keys : " + read.keySet());
//      
//      
//      Hashtable<String, String> h = dump(new SerializeUtil());
//      System.out.println(h);
//   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Dumps object field status
   // Assumes that toString() is implemented for complex objects
   ////////////////////////////////////////////////////////////////////////////////
   public static Hashtable<String, String> dump(Object o) {
      Hashtable<String, String> result = new Hashtable<String, String>();
      try {
         Class c = o.getClass();
         Field[] flist = c.getFields();
         
         for (int i=0; i < flist.length; i++) {
            Field f = flist[i];
            String key = f.getName();
            String val = (f.get(o)) == null? "null" : (f.get(o)).toString();
            result.put(key, val);
         }
      } catch (Exception e) {
         // This is not a fatal error, ignore at will
         e.printStackTrace();
      }
      return result;
   }
   
   
   public static final String OBJ_HOME = "C:\\Users\\daniel\\temporary\\";
   
   //////////////////////////////////////////////////////////////////////////////// 
   // Serialize to object file
   //////////////////////////////////////////////////////////////////////////////// 
   public static void objOut(Object obj, String name) throws Exception {
      System.out.println("serializing " + obj.getClass().toString() + " to " + name);
      String filename = OBJ_HOME + name;      
      FileOutputStream fileOut = new FileOutputStream(filename);
      ObjectOutputStream objOut = new ObjectOutputStream(fileOut);
         
      objOut.writeObject(obj);
      objOut.flush();
      objOut.close();
      fileOut.close();
   }
   
   
   //////////////////////////////////////////////////////////////////////////////// 
   // De-serialize to object file
   //////////////////////////////////////////////////////////////////////////////// 
   public static Object objIn(String name) throws Exception {
      Object obj = null;
      String filename = OBJ_HOME + name;      
      FileInputStream fileIn = new FileInputStream(filename);
      ObjectInputStream objIn = new ObjectInputStream(fileIn);
         
      obj = objIn.readObject();
      objIn.close();
      fileIn.close();
      return obj;
   }
   
   
}
