package util;

// Matrix Library ... 
// Ported from my raytracer
public class DCMatrix {
   
   
   public static void main(String args[]) {
      DCMatrix test1 = new DCMatrix();
      DCMatrix test2 = new DCMatrix();
      
      test2.v[1]=2;
      test2.v[2]=3;
      test2.v[3]=4;
      
      test1.v[1]=2;
      test1.v[2]=3;
      test1.v[3]=4;
      
      test1.mult(test2).print(); 
   }
   
   
   // Default constructor - identity matrix
   public DCMatrix() {
      for (int i=0; i < 16; i++) v[i] = 0;
      v[0] = v[5] = v[10] = v[15] = 1.0f;
   }
   
   public DCMatrix( DCMatrix c ) {
      for (int i=0; i < 16; i++) v[i] = c.v[i];
   }
   
   
   public DCMatrix mult(DCMatrix c) {
      DCMatrix result = new DCMatrix();
      
      for (int i=0; i < 4; ++i) {
         int x = 4*i;
         for (int j=0; j < 4; ++j) {
            result.v[x+j] = this.v[x] * c.v[j] +
                            this.v[x+1] * c.v[4+j] +
                            this.v[x+2] * c.v[8+j] +
                            this.v[x+3] * c.v[12+j];
         }
      }
      return result;
   }
   
   
   public void print() {
      for (int i=0; i < 16; i++) {
         System.out.print(v[i]+"\t");    
         if (i==3||i==7||i==11||i==15) System.out.print("\n");
      }
   }
   
   
   
   float v[] = new float[16];
}
