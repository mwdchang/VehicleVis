package util;
import javax.media.opengl.GL2;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;

import model.DCColour;
import model.DCTriple;

import com.jogamp.graph.curve.opengl.RenderState;
import com.jogamp.graph.curve.opengl.TextRenderer;
import com.jogamp.graph.font.Font;
import com.jogamp.graph.font.FontFactory;
import com.jogamp.graph.geom.AABBox;
import com.jogamp.graph.geom.opengl.SVertex;
import com.jogamp.opengl.util.glsl.ShaderState;


/////////////////////////////////////////////////////////////////////////////////
// Handles GPU base fonts
/////////////////////////////////////////////////////////////////////////////////
public class GPUFont {
   
   protected GPUFont() {
      renderState = RenderState.createRenderState(new ShaderState(), SVertex.factory());
      renderer = TextRenderer.create(renderState, 0);
      fontSet = FontFactory.UBUNTU;
      font = FontFactory.get(fontSet).getDefault();      
      offset = new DCTriple(0, 0, 0);
   }
   
   
   public void startOrtho(GL3 gl3, float width, float height) { // Move the offset to 0      
      
      // Create an orthonormal projection
      renderer.reshapeOrtho(null, (int)width, (int)height, 0.1f, 3000.0f);
      
      // Check and init if necessary
      if (renderer.isInitialized() == false) {
         System.out.println("Initalizing GPUFont....");
         renderer.init(gl3);  
      }
      //renderer.resetModelview(null);
      //renderer.translate(gl3, offset.x, offset.y, -1); // Ortho mode...so z doesn't really matter as long as it is not clipped
   }
   
   
   public void draw(GL3 gl3, DCColour c, String s) {
      AABBox box = getDimension(s, 20);
      float h = box.getWidth();
      if (localX + h > width) {
         localX = 0.0f;
         localY -= box.getHeight();
      }
         
      renderer.resetModelview( null );
      renderer.translate(gl3,
                         offset.x + localX,
                         offset.y + localY,
                         -1);
         
      //renderer.translate(gl3, localX, localY, 0);
      
      renderer.setColorStatic(gl3, c.r, c.g, c.b);
      renderer.drawString3D(gl3, font, s, new float[]{0.0f, 0.0f, 0.0f}, 20, 1); 
      localX += h;
      
      //renderer.translate(gl3, box.getWidth(), 0, 0);
   }
   
   public void reset() {
      localX = localY = 0.0f;   
   }
   
   
   public void drawBoundingBox(GL2 gl2) {
      gl2.glColor4d(1, 1, 1, 1);
      gl2.glBegin(GL2.GL_LINE_LOOP);     
        gl2.glVertex3f(offset.x, offset.y, -1);
        gl2.glVertex3f(offset.x+width, offset.y, -1);
        gl2.glVertex3f(offset.x+width, offset.y+height, -1);
        gl2.glVertex3f(offset.x, offset.y+height, -1);
      gl2.glEnd();
   }
   
   
   // Get a boudning box
   public AABBox getDimension(String s, float size) {
      return font.getStringBounds(s, size);
   }
   
   
   // Singleton stuff
   private static GPUFont inst;
   public static GPUFont instance() {
      if (inst == null) inst = new GPUFont();
      return inst;
   }
   
   
   // Class vars
   public TextRenderer renderer;
   public RenderState renderState;
   public int fontSet;
   public Font font;
   public DCTriple offset = new DCTriple(0,0,0);
   public AABBox fontNameBox;   
   
   public float width  = 450;
   public float height = 200;
   public float localX = 0; // local x
   public float localY = height; // local y
   
}
