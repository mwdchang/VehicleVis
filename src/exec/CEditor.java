package exec;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import model.DCColour;


/////////////////////////////////////////////////////////////////////////////////
// Captures variable attribute via the Java Reflection API. 
// Variables are stored locally and can be examined and modified at runtime
// through an user interface.
// The implication of this is that the program must follow certain paradigms 
// in order for the changes to place at runtime, that is, the colour function must
// reference back to the original variable field and not act on a copy/clone or
// pass in by value. For example :
//
// GL.glColor4fv( class.colour.to4fv() );
// GL.glSetUniform4f( class.colour.r, class.colour.g, class.colour.b, class.colour.a);
//
// UI Attributes
// JLabel clabel[]     - name of variable
// JLabel label[]      - should be used to show colour
// JTextField rBox[]   - red value place holder
// JTextField gBox[]   - green value place holder
// JTextField bBox[]   - blue value place holder
// JTextField aBox[]   - alpha value place holder
// JButton button[]    - confirm button
// JButton cbutton[]   - colour picker button
// JTextArea textArea  - text area
//
//
// initFields() - Specifies the variable capture functions
// setColour()  - Behaviour when colour change is confirmed
// setColourFromPicker() - Behaviour when colour is chosen from colour picker
// setDisplay() - How each item is to be displayed, store backup, if any
// resetItem()  - Restore backup, if any
// fillTextArea() - Update the textArea (IE: fill with source code )
//
//
//
//Limitations and bugs:
//1) Currently only working for static variables, I haven't got around or need non-static ones...
//2) The repaint seem to be problematic for the label elements...a work around is to resize the window
// a bit after the button is pressed.
/////////////////////////////////////////////////////////////////////////////////
public class CEditor extends JFrame {
   
