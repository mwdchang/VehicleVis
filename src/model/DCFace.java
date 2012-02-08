package model;

public class DCFace extends DCObj {
   public DCTriple p1, p2, p3;    // Vertices
   public DCTriple n1, n2, n3;    // Vertex Normals
   public DCTriple c1, c2, c3;    // Vertex Colours
   public DCTriple midpoint;      // Centroid of the face
   public DCTriple en1, en2, en3; // Edge normals (face_normal X edge)
   public DCTriple fn;            // Face normal
   
   // The vertices of adjacent triangles
   // a1 : between p1 and p2
   // a2 : between p2 and p3
   // a3 : between p3 and p1
   public DCTriple a1, a2, a3;    
   
   
   
   public DCFace() {      
   }
   
   
   public DCFace(DCFace copy) {
      this.p1 = new DCTriple(copy.p1);    
      this.p2 = new DCTriple(copy.p2);    
      this.p3 = new DCTriple(copy.p3);    
      this.n1 = new DCTriple(copy.n1);    
      this.n2 = new DCTriple(copy.n2);    
      this.n3 = new DCTriple(copy.n3);    
   }
      
   public DCFace(DCTriple p1, DCTriple p2, DCTriple p3) {
      this.p1 = p1; this.p2 = p2; this.p3 = p3;
      this.n1=this.n2=this.n3 = new DCTriple(0,0,0);
   }
   
   // interpolate face normal across
   public void calNormal() {
      n1 = (p2.sub(p1).cross(p3.sub(p1)));   
      n1.normalize();
      n2 = n1;
      n3 = n2;
   }
      
   // Recalculate the attributes that depends on the
   // vertex and vertex normals
   //
   //      P1
   //     /  \
   //    P2__ P3
   // 
   public void recalc() {
      fn = (p2.sub(p1)).cross(p3.sub(p2));
      fn.normalize();
      
      en1 = (p2.sub(p1)).cross(fn);
      en2 = (p3.sub(p2)).cross(fn);
      en3 = (p1.sub(p3)).cross(fn);
      en1.normalize();
      en2.normalize();
      en3.normalize();
      
      midpoint = new DCTriple(
         (p1.x+p2.x+p3.x)/3.0f,       
         (p1.y+p2.y+p3.y)/3.0f,       
         (p1.z+p2.z+p3.z)/3.0f       
      );
   }
   
   @Override
   public String toString() {
      return p1.toString() + "-" + p2.toString() + "-" + p3.toString();	
   }
}
