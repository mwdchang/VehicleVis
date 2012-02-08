package util;

import java.util.Vector;

import datastore.Const;

import model.DCComponent;
import model.DCFace;
import model.DCModel;
import model.DCTriple;
import model.WaveFrontLoader;

///////////////////////////////////////////////////////////////////////////////
// An implementation of the BSP Tree 
// for partitioning model component for use in
// alpha blending ( we are essentially sorting the polygons )
//
// Each BSPTree node contains a DCFace reference, which is used both
// as the cutting plane as well as the actual rendering triangle
///////////////////////////////////////////////////////////////////////////////
public class BSPTree {
	
//   public static void main(String args[]) {
//      DCModel model = new DCModel();
//      try {
//         model = WaveFrontLoader.loadWaveFrontObj(Const.MODEL_FILE);    
//      } catch (Exception e) { e.printStackTrace(); System.exit(-1);}
//      
//      
////      Vector<DCComponent> list = new Vector<DCComponent>(model.componentTable.values());
//      Vector<DCComponent> list = new Vector<DCComponent>();
//      list.add(model.componentTable.get("windshield"));
//      list.add(model.componentTable.get("wheel"));
//      
//      System.out.println("Splitting " + list.size() + " Components");
//      
//      int seed = (int)(Math.random() * list.elementAt(0).faceList.size());
//      BSPTree root = new BSPTree(list.elementAt(0).faceList.elementAt(seed));
//      
//      long start = 0, end = 0;
//      start = System.currentTimeMillis();
//      System.out.println("Time Start: " + start);
//      BSPTree.buildBSPTreeObj(root, list);
//      end = System.currentTimeMillis();
//      System.out.println("Time End: " + end);
//      System.out.println("Total Time : " + (end-start));
//      System.out.println("New # Components : " + BSPTree.sizeof(root)); 
//   }
   
   /*
	public static void main(String args[]) {
		DCFace f1 = new DCFace( new DCTriple(0.0f, 0.0f, 0.0f),
				                  new DCTriple(1.0f, 0.0f, 0.0f),
				                  new DCTriple(1.0f, 1.0f, 0.0f));
		f1.recalc();
		DCFace f2 = new DCFace( new DCTriple(0.0f, 0.0f, 1.0f),
				                  new DCTriple(1.0f, 0.0f, 1.0f),
				                  new DCTriple(1.0f, 1.0f, 1.0f));
		f2.recalc();
		DCFace f3 = new DCFace( new DCTriple(0.0f, 0.0f, -1.0f),
				                  new DCTriple(1.0f, 0.0f, -1.0f),
				                  new DCTriple(1.0f, 1.0f, -1.0f));
		f3.recalc();
		DCFace f4 = new DCFace( new DCTriple(0.0f, 0.0f, 0.5f),
				                  new DCTriple(1.0f, 0.0f, 0.5f),
				                  new DCTriple(1.0f, 1.0f, 0.5f));
		f4.recalc();
		
		Vector<DCFace> list = new Vector<DCFace>();
		list.add(f1);
		list.add(f2);
		list.add(f3);
		list.add(f4);
		
		BSPTree root = new BSPTree(list.remove(0));
		
		BSPTree.buildBSPTree(root, list);
		walk(root);
		System.out.println( "Tree has : " + BSPTree.sizeof(root) + " nodes");
	}*/
	
	
	////////////////////////////////////////////////////////////////////////////
	// Pre-order traversal : root=>left=>right
	// In-order traversal : left=>root=>right
	// Post-order traversal : left=>right=>root
	//
	// This is a in-order traversal of a BSPTree
	////////////////////////////////////////////////////////////////////////////
	public static void walk(BSPTree root) {
      if (root == null) return;
      walk(root.front);
      System.out.println(root.face.toString());
      walk(root.back);
	}
	
	
	public static void cascadeInit(BSPTree root) {
      if (root == null) return;
      if (root.compObj != null) root.compObj.recalculateAttribute();
      cascadeInit(root.front);      
      cascadeInit(root.back);      
	}
	
	
	////////////////////////////////////////////////////////////////////////////
	// Get the size of the sub tree at root
	////////////////////////////////////////////////////////////////////////////
	public static int sizeof(BSPTree root) {
      if (root == null) return 0;
      return 1 + sizeof(root.front)+ sizeof(root.back);  
	}
	
	
	
	// Slightly perturb the face to avoid infinite recursions
	public static DCFace jiggleFace(DCFace f) {
	   DCFace jiggle = new DCFace(f);
	   jiggle.p1.x += (0.5f-Math.random());
	   jiggle.p1.y += (0.5f-Math.random());
	   jiggle.p1.z += (0.5f-Math.random());
	   jiggle.p2.x += (0.5f-Math.random());
	   jiggle.p2.y += (0.5f-Math.random());
	   jiggle.p2.z += (0.5f-Math.random());
	   jiggle.p3.x += (0.5f-Math.random());
	   jiggle.p3.y += (0.5f-Math.random());
	   jiggle.p3.z += (0.5f-Math.random());
	   jiggle.recalc();
	   
	   return jiggle;
	}
	
	
	
