package util;

import java.io.BufferedWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/////////////////////////////////////////////////////////////////////////////////
// Logs user activity
/////////////////////////////////////////////////////////////////////////////////
public class ALogger {

   
   public static void main(String args[]) throws Exception {
      ALogger.instance();
      ALogger.instance().log("Test 1");
      Thread.sleep(4000);
      ALogger.instance().log("Test 2");
      Thread.sleep(4000);
      ALogger.instance().log("Test 3");
      ALogger.instance().cleanup();
   }
   
   
   
   // Only call this at program end
   public void cleanup() {
      try {
         writer.flush();
         writer.close();
      } catch (Exception e) {
         e.printStackTrace(); 
      }
   }
   
   
   
   public void log(String s) {
      Calendar cal = Calendar.getInstance();
      String time = this.prefixFormatter.format(cal.getTime());
      try {
         writer.write(time + "\t" + s);    
         writer.newLine();
         writer.flush();
      } catch (Exception e) {}
   }
   
   
   protected ALogger() {
      
      // Get a usable file name
      String name = "";
      int num = 1;
      
      // Check if the file exist or not, if exist increment suffix counter to 
      // make it unique
      Calendar cal = Calendar.getInstance();
      while(true) {
         name = ACTIVITY_LOG_DIR + "LOG_" + formatter.format(cal.getTime()) + "_" + num + ".txt";
         File f = new File(name);
         if (f.exists()) {
            System.err.println("File already exist...incrementing counter");
            num++;
         } else {
            break;
         }
      }
      
      try {
         writer = DCUtil.openWriter(name);
      } catch (Exception e) {
         e.printStackTrace();
      }
      
      
      
   }
   
   
   public static ALogger instance() {
      if (inst == null) {
         inst = new ALogger();
      }
      return inst;
   }
   
   
   private static ALogger inst;
   public SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd");
   public SimpleDateFormat prefixFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
   
   public static String ACTIVITY_LOG_DIR = "C:\\users\\daniel\\temporary\\";
   public BufferedWriter writer;   
}
