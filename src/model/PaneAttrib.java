package model;

/////////////////////////////////////////////////////////////////////////////////
// Generic attribute holder for a GUI panel
/////////////////////////////////////////////////////////////////////////////////
public class PaneAttrib {
   
   public PaneAttrib() {
      anchorX = anchorY = width = height = 0;       
      
      textureHeight = height;
      yOffset = height;
      active = false;
   }
   
   public PaneAttrib(float _anchorX, float _anchorY, float _width, float _height, int _direction) {
      anchorX = _anchorX;
      anchorY = _anchorY;
      width   = _width;
      height  = _height;
      direction = _direction;
      
      textureHeight = height;
      yOffset = height;
      active = false;
      
   }
   
   
   public float anchorX;
   public float anchorY;
  
   public float width;
   public float height;
   
   public float textureHeight;
   public boolean active;
   
   public float yOffset;
   public String selected = null;
   
   public int direction = 0;
   
   public boolean hasAllSelection = true; //All is treated special
   

}
