package util;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.JFileChooser;

import model.DCTriple;

/////////////////////////////////////////////////////////////////////////////////
// Miscellaneous stuff that doesn't really belong in any categories
//
//  atan2                   - NVidia's ATAN2 approximation
//  pointInTriangle         - Check if point is in triangle
//  areaTriangle            - Calculate area of triangle with cross product
//  dist                    - Calculate the pythagorean distance
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
   
//   public static void main(String args[]) {
//      DCTriple a = new DCTriple(0, 0, 0);
//      DCTriple b = new DCTriple(2, 0, 0);
//      DCTriple c = new DCTriple(2, 2, 0);
//      DCTriple test = new DCTriple(1.5, 1.6, 0);
//      
//      System.out.println(DCUtil.areaTriangle(a, b, c));
//      System.out.println(DCUtil.pointInTriangle(test, a, b, c));
//   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // NVidia's Atan2 approximation algorithm
   // http://http.developer.nvidia.com/Cg/atan2.html
   ////////////////////////////////////////////////////////////////////////////////
   public static double atan2(double y, double x) {
      double t0, t1, t2, t3, t4;   
      
      t3 = Math.abs( x );
      t1 = Math.abs( y );
      t0 = Math.max(t3, t1);
      t1 = Math.min(t3, t1);
      t3 = 1.0/t0;
      t3 = t1*t3;
      
      t4 = t3 * t3;
      t0 =         - (0.013480470);
      t0 = t0 * t4 + (0.057477314);
      t0 = t0 * t4 - (0.121239071);
      t0 = t0 * t4 + (0.195635925);
      t0 = t0 * t4 - (0.332994597);
      t0 = t0 * t4 + (0.999995630);
      t3 = t0 * t3;

      t3 = (Math.abs(y) > Math.abs(x)) ? (1.570796327) - t3 : t3;
      t3 = (x < 0) ?  Math.PI - t3 : t3;
      t3 = (y < 0) ? -t3 : t3;

      return t3;      
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   //
   // Check 2D line segment intersection (P1, P2) and (P3, P4)
   // Return the intersection point, or null of not intersecting or parallel (incidental)
   //
   //    P3    P2
   //      \  /
   //       \/
   //       /\
   //      /  \
   //    P1    P4
   //
   ////////////////////////////////////////////////////////////////////////////////
   public static DCTriple intersectLine2D(DCTriple p1, DCTriple p2, DCTriple p3, DCTriple p4) {
      
      float UA_numerator = ((p4.x - p3.x)*(p1.y - p3.y) - (p4.y - p3.y)*(p1.x - p3.x));
      float UB_numerator = ((p2.x - p1.x)*(p1.y - p3.y) - (p2.y - p1.y)*(p1.x - p3.x));
      float denominator  = ((p4.y - p3.y)*(p2.x - p1.x) - (p4.x - p3.x)*(p2.y - p1.y)); 
      
      // Incidental or parallel
      if ( Math.abs(denominator) <= 0.00001 ) return null;
      
      float UA = UA_numerator / denominator;
      float UB = UB_numerator / denominator;
      
      if (UA >= 0 && UA <= 1 && UB >=0 && UB <= 1) {
         float newX = p1.x + UA*(p2.x - p1.x);
         float newY = p1.y + UA*(p2.y - p1.y);
         
         return new DCTriple(newX, newY, 0);
      }
      
      return null;   
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Check if a point P is in triangle abc using barycentric coordinates
   ////////////////////////////////////////////////////////////////////////////////
   public static boolean pointInTriangle(DCTriple point, DCTriple a, DCTriple b, DCTriple c) {
      if (a == null || b == null || c == null) return false;
      
      double areaABC = areaTriangle(a, b, c);   
      double area1   = areaTriangle(point, a, b)/areaABC;
      double area2   = areaTriangle(point, b, c)/areaABC;
      double area3   =  1- area1 - area2;
      
      if (area1 < 0 || area1 > 1) return false;
      if (area2 < 0 || area2 > 1) return false;
      if (area3 < 0 || area3 > 1) return false;
      
      return true;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns the area of a triangle using cross products
   ////////////////////////////////////////////////////////////////////////////////
   public static double areaTriangle(DCTriple p1, DCTriple p2, DCTriple p3) {
      return 0.5*(p1.sub(p2)).cross( p2.sub(p3)).mag(); 
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns distance 
   ////////////////////////////////////////////////////////////////////////////////
   public static double dist(double x, double y) {
      return Math.sqrt( x*x + y*y);  
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Load default font
   ////////////////////////////////////////////////////////////////////////////////
   public static Font loadFont(String path, int style, float size) {
      try {
         InputStream is = new FileInputStream(new File(path));   
         return Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(style).deriveFont(size);
      } catch (Exception e) {
         e.printStackTrace();
      }
      return new Font("Arial", Font.PLAIN, 24);
   }
   
   
   public static File fileChooser() {
      JFileChooser fc = new JFileChooser();
      int r = fc.showOpenDialog(null);
      if (r == JFileChooser.APPROVE_OPTION) {
         return fc.getSelectedFile();   
      }
      return null;
   }
   
   
   public static Hashtable<Integer, Integer> mergeHash(Hashtable<Integer, Integer> a, Hashtable<Integer, Integer> b) {
      Hashtable<Integer, Integer> result = new Hashtable<Integer, Integer>();
      
      // setup a new one
      for (Integer key: a.keySet()) {
         result.put(key, a.get(key));   
      }
      
      // merge in the second one
      for (Integer key: b.keySet()) {
         if (result.containsKey(key)) {
            Integer t = result.get(key);
            result.put(key, t+b.get(key));
         } else {
            result.put(key, b.get(key)); 
         }
      }
      return result;
   }
   
   
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
   
   
   public static String formatDateTextPanel(Date d) {
      try {
         return sdfText.format(d);
      } catch (Exception e) {
         e.printStackTrace();
         return null;   
      }
   }
   
   
   
   public static long timeStart;
   public static long timeEnd;
   public static SimpleDateFormat sdfIn  = new SimpleDateFormat("yyyyMMdd");
   public static SimpleDateFormat sdfOut  = new SimpleDateFormat("yyyy/MM/dd");
   public static SimpleDateFormat sdfText = new SimpleDateFormat("yyyy-MM-dd");
   
}
