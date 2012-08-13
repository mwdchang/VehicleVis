package parser;

import java.io.BufferedReader;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import util.DCUtil;

import datastore.Const;
import db.DBWrapper;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

////////////////////////////////////////////////////////////////////////////////
// One off helper class to find missing entities in wordnet
////////////////////////////////////////////////////////////////////////////////
public class FindNewEntity {
   public static void main(String[] args) {
      FindNewEntity test = new FindNewEntity();
      test.parse("C:\\Users\\Daniel\\StaticResources\\FLAT_CMPL.txt", 1, 50);
   }
   
   public FindNewEntity() {}
   
   
   // Main parsing loop
   public int parse(String filename, int startIdx, int endIdx) {
      Hashtable<String, String> dict = createDictionary();
      
      LexicalizedParser lp = new LexicalizedParser(Const.GRAMMAR_FILE);
      lp.setOptionFlags("-maxLength", "80", "-retainTmpSubcategories"); 
      TreebankLanguagePack tlp = new PennTreebankLanguagePack();
      GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();      
      
      try {
         int counter = 0;
         BufferedReader reader = DCUtil.openReader(filename);
         String line;
         
         Hashtable<String, Integer> missingPartTable = new Hashtable<String, Integer>();
         
         while ( (line = reader.readLine()) != null) {
            
            // Index handling
            counter++;
            if (counter < startIdx) continue; 
            if (counter > endIdx) continue;
            
            // Fetch and correct string description
            String desc = line.split("\t")[ Const.DESC_IDX ];   
            desc = desc.toLowerCase(); 
            
            Tree parseTree = lp.apply(desc);
            GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
            Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed(false);
            Iterator<TypedDependency> i = tdl.iterator();            
            while (i.hasNext()) {
               TypedDependency tp = (TypedDependency)i.next();
               //System.out.println(tp.toString());               
               if (tp.reln().toString().equals("nn") || tp.reln().toString().equals("nsubj")) {
                  String gov = tp.gov().toString();
                  String dep = tp.dep().toString();
                  
                  gov = gov.replaceAll("-.*$", "");
                  dep = dep.replaceAll("-.*$", "");
                  
                  //System.out.println( gov );
                  Integer tmp = missingPartTable.get(gov);
                  int val;
                  if (tmp == null) {
                     val = 1;   
                  } else {
                     val = tmp.intValue() + 1;
                  }
                  missingPartTable.put(gov, val);
               }
            }
            //System.out.println(counter + "> " + desc);
         } // end while
         
         // print out the missing parts
         for (String s : missingPartTable.keySet()) {
            System.out.println( s + " " + missingPartTable.get(s));
         }
         
      } catch (Exception e) { e.printStackTrace(); }
      
      return 0;
   }
   
   
   public Hashtable<String, String> createDictionary() {
      Hashtable<String, String> result = new Hashtable<String, String>();   
      DBWrapper dbh = new DBWrapper();
      
      try {
         ResultSet rs = dbh.execute("select * from grp");
         while (rs!= null && rs.next()) {
            String id = rs.getString(1);
            String name = rs.getString(2).trim();
            
            // Unfuddle the underscores
            name = name.replace("_", " ");
            
            result.put(name, name);
         }
      } catch (Exception e) { e.printStackTrace(); }
      return result;
   }

}