   ////////////////////////////////////////////////////////////////////////////	
	// Build a BSP Sub tree using component as objects instead of 
	// individual triangle polyogns
	//
	// TODO: Find better splitting planes
   ////////////////////////////////////////////////////////////////////////////	
	public static BSPTree buildBSPTreeObj(BSPTree root, Vector<DCComponent> list) {
	   
	   // exit condition
	   if (list.size() == 1) {
	      root.compObj= list.elementAt(0);   
	      return root;
	   }
	   
	   
	   Vector<DCComponent> frontList = new Vector<DCComponent>();
	   Vector<DCComponent> backList = new Vector<DCComponent>();
	   
	   // Ax + By + Cz + D = 0
	   float a =  root.face.fn.x;
	   float b =  root.face.fn.y;
	   float c =  root.face.fn.z;
	   float d = -(root.face.fn.x*root.face.p1.x + root.face.fn.y*root.face.p1.y + root.face.fn.z*root.face.p1.z);
	   
	   
      // Loop through each component
	   for (int i=0; i < list.size(); i++) {
  	       //TODO: Polygon(Triangle) split
	      DCComponent cObj = list.elementAt(i);
	      DCComponent frontObj = new DCComponent();
	      DCComponent backObj  = new DCComponent();
	      frontObj.cname = cObj.cname;
	      backObj.cname  = cObj.cname;
	      for (int j=0; j < cObj.faceList.size(); j++) {
            DCFace f = cObj.faceList.elementAt(j);	         
    	      if (f.p1.isOnPlane(a, b, c, d) <= 0 &&
   	   		f.p2.isOnPlane(a, b, c, d) <= 0 &&
   	   		f.p3.isOnPlane(a, b, c, d) <= 0) {
    	         frontObj.faceList.add(f);  
   	      } else {
   	         backObj.faceList.add(f);
   	      }           
	      }
	      
	      // Now the mesh is split, put them into the appropriate
	      // list, if applicable
	      if (frontObj.faceList.size() > 0) {
	         frontList.add(frontObj);  
	      } else { frontObj = null; }
	     
	      if (backObj.faceList.size() > 0) {
	         backList.add(backObj);  
	      } else { backObj = null; }
	   } // end for i
	   
	   
	   // recursive run on left (front)
	   if (frontList.size() > 0) {
	      root.front = new BSPTree( BSPTree.jiggleFace(frontList.elementAt(0).faceList.elementAt(0)));
	      buildBSPTreeObj(root.front, frontList);
	   } 
	   
		// recursive run on right (back)
	   if (backList.size() > 0) {
	      root.back = new BSPTree( BSPTree.jiggleFace(backList.elementAt(0).faceList.elementAt(0)));
	      buildBSPTreeObj(root.back, backList);
	   }   
	   
	   return root;      
	}
	
	
	
	
   ////////////////////////////////////////////////////////////////////////////	
	// Builds a BST Sub tree, assumes that the root is initialized
	//   Should we call System.gc() afterward to clean up residue memory locations ???
   ////////////////////////////////////////////////////////////////////////////	
	public static BSPTree buildBSPTree(BSPTree root, Vector<DCFace> list) {
	   Vector<DCFace> frontList = new Vector<DCFace>();
	   Vector<DCFace> backList = new Vector<DCFace>();
	   
	   // Ax + By + Cz + D = 0
	   float a = root.face.fn.x;
	   float b = root.face.fn.y;
	   float c = root.face.fn.z;
	   float d = -(root.face.fn.x*root.face.p1.x + root.face.fn.y*root.face.p1.y + root.face.fn.z*root.face.p1.z);
	   	
	   //TODO: Polygon(Triangle) split
	   for (int i=0; i < list.size(); i++) {
	     DCFace f = list.elementAt(i);
	     if (f.p1.isOnPlane(a, b, c, d) <=0 &&
	   		f.p2.isOnPlane(a, b, c, d) <=0 &&
	   		f.p3.isOnPlane(a, b, c, d) <=0) {
	        frontList.add(f);	  
	     } else {
	        backList.add(f);	  
	     }
	   }
	   
	   // recursive run on left (front)
	   if (frontList.size() > 0) {
	      root.front = new BSPTree( frontList.remove(0));	
	      buildBSPTree(root.front, frontList);
	   }
	   
	   // recursive run on right (back)
	   if (backList.size() > 0) {
	      root.back = new BSPTree( backList.remove(0));	
	      buildBSPTree(root.back, backList);
	   }
	   
	   return root;	
	}
	
	
	// Default constructor, use the other one
	public BSPTree() {
      front = null;
      back = null;
      face = null;
      compObj = null;
	}
	
	public BSPTree(DCFace f) {
		front = null;
		back = null;
		compObj = null;
	   face = f;  	
	}
	
	
	public DCFace face;
	//public Vector<DCComponent> objects;
	public DCComponent compObj;
	
   public BSPTree front;
   public BSPTree back;
}
