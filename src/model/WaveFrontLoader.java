package model;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileReader;
import java.util.Vector;

import datastore.Const;

import parser.StatManager;
import util.BSPTree;
import util.DWin;

// A loader to load a customized OBJ file
public class WaveFrontLoader {
   public WaveFrontLoader() {  
   }
      
//   public static void main(String args[]) {
//      WaveFrontLoader x = new WaveFrontLoader();
//      try {
//         System.out.println("Loading...");
//         DCModel m = x.loadWaveFrontObj( Const.MODEL_FILE );
//         m.printInfo();
//      } catch (Exception e ) { e.printStackTrace();}
//   }
   
   
   
   // This method will parse the Google Sketchup OBJ exported output
   // It may or may not work for OBJ files in general because it relies on the
   // group name dependencies built from Sketchup's component hierarchies
   //
   // The exporter gives the group in the following format : 
   // <name>_c_<component>-<material>
   //
   public static DCModel loadWaveFrontObj(String path) throws Exception {
      DCModel model = new DCModel();
      
      BufferedReader reader = new BufferedReader(new FileReader(path));
      String line = "";
      int vertex_cnt = 0;  // Vertex count
      int normal_cnt = 0;  // Vertex normal count         
      int uv_cnt = 0;      // Texcoord UV count
      int face_cnt = 0;    // Face count
      
      Vector<DCTriple> vertex_list = new Vector<DCTriple>();
      Vector<DCTriple> normal_list = new Vector<DCTriple>();
      Vector<DCFace> facelist = new Vector<DCFace>();
      
      
      String groupname = null;
      
      while ( (line=reader.readLine()) != null) {         
         // Parse out blanks and comments
         if (line.startsWith("#") || line.equals("")) continue;
         
         // Parse out material components for now
         if (line.startsWith("mtlib")|| line.startsWith("usemtl")) continue;        
         if (line.startsWith("vt")) continue;
         
         // If the group name "changes" than starts a new mesh
         if (line.startsWith("g")) {
            
            String tmp1[] = line.split(" ");
            
            String tmp2[] = tmp1[1].split("-");
            
            String name = tmp2[0];            
            if ( groupname != null && ! groupname.equalsIgnoreCase(name)) {               
               //model.components.put( groupname, (Vector<DCFace>)facelist.clone() );
               model.componentTable.remove(groupname);
               model.componentTable.put(groupname, new DCComponent(groupname, (Vector<DCFace>)facelist.clone()));
               
               // Already in there somewhere...
               facelist.removeAllElements();               
               if (model.componentTable.get(name) != null) {
                  facelist = (Vector<DCFace>)model.componentTable.get(name).faceList.clone();
               }
            }
            groupname = name;
         }
         
         String tk[] = line.split(" ");
         if (tk[0].equals("v")) {
            vertex_cnt ++;
            float x = Float.parseFloat(tk[1]);
            float y = Float.parseFloat(tk[2]);
            float z = Float.parseFloat(tk[3]);            
            vertex_list.add(new DCTriple(x,y,z));
         } else if (tk[0].equals("vn")) {
            normal_cnt ++;
            float x = Float.parseFloat(tk[1]);
            float y = Float.parseFloat(tk[2]);
            float z = Float.parseFloat(tk[3]);            
            normal_list.add(new DCTriple(x,y,z));            
         } else if (tk[0].equals("f")) {
            String seg1[] = tk[1].split("/");
            String seg2[] = tk[2].split("/");
            String seg3[] = tk[3].split("/");
            
            int i1 = Integer.parseInt(seg1[0]);
            int i2 = Integer.parseInt(seg2[0]);
            int i3 = Integer.parseInt(seg3[0]);
                      
            DCFace f = new DCFace();
            // Set the vertices
            f.p1 = vertex_list.elementAt( i1 - 1); 
            f.p2 = vertex_list.elementAt( i2 - 1);
            f.p3 = vertex_list.elementAt( i3 - 1);
            
            // Set the normals
            i1 = Integer.parseInt(seg1[2]); 
            i2 = Integer.parseInt(seg2[2]); 
            i3 = Integer.parseInt(seg3[2]); 
            f.n1 = normal_list.elementAt( i1 - 1);
            f.n2 = normal_list.elementAt( i2 - 1);
            f.n3 = normal_list.elementAt( i3 - 1);
            f.recalc();
            
                        
            facelist.addElement(f);            
            face_cnt ++;
         }                          
      }      
      
      // Done reading the file, put the remaining data into group
      //model.components.put( groupname, (Vector<DCFace>)facelist.clone() );
      model.componentTable.put(groupname, new DCComponent(groupname, (Vector<DCFace>)facelist.clone()));
      
      //facelist.removeAllElements();
      
      // Close reader and cleanup
      DWin.instance().debug("Total vertices: " + vertex_list.size());
      reader.close();
      System.gc(); // Please please collect some crap
      
      
      // Fun times
      /*
      System.out.println("Lets see how long it takes to binary sort " + facelist.size() + "polygons");
      System.out.println("Starting time : " + System.currentTimeMillis());
      BSPTree test = new BSPTree(facelist.remove(0));
      BSPTree.buildBSPTree(test, facelist);
      System.out.println("Ending time : " + System.currentTimeMillis());
      */
      
      /*
      Vector<DCComponent> list = new Vector<DCComponent>( model.componentTable.values());
      model.root = new BSPTree( BSPTree.jiggleFace(list.elementAt(0).faceList.elementAt(0)) );
      */
      
//      BSPTree.buildBSPTreeObj(model.root, list);
//      System.out.println("BSP Size : " + BSPTree.sizeof(model.root));
//      BSPTree.cascadeInit(model.root);
      
      
      return model;
   }
   
   
     
}
