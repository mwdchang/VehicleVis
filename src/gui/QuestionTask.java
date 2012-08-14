package gui;

import java.awt.Color;
import java.awt.Font;
import java.util.Vector;

import javax.media.opengl.GL2;

import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.interpolation.PropertySetter;


import TimingFrameExt.FloatEval;

import model.DCColour;

import datastore.SSM;

import util.ALogger;
import util.DCCamera;
import util.DCUtil;
import util.GraphicUtil;
import util.TextureFont;

import exec.RenderTask;

////////////////////////////////////////////////////////////////////////////////
// Shows a series of questions/scenarios in sequential ordering
////////////////////////////////////////////////////////////////////////////////
public class QuestionTask implements RenderTask {

   @Override
   public void render(GL2 gl2) {
      
      if (SSM.useScenario == false) return;
      
      if (q.elementAt(qIdx).answered()) {
         GraphicUtil.drawRoundedRect(gl2, (SSM.windowWidth - 150), (SSM.windowHeight - 100 + 20), 0,
               50, 25, 5, 5, 
               DCColour.fromDouble(0.0, 0.2, 0.8, 0.8).toArray(), 
               DCColour.fromDouble(0.0, 0.2, 0.6, 0.8).toArray());
      } else {
         GraphicUtil.drawRoundedRect(gl2, (SSM.windowWidth - 150), (SSM.windowHeight - 100 + 20), 0,
               50, 25, 5, 5, 
               DCColour.fromDouble(0.8, 0.8, 0.8, 0.8).toArray(), 
               DCColour.fromDouble(0.6, 0.6, 0.6, 0.8).toArray());
      }
      
      tf.anchorX = SSM.windowWidth  - 200;
      tf.anchorY = SSM.windowHeight - 100;
      tf.render(gl2);
      //tf.renderBorder(gl2);
      
      //q_tf.anchorX = SSM.windowWidth - 500;
      //q_tf.anchorY = SSM.windowHeight - 100;
      q_tf.render(gl2);
      //q_tf.renderBorder(gl2);
      
   }

   @Override
   public void init(GL2 gl2) {
      tf = new TextureFont();
      tf.width = 100;
      tf.height = 50;
      tf.addMark("Proceed", Color.LIGHT_GRAY, GraphicUtil.labelFont, 15, 15);
      tf.renderToTexture(null);
      
      q_tf = new TextureFont();
      q_tf.width  = 350;
      q_tf.height = 50;
      //q_tf.addMark("Task : " + qIdx, Color.BLACK, GraphicUtil.font, 5, 20);
      //q_tf.addMark(q.elementAt(qIdx).text(), Color.BLACK, GraphicUtil.font, 5, 5);
      //String s[] = q.elementAt(qIdx).text().split("\n");
      Vector<StringBuffer> sb = tSplit(q.elementAt(qIdx).text());
      for (int i=0; i < sb.size(); i++) {
         q_tf.addMark(sb.elementAt(i).toString(), Color.BLACK, GraphicUtil.font, 2, q_tf.height - (i+1)*getHardFontHeight(GraphicUtil.font));
      }
      q_tf.renderToTexture(null);
      q_tf.anchorX = SSM.windowWidth - 500;
      q_tf.anchorY = SSM.windowHeight - 100;
   }

   public float getHardFontHeight(Font f) {
      float size = f.getSize();
      return size * 1.02f;
   }   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Just checking that the button is pressed ... somehow
   ////////////////////////////////////////////////////////////////////////////////
   @Override
   public void picking(GL2 gl2, float px, float py, float pz) {
      if (SSM.useScenario == false) return;
      if (SSM.l_mouseClicked == false) return;
      
      float realX = px;
      float realY = SSM.windowHeight - py;
      
      if (DCUtil.between(realX, SSM.windowWidth-200, SSM.windowWidth-100)) {
         if (DCUtil.between(realY, SSM.windowHeight-100, SSM.windowWidth-60)) {
            // Set picking to true so we do not go through other tasks
            SSM.stopPicking = 1;
            
            if (q.elementAt(qIdx).answered() && qIdx < q.size()) {
               System.out.println("about to log.................");
               q.elementAt(qIdx).endTime = System.currentTimeMillis();
               
               log( q.elementAt(qIdx) );
               qIdx ++;   
               q_tf.clearMark();
               
               Vector<StringBuffer> sb = tSplit(q.elementAt(qIdx).text());
               for (int i=0; i < sb.size(); i++) {
                  q_tf.addMark(sb.elementAt(i).toString(), Color.BLACK, GraphicUtil.font, 2, q_tf.height - (i+1)*getHardFontHeight(GraphicUtil.font));
               }               
               
               
               q_tf.renderToTexture(null);
               q.elementAt(qIdx).startTime = System.currentTimeMillis();
               q.elementAt(qIdx).set();
               SSM.stopPicking = 1;
               
               
               //q_tf.anchorX += 400;
               Animator moveAnimator = PropertySetter.createAnimator(1000, q_tf, "anchorX", new FloatEval(), q_tf.anchorX+800, q_tf.anchorX);
               moveAnimator.start();
               
               // Hack to clean up memory
               System.gc();
               
            }
         }
      }
   }
   
