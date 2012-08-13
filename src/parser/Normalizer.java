package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.text.SimpleDateFormat;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Vector;

import datastore.Const;

import util.DCUtil;
import util.DWin;

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
      
      Vector<String> whiteList = new Vector<String>();
      whiteList.add("GENERAL MOTORS CORP.");
      
      n.parse( Const.DATA_FILE, 0, whiteList, true);
      
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
   public int parse(String filename, int num, Vector<String> whiteList, boolean replaceNames) {
      String segments[] = null;
      String line;
      BufferedReader reader = null;
      BufferedWriter writer = null;
      SimpleDateFormat sdfIn = null;
      SimpleDateFormat sdfOut   = null;
      
      // Used to avoid repeated text description
      // Hashcode -> Last unique document identifier
      Hashtable<Long, String> collisionTable = new Hashtable<Long, String>();
      int collision = 0;
      
      DWin.instance().debug("File name: " + filename);
      DWin.instance().debug("White List: " + whiteList);
      DWin.instance().debug("Using replacement: " + replaceNames);
      
      int counter = 0;
      try {
         reader = DCUtil.openReader(filename);    
         writer = DCUtil.openWriter("cmp_clean.txt");
         sdfIn  = new SimpleDateFormat("yyyymmdd");
         sdfOut = new SimpleDateFormat("yyyy/mm/dd");
         
         
         int mfr_count   = 0;
         int make_count  = 0;
         int model_count = 0;
         Hashtable<String, String> replacementTable = new Hashtable<String, String>();
         
         while ( (line = reader.readLine()) != null ) {
            
            if (  num > 0 && counter >= num) { 
               break;
            }
            counter++;
            line = line.replaceAll("\t", "\t ");
            segments = line.split("\t");   
            
            if (counter % 10000 == 0) {
               System.out.println("Processed : " + counter + " rows");
            }            
            
            boolean isCollision = false;
            
            
            ////////////////////////////////////////////////////////////////////////////////
            // Only take in complaints relating to vehicles
            ////////////////////////////////////////////////////////////////////////////////
            String prod_type = segments[Const.PROD_TYPE_IDX].trim();;
            if (prod_type.equalsIgnoreCase("C") || prod_type.equalsIgnoreCase("T") || prod_type.equalsIgnoreCase("E"))
               continue;
            
            
            // Get rid of extra spaces
            for (int i=0; i < segments.length; i++) 
               segments[i] = segments[i].trim();
            
            
            ////////////////////////////////////////////////////////////////////////////////
            // Only take in white listed vehicles if a list is provided
            ////////////////////////////////////////////////////////////////////////////////
            if (whiteList != null) {
               if ( ! whiteList.contains( segments[Const.MFR_IDX] )) continue;
            }
            
            
            
            
            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll("Tt.$", "");        // Clean up trailing characters
            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll("\\*ak.$", "");     // Clean up trailing characters
            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll("\\*skd.$", "");    // Clean up trailing characters
            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll("\\*jb.$", "");     // Clean up trailing characters
            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll("\\*aw.$", "");     // Clean up trailing characters
            
            
            // Hacky replacement scheme to rub out identifiable names
            if (replaceNames == true) {
               String mfr_key   = segments[Const.MFR_IDX];
               String make_key  = segments[Const.MFR_IDX] + "_" + segments[Const.MAKE_IDX];
               String model_key = segments[Const.MFR_IDX] + "_" + segments[Const.MAKE_IDX] + "_" + segments[Const.MODEL_IDX];
               if ( replacementTable.get( mfr_key ) == null ) {
                  replacementTable.put( mfr_key, "MFR"+mfr_count);
                  mfr_count++;
               }
               if ( replacementTable.get( make_key ) == null ) {
                  replacementTable.put(make_key, "MAKE"+make_count);
                  make_count++; 
               }
               if ( replacementTable.get( model_key ) == null ) {
                  replacementTable.put( model_key , "MODEL"+model_count);
                  model_count++;
               }
               
               segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll( (segments[Const.MAKE_IDX] + " " + segments[Const.MODEL_IDX]) , replacementTable.get(model_key)); 
               segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll( (segments[Const.MAKE_IDX]) , replacementTable.get(make_key)); 
               segments[Const.DESC_IDX] = segments[Const.DESC_IDX].replaceAll( (segments[Const.MODEL_IDX]) , replacementTable.get(model_key)); 
               
               
               segments[Const.MFR_IDX] = replacementTable.get( mfr_key );
               segments[Const.MAKE_IDX] = replacementTable.get( make_key );
               segments[Const.MODEL_IDX] = replacementTable.get( model_key );
            }
            // End crazy hacky replacement scheme
            
            
            
            // Normalize descriptions
            segments[Const.DESC_IDX] = normalizDescription( segments[Const.DESC_IDX] );
            segments[Const.DESC_IDX] = segments[Const.DESC_IDX].trim();
            segments[Const.DATEA_IDX] = sdfOut.format(sdfIn.parse(segments[Const.DATEA_IDX]));            
            
            
            // Check to see if there are any collisions (Same repeated 
            long hashCode = segments[Const.DESC_IDX].hashCode();
            if (collisionTable.get(hashCode) != null) {
               String complaintId = collisionTable.get( hashCode );
               // now check if the ids are too close apart
               if ( Math.abs(Integer.valueOf(segments[Const.CMPL_IDX]).intValue() - Integer.valueOf(complaintId).intValue()) < 10 ) {
                  System.err.println("Collision detected at : " + segments[Const.CMPL_IDX]);   
                  collision++;
                  isCollision = true;
               }
            }
            collisionTable.put(hashCode, segments[Const.CMPL_IDX]);
            
            
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
            
            // Only write the record if it is unique
            if (isCollision == false) {
               writer.write( DCUtil.fromArray(subArray, "\t"));
               writer.newLine();
            }

         }
         System.out.println("Processed : " + counter + " rows");
         reader.close();
         writer.flush();
         writer.close();
         
         DWin.instance().debug("Found: " + collision + " collisions");
         
         
         // Write out the key-value pairs
         writer = DCUtil.openWriter("KeyValue.txt");
         for (String k : replacementTable.keySet()) {
            writer.write(k + "|" + replacementTable.get(k));   
            writer.newLine();
         }
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


