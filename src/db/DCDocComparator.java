package db;

import java.util.Comparator;

/////////////////////////////////////////////////////////////////////////////////
// Straight forward comparator that compares document by document id (complaint_id)
/////////////////////////////////////////////////////////////////////////////////
public class DCDocComparator<DCDOc> implements Comparator<DCDoc> {

   @Override
   public int compare(DCDoc docA, DCDoc docB) {
       return docA.docId - docB.docId; 
   }
   

}