   ////////////////////////////////////////////////////////////////////////////////
   // Generic logging procedure
   ////////////////////////////////////////////////////////////////////////////////
   public void log(Question q) {
      ALogger.instance().log("Task: " + q.text() );
      ALogger.instance().log("Duration : " + (q.endTime-q.startTime));
      //ALogger.instance().log("Start Time: "+ q.startTime);
      //ALogger.instance().log("End Time: "+ q.endTime);
      ALogger.instance().log("Start Year: " + SSM.startYear);
      ALogger.instance().log("End   Year: " + SSM.endYear);
      ALogger.instance().log("Start Month: " + SSM.startMonth);
      ALogger.instance().log("End   Month: " + SSM.endMonth);
      ALogger.instance().log("Use Comparison: " + SSM.useComparisonMode);
      ALogger.instance().log("Use Aggregation: " + SSM.useAggregate);
      ALogger.instance().log("Heatmap Perspective: " + SSM.chartMode);
      ALogger.instance().log("1 Manufacturer: " + SSM.manufactureAttrib.selected);
      ALogger.instance().log("1 Make        : " + SSM.makeAttrib.selected);
      ALogger.instance().log("1 Model       : " + SSM.modelAttrib.selected);
      ALogger.instance().log("1 Year        : " + SSM.yearAttrib.selected);
      ALogger.instance().log("2 Manufacturer: " + SSM.c_manufactureAttrib.selected);
      ALogger.instance().log("2 Make        : " + SSM.c_makeAttrib.selected);
      ALogger.instance().log("2 Model       : " + SSM.c_modelAttrib.selected);
      ALogger.instance().log("2 Year        : " + SSM.c_yearAttrib.selected);
      ALogger.instance().log("Perspective: " + DCCamera.instance().eye + " " + SSM.rotateX + " " + SSM.rotateY);
      ALogger.instance().log("Selected Entities: " + SSM.selectedGroup.values().toString() );
      
      for (int i=0; i < SSM.lensList.size(); i++) {
         ALogger.instance().log("Lens: " +  
            SSM.lensList.elementAt(i).magicLensX + " " +
            SSM.lensList.elementAt(i).magicLensY + " " +
            SSM.lensList.elementAt(i).magicLensRadius 
         );   
      }
      ALogger.instance().log("................................................................................");
   }
   
   
   public TextureFont tf   = null; // Button Texture
   public TextureFont q_tf = null; // Question Texture 
   
   
   // Class presenting a single scenario
   public abstract class Question {
      public abstract boolean answered();   
      public abstract void set();
      public abstract String text();
      //public String txt;
      
      // Not really tracking performance here, but might as well
      // squeeze it in just in case we do use timing metrics
      // Use system milliseconds
      public long startTime;
      public long endTime;
   }
   
