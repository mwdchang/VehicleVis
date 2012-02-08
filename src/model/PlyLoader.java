package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.Vector;

import datastore.Const;

/////////////////////////////////////////////////////////////////////////////////
// Loads a simple ascii ply model
//    Currently only parse faces with 3 vertices
/////////////////////////////////////////////////////////////////////////////////
public class PlyLoader {
   
   public static void main(String args[]) {
      try {
         DCModel m = PlyLoader.loadPly(Const.MODEL_PATH + "bunny.ply");   
      } catch (Exception e) {
         e.printStackTrace(); 
      }
   }
   
   public static DCModel loadPly(String path) throws Exception {
      DCModel model = new DCModel();
      Vector<DCTriple> vertexList = new Vector<DCTriple>();
      Vector<DCFace> faceList = new Vector<DCFace>();
      BufferedReader reader = new BufferedReader(new FileReader(path));    
      
      int num_vertex = 0;
      int num_face = 0;
      int c = 0;
      
      ////////////////////////////////////////////////////////////////////////////////
      // Parse out the ply header
      ////////////////////////////////////////////////////////////////////////////////
      boolean readingHeader = true;
      String line = "";
      while (readingHeader == true) {
         line = reader.readLine();  
         if (line == null) throw new Exception("Unabled to parse header, something bad happened. (" + c + ")");
         line =  line.trim();
         
         if (line.equalsIgnoreCase("end_header")) readingHeader = false;
         if (line.startsWith("element face")) num_face   = Integer.parseInt( line.split(" ")[2]);
         if (line.startsWith("element vertex")) num_vertex = Integer.parseInt( line.split(" ")[2]);
      }
      System.out.println("number of vertex : " + num_vertex);
      System.out.println("number of face: " + num_face);
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Parse out the vertices (only take the x,y and z. ignore other attribute for now)
      ////////////////////////////////////////////////////////////////////////////////
      c = 0;
      while (true) {
         line = reader.readLine();
         if (line == null) throw new Exception("Unable to parse body - vertex, something bad happened. (" + c + ")" );
         line = line.trim();
         String s[] = line.split(" "); 
         
         float x = Float.parseFloat(s[0]);
         float y = Float.parseFloat(s[1]);
         float z = Float.parseFloat(s[2]);
         vertexList.add( new DCTriple(x, y, z) );
            
         c++;
         if (c >= num_vertex) break; // exit condition
      }
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Parse out the faces
      ////////////////////////////////////////////////////////////////////////////////
      c = 0;
      while (true) {
         line = reader.readLine();
         if (line == null) throw new Exception("Unable to parse body - faces, something bad happened. (" + c + ")");
         line = line.trim();
         String s[] = line.split(" ");
         
         int num = Integer.parseInt(s[0]);
         if (num != 3) throw new Exception("Does not support face != 3 vertex");
         int v1 = Integer.parseInt(s[1]);
         int v2 = Integer.parseInt(s[2]);
         int v3 = Integer.parseInt(s[3]);
         
         DCFace f = new DCFace (
               vertexList.elementAt(v1),
               vertexList.elementAt(v2),
               vertexList.elementAt(v3)
         );
         f.calNormal();
         f.recalc();
         faceList.add(f);
         
         c++;
         if (c >= num_face) break;   
      }
      DCComponent comp = new DCComponent("Engine", faceList);
      model.componentTable.put("Engine", comp);
      
      return model;
   }
   
}
