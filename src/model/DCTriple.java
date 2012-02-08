package model;
/* This class represent a triplet in the form of <x,y,z> */

public class DCTriple extends DCObj {
   public float x, y, z;
   
   public DCTriple() {      
   }
   
   public DCTriple(float[] val) {
      this.x = val[0]/val[3];   
      this.y = val[1]/val[3];   
      this.z = val[2]/val[3];   
   }
   
   public DCTriple(double[] val) {
      this.x = (float)(val[0]/val[3]);   
      this.y = (float)(val[1]/val[3]);   
      this.z = (float)(val[2]/val[3]);   
   }
   
   public DCTriple(DCTriple copy) {
     this.x = copy.x;
     this.y = copy.y;
     this.z = copy.z;
   }
   
   public DCTriple(float x, float y, float z) {
      this.x = x; this.y = y; this.z = z;
   }
   
   // Addition
   public DCTriple add(DCTriple p) {
      return new DCTriple(this.x + p.x, this.y+p.y, this.z+p.z);
   }
   
   // Subtraction
   public DCTriple sub(DCTriple p) {
      return new DCTriple(this.x - p.x, this.y-p.y, this.z-p.z);
   }
   
   // Scalar multiplication
   public DCTriple mult(float p) {
      return new DCTriple( p*this.x, p*this.y, p*this.z);
   }
   
   // Scalar divide
   public DCTriple div(float p) {
      return new DCTriple( this.x/p, this.y/p, this.z/p);
   }
   
   // Dot product
   public float dot(DCTriple p) {
      return this.x*p.x + this.y*p.y + this.z*p.z;
   }
      
   // Magnitude
   public float mag2() {
      return this.x*this.x + this.y*this.y + this.z*this.z;
   }
   
   // Magnitude
   public float mag() {
      return (float)Math.sqrt(mag2());
   }
   
   // unit vector 
   public void normalize() {
      float mag = mag();
      this.x /= mag;
      this.y /= mag;
      this.z /= mag;
   }
   
   
   
   // Cross product
   public DCTriple cross(DCTriple p) {
      return new DCTriple(
         this.y*p.z - this.z*p.y,
         this.z*p.x - this.x*p.z,
         this.x*p.y - this.y*p.x
      );
   }
   
   
   // check if point is on the plane
   // given ax+by+cz+d = 0 planar equation
   public float isOnPlane(float a, float b, float c, float d) {
   	return a*this.x + b*this.y + c*this.z + d;
   }
   
   
   // To array formats
   public float[] toArrayf() {
      return new float[]{this.x, this.y, this.z, 1.0f};   
   }
   
   public double[] toArrayd() {
      return new double[]{this.x, this.y, this.z, 1.0};   
   }
   
   public float[] toArray3f() {
      return new float[] { this.x, this.y, this.z };   
   }
   
   public double[] toArray3d() {
      return new double[] { this.x, this.y, this.z };       
   }
   
   
   
   @Override 
   public boolean equals(Object obj) {
      if (obj==null || ! (obj instanceof DCTriple)) return false;   
      return this.x == ((DCTriple)obj).x && this.y == ((DCTriple)obj).y && this.z == ((DCTriple)obj).z;
   }
   
   @Override
   public int hashCode() {
      return toString().hashCode();   
   }
   
   
   @Override
   public String toString() {
      return "(" + x + ", " + y + ", " + z + ")";
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Ray-Quad intersection
   // Assumes a 1x1 quad with bottom left corner at (0,0,0) and normal at (0, 0, 1)
   // So our quad is (0,0,0) => (1, 0, 0) => (1, 1, 0) => (0, 1, 0)
   //
   // Returns a T value, >=0 if intersecting, -1 if not
   ////////////////////////////////////////////////////////////////////////////////
   public static float findIntersection( DCTriple eyePosition, DCTriple rayVector) {
      float T = -1.0f;    
      
      // Default assumption
      DCTriple pointOnQuad  = new DCTriple(0.0f, 0.0f, 0.0f);
      DCTriple normalVector = new DCTriple(0.0f, 0.0f, 1.0f); 
      
      float nominator   = -normalVector.dot(eyePosition.sub(pointOnQuad));
      float denominator =  normalVector.dot(rayVector);
      
      T = nominator / denominator;
      
      
      // Check if we miss the plane completely
      if (T < 0) return -1;
      
      // Check boundaries
      DCTriple hit = eyePosition.add(rayVector.mult(T));
      //System.out.println("My T in method is : " + T + " My hit point is : " + hit.toString()); 
      if (hit.x < 0 || hit.x > 1) return -1;
      if (hit.y < 0 || hit.y > 1) return -1;
      
      return T;
   }
   
   
      
}
