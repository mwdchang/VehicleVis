package model;

import gui.ComponentChart;
import gui.Heatmap;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.glu.GLU;

import org.jdesktop.animation.timing.Animator;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.GLBuffers;

import datastore.HierarchyTable;
import datastore.SSM;
import datastore.SchemeManager;

import util.DCCamera;
import util.GLSLUtil;
import util.GraphicUtil;
import util.MatrixUtil;
import util.ShaderObj;

import Jama.Matrix;

/////////////////////////////////////////////////////////////////////////////////
// A class to abstract a model component, basically a list of triangular faces
// with some additional attribute
//
// Also provide basic rendering method for each component
/////////////////////////////////////////////////////////////////////////////////
public class DCComponent extends DCObj {
   
   /*
   public static void main(String args[] ) {
      Vector<DCFace> test = new Vector<DCFace>();
      test.add(new DCFace(new DCTriple(0, 0, 0), new DCTriple(1,1,1), new DCTriple(2,2,2))); 
      test.add(new DCFace(new DCTriple(1, 1, 1), new DCTriple(2,2,2), new DCTriple(3,3,3))); 
      test.add(new DCFace(new DCTriple(6, 6, 6), new DCTriple(5,5,5), new DCTriple(4,4,4))); 
      
      DCComponent x = new DCComponent("Test", test);
   } */
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Default constructor 
   ////////////////////////////////////////////////////////////////////////////////
   public DCComponent() {
      faceList = new Vector<DCFace>();
      center = new DCTriple(0,0,0);
      centerBBox = new DCTriple(0,0,0);
      boundingBox = new DCBoundingBox();
      colour = new DCColour();
      cname = "";
      baseName = "";
      transform = MatrixUtil.identity();
      
      scaleFactor = new DCTriple(1.0f, 1.0f, 1.0f);
      occlusionList = new Vector<DCComponent>();
      
      cchart = new Heatmap(new float[]{0.0f}, 100, 80);
      
      level = 0;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Init constructor
   ////////////////////////////////////////////////////////////////////////////////
   public DCComponent(String name, Vector<DCFace> v) {
      this.cname = name;  
      this.faceList = v;
      this.colour = new DCColour();
      this.center = new DCTriple(0,0,0);
      this.centerBBox = new DCTriple(0,0,0);
      transform = MatrixUtil.identity();
      
      scaleFactor = new DCTriple(1.0f, 1.0f, 1.0f);
      occlusionList = new Vector<DCComponent>();
      
      
      // Note boundingbox is reinitialized in recalc
      boundingBox = new DCBoundingBox(this.faceList);
      level = 0;
      
      //cchart = new SegmentSparkLine(new float[]{0.0f}, 100, 80);
      cchart = new Heatmap(new float[]{0.0f}, 100, 80);
      
      // Remove the spatial location tags
      if (cname.endsWith("_FR") || cname.endsWith("_FL") || cname.endsWith("_BR") || cname.endsWith("_BL")) {
         baseName = cname.substring(0, cname.length()-3);
      } else {
         baseName = cname;
      }
      
      
      /*
      if (cname.indexOf("_") < 0) {
         baseName = cname;      
      } else {
         baseName = cname.substring(0, cname.indexOf("_")); 
      }
      */

   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Re calculate various component attribute
   //   This should be called after all components in the model are finalized
   /////////////////////////////////////////////////////////////////////////////////
   public void recalculateAttribute() {
      
      // Find the approx centroid by averaging the vertices
      double x,y,z;
      int size = this.faceList.size();
      float total_vert = size*3.0f; // Each face is a triangle
      x=y=z=0;
      for (int i=0; i < size; i++) {
         x += faceList.elementAt(i).p1.x; 
         x += faceList.elementAt(i).p2.x; 
         x += faceList.elementAt(i).p3.x; 
         y += faceList.elementAt(i).p1.y; 
         y += faceList.elementAt(i).p2.y; 
         y += faceList.elementAt(i).p3.y; 
         z += faceList.elementAt(i).p1.z; 
         z += faceList.elementAt(i).p2.z; 
         z += faceList.elementAt(i).p3.z; 
      }
      x /= total_vert;
      y /= total_vert;
      z /= total_vert;
      center = new DCTriple((float)x, (float)y, (float)z);
      
      // Calculate boundingbox
      boundingBox = new DCBoundingBox(this.faceList);
      
      // Find adjacency information
      findAdjacentTriangles();
      
      
      // Find the scaling factor
      DCTriple max = new DCTriple(boundingBox.maxx, boundingBox.maxy, boundingBox.maxz);
      DCTriple min = new DCTriple(boundingBox.minx, boundingBox.miny, boundingBox.minz);
      float dist = max.sub(min).mag();
      
      scaleFactor.x = Math.abs(max.x - min.x);
      scaleFactor.y = Math.abs(max.y - min.y);
      scaleFactor.z = Math.abs(max.z - min.z);
      
      
      // Get the center of the bounding box (this varies slighting fromt he center of the mesh, 
      // pending the distribution)
      centerBBox.x = 0.5f*(boundingBox.maxx - boundingBox.minx);
      centerBBox.y = 0.5f*(boundingBox.maxy - boundingBox.miny);
      centerBBox.z = 0.5f*(boundingBox.maxz - boundingBox.minz);
      
      
      // If we can grab an ID, assign it to the spark line chart
      Vector<Integer> c = HierarchyTable.instance().getGroupId(baseName);
      if (c != null && c.size() > 0) {
         //System.out.println(">>>> Setting sparkline id : " + c.elementAt(0));
      	System.out.println(">>> Setting component/group id for : " + baseName);
         cchart.id = c.elementAt(0);
         id = c.elementAt(0);
      } else {
         cchart.id = -999;
         id = -999;
      }
      c.removeAllElements();
      c = null;      
      
      
      // Print debugging information
      if (DEBUG) {
         System.out.println(" === Model Component === ");  
         System.out.println("Component name : " + this.cname);
         System.out.println("Number of faces : "  + faceList.size());
         System.out.println("Approx centroid    : " + center.toString());
         System.out.println("Bounding box       : " + boundingBox.toString());
         System.out.println("Scaling       : " + scaleFactor.toString());
      }
      
      
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Wrapper to render a single DCTriple as openGL vec3
   ////////////////////////////////////////////////////////////////////////////////
   public void renderPoint(GL2 gl2, DCTriple p) {
      gl2.glVertex3f(p.x, p.y, p.z);
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Wrapper to render a single DCTriple as openGL normal3
   ////////////////////////////////////////////////////////////////////////////////
   public void renderNormal(GL2 gl2, DCTriple p) {
      gl2.glNormal3f(p.x, p.y, p.z);
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Spherical Expansion 
   // Expand each vertex outward away from a given origin point. 
   //    origin - the origin of the expansion
   //    radius - number of units to expand
   //
   // Given P point and O origin, and R radius: P = P + (P-O)*r
   ////////////////////////////////////////////////////////////////////////////////
   public void renderSphericalDistortion(GL2 gl2, DCTriple origin, float radius) {
      gl2.glPushMatrix();
      gl2.glBegin(GL2.GL_TRIANGLES);
         for (int i=0; i < faceList.size(); i++) {
            DCFace f = faceList.elementAt(i);   
            DCTriple d1 = f.p1.sub(origin); d1.normalize();
            DCTriple d2 = f.p2.sub(origin); d2.normalize();
            DCTriple d3 = f.p3.sub(origin); d3.normalize();
            
            renderPoint(gl2, f.p1.add(d1.mult(radius)));            
            renderPoint(gl2, f.p2.add(d2.mult(radius)));            
            renderPoint(gl2, f.p3.add(d3.mult(radius)));            
         }
      gl2.glEnd();
      gl2.glPopMatrix();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Send in triangles with adjacency information.
   //   Note: this is for geometry, it does not work well sending it directly to
   //   the default pipeline. The inverse is also true
   ////////////////////////////////////////////////////////////////////////////////
   public void renderTriangleWithAdj(GL2 gl2) {
      gl2.glPushMatrix();
//      gl2.glEnable(GL2.GL_DEPTH_TEST);
//      gl2.glDisable(GL2.GL_BLEND);
      gl2.glBegin(GL2.GL_TRIANGLES_ADJACENCY_EXT);
         for (int i=0; i < faceList.size(); i++) {
            DCFace f = faceList.elementAt(i);
            gl2.glVertex3f(f.p1.x, f.p1.y, f.p1.z);
            gl2.glVertex3f(f.a1.x, f.a1.y, f.a1.z);
            gl2.glVertex3f(f.p2.x, f.p2.y, f.p2.z);
            gl2.glVertex3f(f.a2.x, f.a2.y, f.a2.z);
            gl2.glVertex3f(f.p3.x, f.p3.y, f.p3.z);
            gl2.glVertex3f(f.a3.x, f.a3.y, f.a3.z);
         }
      gl2.glEnd();
//      gl2.glEnable(GL2.GL_BLEND);
      gl2.glPopMatrix();
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render the edges with no adjacent triangles
   ////////////////////////////////////////////////////////////////////////////////
   public void renderEdgeWithNoAdjacent(GL2 gl2) {
      gl2.glPushMatrix();
//      gl2.glLineWidth(1.0f);
    	gl2.glBegin(GL2.GL_LINES);
      for (int i=0; i < faceList.size(); i++) {
      	DCFace f = faceList.elementAt(i);
      	//if (f.a1 == null) {
      	  gl2.glVertex3f(f.p1.x, f.p1.y, f.p1.z); 	
      	  gl2.glVertex3f(f.p2.x, f.p2.y, f.p2.z); 	
      	//}
      	//if (f.a2 == null) {
      	  gl2.glVertex3f(f.p2.x, f.p2.y, f.p2.z); 	
      	  gl2.glVertex3f(f.p3.x, f.p3.y, f.p3.z); 	
      	//}
      	//if (f.a3 == null) {
      	  gl2.glVertex3f(f.p3.x, f.p3.y, f.p3.z); 	
      	  gl2.glVertex3f(f.p1.x, f.p1.y, f.p1.z); 	
      	//}
      }
   	gl2.glEnd();
//      gl2.glLineWidth(1.0f);
      gl2.glPopMatrix();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Just pass the triangle soup through the pipeline  
   ////////////////////////////////////////////////////////////////////////////////
   public void renderBasicMesh(GL2 gl2) {
      gl2.glPushMatrix();
      gl2.glBegin(GL2.GL_TRIANGLES);
      for (int i=0; i < faceList.size(); i++) {
         DCFace f = faceList.elementAt(i);
         gl2.glVertex3f( f.p1.x, f.p1.y, f.p1.z);
         gl2.glVertex3f( f.p2.x, f.p2.y, f.p2.z);
         gl2.glVertex3f( f.p3.x, f.p3.y, f.p3.z);
      }
      gl2.glEnd();
      gl2.glPopMatrix();
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render basic mesh with normal coordinates
   ////////////////////////////////////////////////////////////////////////////////
   public void renderBasicMeshWithNormal(GL2 gl2) {
      gl2.glPushMatrix();
//      gl2.glDisable(GL2.GL_DEPTH_TEST);
      gl2.glBegin(GL2.GL_TRIANGLES);
      for (int i=0; i < faceList.size(); i++) {
         DCFace f = faceList.elementAt(i);   
         renderNormal(gl2, f.n1);
         renderPoint(gl2, f.p1);
         
         renderNormal(gl2, f.n2);
         renderPoint(gl2, f.p2);
         
         renderNormal(gl2, f.n3);
         renderPoint(gl2, f.p3);
      }
      gl2.glEnd();
      gl2.glPopMatrix();
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   //
   // Using faceList as a starting point, 
   // find the neighbouring triangles
   //
   //    p4(0, 1)------- p3(1, 1)
	//           |     / |
	//           |    /  |
	//           |   /   |
	//           |  /    |
	//    p1(0, 0) ------ p2(1, 0)
	//
	//
	//    p1 -> { p2, p3, p4 }
	//    p2 -> { p1, p3 }
	//    p3 -> { p1, p2, p4 }
	//    p4 -> { p1, p3 } 
   //
   ////////////////////////////////////////////////////////////////////////////////
   public void findAdjacentTriangles() {
   	// Build a lookup table
      Hashtable<String, HashSet<DCTriple>> table = new Hashtable<String, HashSet<DCTriple>>();
      for (int i=0; i < faceList.size(); i++) {
         DCFace fc = faceList.elementAt(i); 	
      	if (table.get(fc.p1.toString()) == null) table.put(fc.p1.toString(), new HashSet<DCTriple>()); 
      	if (table.get(fc.p2.toString()) == null) table.put(fc.p2.toString(), new HashSet<DCTriple>()); 
      	if (table.get(fc.p3.toString()) == null) table.put(fc.p3.toString(), new HashSet<DCTriple>());          
      	
      	// Encode the values
      	table.get(fc.p1.toString()).add(fc.p2);
      	table.get(fc.p1.toString()).add(fc.p3);
      	table.get(fc.p2.toString()).add(fc.p1);
      	table.get(fc.p2.toString()).add(fc.p3);
      	table.get(fc.p3.toString()).add(fc.p1);
      	table.get(fc.p3.toString()).add(fc.p2);      	
      } // end for
      
      
//      if (this.cname.equals("wheel")) {
//         System.out.println("Face list size is : " + faceList.size());
//         System.out.println("Table isze is : " + table.size()); 
//         Enumeration<String> e = table.keys();
//         while (e.hasMoreElements()) {
//            System.out.println("\t" + table.get(e.nextElement()).size());   
//         }
//      }
    
      
      
      // Now see if we can find adjacent triangle   	
      // For each face we check 3 edges
      //   p1-p2
      //   p2-p3
      //   p3-p1
      HashSet<DCTriple> h1 = new HashSet<DCTriple>();
     	HashSet<DCTriple> h2 = new HashSet<DCTriple>();
     	Iterator<DCTriple> iter;
     	for (int i=0; i < faceList.size(); i++) {
     	   

         DCFace f = faceList.elementAt(i); 		
         
      	// p1 and p2
      	h1 = new HashSet<DCTriple>( table.get(f.p1.toString()) );
      	h2 = new HashSet<DCTriple>( table.get(f.p2.toString()) );
      	h1.retainAll(h2);
      	h1.remove(f.p2);
      	h1.remove(f.p3);
      	if (h1.isEmpty()) {
      		f.a1 = null;
      	} else {
      	   iter = h1.iterator();
      	   f.a1 = iter.next();
      	}
      	
         // p2 and p3
      	h1 = new HashSet<DCTriple>( table.get(f.p2.toString()) );
      	h2 = new HashSet<DCTriple>( table.get(f.p3.toString()) );
      	h1.retainAll(h2);
      	h1.remove(f.p3);
      	h1.remove(f.p1);
      	if (h1.isEmpty()) {
      		f.a2 = null;
      	} else {
      		iter = h1.iterator();
      		f.a2 = iter.next();
      	}
      	
         // p3 and p1
      	h1 = new HashSet<DCTriple>( table.get(f.p3.toString()) );
      	h2 = new HashSet<DCTriple>( table.get(f.p1.toString()) );
      	h1.retainAll(h2);
      	h1.remove(f.p1);
      	h1.remove(f.p2);      	
      	if (h1.isEmpty()) {
      		f.a3 = null;
      	} else {
      		iter = h1.iterator();
      		f.a3 = iter.next();
      	}
      	
     	} // end for
     	
     	
     	
     	// if the mesh is not closed, make the unclosed edge "fold back" onto itself onto the
     	for (int i=0; i < faceList.size(); i++) {
     	   DCFace f = faceList.elementAt(i); 
     	   
     	   
         if (f.a1 == null) {
            //silhouetteList.add(new DCEdge(f.p1, f.p2));
            f.a1 = f.p3;     	    
         } else {
         }
         
         if (f.a2 == null) {
            //silhouetteList.add(new DCEdge(f.p2, f.p3));
            f.a2 = f.p1;
         } else {
         }
         
         if (f.a3 == null) {
            //silhouetteList.add(new DCEdge(f.p3, f.p1));
            f.a3 = f.p2;
         } else {
         }
         
         // Fake the edges
   	   DCTriple v0 = f.p1;
     	   DCTriple v1 = f.a1;
     	   DCTriple v2 = f.p2;
     	   DCTriple v3 = f.a2;
     	   DCTriple v4 = f.p3;
     	   DCTriple v5 = f.a3;
     	   
     	   DCTriple normal_042 = (v4.sub(v0)).cross(v2.sub(v0));
     	   DCTriple normal_021 = (v2.sub(v0)).cross(v1.sub(v0));
     	   DCTriple normal_243 = (v4.sub(v2)).cross(v3.sub(v2));
     	   DCTriple normal_405 = (v0.sub(v4)).cross(v5.sub(v4));
     	   
     	   normal_042.normalize();
     	   normal_021.normalize();
     	   normal_243.normalize();
     	   normal_405.normalize();
     	   
     	   if (normal_042.dot(normal_021) < 0) {
     	      silhouetteList.add(new DCEdge(f.p1, f.p2));   
     	   }
     	   if (normal_042.dot(normal_243) < 0) {
     	      silhouetteList.add(new DCEdge(f.p2, f.p3));   
     	   }
     	   if (normal_042.dot(normal_405) < 0) {
     	      silhouetteList.add(new DCEdge(f.p3, f.p1));   
     	   }
     	   
        
     	}
     	
     	
      // Do clean up now
      table.clear();
      h1.clear();
      h2.clear();
      table = null;
      h1 = null;
      h2 = null;
     	
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render pre-computed silhouettes
   ////////////////////////////////////////////////////////////////////////////////
   public void renderSilhouette(GL2 gl2, DCColour c) {
      gl2.glColor4fv(c.toArray(), 0);   
      //gl2.glLineWidth(2.0f);
      gl2.glBegin(GL2.GL_LINES);
      for (int i=0; i < silhouetteList.size(); i++) {
         DCTriple p1 = silhouetteList.elementAt(i).p1;
         DCTriple p2 = silhouetteList.elementAt(i).p2;
         gl2.glVertex3f(p1.x, p1.y, p1.z);
         gl2.glVertex3f(p2.x, p2.y, p2.z);
      }
      gl2.glEnd();
      gl2.glLineWidth(1.0f);
   }
   
   
   
   public void renderBufferToon(GL2 gl2) { //, DCColour colour) {
      gl2.glBindVertexArray(vao[0]);
      
      normalShader.bind(gl2);
         float buffer[] = new float[16];
         
         gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
         normalShader.setUniform4x4(gl2, "projection_matrix", buffer);
         
         gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
         normalShader.setUniform4x4(gl2, "modelview_matrix", buffer);
         
         normalShader.setUniform1i(gl2, "mode", 4);
         gl2.glActiveTexture(GL2.GL_TEXTURE4);
         gl2.glBindTexture(GL2.GL_TEXTURE_1D, toonTextureId);
         normalShader.setUniform1i(gl2, "texMap", 4);
         
         gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, faceList.size()*3);
      normalShader.unbind(gl2);
        
      gl2.glBindVertexArray(0);
      gl2.glActiveTexture(GL2.GL_TEXTURE0);
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render vertex array object
   ////////////////////////////////////////////////////////////////////////////////
   public void renderBuffer(GL2 gl2, DCColour colour) {
      renderBuffer(gl2, colour, 1);
   }

   public void renderBuffer(GL2 gl2, DCColour colour, int mode) {
      float buffer[] = new float[16];
      gl2.glBindVertexArray(vao[0]);
      
      normalShader.bind(gl2);
         
         gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
         normalShader.setUniform4x4(gl2, "projection_matrix", buffer);
         
         gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
         normalShader.setUniform4x4(gl2, "modelview_matrix", buffer);
         
         normalShader.setUniform1i(gl2, "mode", mode);
         
         gl2.glActiveTexture(GL2.GL_TEXTURE4);
         gl2.glBindTexture(GL2.GL_TEXTURE_1D, toonTextureId);
         normalShader.setUniform1i(gl2, "texMap", 4);
         
         if (SSM.instance().colourRampReverseAlpha) {
            normalShader.setUniformf(gl2, "comp_colour", colour.r, colour.g, colour.b, 1.0f-colour.a);   
         } else {
            normalShader.setUniformf(gl2, "comp_colour", colour.r, colour.g, colour.b, colour.a);   
         }
         
         gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, faceList.size()*3);
      normalShader.unbind(gl2);
     
      gl2.glBindVertexArray(0);
      gl2.glActiveTexture(GL2.GL_TEXTURE0);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render the vertex array object, with the current eye position
   // as the source of light
   ////////////////////////////////////////////////////////////////////////////////
   public void renderBufferVaryLight(GL2 gl2, DCColour colour) {
      gl2.glBindVertexArray(vao[0]);
      
      normalShader.bind(gl2);
         float buffer[] = new float[16];
         
         gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
         normalShader.setUniform4x4(gl2, "projection_matrix", buffer);
         
         gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
         normalShader.setUniform4x4(gl2, "modelview_matrix", buffer);
         
         normalShader.setUniform1i(gl2, "mode", 3);
         normalShader.setUniformf(gl2, "comp_colour", colour.r, colour.g, colour.b, colour.a);   
         normalShader.setUniformf(gl2, "listPos", DCCamera.instance().eye.x, DCCamera.instance().eye.y, DCCamera.instance().eye.z);
         
         gl2.glDrawArrays(GL2.GL_TRIANGLES, 0, faceList.size()*3);
      normalShader.unbind(gl2);
      
      gl2.glBindVertexArray(0);
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render silhouette ... hopefully
   ////////////////////////////////////////////////////////////////////////////////
   public void renderBufferSilhouette(GL2 gl2,  DCColour colour) {
      if (colour == null) colour = SchemeManager.silhouette_default;
      
      gl2.glBindVertexArray(vaoAdj[0]);
      silShader.bind(gl2);
         float buffer[] = new float[16];
         gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
         silShader.setUniform4x4(gl2, "projection_matrix", buffer);
         
         gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
         silShader.setUniform4x4(gl2, "modelview_matrix", buffer);         
         
         silShader.setUniformf(gl2, "comp_colourAdj", colour.r, colour.g, colour.b, colour.a);
//         edgeShader.setUniformf(gl2, "eyePosition", DCCamera.instance().eye.x, DCCamera.instance().eye.y, DCCamera.instance().eye.z);
        
         gl2.glDrawArrays(GL2.GL_TRIANGLES_ADJACENCY_EXT, 0, faceList.size()*6);
      silShader.unbind(gl2);
      gl2.glBindVertexArray(0);
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Renders the triangle adjacency as a vertex array object
   ////////////////////////////////////////////////////////////////////////////////
   public void renderBufferAdj(GL2 gl2, DCColour colour) {
      if (colour == null) colour = SchemeManager.silhouette_default;
      
      gl2.glBindVertexArray(vaoAdj[0]);
      
      edgeShader.bind(gl2);
         float buffer[] = new float[16];
         
         gl2.glGetFloatv(GL2.GL_PROJECTION_MATRIX, buffer, 0);
         edgeShader.setUniform4x4(gl2, "projection_matrix", buffer);
         
         gl2.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, buffer, 0);
         edgeShader.setUniform4x4(gl2, "modelview_matrix", buffer);         
         
         edgeShader.setUniformf(gl2, "comp_colourAdj", colour.r, colour.g, colour.b, colour.a);
//         edgeShader.setUniformf(gl2, "eyePosition", DCCamera.instance().eye.x, DCCamera.instance().eye.y, DCCamera.instance().eye.z);
         
//         edgeShader.setUniform1i(gl2, "modeAdj", 2);
         
         gl2.glDrawArrays(GL2.GL_TRIANGLES_ADJACENCY_EXT, 0, faceList.size()*6);
      edgeShader.unbind(gl2);
      
      
      gl2.glBindVertexArray(0);
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render vertex array object
   ////////////////////////////////////////////////////////////////////////////////
   public void renderBuffer(GL2 gl2) {
      renderBuffer(gl2, SchemeManager.car_normal1);
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Render the normal - for debugging
   ////////////////////////////////////////////////////////////////////////////////
   public void renderVNormal(GL2 gl2) {
      gl2.glPushMatrix();   
      gl2.glLineWidth(1.0f);
      gl2.glBegin(GL2.GL_LINES);
      for (int i=0; i < faceList.size(); i++) {
      	DCFace f = faceList.elementAt(i);
      	
      	gl2.glColor3d(0, 0, 1);
      	gl2.glVertex3f(f.p1.x, f.p1.y, f.p1.z);
      	gl2.glColor3d(1, 0, 1);
      	gl2.glVertex3f(f.p1.x+f.n1.x, f.p1.y+f.n1.y, f.p1.z + f.n1.z);
      	
      	gl2.glColor3d(0, 0, 1);
      	gl2.glVertex3f(f.p2.x, f.p2.y, f.p2.z);
      	gl2.glColor3d(1, 0, 1);
      	gl2.glVertex3f(f.p2.x+f.n2.x, f.p2.y+f.n2.y, f.p2.z + f.n2.z);
      	
      	gl2.glColor3d(0, 0, 1);
      	gl2.glVertex3f(f.p3.x, f.p3.y, f.p3.z);
      	gl2.glColor3d(1, 0, 1);
      	gl2.glVertex3f(f.p3.x+f.n3.x, f.p3.y+f.n3.y, f.p3.z + f.n3.z);
      }
      gl2.glEnd();
      gl2.glLineWidth(1.0f);
      gl2.glPopMatrix();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Renders face normals - for debugging
   ////////////////////////////////////////////////////////////////////////////////
   public void renderFNormal(GL2 gl2) {
      gl2.glPushMatrix();   
      gl2.glLineWidth(2.0f);
      gl2.glBegin(GL2.GL_LINES);
      for (int i=0; i < faceList.size(); i++) {
      	DCFace f = faceList.elementAt(i);
      	/*
      	gl2.glColor3d(0, 0, 1);
      	gl2.glVertex3f(f.midpoint.x, f.midpoint.y, f.midpoint.z);
      	gl2.glColor3d(1, 0, 1);
      	gl2.glVertex3f(f.midpoint.x + f.fn.x, f.midpoint.y + f.fn.y, f.midpoint.z+f.fn.z);
      	*/
       	gl2.glColor3d(0, 0, 0.8);
      	gl2.glVertex3f(f.p1.x, f.p1.y, f.p1.z);
      	gl2.glColor3d(1.0, 0.5, 0);
      	gl2.glVertex3f(f.p1.x + f.fn.x, f.p1.y + f.fn.y, f.p1.z+f.fn.z);
      }
      gl2.glEnd();
      gl2.glLineWidth(1.0f);
      gl2.glPopMatrix();
   }
   
   
   // Primitive buffers .. should upgrade when time allows
   public void createBuffersOIT(GL2 gl2) {
      FloatBuffer buffer = (FloatBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, faceList.size()*6*3); 
      IntBuffer  ibuffer = (IntBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_UNSIGNED_INT, faceList.size()*3);
      
      int counter = 0;
      for (int i=0; i < faceList.size(); i++) {
         // p1
         buffer.put(faceList.elementAt(i).p1.toArray3f());
         buffer.put(faceList.elementAt(i).n1.toArray3f());
         ibuffer.put(counter);
         counter++;
         
         // p2
         buffer.put(faceList.elementAt(i).p2.toArray3f());
         buffer.put(faceList.elementAt(i).n2.toArray3f());
         ibuffer.put(counter);
         counter++;
         
         // p3
         buffer.put(faceList.elementAt(i).p3.toArray3f());
         buffer.put(faceList.elementAt(i).n3.toArray3f());
         ibuffer.put(counter);
         counter++;
         
      }           
      buffer.rewind();
      ibuffer.rewind();
      
      // Generate vertex buffer
      System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> VBO is here");
      gl2.glGenBuffers(2, vboOIT, 0);        
      
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboOIT[0]);
      gl2.glBufferData(GL2.GL_ARRAY_BUFFER, buffer.capacity()*Buffers.SIZEOF_FLOAT, buffer, GL2.GL_STATIC_DRAW);
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, 0);
    
      gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, vboOIT[1]);
      gl2.glBufferData(GL2.GL_ELEMENT_ARRAY_BUFFER, ibuffer.capacity()*Buffers.SIZEOF_INT, ibuffer, GL2.GL_STATIC_DRAW);
      gl2.glBindBuffer(GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Create an simplified adjacency vao/vbo buffer 
   ////////////////////////////////////////////////////////////////////////////////
   public void createBuffersAdj(GL2 gl2) {
      FloatBuffer vBuffer = (FloatBuffer)GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, faceList.size()*6*3); // xyz * 6 points
      
      for (int i=0; i < faceList.size(); i++) {
         vBuffer.put( faceList.elementAt(i).p1.toArray3f());
         vBuffer.put( faceList.elementAt(i).a1.toArray3f());
         vBuffer.put( faceList.elementAt(i).p2.toArray3f());
         vBuffer.put( faceList.elementAt(i).a2.toArray3f());
         vBuffer.put( faceList.elementAt(i).p3.toArray3f());
         vBuffer.put( faceList.elementAt(i).a3.toArray3f());
      }
      
      
      // Generate vertex array
      gl2.glGenVertexArrays(1, vaoAdj, 0);
      gl2.glBindVertexArray(vaoAdj[0]);
      
      // Generate vertex buffer
      gl2.glGenBuffers(1, vboAdj, 0);         
      
      vBuffer.flip();
      
      
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vboAdj[0]);
      gl2.glBufferData(GL2.GL_ARRAY_BUFFER, vBuffer.capacity()*4, vBuffer, GL2.GL_STATIC_DRAW);
      gl2.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 0, 0);
      gl2.glEnableVertexAttribArray(0);         
    
      // Clean up
      gl2.glBindVertexArray(0);
      vBuffer.clear();
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Start the shaders
      ////////////////////////////////////////////////////////////////////////////////
      edgeShader.createShader(gl2, "src\\Shader\\vert_adj.glsl", GL2.GL_VERTEX_SHADER);
      edgeShader.createShader(gl2, "src\\Shader\\geom_adj.glsl", GL3.GL_GEOMETRY_SHADER);
      edgeShader.createShader(gl2, "src\\Shader\\frag_adj.glsl", GL2.GL_FRAGMENT_SHADER);
      edgeShader.createProgram(gl2); 
      gl2.glBindAttribLocation(edgeShader.programID,  0, "in_position");
      edgeShader.linkProgram(gl2);
      edgeShader.bindFragColour(gl2, "outColour");
      
     
      silShader.createShader(gl2, "src\\Shader\\vert_adj.glsl", GL2.GL_VERTEX_SHADER);
      silShader.createShader(gl2, "src\\Shader\\geom_adj_silhouette.glsl", GL3.GL_GEOMETRY_SHADER);
      silShader.createShader(gl2, "src\\Shader\\frag_adj.glsl", GL2.GL_FRAGMENT_SHADER);
      silShader.createProgram(gl2); 
      gl2.glBindAttribLocation(silShader.programID,  0, "in_position");
      silShader.linkProgram(gl2);
      silShader.bindFragColour(gl2, "outColour");
  }
   
   
   
   ////////////////////////////////////////////////////////////////////////////
   // Creates vertex array objects, which encapsulates 
   // vertex, normal, colour and texcoord attributes
   //
   // Note: textcoord and colour are zeroed out, they are here to keep the 
   // shader happy
   ////////////////////////////////////////////////////////////////////////////
   public void createBuffers(GL2 gl2) {
      // Init toon texture
      toonTextureId = GraphicUtil.gen1DTexture(gl2);
      
      
      FloatBuffer vBuffer = (FloatBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, faceList.size()*3*3); // xyz * 3 points
      FloatBuffer nBuffer = (FloatBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, faceList.size()*3*3); // xyz * 3 points
      FloatBuffer cBuffer = (FloatBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, faceList.size()*4); // rgba *1 face
      FloatBuffer tBuffer = (FloatBuffer) GLBuffers.newDirectGLBuffer(GL2.GL_FLOAT, faceList.size()*2*3); // uv * 3 points
      
      float emptyColour[] = {0, 0, 0, 0};
      float emptyTexture[] = {0, 0, 0, 0, 0, 0};
      
      for (int i=0; i < faceList.size(); i++) {
         // Vertex
         vBuffer.put(faceList.elementAt(i).p1.toArray3f());
         vBuffer.put(faceList.elementAt(i).p2.toArray3f());
         vBuffer.put(faceList.elementAt(i).p3.toArray3f());
         
         // normal
         nBuffer.put( faceList.elementAt(i).n1.toArray3f());
         nBuffer.put( faceList.elementAt(i).n2.toArray3f());
         nBuffer.put( faceList.elementAt(i).n3.toArray3f());
         
         // colour - one colour per face
         cBuffer.put( emptyColour );
         
         // texture 
         tBuffer.put( emptyTexture );
      }
         
      
      // Generate vertex array
      gl2.glGenVertexArrays(1, vao, 0);
      gl2.glBindVertexArray(vao[0]);
      
      // Generate vertex buffer
      gl2.glGenBuffers(4, vbo, 0);         
      
      System.out.println("VBO Ids for " + cname + ": " + vao[0] + "\t" + vbo[0] + "\t" + vbo[1]);
      System.out.println("");
      
      nBuffer.flip();
      vBuffer.flip();
      cBuffer.flip();
      tBuffer.flip();
      
      
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[0]);
      gl2.glBufferData(GL2.GL_ARRAY_BUFFER, vBuffer.capacity()*4, vBuffer, GL2.GL_STATIC_DRAW);
      gl2.glVertexAttribPointer(0, 3, GL2.GL_FLOAT, false, 0, 0);
      gl2.glEnableVertexAttribArray(0);         
      
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[1]);
      gl2.glBufferData(GL2.GL_ARRAY_BUFFER, nBuffer.capacity()*4, nBuffer, GL2.GL_STATIC_DRAW);
      gl2.glVertexAttribPointer(1, 3, GL2.GL_FLOAT, false, 0, 0);
      gl2.glEnableVertexAttribArray(1);         
      
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[2]);
      gl2.glBufferData(GL2.GL_ARRAY_BUFFER, cBuffer.capacity()*4, nBuffer, GL2.GL_STATIC_DRAW);
      gl2.glVertexAttribPointer(2, 4, GL2.GL_FLOAT, false, 0, 0);
      gl2.glEnableVertexAttribArray(2);         
      
      gl2.glBindBuffer(GL2.GL_ARRAY_BUFFER, vbo[3]);
      gl2.glBufferData(GL2.GL_ARRAY_BUFFER, tBuffer.capacity()*4, nBuffer, GL2.GL_STATIC_DRAW);
      gl2.glVertexAttribPointer(3, 2, GL2.GL_FLOAT, false, 0, 0);
      gl2.glEnableVertexAttribArray(3);         
      
      
      // Clean up
      gl2.glBindVertexArray(0);
      vBuffer.clear();
      nBuffer.clear();
      cBuffer.clear();
      tBuffer.clear();
      
      
      // Start the shaders
      normalShader.createShader(gl2, "src\\Shader\\vert_pass_thru.glsl", GL2.GL_VERTEX_SHADER);
      normalShader.createShader(gl2, "src\\Shader\\frag_pass_thru.glsl", GL2.GL_FRAGMENT_SHADER);
      normalShader.createProgram(gl2);
      
      gl2.glBindAttribLocation(normalShader.programID,  0, "in_position");
      gl2.glBindAttribLocation(normalShader.programID,  1, "in_normal");
      gl2.glBindAttribLocation(normalShader.programID,  2, "in_colour");      
      gl2.glBindAttribLocation(normalShader.programID,  3, "in_texcoord");      
      
      normalShader.linkProgram(gl2);
      
      
      // Bind fragment shader
      normalShader.bindFragColour(gl2, "outColour");
      
      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Enlarge or shrink the component and its bounding box, based
   // on factor
   // 
   // Note: The whole component will need to be recalculated if this is called
   // after initialization. (Update VBOs, center, adj-edges...etc etc);
   ////////////////////////////////////////////////////////////////////////////////
   public void resize(float factor) {
      // Resize the bounding box      
      boundingBox.maxx *= factor;
      boundingBox.maxy *= factor;
      boundingBox.maxz *= factor;
      boundingBox.minx *= factor;
      boundingBox.miny *= factor;
      boundingBox.minz *= factor;
      
      // resize the model itself
      for (int i=0; i < faceList.size(); i++) {
         faceList.elementAt(i).p1 = faceList.elementAt(i).p1.mult( factor );
         faceList.elementAt(i).p2 = faceList.elementAt(i).p2.mult( factor );
         faceList.elementAt(i).p3 = faceList.elementAt(i).p3.mult( factor );
      }
   }
   
   
   
   // For animation
   public void setOccurrence(float d) {
      occurrence = d;   
   }
   public float getOccurrence() {
      return occurrence;   
   }
   
   
   
   
   //////////////////////////////////////////////////////////////////////////////// 
   // Component attribute
   //////////////////////////////////////////////////////////////////////////////// 
   public GLU glu = new GLU();
   
   
   // Vertex array and vertex buffer objects (for performance boost)
   // vao1 - Regular VBO buffers
   // vao2 - Adjacent VBO buffers
   public int vao[] = new int[1];
   public int vbo[] = new int[4];
   public int vaoAdj[] = new int[1];
   public int vboAdj[]  = new int[1];
   
   public int vaoOIT[] = new int[1];
   public int vboOIT[] = new int[2];
   
   ////////////////////////////////////////////////////////////////////////////////
   // Normal shader
   //    More or less just a pass through shader
   ////////////////////////////////////////////////////////////////////////////////
   ShaderObj normalShader = new ShaderObj();
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Adj shader
   //   Edge/Feature detector
   //   Silhouette detector
   ////////////////////////////////////////////////////////////////////////////////
   ShaderObj edgeShader = new ShaderObj();
   ShaderObj silShader  = new ShaderObj();
   
   
   public Vector<DCComponent> occlusionList;
   public DCTriple scaleFactor;
   public Matrix transform;    // not used ???
   public DCTriple center;
   public DCTriple projCenter; // Projection image 
   public int level;           // Occlusion level
   
   public DCTriple centerBBox; // Center of bounding box
   public DCColour colour;
   public DCColour silhouetteColour = new DCColour(0.5f, 0.5f, 0.5f, 1.0);
   
   
   public int toonTextureId;
   
   
   public boolean active = true;
   public boolean hasContext = true;
   public DCBoundingBox boundingBox;
   public Vector<DCFace> faceList;
   
   
   public ComponentChart cchart;
   public String cname;            
   public String baseName;
   public int id;
   
   
   public float occurrence = 0;
   
   // Animator and Evaluators
   public Animator canimator;
   
   public Vector<DCEdge> silhouetteList = new Vector<DCEdge>();
   
   
}
