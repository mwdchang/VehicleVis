package util;

import java.util.Vector;


/////////////////////////////////////////////////////////////////////////////////
// Provides a bunch of string related utility functions
/////////////////////////////////////////////////////////////////////////////////
public class StringUtil {
   
   public static String cleanString(String str) {
      String r = str;
      r = r.replaceAll("\\p{Punct}", ""); 
      r = r.replaceAll("\\s\\s+", " ");
      return r;
   }
   
   public static Vector<String> splitSpace(String str) {
      Vector<String> list = new Vector<String>();   
      String r = str.replaceAll("\\s\\s+", " ");
//      String token[] = r.split("\\s\\s+");
      String token[] = r.split(" ");
      
      for (int i=0; i < token.length; i++) {
         list.add( token[i] );   
      }
      return list;
   }
   
}
