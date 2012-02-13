package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

import datastore.Const;

import util.DCUtil;

/////////////////////////////////////////////////////////////////////////////////
// This class is responsible for normalizing the input data into a sensible format
// 1) Format (as much as we can) into proper upper and lower case phrases.
// 2) Remove weird characters or cluster of characters that makes no sense
//
// TODO ?
//
// Input : The raw file
// Output: A stripped down, normalized version
/////////////////////////////////////////////////////////////////////////////////
public class Normalizer {
   
   public static void main(String args[]) {
      Normalizer n = new Normalizer();    
      
      String s = "Test something something something. Tt.";
      
      //s = s.replaceAll("Tt.$", "");
      
      //System.out.println(s);
      
      n.parse( Const.DATA_FILE, 0 );
      
      /*
      String s = "THE QUICK BROWN    FOX JUMPS OVER THE LAZY BLUE COW. Blah blah blah is said to I to be very blah, so blah off";
      System.out.println(s);
      System.out.println(n.normalizDescription(s));
      */
   }

   
   public Normalizer() {
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Performs mixed case transformation of arbitrary text 
   //   filename - name of the text file
   //   num      - number of records to look (for debugging)
   ////////////////////////////////////////////////////////////////////////////////
   public int parse(String filename, int num) {
      String segments[] = null;
      String line;
      BufferedReader reader = null;
      BufferedWriter writer = null;
      SimpleDateFormat sdfIn = null;
      SimpleDateFormat sdfOut   = null;
      
      
      int counter = 0;
      try {
         reader = DCUtil.openReader(filename);    
         writer = DCUtil.openWriter("cmp_clean.txt");
         sdfIn  = new SimpleDateFormat("yyyymmdd");
         sdfOut = new SimpleDateFormat("yyyy/mm/dd");
         
         while ( (line = reader.readLine()) != null ) {
            if (  num > 0 && counter >= num) { 
               break;
            }
            counter++;
            line = line.replaceAll("\t", "\t ");
            segments = line.split("\t");   
            
            String prod_type = segments[Const.PROD_TYPE_IDX].trim();;
            if (prod_type.equalsIgnoreCase("C") || prod_type.equalsIgnoreCase("T") || prod_type.equalsIgnoreCase("E"))
               continue;
            
            for (int i=0; i < segments.length; i++) 
               segments[i] = segments[i].trim();
            
            
            
            // Normalize descriptions
            segments[Const.DESC_IDX] = normalizDescription( segments[Const.DESC_IDX] );
            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].trim();
            segments[Const.DATEA_IDX] = sdfOut.format(sdfIn.parse(segments[Const.DATEA_IDX]));
            
            
//            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll("Tt.$", "");        // Clean up trailing characters
//            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll("\\*ak.$", "");     // Clean up trailing characters
//            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll("\\*skd.$", "");     // Clean up trailing characters
//            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll("\\*jb.$", "");     // Clean up trailing characters
//            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll("\\*aw.$", "");     // Clean up trailing characters
            
            
            // Recreate a lean and mean version of the raw data
            String subArray[] = DCUtil.range(segments, 
               Const.CMPL_IDX, 
               Const.DATEA_IDX,
               Const.MFR_IDX,
               Const.MAKE_IDX,
               Const.MODEL_IDX,
               Const.YEAR_IDX,
               Const.DESC_IDX
            );
            
            writer.write( DCUtil.fromArray(subArray, "\t"));
            writer.newLine();
            if (counter % 10000 == 0) {
               System.out.println("Processed : " + counter + " rows");
            }
         }
         System.out.println("Processed : " + counter + " rows");
         reader.close();
         writer.flush();
         writer.close();
      } catch (Exception e) {
         e.printStackTrace();
         System.out.println("Last processed index : " + counter);
         System.out.println("Line token # : " + segments.length);
         System.exit(0);
      }
     return 0;
   }
   
   
   
   public String normalizDescription(String s) {
      if (s == null || s.length() == 0) return "";
      String l[] = s.split("\\.");   
      String r = "";
      for (int i=0; i < l.length; i++) {
         l[i] = l[i].trim();
         l[i] = l[i].replaceAll("\\s\\s+", " ");
         if ( l[i].length() > 1) {
            r += l[i].substring(0, 1).toUpperCase() + l[i].substring(1).toLowerCase()  + ". ";     
         } else {
            r += l[i];
         }
      }
      
      // Undo the over-zealous format
      r = r.replaceAll("\\si\\s", " I ");
      return r;
   }
   
}


