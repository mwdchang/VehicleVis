package exec;

import java.io.BufferedWriter;
import java.util.Scanner;
import java.util.Vector;

import util.DCUtil;


public class QLogger {

   public static void main(String args[]) {
      QLogger q = new QLogger();
      try {
         q.init();
         q.run();
      } catch (Exception e) { e.printStackTrace(); System.exit(-1); }
   }
   
   public void run() throws Exception {
      BufferedWriter bw = DCUtil.openWriter("Test.txt");
      Scanner scanner = new Scanner(System.in);
      for (int i=0; i < qlist.size(); i++) {
         System.out.println(qlist.elementAt(i));
         bw.write("..............");
         bw.newLine();
         bw.write(qlist.elementAt(i));
         bw.newLine();
         long start = System.currentTimeMillis();
         String input = scanner.nextLine();
         long end = System.currentTimeMillis();
         bw.write(input);
         bw.newLine();
         bw.write( String.valueOf((end-start)) );
         bw.newLine();
      }
      bw.flush();
      bw.close();
      System.out.println(" All Done ");
   }
   
   
   public void init() {
      qlist.add("Press Enter to Start");
      qlist.add("What is the most frequently occurring component in 1998");
   }
   
   public Vector<String> qlist = new Vector<String>();
   
}
