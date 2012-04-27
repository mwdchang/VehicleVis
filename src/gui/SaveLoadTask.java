package gui;

import java.util.Vector;

import javax.media.opengl.GL2;

import model.DCColour;

import org.jdesktop.animation.timing.interpolation.PropertySetter;

import TimingFrameExt.FloatEval;

import util.DCUtil;
import util.GraphicUtil;

import datastore.SSM;

import exec.RenderTask;

////////////////////////////////////////////////////////////////////////////////
// This class provides the save and load functions over a session
// This class is not self contained, in that partial user interactions
// are coded in SSM and EventManager class
////////////////////////////////////////////////////////////////////////////////
public class SaveLoadTask implements RenderTask {

	@Override
	public void render(GL2 gl2) {
	   GraphicUtil.setOrthonormalView(gl2, 0, SSM.windowWidth, 0, SSM.windowHeight, -10, 10);
	   saveLoad.yoffset = SSM.instance().saveLoadYOffset;
	   saveLoad.render(gl2);
	   
	   GraphicUtil.drawRoundedRect(gl2, (double)saveLoad.anchorX-30, (double)saveLoad.anchorY-10, 0.0, 22, 10, 4, 4,
            DCColour.fromDouble(0.68, 0.68, 0.68, 0.65).toArray(), 
            DCColour.fromDouble(0.77, 0.77, 0.77, 0.65).toArray());	         
	   
//	   gl2.glBegin(GL2.GL_QUADS);
//	      gl2.glColor4fv(SchemeManager.colour_red.toArray(), 0);
//	      gl2.glVertex3d(0, 0, 0);
//	      gl2.glVertex3d(100, 0, 0);
//	      gl2.glVertex3d(100, 100, 0);
//	      gl2.glVertex3d(0, 100, 0);
//	   gl2.glEnd();
	}

	
	@Override
	public void init(GL2 gl2) {
		saveLoad= new DCScrollPane("Previous");
		saveLoad.anchorX = SSM.instance().saveLoadAnchorX;
		saveLoad.anchorY = SSM.instance().saveLoadAnchorY;
		saveLoad.tagList.clear();
		saveLoad.direction = DCScrollPane.DOWN;
		
   	saveState(gl2);
		saveState(gl2);
		saveState(gl2);
		
		resetData(gl2);
	}
	
	
	
   ////////////////////////////////////////////////////////////////////////////////	
	// Load from a saved state
   ////////////////////////////////////////////////////////////////////////////////	
	public void loadState(SaveState ss) {
		// Restore state variables
      SSM.startTimeFrame = ss.startTimeFrame;
		SSM.endTimeFrame   = ss.endTimeFrame;
		SSM.startMonth     = ss.startMonth;
		SSM.endMonth       = ss.endMonth;
		SSM.startYear      = ss.startYear;
		SSM.endYear        = ss.endYear;
		SSM.manufactureAttrib.selected = ss.selectedManufacture;
		SSM.makeAttrib.selected = ss.selectedMake;
		SSM.modelAttrib.selected = ss.selectedModel;
		SSM.yearAttrib.selected = ss.selectedYear;
		
		// Reset all views
		SSM.dirty = 1;
		SSM.dirtyGL = 1;
		
		System.err.println("About to load state : " + SSM.startTimeFrame + " " + SSM.endTimeFrame);
		//SSM.instance().lensList.clear();
		
	}
	
	
   ////////////////////////////////////////////////////////////////////////////////	
	// Create a new save state
   ////////////////////////////////////////////////////////////////////////////////	
	public void saveState(GL2 gl2) {
		SaveState ss = new SaveState();
		ss.startTimeFrame = SSM.startTimeFrame;
		ss.endTimeFrame   = SSM.endTimeFrame;
		ss.startMonth     = SSM.startMonth;
		ss.endMonth       = SSM.endMonth;
		ss.startYear      = SSM.startYear;
		ss.endYear        = SSM.endYear;
		ss.selectedManufacture = SSM.manufactureAttrib.selected;
		ss.selectedMake = SSM.makeAttrib.selected;
		ss.selectedModel = SSM.modelAttrib.selected;
		ss.selectedYear = SSM.yearAttrib.selected;
		ss.label = ss.startTimeFrame + "-" + ss.endTimeFrame;
		stateList.add(ss);
		
//	   resetData(gl2);	
	}
	
	
   ////////////////////////////////////////////////////////////////////////////////	
	// Set the widget data
   ////////////////////////////////////////////////////////////////////////////////	
	public void resetData(GL2 gl2) {
		//stateList.clear();
		saveLoad.tagList.clear();
		
	
		
      int cnt=0;
		//saveLoad.tagList.add(new GTag(1, (i+1)*DCScrollPane.spacing, i*DCScrollPane.spacing, "Save", "Test 1"));
		for (int i=0; i < stateList.size(); i++) {
		   //cnt++;
		   SaveState ss = stateList.elementAt(i);
		   String label = ss.label;
		   String index = i+"";
   		saveLoad.tagList.add(new GTag(1, (i+1)*DCScrollPane.spacing, i*DCScrollPane.spacing, label, index, -1));
		}
		
		if (saveLoad.tagList.size() <= 0) return;
		
      int prevSaveLoad = -1;		
      saveLoad.texPanelHeight = saveLoad.tagList.lastElement().y;
      SSM.instance().saveLoadTexHeight = saveLoad.texPanelHeight;
      SSM.instance().saveLoadHeight = Math.min(SSM.instance().saveLoadTexHeight, SSM.instance().defaultScrollHeight);
      if (saveLoad.height > 0) saveLoad.height = SSM.instance().saveLoadHeight;
      saveLoad.dirty = true;       
      if (prevSaveLoad < 0) {
         saveLoad.current = 0;   
         saveLoad.currentStr = saveLoad.tagList.elementAt(0).val;
         SSM.instance().selectedSaveLoad = null;
         SSM.instance().saveLoadYOffset = SSM.instance().saveLoadHeight;
      }		
      float tempY = 0;
      if (prevSaveLoad >= 0) {
         tempY = saveLoad.tagList.elementAt(prevSaveLoad).y + DCScrollPane.spacing;
         SSM.instance().saveLoadYOffset = Math.max( tempY, SSM.instance().saveLoadHeight);
      }      
	
	}
	
	
	

