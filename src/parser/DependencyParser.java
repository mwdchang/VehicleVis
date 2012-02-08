package parser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import util.DWin;

import datastore.Const;
import datastore.HierarchyTable;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;

// Parser for the car complaint database
public class DependencyParser {   
   
   // For sampling test
   public BufferedWriter partWriter = null;
   
   public static void main(String args[]) {      
      try {
         DependencyParser dp = new DependencyParser();  
         dp.parse3(Const.DATA_FILE, 1, 100);
      } catch (Exception e) {}
      
      /*
      try {
         DependencyParser dp = new DependencyParser();
         int record = 40000;
         int start = 1;
         for (int i=0; i < 23; i++) {
            dp.parse3(Const.DATA_FILE, start, record);
            start += record;
         }
      } catch(Exception e) {
         e.printStackTrace();
      }
      */
   }   
   
   
//   public static void main(String args[]) {      
//      try {
//         DependencyParser dp = new DependencyParser();
//         //dp.parse(Const.DATA_FILE);
//         int record = 500;
//         int start = 1;
//         for (int i=0; i < 1; i++) {
//            dp.parse2(Const.DATA_FILE, start, record);
//            start += record;
//         }
//      } catch(Exception e) {
//         e.printStackTrace();
//      }
//   }
   
   
//   public static void main(String args[]) {
//      DependencyParser dp = new DependencyParser();
//      LexicalizedParser lp = new LexicalizedParser(Const.GRAMMAR_FILE);
//      TreebankLanguagePack tlp = new PennTreebankLanguagePack();
//      GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
//      lp.setOptionFlags("-maxLength", "80", "-retainTmpSubcategories");
//
//      //String sentence = "Vehicle stalls at high speed, resulting in loss of steering ability";
//      //String sentence = "The accelerator was sticking in the vehicle";
//      //String sentence = "Customer says vehicle stalls when turning, resulting in loss of steering ability";
//      //String sentence = "Mutation0 increased resistance to Drug0 but not Drug1";
//      //String sentence = "Dan punches Mike, resulting in Mike falling backward.";
//      //String sentence = "Car fire caused by overheated engine.";
//      String sentence = "Overheated engine resulted in giant car fire.";
//      //String sentence = "Overheated engine resulted in the fire";
//      //String sentence = "Overheated engine caused the giant fire";
//      
//      Tree parseTree = lp.apply(sentence);
//      GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
//      List<TypedDependency> tdl = gs.typedDependenciesCCprocessed(true);
//      
//      
//      
//      System.out.println(sentence);
//      System.out.println(lp.getBestParse());
//      //System.out.println(tdl.toString());
//      Iterator<TypedDependency> iter = tdl.iterator();
//      while (iter.hasNext()) System.out.println(iter.next());
//      System.out.println(""); 
//
//      ArrayList alist = new ArrayList();
//      alist.addAll(tdl);      
//      
//      
//      /////////////////////////////////////////////////////////////////////////           
//      //1) Direct/Indirect object relations, A implies B and that sorts of thing
//      /////////////////////////////////////////////////////////////////////////      
//      for (int i=0; i < alist.size(); i++) {
//    	  TypedDependency td = (TypedDependency)alist.get(i);
//    	  
//    	  // caused, causes
//    	  if (td.reln().toString().equals("dobj")) {
//             String govStr = td.gov().toString().split("-")[0];  
//             String govPos = td.gov().toString().split("-")[1];
//             String depStr = td.dep().toString().split("-")[0];
//             String depPos = td.dep().toString().split("-")[1];
//                                      
//             // Find the noun subject
//             for (int j=i; j >= 0; j--) {
//            	 TypedDependency t = (TypedDependency)alist.get(j);
//            	 //if (t.reln().toString().equals("nsubj") ||
//            	 //    t.reln().toString().equals("nn")) {
//            	 if (t.reln().toString().equals("nsubj")) {
//            		// TODO: Check if the subject is in dictionary
//            	    System.out.println(t.dep().toString() + " ==["  + govStr + "]==> "+ depStr);
//            	    break;
//            	 }            	 
//             } // end for j
//    	  }
//      } // end for
//      
//      
//      /////////////////////////////////////////////////////////////////////////      
//      //2) Prepositions, results in, caused by ... etc etc
//      /////////////////////////////////////////////////////////////////////////      
//      for (int i=0; i < alist.size(); i++) {
//    	  TypedDependency td = (TypedDependency)alist.get(i);
//    	  if (td.reln().toString().matches("^prep.*")) {
//              String govStr = td.gov().toString().split("-")[0];  
//              String govPos = td.gov().toString().split("-")[1];
//              String depStr = td.dep().toString().split("-")[0];
//              String depPos = td.dep().toString().split("-")[1];
//              
//              // Find the noun subject
//              for (int j=i; j >= 0; j--) {
//             	 TypedDependency t = (TypedDependency)alist.get(j);             	 
//            	 //if (t.reln().toString().equals("nsubj") ||
//                //     t.reln().toString().equals("nn")) {
//            	 if (t.reln().toString().equals("nsubj")) { 
//             		// TODO: Check if the subject is in dictionary
//                     System.out.println(t.dep().toString() + " ==["  + govStr + "]==> "+ depStr);
//                     break;
//                 }             	              	 
//              } // end for j
//    	  } 
//      } // end for i
//      
//      
//      
//      // Find by using grammatical relations
//      /*
//      iter = tdl.iterator();
//      while (iter.hasNext()) {
//         TypedDependency td = iter.next();   
//         if (td.reln().toString().equals("nsubj")) {
//            String subj_gov = td.gov().toString().split("-")[0];
//            while (iter.hasNext()) {
//               TypedDependency td2 = iter.next();      
//               String rln = td2.reln().toString().split("_")[0];
//               String dep = td2.dep().toString().split("-")[0];
//               String gov = td2.gov().toString().split("-")[0];
//               // Should be a verb most of the time
//               if (subj_gov.equals(gov) && rln.equals("dobj")) {
//                  causeStr  = td.dep().toString();   
//                  effectStr = td2.dep().toString();
//               } else if (subj_gov.equals(gov) && rln.equals("prep")) {
//                  if (td2.reln().toString().split("_")[1].equals("by")) {
//                     causeStr  = td2.dep().toString();
//                     effectStr = td.dep().toString();
//                  } else {
//                     causeStr  = td.dep().toString();   
//                     effectStr = td2.dep().toString();
//                  }
//               }
//            }
//         } // End if subj
//      }
//      System.out.println(causeStr + "==>" + effectStr);
//      */ 
//      
//      
//      /*
//      Collection<TreeGraphNode> nc = gs.getNodes();
//      Iterator<TreeGraphNode> iter2 = nc.iterator();
//      while (iter2.hasNext()) {
//      }
//      */
//      
//      /* 1) Looking for <np> <prep> <np> */
//      /*
//      TypedDependency subj = dp.findSpecificRelation(tdl, "nsubj", -1); //start searching at the beginning
//      if (subj != null) {
//         //System.out.println(subj.dep().toString());
//         int subj_position = Integer.parseInt(subj.dep().toString().split("-")[1]);
//         TypedDependency prep = dp.findSpecificRelation(tdl, "prep", subj_position); //start searching after subject position
//         if (prep != null) {
//            // Use canned data to differentiate [cause]==>[effect] and [effect]<==[cause] structures, 
//            // for example: [X} caused by [Y]  versus [Y] results in [X}
//            String prepStr = prep.gov().toString().split("-")[0] + " " + prep.reln().toString().split("_")[1]; 
//            if (prepStr.equals("caused by")) {
//               effectStr = subj.dep().toString();  
//               causeStr  = prep.dep().toString();
//            } else if (prepStr.equals("resulted in")) {
//               causeStr  = subj.dep().toString();
//               effectStr = prep.dep().toString();
//            } else {
//               effectStr = subj.dep().toString();  
//               causeStr  = prep.dep().toString();
//            }
//            
//            System.out.println("prep : " + prepStr);
//            System.out.println(causeStr + " ==> " + effectStr);
//         }
//      }
//      */
//
//   }
   
   
   public DependencyParser() {
      try {
         //partWriter = new BufferedWriter(new FileWriter("partsFound.txt"));
         //DWin.instance().setFilter(0);
      } catch (Exception e) {}
   }
   
   
   // Super inefficient crawler to find relations in the typeddependency collection
   // .... but it should work well
//   public TypedDependency findSpecificRelation( Collection<TypedDependency> tdl, String rln, int start) {
//      Iterator<TypedDependency> iter = tdl.iterator();
//      final short distance = 5;
//      int cnt = 0;
//      while (iter.hasNext()) {
//         cnt ++;
//         TypedDependency td = iter.next();
//         // We found the relation we are looking for, 
//         // now check if this relation is within the specified distance field
//         if (td.reln().toString().split("_")[0].equals(rln)) {
//            //int depIdx = Integer.parseInt(depToken.split("-")[1]);
//            //if ( (Math.abs(depIdx - start) > 4 || start == depIdx) && start > 0) continue;
//            
//            if ( (cnt - start > distance || cnt == start) && start > 0) continue;
//            return td;
//         }
//      }
//      return null;
//   }
   
   
//   public void findRelations(Collection<TypedDependency> tdl) {
//      //Iterator<TypedDependency> i = tdl.iterator();
//      TypedDependency[] depList = tdl.toArray(new TypedDependency[0]);
//      //System.out.println( depList );
//      
//      String subject  = "";
//      // Find the subject(s)
//      for (int i=0; i < depList.length; i++) {
//         String rln = depList[i].reln().toString();
//         String gov = depList[i].gov().toString();
//         String dep = depList[i].dep().toString();
//         
//         if (rln.equals("nsubj")) {
//            System.out.println("Subject ==> " + dep);
//            subject = dep;
//            break;
//         } 
//      }
//      
//      // Find the relation
//      for (int i=0; i < depList.length; i++) {
//         String rln = depList[i].reln().toString();
//         String gov = depList[i].gov().toString();
//         String dep = depList[i].dep().toString();
//         if (rln.equals("dobj") || rln.equals("ccomp") || rln.equals("xcomp")) {
//            System.out.println(gov + " ==> " + dep + "[" + rln + "]");
//            // Find the 2nd subject ?? Look at the neighbours to find if a noun object exist           
//            for (int j=i; j <= i+3 && j < depList.length; j++) {
//               String rln2 = depList[j].reln().toString();
//               String gov2 = depList[j].gov().toString();
//               String dep2 = depList[j].dep().toString();
//               if (rln.equals("nsubj")) {
//                  //System.out.println(gov + " " + gov2);
//               }
//            }
//         }
//      }
//   }
   
   
   
