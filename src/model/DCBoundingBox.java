package model;

import java.util.Vector;

import javax.media.opengl.GL2;

import datastore.SchemeManager;

// An object used to represent a bounding box volume
public class DCBoundingBox extends DCObj {
   	
	// Default constructor
   public DCBoundingBox() {
       minx=miny=minz=maxx=maxy=maxz = 0.0f;
   }
   
   // Create a bounding box given the min and max point
   // in 3D space
   public DCBoundingBox(DCTriple min, DCTriple max) {
   	minx = min.x;
   	miny = min.y;
   	minz = min.z;
   	maxx = max.x;
   	maxy = max.y;
   	maxz = max.z;
   }
   
   
   // Check if one bounding box contains another bounding box
   public boolean contains(DCBoundingBox box) {
      return 
         this.minx <= box.minx && this.miny <= box.miny && this.minz <= box.minz &&
         this.maxx >= box.maxx && this.maxy >= box.maxy && this.maxz >= box.maxz;
   }
	
   
   // Create a bounding box given a list of triangluar faces
   public DCBoundingBox(Vector<DCFace> faceList) {
      maxx = faceList.elementAt(0).p1.x;
      maxy = faceList.elementAt(0).p1.y;
      maxz = faceList.elementAt(0).p1.z;
      minx = faceList.elementAt(0).p1.x;
      miny = faceList.elementAt(0).p1.y;
      minz = faceList.elementAt(0).p1.z;

      
   	for (int i=0; i < faceList.size(); i++) {
      	DCFace f = faceList.elementAt(i);
      	float array_x[] = new float[]{f.p1.x, f.p2.x, f.p3.x};
      	float array_y[] = new float[]{f.p1.y, f.p2.y, f.p3.y};
      	float array_z[] = new float[]{f.p1.z, f.p2.z, f.p3.z};
      	
      
      	
      	for (int j=0; j < 3; j++) {
      		if (minx > array_x[j]) minx = array_x[j];
      		if (miny > array_y[j]) miny = array_y[j];
      		if (minz > array_z[j]) minz = array_z[j];
      		
      		if (maxx < array_x[j]) maxx = array_x[j];
      		if (maxy < array_y[j]) maxy = array_y[j];
      		if (maxz < array_z[j]) maxz = array_z[j];      		      		      		
      	}
      }
   }
   
   public void renderBoundingBox(GL2 gl2) {
      renderBoundingBox(gl2, SchemeManager.colour_blue);
   }
   
   // Render a wire-frame version the bounding box
   public void renderBoundingBox(GL2 gl2, DCColour colour) {
   	
   	// Fake some random colour based on volume, just to see more clearly each box
   	//float volume = Math.abs(maxx-minx) * Math.abs(maxy-miny) * Math.abs(maxz-minz);
   	//float index = volume%255;
   	//System.out.println(minx + " " + miny + " " + minz);
   	//System.out.println(maxx + " " + maxy + " " + maxz);
   	
      boolean reEnableLighting = false; 
   	gl2.glPushMatrix();
   	   
   	   //gl2.glColor3f(0.0f, 0.8f, 0.8f);
//   	   gl2.glColor4fv( SchemeManager.colour_blue.toArray(), 0);
   	   gl2.glColor4fv( colour.toArray(), 0);
         gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
         gl2.glLineWidth(0.8f);
      	gl2.glBegin(GL2.GL_QUADS);
      	   // back
      	   gl2.glVertex3f(minx, miny, maxz);
      	   gl2.glVertex3f(maxx, miny, maxz);
      	   gl2.glVertex3f(maxx, maxy, maxz);
      	   gl2.glVertex3f(minx, maxy, maxz);

      	   // front
      	   gl2.glVertex3f(minx, miny, minz);
      	   gl2.glVertex3f(maxx, miny, minz);
      	   gl2.glVertex3f(maxx, maxy, minz);
      	   gl2.glVertex3f(minx, maxy, minz);      	   
      	   
      	   // top
      	   gl2.glVertex3f(minx, maxy, minz);
      	   gl2.glVertex3f(maxx, maxy, minz);
      	   gl2.glVertex3f(maxx, maxy, maxz);
      	   gl2.glVertex3f(minx, maxy, maxz);
      	   
      	   // bottom
      	   gl2.glVertex3f(minx, miny, minz);
      	   gl2.glVertex3f(maxx, miny, minz);
      	   gl2.glVertex3f(maxx, miny, maxz);
      	   gl2.glVertex3f(minx, miny, maxz);
      	   
      	   // left
      	   gl2.glVertex3f(minx, miny, minz);
      	   gl2.glVertex3f(minx, miny, maxz);
      	   gl2.glVertex3f(minx, maxy, maxz);
      	   gl2.glVertex3f(minx, maxy, minz);
      	    
      	   // right
      	   gl2.glVertex3f(maxx, miny, minz);
      	   gl2.glVertex3f(maxx, miny, maxz);
      	   gl2.glVertex3f(maxx, maxy, maxz);
      	   gl2.glVertex3f(maxx, maxy, minz);
      	   
         gl2.glEnd();
         gl2.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);     
         gl2.glLineWidth(1.0f);
      gl2.glPopMatrix();
   }
   
   
   @Override
   public String toString() {
      return "[" + minx +", " + miny + ", " + minz +"] - [" + maxx + ", " + maxy + ", " + maxx + "]"; 
   }
   
   
   public float minx, miny, minz;
   public float maxx, maxy, maxz;
}
