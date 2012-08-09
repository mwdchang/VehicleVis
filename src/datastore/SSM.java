package datastore;

import gui.DCTip;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.jdesktop.animation.timing.interpolation.PropertySetter;

import TimingFrameExt.FloatEval;

import touch.WCursor;
import util.DWin;
import model.DCTriple;
import model.LensAttrib;
import model.PaneAttrib;



/////////////////////////////////////////////////////////////////////////////////
// SSM (System State Manager)
// Stores the system states that may be shared across different modules 
// for example: 
//   - currently focused group object
//   - the global date range 
//
/////////////////////////////////////////////////////////////////////////////////
public class SSM {
   private static SSM instance;
   
   public static String database = "demo";
   //public static String database = "projectv3";
   
   public static double HOLD_DELAY = 350;
   public static DCTriple waitMarker = null;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Default constructor
   //   Just make sure all the variable have some sort of default value
   ////////////////////////////////////////////////////////////////////////////////
   protected SSM() {
      dirty = 0; 
      dirtyGL = 0;
      selectedGroup = new Hashtable<Integer, Integer>();
      startTimeFrame = "19950101";  // yyyyMMdd
      endTimeFrame   = "19951231";  // yyyyMMdd
      startYear = 1995;
      endYear = 1995;
      startMonth = 0;
      endMonth = 0;
      
      renderSihoulette = false;
      mouseX = 0;
      mouseY = 0;
      refreshMagicLens = true;
      refreshOITBuffers = true;
      occlusionLevel = 0;
      useGuide = false;
      useCircularLabel = false;
      relatedList = new Vector<Integer>();
      
      
      // Parse runtime parameters
      useTUIO         = Boolean.parseBoolean(System.getProperty("UseTUIO", "true"));
      useFullScreen   = Boolean.parseBoolean(System.getProperty("UseFullScreen", "true"));
      refreshRate     = Long.parseLong(System.getProperty("RefreshRate", "800"));
      nearThreshold   = Float.parseFloat(System.getProperty("NearThreshold", "0.2f")); 
      downsampleRate  = Integer.parseInt(System.getProperty("DownsampleRate", "3"));
      useScenario     = Boolean.parseBoolean(System.getProperty("UseScenario", "true"));
      
   }
   
   
   public static SSM instance() {
      if (instance == null) instance = new SSM();
      return instance;
   }
   
   
   // Runtime parameters
   public static boolean useScenario = false;
   