   public int parse3(String filename, int startnum, int record) throws Exception { 
      StatManager.instance();
      
      // Flush and start a new file 
      if (partWriter != null) { partWriter.flush(); partWriter.close(); }
      partWriter = new BufferedWriter(new FileWriter("partsFound" + startnum + "_" + record + ".txt"));
      
      LexicalizedParser lp = new LexicalizedParser(Const.GRAMMAR_FILE);
      TreebankLanguagePack tlp = new PennTreebankLanguagePack();
      GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
      lp.setOptionFlags("-maxLength", "80", "-retainTmpSubcategories");
      
      // Init the dictionary and hierarchies      
      //Hashtable<String, String> partTable = StatManager.instance().dict;
      System.out.println("Reading : " + filename.toString() + " => " + "[" + startnum + "-" + (startnum+record) + "]");
      
      int counter = 0; // count the line numbers      
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      String line = "";      
      
      while ( (line=reader.readLine()) != null) {
         counter ++;
           
         // Check exit and invalid conditions
         if (counter > (startnum + record - 1)) break; 
         if (counter < startnum) continue;
         
         // Main parsing logic
         String textId = line.split("\t")[0];
         String desc = line.split("\t")[Const.DESC_IDX].toLowerCase();      // Fetch description
         desc = StatManager.instance().cleanString(desc);                   // Clean and tag the description
         
         System.err.println(counter + "[" + textId + "]");
        
         //DWin.instance().debug(desc);
         DocumentPreprocessor processor =  new DocumentPreprocessor(new StringReader(desc));
         Iterator<List<HasWord>> sentenceIterator;
         Hashtable<String, String> partTable = new Hashtable<String, String>();
         
         
         // Loop through each sentence in the description and get a dependency tree
         sentenceIterator = processor.iterator();
         while (sentenceIterator.hasNext()) {
            List<HasWord> sentence = sentenceIterator.next();
            
            for (int i=0; i < sentence.size(); i++) {
               String wordToken = sentence.get(i).word();   
               if (StatManager.instance().dict.contains(wordToken)) {
                  if (! partTable.containsKey(wordToken)) {
                     partTable.put(wordToken, wordToken);
                     // Just grab the first one for now, deal with repeat later
                     //Integer groupId = StatManager.instance().ht.getGroupId(wordToken).firstElement();
                     Integer groupId = HierarchyTable.instance().getGroupId(wordToken).firstElement();
                     partWriter.write(textId + "\t" + groupId +"\r\n");
                     partWriter.flush();
                  }
               }
            }
            
            /* temp hack for parts */ if (true) continue; /* end temp hack */
            
            Tree parseTree = lp.apply( sentence );
            GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
            List<TypedDependency> tdl = gs.typedDependenciesCCprocessed(false);
            
            // Limit a minimum size
            if (tdl.size() < 2) {
               System.err.println("Sentence too short...skipping");
               continue;
            }
            parse3Helper(textId, tdl, sentence);
            
         } // End sentenceIterator
      } // End while readLine
      
      // Clean up 
      reader.close();
      return 0;
   }
   
   
   
