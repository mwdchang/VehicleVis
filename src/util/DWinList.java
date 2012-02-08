package util;

import java.util.ArrayList;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

// Daniel's Ubber JList
// Idea taken from 
//    http://codeidol.com/java/swing/Lists-and-Combos/Filter-JLists
public class DWinList extends JList {
    
   public FilterField ff = null;
   public FilterModel fm = null;
   
   // Constructor
   public DWinList() {
       super();    
       fm = new FilterModel();
       ff = new FilterField(20);
       this.setModel(fm);
   }
   

   
   ////////////////////////////////////////////////////////////////////////////
   // ListModel inner class
   ////////////////////////////////////////////////////////////////////////////
   class FilterModel extends DefaultListModel{
      public ArrayList<String> item = null;
      public ArrayList<String> filterItem = null;
      public int filter = DWin.DBG | DWin.MSG | DWin.ERR;
      
      // Constructor
      public FilterModel() {
         super();
         item = new ArrayList<String>();
         filterItem = new ArrayList<String>();
      }
      
      //@override getElementAt()
      public Object getElementAt(int index) {
         return index > filterItem.size() ? null : filterItem.get(index);   
      }
      
      //@override geSize()
      public int getSize() {
         return filterItem.size();
      }
      
      //@override addElement()
      public void addElement(Object o) {
         item.add( o.toString() );   
         doFilter();
      }
      
      //@override clear()
      public void clear() {
         item.clear();
         doFilter();
      }
      
      // @override remove()
      public Object remove(int index) {
         Object o = item.remove(index);
         doFilter();
         return o;
      }
      
      // Filtering 
      public void doFilter() {
         filterItem.clear();
         String txt = ff.getText();
         for (int i=0; i < item.size(); i++) {
            if (txt.equals("") || item.get(i).toString().indexOf(txt,0) > 0)
            	filterItem.add( item.get(i));         
         }
         this.fireContentsChanged(this, 0, getSize());
      }
   }
   
   
   ////////////////////////////////////////////////////////////////////////////
   // TextField inner class 
   ////////////////////////////////////////////////////////////////////////////
   class FilterField extends JTextField implements DocumentListener {
      private static final long serialVersionUID = 1L;
      
      public FilterField(int width) {
         super(width);
         getDocument().addDocumentListener(this);
      }
      public void removeUpdate(DocumentEvent e) {
         ((FilterModel)getModel()).doFilter();
      }
      public void insertUpdate(DocumentEvent e) {
         ((FilterModel)getModel()).doFilter();
      }
      public void changedUpdate(DocumentEvent e) {
         ((FilterModel)getModel()).doFilter();
      }
   }
   
   
}
