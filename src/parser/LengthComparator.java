package parser;

import java.util.Comparator;

public class LengthComparator implements Comparator<String> {
   
   @Override
   public int compare(String a, String b) {
      int alen = a.length();
      int blen = b.length();
      
      if (alen < blen) return 1;
      if (alen > blen) return -1;
      return 0;
   }

}