   //public Question[] q = new Question[3];
   public Vector<Question> q = new Vector<Question>();
   public int qIdx = 0;
   
   
   public boolean dcEquals(String a, String b) {
      if (a != null && b != null) {
         return a.equals(b);   
      } 
      if (a == null && b == null) return true;
      return false;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Customized string splitter
   ////////////////////////////////////////////////////////////////////////////////
   public Vector<StringBuffer> tSplit(String s) {
      Vector<StringBuffer> result = new Vector<StringBuffer>();
      String tokens[] = s.split(" "); 
      
      int c=0;
      result.add(new StringBuffer());
      for (int i=0; i < tokens.length; i++) {
         c += tokens[i].length();
         if (c > 40) {
            result.add(new StringBuffer());   
            c = 0;
         }
         result.lastElement().append(tokens[i] + " ");
      }
      return result;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Each question can be loaded as a test trial scenario
   ////////////////////////////////////////////////////////////////////////////////
   public QuestionTask() {
      ////////////////////////////////////////////////////////////////////////////////
      // Warm up task
      ////////////////////////////////////////////////////////////////////////////////
      q.add( new Question() {
         public boolean answered() {
            return SSM.startYear == 2000 && 
                   SSM.endYear   == 2001;
         }
         public void set() {
            SSM.startMonth = 5;
            SSM.endMonth = 6;
            SSM.dirty = 1;
            SSM.dirtyLoad = 1;
         }
         public String text() { return "Warm up task: Select years 2000 and 2001 on the year slider"; }
      });
      
      q.add(new Question() {
         public boolean answered() {
            return SSM.selectedGroup.size() > 1;
         }
         public void set() {
            SSM.reset();
         }
         public String text() { return "Warm up task: Select at least two components on the 3D vehicle model"; }
      });
      
      q.add(new Question() {
         public boolean answered() {
            return (
                  SSM.makeAttrib.selected != null && 
                  SSM.c_makeAttrib.selected != null && 
                  ! SSM.makeAttrib.selected.equals(SSM.c_makeAttrib.selected)
                  );
         }
         public void set() { SSM.reset(); }
         public String text() { return "Warm up task: Use the comparison functionality to compare two different vehicle Models"; }
      });
      
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Objective Tasks
      ////////////////////////////////////////////////////////////////////////////////
      q.add( new Question() {
         public boolean answered() {
            return true;
         }
         public void set() {
            SSM.selectedGroup.clear();
            SSM.reset();
            SSM.dirty = 1;
            SSM.dirtyLoad = 1;
         }
         public String text() { 
            return "Select the vehicle component with the highest rate of vehicle complaints";
         }
      });
      
      q.add(new Question() {
         public boolean answered() { 
            return true;
         }
         public void set() {
            SSM.selectedGroup.clear();
            SSM.dirty = 1; 
         }
         public String text() {
            return "Select the component with the highest rate of complaint in July.";
         }
         
      });
      
      /* 
      q.add(new Question() {
         public boolean answered() {
            return true;
         }
         public void set() {
            SSM.selectedGroup.clear(); 
            SSM.showLabels = false;
            SSM.dirty = 1;
         }
         public String text() {
            return "Use the lens widget to hover over the region of the vehicle that you believe to have the highest number of failures.";
         }
      });
      */
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Subjective tasks, this is only partially automated for the sake
      // of convenience - these will always be true regardless, the investigators
      // will need to record the answers
      ////////////////////////////////////////////////////////////////////////////////
      q.add(new Question() {
         public boolean answered() { return true; }
         public void set() {
            SSM.selectedGroup.clear();   
            SSM.showLabels = true;
            SSM.dirty = 1;
         }
         public String text() {
            return "Tell us what components are related to complaints about wheels";
         }
      });
      
      q.add(new Question(){
         public boolean answered() { return true; }
         public void set() {
            SSM.selectedGroup.clear();   
            SSM.dirty = 1;
            SSM.dirtyGL = 1;
         }
         public String text() {
            return "Which manufacturer had the most complaints in the summer months(May to August)? What are these complaints about?";
         }
      });
      
      q.add(new Question() {
         public boolean answered() { return true; }
         public void set() {
            SSM.selectedGroup.clear();   
            SSM.showLabels = true;
            SSM.dirty = 1;
            SSM.startYear = 1997;
            SSM.endYear = 1997;
            SSM.startMonth = 0;
            SSM.endMonth = 11;
         }
         public String text() {
            return "Using the lens and heatmap widgets, observe for any trends or patterns, tell us your findings.";
         }
      });
      
      q.add(new Question() {
         public boolean answered() { return true; }
         public void set() {
            SSM.selectedGroup.clear();   
            SSM.showLabels = true;
            SSM.dirty = 1;
         }
         public String text() {
            return "Given a list of similarly priced vehicles, which one would you purchase and why? Are you free to use all available widgets";
         }
      });
     
      
      
      ////////////////////////////////////////////////////////////////////////////////
      // Last question...answered should always return false
      ////////////////////////////////////////////////////////////////////////////////
      q.add(new Question() {
         public boolean answered() { return false; }
         public void set() {}
         public String text() { return "The End"; }
      });
   }

}
