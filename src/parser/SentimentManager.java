package parser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import datastore.Const;

// Singleton class to hold sentiment evaluations using SentiWordNet 3.0
public class SentimentManager {
   private static SentimentManager instance = null;
   public static HashMap<String, Double> dict;

   // Unit Test
   public static void main(String args[]) {
      SentimentManager.instance();
      System.out.println( SentimentManager.instance().lookup("sad"));
   }
   
   // Sentiment value lookup
   // For now just iterate through the variations of words (nouns, verbs..etc etc)
   // and return the first one that is not null
   public Double lookup(String s) {      
      //return dict.get(s);
      String lookup = s.replaceAll(" ", "_");     
      String[] variation = {"#n", "#a", "#v", "#r"};
      Double d = null;
      for (int i=0; i < variation.length; i++) {
         String tmp = lookup + variation[i];         
         d = dict.get(tmp);
         if (d != null) break;
      }      
      return d;
   }
   
   // Accessor
   public static SentimentManager instance() {
      if (instance == null) { instance = new SentimentManager(); }
      return instance;
   }      
   
   // Initialize the sentiwordnet file into a dictionary
   protected SentimentManager() {
      try {
         System.out.println("Reading : " + Const.SWN_FILE);
         BufferedReader reader = new BufferedReader(new FileReader(Const.SWN_FILE));
         String line = "";
         HashMap<String, Vector<Double>> _temp = new HashMap<String, Vector<Double>>();
         dict = new HashMap<String, Double>();
         
         while ((line=reader.readLine()) != null) {
            // Skip parsing if line is empty or starts with a comment(#)
            if (line.isEmpty() || line.startsWith("#")) continue;
            
            // Segment into data string tokens
            // Algorithm to calculate the score is taken from the sentiwordnet web site
            // http://sentiwordnet.isti.cnr.it/
            String data[] = line.split("\t");
            String words[] = data[4].split(" ");
            double score = Double.parseDouble(data[2]) - Double.parseDouble(data[3]);
            
            // Java 5.0+ paradigm .... fix this later ???
            for(String w:words) {
               String[] w_n = w.split("#");
               w_n[0] += "#"+data[0];
               int index = Integer.parseInt(w_n[1])-1;
               if(_temp.containsKey(w_n[0])) {
                  Vector<Double> v = _temp.get(w_n[0]);
                  if(index>v.size())
                     for(int i = v.size();i<index; i++)
                        v.add(0.0);
                  v.add(index, score);
                  _temp.put(w_n[0], v);
               } else {
                  Vector<Double> v = new Vector<Double>();
                  for(int i = 0;i<index; i++)
                     v.add(0.0);
                  v.add(index, score);
                  _temp.put(w_n[0], v);
               }
            } // End for            
         } // End while
         
         // Now re-score and put into dictionary by words
         Set<String> temp = _temp.keySet();
         for (Iterator<String> iterator = temp.iterator(); iterator.hasNext();) {
            String word = (String) iterator.next();
            Vector<Double> v = _temp.get(word);
            double score = 0.0;
            double sum = 0.0;
            for(int i = 0; i < v.size(); i++)
               score += ((double)1/(double)(i+1))*v.get(i);
            for(int i = 1; i<=v.size(); i++)
               sum += (double)1/(double)i;
            score /= sum;
            //if (word == null) { System.out.println("Arrrrrrrrrrrrrrrrrgggggggg"); }
            //System.out.println("word is " + word);
            dict.put(word, Double.valueOf(score));
         }       
         
         // Info + sanity check
         System.out.println("Sent Dictionary Size: " + dict.size());
         
      } catch (Exception e) {
         e.printStackTrace();
         System.exit(0);
      }
   }
   
     
}