   public static boolean useTUIO = false;
   public static boolean useFullScreen = false;
   public static long refreshRate = 800L;
   public static float nearThreshold = 0.2f;
   public static int downsampleRate = 3;
   
   
   // This should be a multiple of 2 due to the rounded rectangle thingy
   public static float scrollHeight = 24;
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset data
   // - destroy all lens
   // - reset near far and fov 
   ////////////////////////////////////////////////////////////////////////////////
   public static void reset() {
      fov = 30.0f;   
      nearPlane = 1.0f;
      farPlane = 1000.0f;
      
      globalFetchIdx = 0;
      docStartIdx = 0;
      dirtyGL = 1;
      dirty = 1;
      refreshMagicLens = true;
      refreshOITBuffers = true;
      
      
      // Clear out panel
      docActive = false;
      
      
      manufactureAttrib.selected = null;
      manufactureAttrib.active = false;
      
      makeAttrib.selected = null;
      makeAttrib.active = false;
      
      modelAttrib.selected = null;
      modelAttrib.active = false;
      
      yearAttrib.selected = null;
      yearAttrib.active = false;
      
      selectedGroup = new Hashtable<Integer, Integer>();
      
      c_manufactureAttrib.selected = "None";
      c_manufactureAttrib.active =false;
      
      c_makeAttrib.selected = null;
      c_makeAttrib.active = false;
      
      c_modelAttrib.selected = null;
      c_modelAttrib.active = false;
      
      c_yearAttrib.selected = null;
      c_yearAttrib.active = false;
      
      
      relatedList.clear();
      
      // Clear buffers
      pickPoints.clear();
      lensList.clear();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Thanks to IEEE 754 ... we need an EPS number ... thanks to Java...this
   // needs to be a double .... sigh...
   ////////////////////////////////////////////////////////////////////////////////
   public static double EPS = 1E-4;
   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // 3D perspective variables
   ///////////////////////////////////////////////////////////////////////////////// 
   public static float fov = 30.0f;
   public static float nearPlane = 1.0f;
   public static float farPlane  = 1000.0f; 
   
   public static boolean  renderSihoulette;
   public static boolean  useLight = true;
   public static float rotateX = 0.0f;
   public static float rotateY = 0.0f;
   
   
//   
   // Need to sync this because graphics
   // and logic are running in different threads ... thanks Java !!!
//   public synchronized void setCurrentGroup(Integer group) {
//      currentGroup = group;   
//   }
//   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Indicators for graphic effects
   ///////////////////////////////////////////////////////////////////////////////// 
   
   public static int LEN_TEXTURE_WIDTH = 800;   
   public static int LEN_TEXTURE_HEIGHT = 800;
   public static boolean refreshMagicLens;
   public static boolean refreshOITBuffers = true;
   public static boolean refreshOITTexture = true;
   public static boolean refreshGlowTexture = true;
   
   //public short NUM_LENS = 1;   
   
   public static Vector<LensAttrib> lensList = new Vector<LensAttrib>();
   
   public static void clearLens() {
      for (int i=0; i < lensList.size(); i++) {
         lensList.elementAt(i).handleSelected = false;
         lensList.elementAt(i).magicLensSelected = 0;
         lensList.elementAt(i).deSelectAnimator = PropertySetter.createAnimator(200, lensList.elementAt(i), "borderSize", new FloatEval(), lensList.elementAt(i).borderSizeNormal);
         lensList.elementAt(i).deSelectAnimator.start();
      }
   }
   
   public static int lensSelected() {
      for (int i=0; i < lensList.size(); i++) {
         if (lensList.elementAt(i).magicLensSelected == 1) return 1;    
      }
      return 0;
   }
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Hold the coordinates to execute picking
   ///////////////////////////////////////////////////////////////////////////////// 
   public static Vector<DCTriple> pickPoints  = new Vector<DCTriple>(0);
   
   public static Hashtable<Long, DCTriple> dragPoints = new Hashtable<Long, DCTriple>(0);
   
   public static Hashtable<Long, DCTriple> hoverPoints = new Hashtable<Long, DCTriple>();
   public static Hashtable<Long, DCTip> tooltips = new Hashtable<Long, DCTip>();
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Level of Detail 
   ///////////////////////////////////////////////////////////////////////////////// 
   public static int g_numPasses = 5;
   public static int g_numGeoPasses = 0;
   
   public static short stipplePattern = (short)0xF0FA;

   
   ///////////////////////////////////////////////////////////////////////////////// 
   // GUI Environment
   ///////////////////////////////////////////////////////////////////////////////// 
   public static float sparkLineHeight  = 80;
   public static float sparkLineWidth   = 200.0f;
   public static int   sparkLineSegment = 50; 
   
   public static float heatmapCellHeight = 12;
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // The current focus group/component
   ///////////////////////////////////////////////////////////////////////////////// 
   public static Hashtable<Integer, Integer> selectedGroup;
   public static Integer occlusionLevel;
   public static Vector<Integer> relatedList; // The list of components that are related to the selected components
   public static int maxOccurrence = Integer.MAX_VALUE;
   public static int minOccurrence = Integer.MIN_VALUE;
   
   public static int stopPicking = 0; // Short circuit logic to exit picking loop
   
   
   // For grid selection
   public int selectedX = -1;
   public int selectedY = -1;
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Switches
   ////////////////////////////////////////////////////////////////////////////////
   public static boolean useGuide = false;         // Show various debugging artifacts
   public static boolean useCircularLabel = false; // Whether to laybel the sparklines in a circular pattern 
   public static boolean showLabels = true;        // Whether to show labels at all
   public static boolean captureScreen = false;
   public static int sortingMethod = 0;            // Controls how the components are sorted (with respect to rendering order)
   public static int colouringMethod = 4;
   public static int sparklineMode = 1;
   public static boolean useAggregate = false;        // Whether the occurrence count should crawl the parts hierarchy
   public static boolean useFullTimeLine = true;      // Whether to use the entire timeline for the component chart
   public static boolean useDualDepthPeeling = true;  // Whether to use OIT transparency
   public static boolean useConstantAlpha = false;    // Whether or not to use OIT constant alpha
   public static boolean useGlow = true;
   public static boolean useComparisonMode = false;   // Whether to compare across time lines
   public static boolean useLocalFocus = true;       // Whether to nor to render based on current selected components 
   public static boolean use3DModel = true;          // Whether to use integrated 3D view 
   public static boolean useFlag = true;             // Just a temporary flag to trigger adhoc tests and stuff, not used for real data
   public static boolean useStipple = false;         // Whether to use stippling for the labels
   
   public static boolean checkDragEvent = false; 
   
   public static int chartMode = 1;
   public static final int CHART_MODE_BY_MONTH_MAX     = 1;
   public static final int CHART_MODE_BY_COMPONENT_MAX = 2;
   public static final int CHART_MODE_BY_GLOBAL_MAX    = 3;
   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Indicates the global selected time period
   ///////////////////////////////////////////////////////////////////////////////// 
   public static String startTimeFrame;
   public static String endTimeFrame;
   public static int startMonth = 0;  // 0 - 11
   public static int endMonth = 0;    // 0 - 11
   public static int startYear = 0;
   public static int endYear = 0;
   public static int startIdx = 0;
   public static int endIdx = 0;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Just for fun
   // Things that may or may not be useful but seems interesting to do (to me)
   ////////////////////////////////////////////////////////////////////////////////
   public static boolean colourRampReverseAlpha = false; // Whether to inverse the alpha in the colour scale 
   
   
   public static boolean hidePanel = false;
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Filter Panel management
   ////////////////////////////////////////////////////////////////////////////////
   //public static float DoffsetX = 680;
   //public static float DoffsetY = 850;
   public static float DoffsetX = 0;
   public static float DoffsetY = 0;
   
   // Piggy back on the animator function
   public static void setDoffsetX(float x) { 
      DoffsetX = x; 
      legendAnchorX = 30 + DoffsetX;
      filterControlAnchorX = 120f + DoffsetX;
      c_filterControlAnchorX = 120f + DoffsetX;
      manufactureAttrib.anchorX = 180 + DoffsetX;
      makeAttrib.anchorX = 400 + DoffsetX;
      modelAttrib.anchorX = 620 + DoffsetX;
      yearAttrib.anchorX = 840 + DoffsetX;
      c_manufactureAttrib.anchorX = 180 + DoffsetX;
      c_makeAttrib.anchorX = 400 + DoffsetX;
      c_modelAttrib.anchorX = 620 + DoffsetX;
      c_yearAttrib.anchorX = 840 + DoffsetX;
      aggregationAnchorX = 100f + DoffsetX;
      perspectiveAttrib.anchorX = 180 + DoffsetX;
      
      yearAnchorX = 30.0f + DoffsetX;
      monthAnchorX = 30.0f + DoffsetX;
   }
   public static float getDoffsetX() { return DoffsetX; }
   
   public static float yearAnchorX = 60.0f;
   public static float yearAnchorY = 970;
   public static float monthAnchorX = 60.0f;
   public static float monthAnchorY = 880;
   
   public static float rangeSliderAnchorX = 80;
   
   public static float legendAnchorX = 30 + DoffsetX;
   public static float legendAnchorY = 20 + DoffsetY;
   
   
   public static float filterControlAnchorX = 120f + DoffsetX;
   public static PaneAttrib manufactureAttrib = new PaneAttrib(180+DoffsetX, 90+DoffsetY, 200, 200, 1);
   public static PaneAttrib makeAttrib        = new PaneAttrib(400+DoffsetX, 90+DoffsetY, 200, 200, 1);
   public static PaneAttrib modelAttrib       = new PaneAttrib(620+DoffsetX, 90+DoffsetY, 200, 200, 1);
   public static PaneAttrib yearAttrib        = new PaneAttrib(840+DoffsetX, 90+DoffsetY, 200, 200, 1);
   
   public static float c_filterControlAnchorX = 120f + DoffsetX;
   public static PaneAttrib c_manufactureAttrib = new PaneAttrib(180+DoffsetX, 50+DoffsetY, 200, 200, 1);
   public static PaneAttrib c_makeAttrib        = new PaneAttrib(400+DoffsetX, 50+DoffsetY, 200, 200, 1);
   public static PaneAttrib c_modelAttrib       = new PaneAttrib(620+DoffsetX, 50+DoffsetY, 200, 200, 1);
   public static PaneAttrib c_yearAttrib        = new PaneAttrib(840+DoffsetX, 50+DoffsetY, 200, 200, 1);
   
   public static PaneAttrib perspectiveAttrib   = new PaneAttrib(180+DoffsetX, 170+DoffsetY, 150, 100, 1);
   
   public static float aggregationAnchorX = 100f + DoffsetX;
   public static float aggregationAnchorY = 125f + DoffsetY;
   
   
   public static float offset_labelX = 130;
   public static float offset_labelY = 30;
   public static float offset_markerX = 160;
   public static float offset_markerY = 30;
   
   
   public static int manufactureMax = 0;
   public static int makeMax = 0;
   public static int modelMax = 0;
   public static int yearMax = 0;
   public static int c_manufactureMax = 0;
   public static int c_makeMax = 0;
   public static int c_modelMax = 0;
   public static int c_yearMax = 0;
   
   
   public static float saveLoadAnchorX = 850;
   public static float saveLoadAnchorY = 950;
   public static float saveLoadYOffset = 200;
   public static float saveLoadHeight = 200;
   public static float saveLoadTexHeight = 200;
   public static boolean saveLoadActive = false;
   
   
   public static float scrollWidth = 200;
   public static float defaultScrollHeight = 200;
   
   
   public static String selectedSaveLoad = null;
   
   
   
   // These are not anchor points, they are actually
   // the reverse, the widgets update these so 
   // it can be used in other place where the widgets
   // are not visisble.
   public static DCTriple yearLow[] = new DCTriple[3];
   public static DCTriple yearHigh[] = new DCTriple[3];
   public static DCTriple monthLow[] = new DCTriple[3];
   public static DCTriple monthHigh[] = new DCTriple[3];
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Document Management
   ////////////////////////////////////////////////////////////////////////////////
   public static int globalFetchIdx = 0;
   public static int globalFetchSize = 30;
   public static int docMaxSize = 0;
   public static int docStartIdx = 0; 
   public static boolean docActive = false;
   public static int resizePanel = 0;
   
   // All document sizes are measures in pixels
   public static float docAnchorX = 400;                 // Bottom left of the document panel
   public static float docAnchorY = 400;                 // Bottom left of the document panel
   public static float docPadding = 25;                  // Padding area (for dragging)
   public static float docWidth  = 550;                  
   public static float docHeight = 400;
   public static float docHeader = 15;     // not currently used
   public static float docFooter = 15;     // not currently used
   public static float yoffset = docHeight;
   
   public static float t1Height = 0; // bad placement, just a proof of concept here
   public static float t2Height = 0; // bad placement, just a proof of concept here
   public static int t1Start = 0;
   public static int t2Start = globalFetchSize;
   public static int docAction = 0;
   
   // Check to see if the mouse cursor is in the document header
//   public boolean inDocHeader(float px, float py) {
//      float mX = mouseX;
//      float mY = windowHeight - mouseY;
//      if (mX >= docAnchorX + docPadding && mX <= docAnchorX+docWidth-docPadding) {
//         if (mY >= docAnchorY+docHeight-docPadding-docHeader && mY <= docAnchorY+docHeight-docPadding) {
//            return true;
//         }
//      }
//      return false;
//   }
//   
//   
//   // Check to see if the mouse cursor is in the document footer
//   public boolean inDocFooter(float px, float py) {
//      float mX = mouseX;
//      float mY = windowHeight - mouseY;
//      if (mX >= docAnchorX + docPadding && mX <= docAnchorX+docWidth-docPadding) {
//         if (mY >= docAnchorY+docPadding && mY <= docAnchorY+docPadding+docFooter) {
//            return true;
//         }
//      }
//      return false;
//   }
   

   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Dynamic position
   // Stuff that are "hooked" into borders ... etc etc
   ////////////////////////////////////////////////////////////////////////////////
   //public float getYearAnchorX() { return 30.0f; }
   //public float getMonthAnchorX() { return 30.0f; }
   //public float getYearAnchorY() { return windowHeight - 80; }
   //public float getMonthAnchorY() { return windowHeight - 170; }

   
   public float rangeFilterHeight = 50;
   public float rangeFilterWidth  = 45;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Animation controls
   // Durations are in milliseconds
   ////////////////////////////////////////////////////////////////////////////////
   public static int SCROLL_DURATION = 500;
   public static int PART_CHANGE_DURATION = 1000;
   public static int TIME_CHANGE_DURATION = 1000;
   public static int FADE_DURATION = 2500;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Big brother
   ////////////////////////////////////////////////////////////////////////////////
   public static int sceneRotate = 0;
   public static int sceneZoom = 0;
   public static int selectObjHeatmap = 0;
   public static int selectObj3D = 0;
   public static int lensMove = 0;
   public static int lensResize = 0;
   public static int lensZoom = 0;
   
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Handle top elements separately
   // These are activated/de-activated with the mouse press and mouse release 
   // respectively
   ////////////////////////////////////////////////////////////////////////////////
   public static int ELEMENT_NONE = 0;
   public static int ELEMENT_LENS = 1;
   public static int ELEMENT_DOCUMENT = 2;
   public static int ELEMENT_MANUFACTURE_SCROLL = 3;
   public static int ELEMENT_MAKE_SCROLL = 4;
   public static int ELEMENT_MODEL_SCROLL = 5;
   public static int ELEMENT_YEAR_SCROLL = 6;
   public static int ELEMENT_SAVELOAD_SCROLL = 7;
   public static int ELEMENT_FILTER = 8;
   public static int ELEMENT_SCENARIO = 9;
   
   public static int ELEMENT_CMANUFACTURE_SCROLL = 13;
   public static int ELEMENT_CMAKE_SCROLL = 14;
   public static int ELEMENT_CMODEL_SCROLL = 15;
   public static int ELEMENT_CYEAR_SCROLL = 16;
   
   public static int ELEMENT_LENS_HANDLE = 99;
   public static int ELEMENT_LENS_RIM = 100;
   
   
   public static int ELEMENT_PERSPECTIVE_SCROLL = 17;
   
   public static int ELEMENT_CLEAR_SELECTION = 18;
   
//   public static int ELEMENT_AGGREGATION = 18;
   
   public static int topElement = ELEMENT_NONE;
   //public int location   = ELEMENT_NONE; // Horrible hack
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns the current time
   // in YYYY_MM_DD_HH_MM_SS format
   ////////////////////////////////////////////////////////////////////////////////
   public String now() {
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_h_mm_ss");
      String r = formatter.format(cal.getTime());
      return Const.TMP_DIR + "cap_" + r + ".png";
   }
   
   
   
   // Debugging
   public void timeFrameStatistics() {
      int sidx = CacheManager.instance().getDateKey(startTimeFrame);
      int eidx = CacheManager.instance().getDateKey(endTimeFrame);
      
      Hashtable<Integer, Integer> h = CacheManager.instance().getPartOccurrenceFilter(
            sidx, 
            eidx, 
            startMonth, 
            endMonth, 
            manufactureAttrib.selected, 
            makeAttrib.selected, 
            modelAttrib.selected);   
      
      DWin.instance().debug("====================================");
      Enumeration<Integer> e = h.keys();
      while (e.hasMoreElements()) {
         Integer key = e.nextElement();   
         String part = HierarchyTable.instance().partTable.get(key).elementAt(0);
         DWin.instance().debug( part + " " + h.get(key));
      }
      DWin.instance().debug("====================================");
      
   }
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Window Environment
   ///////////////////////////////////////////////////////////////////////////////// 
   public static int mouseX;
   public static int mouseY;
   public static int oldMouseX;
   public static int oldMouseY;
   public static int windowWidth;
   public static int windowHeight;
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Events - just enums, not bitmasks
   // TODO: this uses a single variable to track mouse events ... this is NOT GOOD....
   // fix this when bored !!!
   ///////////////////////////////////////////////////////////////////////////////// 
   public static boolean l_mouseClicked = false;
   public static boolean r_mouseClicked = false;
   public static boolean l_mousePressed = false;
   public static boolean r_mousePressed = false;
   
   
   
   public static boolean controlKey = false;
   public static boolean shiftKey = false;
   
   
   // Figure out which parts are selected based on a precedence order of :
   // 1 - UI_LAYER
   // 2 - COMPONENT_LAYER
   // Otherwise an action has taken on non interactive element (IE: camera movement)
   public static int UI_LAYER= 1;
   public static int COMPONENT_LAYER= 2;
   //public int currentFocusLayer = 0;
   
   public static int dirty = 0;
   public static int dirtyGL = 0;
   public static int dirtyLoad = 0;
   public static int dirtyDateFilter = 0;
   
   
   public float segmentMax = 0;
   
   // To store the touch points - as a visual aid, it does not have any logic in itself
   // Store this in OpenGL coordinate system ( bottom left is 0,0 )
   public static Hashtable<Long, WCursor> touchPoint = new Hashtable<Long, WCursor> ();
   public static Vector<DCTriple> invalidPoint = new Vector<DCTriple>();
   
}