   public static void main(String args[]) {
      CEditor c = new CEditor();
      c.setVisible(true);
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Create and display data
   ////////////////////////////////////////////////////////////////////////////////
   public JPanel createPanel() throws Exception {
      panel = new JPanel(new BorderLayout());
      topPanel = new JPanel();
      topPanel.setLayout(new GridBagLayout());
      bottomPanel = new JPanel();
      bottomPanel.setLayout(new GridLayout(0,1));
      
      JScrollPane topPane = new JScrollPane();
      
      int size = clist.size();
      
      clabel = new JLabel[ size ];
      label = new JLabel[ size ];
      rBox = new JTextField[ size ]; 
      gBox = new JTextField[ size ]; 
      bBox = new JTextField[ size ]; 
      aBox = new JTextField[ size ]; 
      button = new JButton[ size ];
      cbutton = new JButton[ size ];
      original = new Object[ size ]; 
      
      
      GridBagConstraints constraint = new GridBagConstraints();
      
      for (i=0; i < clist.size(); i++) {
         clabel[i] = new JLabel( clist.elementAt(i).getName().toString());   
         label[i] = new JLabel( "     " ); 
         rBox[i]  = new JTextField();
         gBox[i]  = new JTextField();
         bBox[i]  = new JTextField();
         aBox[i]  = new JTextField();
         cbutton[i] = new JButton("c");
         button[i] = new JButton("!");
         
//         cbutton[i].setSize(30, 30);
         clabel[i].setPreferredSize(new Dimension(20, 10));
         label[i].setPreferredSize(new Dimension(20, 10));
         cbutton[i].setPreferredSize(new Dimension(20,20));
         button[i].setPreferredSize(new Dimension(20, 20));
         rBox[i].setPreferredSize(new Dimension(30, 20));
         gBox[i].setPreferredSize(new Dimension(30, 20));
         bBox[i].setPreferredSize(new Dimension(30, 20));
         aBox[i].setPreferredSize(new Dimension(30, 20));
         
         
         setDisplay(i);
         map.put(button[i], i);
         map.put(cbutton[i], i);
         
         button[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               int idx = map.get(e.getSource());
               setColour(idx);
            }
         });
         
         cbutton[i].addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               int idx = map.get(e.getSource());
               setColourFromPicker(idx);
            }
         }); 
         
         constraint.fill = GridBagConstraints.HORIZONTAL;
         constraint.gridx = 0;
         constraint.gridy = i;
         topPanel.add(label[i], constraint);
         
         constraint.fill = GridBagConstraints.HORIZONTAL;
         constraint.gridx = 2;
         constraint.gridy = i;
         topPanel.add(clabel[i], constraint);
         
         constraint.fill = GridBagConstraints.HORIZONTAL;
         constraint.gridx = 3;
         constraint.gridy = i;
         constraint.weightx = 0.1;
         topPanel.add(rBox[i], constraint);
         
         constraint.fill = GridBagConstraints.HORIZONTAL;
         constraint.gridx = 4;
         constraint.gridy = i;
         constraint.weightx = 0.1;
         topPanel.add(gBox[i], constraint);
         
         constraint.fill = GridBagConstraints.HORIZONTAL;
         constraint.gridx = 5;
         constraint.gridy = i;
         constraint.weightx = 0.1;
         topPanel.add(bBox[i], constraint);
         
         constraint.fill = GridBagConstraints.HORIZONTAL;
         constraint.gridx = 6;
         constraint.gridy = i;
         constraint.weightx = 0.1;
         topPanel.add(aBox[i], constraint);
         
         constraint.fill = GridBagConstraints.HORIZONTAL;
         constraint.gridx = 7;
         constraint.gridy = i;
         topPanel.add(cbutton[i], constraint);
        
         constraint.fill = GridBagConstraints.HORIZONTAL;
         constraint.gridx = 8;
         constraint.gridy = i;
         topPanel.add(button[i], constraint);
      }

      textArea = new JTextArea();
      textArea.setRows(20);
      textArea.setColumns(40);
      JScrollPane scrollPane = new JScrollPane(textArea);
      bottomPanel.add(scrollPane, constraint);
      
      
      JButton reset = new JButton("Reset");
      reset.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            for (int x=0; x < clist.size(); x++) {
               resetItem(x);
            }
            fillTextArea();
            panel.repaint();
         }
      });
      bottomPanel.add(reset, constraint);
      bottomPanel.setPreferredSize(new Dimension(600, 200));
     
      
      JScrollPane topScroll = new JScrollPane(topPanel);
      topScroll.setPreferredSize(new Dimension(600, 300));
      panel.add(topScroll, BorderLayout.CENTER);
      panel.add(bottomPanel, BorderLayout.SOUTH);
      panel.setVisible(true);
      
      return panel; 
   }
   
   public void init() {
      try {
         initFields();
         JPanel j = createPanel();
         this.add(j);
      } catch (Exception e) {
         e.printStackTrace();
      }
      // Frame attribute
      this.setSize(600, 800);
      this.setTitle("Colour Picker");
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      this.pack();
   }
   
   
   private CEditor() {
      this.init();    
      this.setVisible(false); // default to not show
   }
   
   
   public static CEditor instance() {
      if (inst == null) inst = new CEditor();
      return inst;
   }
   
   
   
   /////////////////////////////////////
   //  Override / Replace the methods below
   /////////////////////////////////////
   public Color obj2Colour(Object obj) {
      DCColour c = (DCColour)obj;
      Color cl = new Color((int)(c.r*255), (int)(c.g*255), (int)(c.b*255), (int)(c.a*255));
      System.out.println(cl);
      return cl;
   }
   
   public Object colour2Obj(Color c) {
      return new DCColour(
         (float)c.getRed()/(float)255.0f,       
         (float)c.getGreen()/(float)255.0f,       
         (float)c.getBlue()/(float)255.0f,       
         (float)c.getAlpha()/(float)255.0f       
      );
   }
   
   public Object cloneObj(Object obj) {
      return new DCColour( (DCColour)obj );
   }
      
   
   
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Captures the variables to cache locally
   ////////////////////////////////////////////////////////////////////////////////
   public void initFields() throws Exception {
      Class c = Class.forName("datastore.SchemeManager");     
      all = c.getDeclaredFields(); 
      for (int i=0; i < all.length; i++) {
         System.out.println(all[i].getType());
         
         // Should probably use instance checker
         String txt = all[i].getType().toString();
         System.out.println(txt); 
         if (txt.equals("class model.DCColour")) {
            clist.add(all[i]);   
         }
      }
   }   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Maps the field instance to the UI elements
   ////////////////////////////////////////////////////////////////////////////////
   public void setDisplay(int i) {
      Object obj;
      try {
         obj = (Object)clist.elementAt(i).get(null);
         Color c = obj2Colour( obj );
         
         
         label[i].setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
         label[i].setFont(new Font("SansSerif", Font.PLAIN, 12));
         label[i].setOpaque(true);
   
         rBox[i].setText( c.getRed() + "" ); // Sigh 
         rBox[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
   
         gBox[i].setText( c.getGreen() + "" ); // Sigh 
         gBox[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
   
         bBox[i].setText( c.getBlue() + "" ); // Sigh 
         bBox[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
   
         aBox[i].setText( c.getAlpha() + "" ); // Sigh
         aBox[i].setFont(new Font("SansSerif", Font.PLAIN, 13));
   
         label[i].setBackground( c );
         original[i] = cloneObj( obj );
      } catch (Exception e) { e.printStackTrace(); }
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Select a colour from the colour picker
   ////////////////////////////////////////////////////////////////////////////////
   public void setColourFromPicker(int idx) {
      Color now = new Color( 
         Integer.parseInt(rBox[idx].getText()),      
         Integer.parseInt(gBox[idx].getText()),      
         Integer.parseInt(bBox[idx].getText()),      
         Integer.parseInt(aBox[idx].getText())      
      );
      
      Color c = JColorChooser.showDialog(null, "Does it Blend", now);
      if (c != null) {
         rBox[idx].setText( "" + (int)c.getRed());
         gBox[idx].setText( "" + (int)c.getGreen());
         bBox[idx].setText( "" + (int)c.getBlue());
         
         Color newC = new Color( 
            Integer.parseInt(rBox[idx].getText()),      
            Integer.parseInt(gBox[idx].getText()),      
            Integer.parseInt(bBox[idx].getText()),      
            Integer.parseInt(aBox[idx].getText())      
         );
         
         
         Object obj = colour2Obj( newC );
         
         label[idx].setBackground( newC );
         try {
            clist.elementAt(idx).set(null, obj);  
         } catch (Exception e) { e.printStackTrace(); }                  
         fillTextArea();
      }      
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Set a new value
   ////////////////////////////////////////////////////////////////////////////////
   public void setColour(int idx) {
      int nr = Integer.parseInt(rBox[idx].getText());   
      int ng = Integer.parseInt(gBox[idx].getText());   
      int nb = Integer.parseInt(bBox[idx].getText());   
      int na = Integer.parseInt(aBox[idx].getText());   
      
      Color c = new Color(nr, ng, nb, na);
      label[idx].setBackground( c );
      
      Object obj = colour2Obj( c );
      try {
         clist.elementAt(idx).set(null, obj);  
      } catch (Exception e) { e.printStackTrace(); }
      fillTextArea();      
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset back to original value
   ////////////////////////////////////////////////////////////////////////////////
   public void resetItem(int idx) {
      Object orig = (Object)original[idx];   
      try {
        clist.elementAt(idx).set(null, orig);  
      } catch (Exception ee) {}
      
      Color c = obj2Colour(orig);
      rBox[idx].setText(c.getRed() + "");
      gBox[idx].setText(c.getGreen() + "");
      bBox[idx].setText(c.getBlue() + "");
      aBox[idx].setText(c.getAlpha() + "");
      label[idx].setBackground( c );
   }
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Java code generation/replacement
   ////////////////////////////////////////////////////////////////////////////////
   public void fillTextArea() {
      textArea.setText("");
      for (int i=0; i < clist.size(); i++) {
         String varname = clist.elementAt(i).getName(); 
         textArea.append("DCColour " + varname);
         textArea.append(" = " );
         textArea.append(" new DCColour(");
         textArea.append(rBox[i].getText() + ", ");
         textArea.append(gBox[i].getText() + ", ");
         textArea.append(bBox[i].getText() + ", ");
         textArea.append(aBox[i].getText() );
         textArea.append(");");
         textArea.append("\n");
      }
   }
  
   
   
   JLabel clabel[];
   JLabel label[];
   JTextField rBox[];
   JTextField gBox[];
   JTextField bBox[];
   JTextField aBox[];
   JButton button[];   
   JButton cbutton[];   
   JTextArea textArea;
   JPanel panel;
   JPanel topPanel;
   JPanel bottomPanel;
   int i;
   
   
   public Vector<Field> clist = new Vector<Field>();
   public Field all[];
   public Object original[];
   public Hashtable<JButton, Integer> map = new Hashtable<JButton, Integer>();
   
   public int size;
   
   private static CEditor inst;
}