   /* does the grammar work */
   public int parse3Helper(String textId, List<TypedDependency> tdl, List<HasWord> sentence) {
      
      DWin.instance().debug("\n"); 
      ArrayList<TypedDependency> alist = new ArrayList<TypedDependency>(); 
      alist.addAll(tdl);      
      
      String s = ">>";
      DWin.instance().error(" ");
      for (int i=0; i < sentence.size(); i++) s += " " + sentence.get(i).word();
      DWin.instance().error(s);
      
      /////////////////////////////////////////////////////////////////////////           
      //0) Calculate statistics for this sentence
      /////////////////////////////////////////////////////////////////////////      
      for (int i=0; i < sentence.size(); i++) {
         //DWin.instance().debug(sentence.get(i).word());
         String word = sentence.get(i).word();
         if (StatManager.instance().dict.contains(word)) {
            DWin.instance().msg(word);   
         }
      }
      for (int i=0; i < tdl.size(); i++) {
         DWin.instance().msg(tdl.get(i).toString());
      }
      
      
      /////////////////////////////////////////////////////////////////////////           
      //1) Direct/Indirect object relations, A implies B and that sorts of thing
      /////////////////////////////////////////////////////////////////////////      
      for (int i=0; i < alist.size(); i++) {
    	  TypedDependency td = (TypedDependency)alist.get(i);    	  
    	  if (td.reln().toString().equals("dobj")) {
             String govStr = td.gov().toString().split("-")[0];  
             String govPos = td.gov().toString().split("-")[1];
             String depStr = td.dep().toString().split("-")[0];
             String depPos = td.dep().toString().split("-")[1];
                                      
             // Find the noun subject
             for (int j=i; j >= 0; j--) {
            	 TypedDependency t = (TypedDependency)alist.get(j);
            	 //if (t.reln().toString().equals("nsubj") ||
            	 //    t.reln().toString().equals("nn")) {
            	 if (t.reln().toString().equals("nsubj")) {
            		// TODO: Check if the subject is in dictionary
            	    //System.out.println(t.dep().toString() + " ==["  + govStr + "]==> "+ depStr);
            	    DWin.instance().debug(t.dep().toString() + " ==["  + govStr + "]==> "+ depStr);
            	    break;
            	 }            	 
             } // end for j
    	   }
      } // end for
   
      
      /////////////////////////////////////////////////////////////////////////      
      //2) Prepositions, results in, caused by ... etc etc
      /////////////////////////////////////////////////////////////////////////      
      for (int i=0; i < alist.size(); i++) {
        TypedDependency td = (TypedDependency)alist.get(i);
        if (td.reln().toString().matches("^prep.*")) {
              String govStr = td.gov().toString().split("-")[0];  
              String govPos = td.gov().toString().split("-")[1];
              String depStr = td.dep().toString().split("-")[0];
              String depPos = td.dep().toString().split("-")[1];
              
              // Find the noun subject
              for (int j=i; j >= 0; j--) {
                TypedDependency t = (TypedDependency)alist.get(j);                
                //if (t.reln().toString().equals("nsubj") ||
                //     t.reln().toString().equals("nn")) {
                if (t.reln().toString().equals("nsubj")) { 
                  // TODO: Check if the subject is in dictionary
                     //System.out.println(t.dep().toString() + " ==["  + govStr + "]==> "+ depStr);
                     DWin.instance().debug(t.dep().toString() + " ==[" + govStr + "]==> " + depStr);
                     break;
                 }                               
              } // end for j
        } 
      } // end for i
 
      
      
      return 0;    
   }
   
   
   
   
   