	@Override
	public void picking(GL2 gl2, float px, float py) {
      if (SSM.instance().l_mouseClicked == false) return;
      
      float mx = px;
      float my = SSM.windowHeight - py;
      
      float sl_anchorX = SSM.instance().saveLoadAnchorX;
      float sl_anchorY = SSM.instance().saveLoadAnchorY;
      
      // Check if the save button is pressed
      if (DCUtil.between(mx, saveLoad.anchorX-30-20, saveLoad.anchorX-30+20)) {
         if (DCUtil.between(my, saveLoad.anchorY-10-10, saveLoad.anchorY-10+10)) {
         	System.out.println("Pressed save button");
         	saveState( gl2 );
            resetData( gl2 );
         	return; 
         }
      }
      
      
      // Check if the load button is pressed
      if (DCUtil.between(mx, sl_anchorX, sl_anchorX+SSM.instance().scrollWidth)) {
         if (DCUtil.between(my, sl_anchorY-20-SSM.instance().saveLoadHeight, sl_anchorY-20)) {
         	
         	System.out.println("Pressed item to load");
            
            // 1) Calculate the texture coordinate
            float texX = mx - sl_anchorX;
            float texY = SSM.instance().saveLoadHeight - Math.abs(my - (sl_anchorY-20));
            
            // 2) Adjust for Y-offset
            texY = SSM.instance().saveLoadYOffset - (texY);
            
            for (int i=0; i < saveLoad.tagList.size(); i++) {
               GTag t = saveLoad.tagList.elementAt(i);                
               // Window system is upside down
               if (texY >= t.yPrime && texY <= t.y) {
                  saveLoad.current = i; 
                  saveLoad.currentStr = t.val;
                  saveLoad.dirty  = true;
                  
                  SSM.dirty = 1;
                  SSM.dirtyGL = 1;
                  SSM.instance().selectedSaveLoad = i==0? null:t.val;
                  
                  System.out.println("Clicked on " + t.val);
                  
                  // Translate item value to previous state
                  SaveState ss = stateList.elementAt(Integer.parseInt(t.val)); 
                  loadState(ss);
                  SSM.dirtyLoad = 1;
                  // Fake a slider movement
                  //SSM.instance().currentFocusLayer = SSM.UI_LAYER; 
                  //SSM.instance().l_mousePressed = false;
                  
                  break;
               }
            }            
            return;
         }
      }      
      if (DCUtil.between(mx, sl_anchorX, sl_anchorX+SSM.instance().scrollWidth)) {
         if (DCUtil.between(my, sl_anchorY-20, sl_anchorY)) {
         	System.out.println("Pressed load button");
            SSM.instance().saveLoadActive = ! SSM.instance().saveLoadActive;
            if (SSM.instance().saveLoadActive) {
               saveLoad.animator = PropertySetter.createAnimator(600, saveLoad, "height", new FloatEval(), saveLoad.height, SSM.instance().saveLoadHeight); 
               saveLoad.animator.start();
            } else {
               saveLoad.animator = PropertySetter.createAnimator(600, saveLoad, "height", new FloatEval(), saveLoad.height, 0.0f); 
               saveLoad.animator.start();
            }
         }
      }		
	}
	
	public DCScrollPane saveLoad;
	public DCScrollPane saveButton;
	
	public Vector<SaveState> stateList = new Vector<SaveState>();

	
	// A sub class to hold all saved state information
	public class SaveState {
		public String label;
	   public String startTimeFrame;
	   public String endTimeFrame;
	   public int startMonth = 0;  // 0 - 11
	   public int endMonth = 0;    // 0 - 11
	   public int startYear = 0;
	   public int endYear = 0;	   
	   public String selectedManufacture = null;
	   public String selectedMake = null;
	   public String selectedModel = null;	   
	   public String selectedYear = null;
	}
}
