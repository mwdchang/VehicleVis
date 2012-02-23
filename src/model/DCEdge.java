package model;

/////////////////////////////////////////////////////////////////////////////////
// An edge that explicitly states two vertices
/////////////////////////////////////////////////////////////////////////////////
public class DCEdge {
   public DCEdge(DCTriple a, DCTriple b) {
      p1 = a;
      p2 = b;
   }
   
   public DCEdge() {
      p1 = new DCTriple();
      p2 = new DCTriple();
   }
   
   public DCTriple p1;
   public DCTriple p2;
}