   // parser version 2
   // Include intermediate output files for parts analysis and
   // line number chunking for multi-threads later on....
   // Disregards the stemmer for now, it seems to have issues parsing tokens that ends in "e" => 2011_06_16
   //   filename - data file to parse
   //   startnum - starting line number
   //   record   - number of records to consume
//   public int parse2(String filename, int startnum, int record) throws Exception {
//      StatManager.instance();
//      LexicalizedParser lp = new LexicalizedParser(Const.GRAMMAR_FILE);
//      TreebankLanguagePack tlp = new PennTreebankLanguagePack();
//      GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
//      lp.setOptionFlags("-maxLength", "80", "-retainTmpSubcategories");
//
//      // Init the dictionary and hierarchies      
//      Hashtable<String, String> partTable = StatManager.instance().dict;
//      
//      System.out.println("Reading : " + filename.toString() + " => " + "[" + startnum + "-" + (startnum+record) + "]");
//       
//      int counter = 0; // count the line numbers      
//      BufferedReader reader = new BufferedReader(new FileReader(filename));
//      String line = "";      
//      while ( (line=reader.readLine()) != null) {
//         counter ++;
//         
//         // Check exit and invalid conditions
//         if (counter > (startnum + record - 1)) break; 
//         if (counter < startnum) continue;
//         
//         // Main parsing logic
//         String sentence = line.split("\t")[Const.DESC_IDX].toLowerCase();      // Fetch description
//         sentence = StatManager.instance().cleanString(sentence);               // Cleanse description
//         
//         // Hackhack
//         //sentence = "The truck's front brakes do not operate, they wear out quickly and cause greater stopping distance.";
//         //sentence = "Blown tires cause car to stop suddenly";
//         
//         System.out.println(counter + "\t: " + sentence);
//         
//         Tree parseTree = lp.apply(sentence);
//         GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
//         Collection<TypedDependency> tdl = gs.typedDependenciesCCprocessed(false);
//         Iterator<TypedDependency> i = tdl.iterator();
//         
//         while( i.hasNext() ) {
//            TypedDependency tp = (TypedDependency)i.next();
//            
//            System.out.println(tp.toString());
//            
//            // Check if the objects identified by the stanford parser exist in our part table
//            if (tp.reln().toString().equals("nn") || tp.reln().toString().equals("nsubj")) {
//               boolean found = false;
//               String token = "";
//               token =  tp.gov().toString().replaceAll("-.*$", "");
//               if (partTable.containsValue(token)) {
//                  StatManager.instance().incrementPartCount(token);
//                  found = true;
//               }
//               
//               token = tp.dep().toString().replaceAll("-.*$", "");
//               if (partTable.containsValue(token)) {
//                  StatManager.instance().incrementPartCount(token);
//                  found = true;
//               }
//               if (! found) StatManager.instance().incrementMissingPartCount(token);                 
//            }                                     
//         } // End while iterator
//         
//         
//         //findSpecificRelation( Collection<TypedDependency> tdl, String rln, int start) {
//         //findRelations(tdl);
//         //findSpecificRelation(tdl, "nsubj", -1);
//         //findSpecificRelation(tdl, "nn",  2);
//         //findSpecificRelation(tdl, "xcomp", 3);
//         
//      } // End while (Line=reader.readLine())
//      reader.close();
//            
//      
//      // Write the debug output to track missing parts
//      /*
//      BufferedWriter bw = new BufferedWriter(new FileWriter("debug_" + startnum + ".txt")); 
//      Enumeration<String> en = StatManager.instance().missingPartTable.keys();      
//      while(en.hasMoreElements()) {
//         String key = en.nextElement();
//         bw.write(key + " ==> " + StatManager.instance().missingPartTable.get(key) );
//         bw.newLine();
//         bw.flush();  // Don't buffer
//      }
//      bw.flush();
//      bw.close();
//      StatManager.instance().missingPartTable.clear();
//      */
//      // Done write debug output
//      
//      return 0;
//   }
   
   
   
   
   
      
   // Version 1
   /*
   public int parse(String filename) throws Exception {
      LexicalizedParser lp = new LexicalizedParser(grammar_path);
      lp.setOptionFlags("-maxLength", "80", "-retainTmpSubcategories"); // Check out the options !!!
      TreebankLanguagePack tlp = new PennTreebankLanguagePack();
      GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
      //DocumentPreprocessor dp = new DocumentPreprocessor( filename );      
      
      //Iterator<List<HasWord>> sentences = null;
      //sentences = dp.iterator();
      System.out.println("Reading : " + filename.toString());
      int counter = 0;
      String line;
      BufferedReader reader = new BufferedReader(new FileReader(filename));
      StatManager.instance(); // Init the constructor to load the data files
       
      
      int debug_counter = 0; //TODO:Remove this later
      while ( (line=reader.readLine()) != null && counter < 100000) {
         debug_counter++; if (debug_counter % 2 == 0) continue; //TODO: Remove this later
         
         String data_str[] = line.split("\t");        
         
         
         String sentence = data_str[Const.DESC_IDX].toLowerCase();
         //String sentence = data_str[19].toLowerCase();
         //String sentence = "electrical system failed";
         sentence = StatManager.instance().cleanString(sentence);
         
         
         Tree parseTree = lp.apply(sentence);
         GrammaticalStructure gs = gsf.newGrammaticalStructure(parseTree);
         Collection tdl = gs.typedDependenciesCCprocessed(true);         
         System.out.println("\n==> Sentence: " + sentence + "[" + data_str[Const.DESC_IDX] + "]" );         
         Iterator i = tdl.iterator();
         
         Hashtable<String, String> dict = StatManager.instance().dict;
         SnowballStemmer stemmer = (SnowballStemmer)new englishStemmer();
         
         while( i.hasNext() ) {
            TypedDependency tp = (TypedDependency)i.next();
            //if (tp.reln().toString().equals("nsubj")) {
            //   System.out.println(tp.dep().toString());
            //}            
            System.out.println( tp.reln().toString() + " " + tp.gov().toString() + " " + tp.dep().toString());
            
            
            // Check if the part hierarchy can catch any part tokens for nn and nsubj
            // relations
            if (tp.reln().toString().equals("nn") || tp.reln().toString().equals("nsubj")) {
               boolean found = false;
               String token = "";
               String stemmedToken = "";
               token =  tp.gov().toString().replaceAll("-.*$", "");               
               //stemmer.setCurrent(token);
               //stemmer.stem();
               //stemmedToken = stemmer.getCurrent();
               //if (! stemmedToken.equals(token)) { System.out.println(token + " == " + stemmedToken); }
               if (dict.containsValue(token)) {
                  StatManager.instance().incrementPartCount(token);
                  found = true;
               }
                              
               token = tp.dep().toString().replaceAll("-.*$", "");
               //stemmer.setCurrent(token);
               //stemmer.stem();
               //stemmedToken = stemmer.getCurrent();
               //if (! stemmedToken.equals(token)) { System.out.println(token + " == " + stemmedToken); }
               if (dict.containsValue(token)) {
                  StatManager.instance().incrementPartCount(token);
                  found = true;
               }                            
               
               if (! found) {
                  StatManager.instance().incrementMissingPartCount(token);
               }
               
            }
                                   
         }                      
         counter ++;
       } // End while
      reader.close();
      
      StatManager.instance().print();
      
      return 0;
   }
   */
         
}
