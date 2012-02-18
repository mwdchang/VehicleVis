package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/////////////////////////////////////////////////////////////////////////////////
// Miscellaneous stuff that doesn't really belong in any categories
//
//  getMonthTransationTable - Convert integer month to string literals
//  checkPoint              - Pause the project by printing out a check point line and waiting for user input
//  makeInClause            - Create SQL IN clause
//  removeLowerBound        - Remove values <= bound
//  removeUpperBound        - Remove valeus >= bound
//  getDeriv                - Gives the 'derivatives' of an array of numbers
//  BContain                - Bitwise op
//  between                 - If b <= a <= c (order matters)
//  betweenX                - If b < a < c (order matters)
//  openReader              - Generic Java buffered file reader
//  openWriter              - Generic Java buffered file writer
//  fromArray               - Convert array into String
//  range                   - Slice and dice array
//  formatDateStr           - Date formatter
//  formatDateYYYYMMDD      - Date formatter
//  startTimer              - Start timer, using System.currentTimeMillis
//  endTimer                - End timer, using System.currentTimeMillis
//
/////////////////////////////////////////////////////////////////////////////////
public class DCUtil {
   
//   public static void main(String args[]) {
//      System.out.println(DCUtil.abbreviation(451));
//      System.out.println(DCUtil.abbreviation(439));
//      System.out.println(DCUtil.abbreviation(1910));
//   }
   
   
   public static void checkPoint(String s) {
      // Just a hack here to check mem usage
      InputStreamReader cin = new InputStreamReader(System.in);
      try {
         System.out.println("Check point (" + s + "):");
         cin.read();
      } catch (Exception e) {}
   }
   
   
   public static int[] subArray(int a[], int b[]) {
      // Sanity check
      if (a.length != b.length) {
         System.err.println("Cannot operate on array of different sizes : " + a.length + " " + b.length);
      }
      
      // Returns a new array that is the difference of a-b
      int result[] = new int[a.length];
      for (int i=0; i < a.length; i++) {
         result[i] = a[i] - b[i];
      }
      return result;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get the time elapsed with System.currentTimeMillis
   ////////////////////////////////////////////////////////////////////////////////
   public static void startTimer(String s) {
      System.out.print(s);
      timeStart = System.currentTimeMillis();
   }
   public static void endTimer(String s) {
      timeEnd = System.currentTimeMillis();
      System.out.println(s + "(" + (timeEnd-timeStart) + ")");
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Get the month tranlation string -- there is probably a better way to do this...
   ////////////////////////////////////////////////////////////////////////////////
   public static Hashtable<String, String> getMonthTranslationTable() {
      Hashtable<String, String> result = new Hashtable<String, String>();
      result.put(1+"", "Jan");
      result.put(2+"", "Feb");
      result.put(3+"", "Mar");
      result.put(4+"", "Apr");
      result.put(5+"", "May");
      result.put(6+"", "Jun");
      result.put(7+"", "Jul");
      result.put(8+"", "Aug");
      result.put(9+"", "Sep");
      result.put(10+"", "Oct");
      result.put(11+"", "Nov");
      result.put(12+"", "Dec");
      
      return result;
   }


   ////////////////////////////////////////////////////////////////////////////////  
   // Format a number to its abbreviated format in terms of thousands(k)
   ////////////////////////////////////////////////////////////////////////////////  
   public static String abbreviation(int d) {
      int result = d;
      int remainder = d % 1000;
      result -= remainder;
      
      String s = d +"";
      //System.out.println(s + " " + s.length());
      if (s.length() > 3) {
         return result/1000 + "." + Math.round( remainder/100.0) + "k";
      } else {
         return "0." + Math.round( remainder/100.0) + "k";
      }
      
      /*
      String suffix[] = new String[]{"", "k", "m", "b", "t"}; // nothing, thousand, million, billion, trillion
      String r = new DecimalFormat("##0E0").format(d);
      r = r.replaceAll("E[0-9]", suffix[Character.getNumericValue(r.charAt(r.length()-1))/3]);
      return r.length()> 4 ?  r.replaceAll("\\.[0-9]+", "") : r;
      */
   }
      
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Convert a vector into a SQL acceptable in 
   // Not sure if there is a something like this some where...apache commons ?
   ////////////////////////////////////////////////////////////////////////////////
   public static String makeInClause(Vector<Integer> list) {
      String str = "(";
      for (int i=0; i < list.size(); i++) {
         str += list.elementAt(i);   
         if (i != list.size()-1) str += ", ";
      }
      str += ")";
      return str;
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Remove anything lower than bound
   ////////////////////////////////////////////////////////////////////////////////
   public static void removeLowerBound(Hashtable<String, Integer> a, int bound) {
   	Enumeration<String> e = a.keys();
   	while (e.hasMoreElements()) {
   		String key = e.nextElement();
   		if ( a.get( key ) <= bound ) 
   			a.remove(key);
   	}
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Remove anything higher than bound
   ////////////////////////////////////////////////////////////////////////////////
   public static void removeUpperBound(Hashtable<String, Integer> a, int bound) {
    	Enumeration<String> e = a.keys();
   	while (e.hasMoreElements()) {
   		String key = e.nextElement();
   		if ( a.get( key ) >= bound ) 
   			a.remove(key);
   	}
  	
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns a derivative of a series of numbers
   ////////////////////////////////////////////////////////////////////////////////
   public static float[] getDeriv(float data[]) {
      float result[] = new float[ data.length ];
      result[0] = 0; //data[0];
      for (int i=1; i < result.length; i++) {
         result[i] = (data[i] - data[i-1]);
      }
      return result;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Bit wise operators
   ////////////////////////////////////////////////////////////////////////////////
   public static boolean BContain(int a, int b) {
      return (a | b) == b;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns true if a is between b and c
   ////////////////////////////////////////////////////////////////////////////////
   public static boolean between(double a, double b, double c) {
      return ( a >= b && a <= c);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns true if a is between b and c exclusiviely
   ////////////////////////////////////////////////////////////////////////////////
   public static boolean betweenX(double a, double b, double c) {
      return (a > b && a < c); 
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns a buffered reader handle
   ////////////////////////////////////////////////////////////////////////////////
   public static BufferedReader openReader(String name) throws Exception {
      return new BufferedReader( new FileReader( name ));
   }
   public static BufferedReader openReader(String folder, String name) throws Exception {
      return openReader(folder+name);   
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns a buffered writer handle
   ////////////////////////////////////////////////////////////////////////////////
   public static BufferedWriter openWriter(String name) throws Exception  {
      return new BufferedWriter( new FileWriter( name ));
   }
   public static BufferedWriter openWriter(String folder, String name) throws Exception {
      return openWriter(folder+name);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Convert an string array to a string
   ////////////////////////////////////////////////////////////////////////////////
   public static String fromArray(String[] s, String delim) {
      StringBuffer sb = new StringBuffer();
      for (int i=0; i < s.length; i++) {
         sb.append(s[i]);   
         if (i < s.length-1) sb.append( delim );
      }
      return sb.toString();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////   
   // Returns arbitrary sub sections of  a string array
   // There are no out of bounds checks
   ////////////////////////////////////////////////////////////////////////////////   
   public static String[] range(String[] str, int... d) {
      String s[] = new String[d.length];
      for (int i=0; i < d.length; i++) {
         s[i] = str[ d[i] ];   
      }
      return s;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Formats a date string from yyyymmdd to yyyy/mm/dd
   ////////////////////////////////////////////////////////////////////////////////
   public static String formatDateStr(String str) {
      try {
         return sdfOut.format(sdfIn.parse(str));   
      } catch (Exception e) {
         return null;
      }
   }
   
   public static String formatDateYYYYMMDD(Date d) {
      try {
         return sdfIn.format(d);
      } catch (Exception e) {
         e.printStackTrace();
         return null;   
      }
   }
   
   
   
   
   
   public static long timeStart;
   public static long timeEnd;
   public static SimpleDateFormat sdfIn  = new SimpleDateFormat("yyyyMMdd");
   public static SimpleDateFormat sdfOut = new SimpleDateFormat("yyyy/MM/dd");
   
}
