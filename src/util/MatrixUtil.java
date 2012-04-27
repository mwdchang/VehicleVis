package util;

import java.text.DecimalFormat;
import Jama.Matrix;

////////////////////////////////////////////////////////////////////////////////
// A wrapper for matrix calculations, particularly for linear transformation 
// and openGL transformation mappings ==> multiplication and inverse
//
// Uses Jama library: http://math.nist.gov/javanumerics/jama/ for the internal mathematics
////////////////////////////////////////////////////////////////////////////////
public class MatrixUtil {
   
	public static void main(String[] args) {
	   Matrix a = MatrixUtil.translationMatrix(2, 3, 4);
	   Matrix b = MatrixUtil.inverse(a);
	   Matrix c = MatrixUtil.mult(a, b);
	   System.out.println(MatrixUtil.getMatrixString(a));
	   System.out.println(MatrixUtil.getMatrixString(b));
	   System.out.println(MatrixUtil.getMatrixString(c));
	   
	   double test[] = {3, 0, 0, 0};
	   
	   double result[] = MatrixUtil.multVector(a, test);
	   System.out.println("Result is :" + MatrixUtil.getVectorString(result));
	   
	   double result2[] = MatrixUtil.multVector(b, result);
	   System.out.println("Result is :" + MatrixUtil.getVectorString(result2));
	   
	   Matrix i = MatrixUtil.identity();
	   System.out.println(i.getArray()[1][0]); 
	   System.out.println(i.getArray()[1][1]); 
	   System.out.println(i.getArray()[1][2]); 
	   System.out.println(i.getArray()[1][3]); 
	}
	
	
	// Multiplication
	// Pass-thru to keep interface consistent
	public static Matrix mult(Matrix a, Matrix b) {
		return a.times(b);
	}
	
	// Inverse matrix
	// Pass-thru to keep interface consistent
	public static Matrix inverse(Matrix a) {
		return a.inverse();
	}
	
	
	// returns a 4x4 identity matrix
	public static Matrix identity() {
	   double val[][] = {
	      {1, 0, 0, 0},
	      {0, 1, 0, 0},
	      {0, 0, 1, 0},
	      {0, 0, 0, 1}
	   };
	   return new Matrix(val);
	}
	
	
	// Flatten a 4x4 array to a 1x16 array
	public static double[] flattenArray(Matrix a) {
	   double result[] = new double[16];   
	   for (int i=0; i < a.getRowDimension(); i++) {
	      for (int j=0; j < a.getColumnDimension(); j++) {
	        result[4*i+j] = a.get(j, i);
	      }
	   }
	   return result;
	}
	
	
	
	
	// Build a glRotatef matrix
   //   angle : angle in degrees
	//   axis  : X,Y,Z or x,y,z
	public static Matrix rotationMatrix(double angle, String axis) {
		double val[][];
		double cosAng = Math.cos(2*angle*Math.PI/360.0);
		double sinAng = Math.sin(2*angle*Math.PI/360.0);
		
		if (axis.equalsIgnoreCase("Z")) {
		   val = new double[][] {
		   	{cosAng, -sinAng, 0, 0},
		   	{sinAng,  cosAng, 0, 0},
		   	{0, 0, 1, 0},
		   	{0, 0, 0, 1}
		   };
		   return new Matrix(val);
		} else if (axis.equalsIgnoreCase("Y")) {
			val = new double[][] {
				{cosAng, 0, sinAng, 0},
				{0, 1, 0, 0},
				{-sinAng, 0, cosAng, 0},
				{0, 0, 0, 1}
			};
		   return new Matrix(val);
		} else if (axis.equalsIgnoreCase("X")) {
		   val = new double[][] {
		   	{1, 0, 0, 0},
		   	{0, cosAng, -sinAng, 0},
		   	{0, sinAng,  cosAng, 0},
		   	{0, 0, 0, 1}
		   };
		   return new Matrix(val);
		}
	   return null;	
	}
	
	
	
	// Build a glTranslatef matrix
	public static Matrix translationMatrix(double x, double y, double z) {
	   double val[][] = {
	   		{1, 0, 0, x},
	   		{0, 1, 0, y},
	   		{0, 0, 1, z},
	   		{0, 0, 0, 1}
	   };
	   return new Matrix(val);
	}
	
	
	// Build a scaling matrix
	public static Matrix scalingMatrix(double x, double y, double z) {
	   double val[][] = {
	         {x, 0, 0, 0},
	         {0, y, 0, 0},
	         {0, 0, z, 0},
	         {0, 0, 0, 1}
	   };
	   return new Matrix(val);
	}
	
	
	
	// Get string representation
	public static String getMatrixString(Matrix m) {
		String str = "";
		DecimalFormat nf = new DecimalFormat("0.00");
		for (int i=0; i < 4; i++) {
		   str += nf.format(m.get(i, 0)) + "\t" + nf.format(m.get(i, 1)) + "\t" + nf.format(m.get(i, 2)) + "\t" + nf.format(m.get(i, 3)) + "\n";
		}
		return str;
	}
	
	
	// Print a vector of arbitrary length
	public static String getVectorString(double v[]) {
		String str = "[";
		for (int i=0; i < v.length; i++) {
			str += v[i] + ", ";
		}
		str += "]";
		return str;
	}
	
	
	// Does M*V 
	// where M is a 4x4 matrix and V is a 4x1 vector
	public static double[] multVector(Matrix m, double[] v) {
	   double val[][] = m.getArray();	
	   double result[] = new double[4];
	   for (int i=0; i < 4; i++) {
	      result[i] = val[i][0] * v[0] +
	                  val[i][1] * v[1] +
	                  val[i][2] * v[2] +
	                  val[i][3] * v[3];
	   }
	   return result;
	}
	public static float[] multVector(Matrix m, float[] v) {
	   double val[][] = m.getArray();	
	   float result[] = new float[4];
	   for (int i=0; i < 4; i++) {
	      result[i] = (float)val[i][0] * v[0] +
	                  (float)val[i][1] * v[1] +
	                  (float)val[i][2] * v[2] +
	                  (float)val[i][3] * v[3];   
	   }
	   return result;
	}
	
	
}
