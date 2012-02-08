package util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;


// Output to a List embedded inside a JFrame
// Mostly for debugging/informational purposes, can support different message types and font styles
// Note this has dependency on Const class....just make up new constants if moving to another project
//
// Possible TODO:
//   Hook System.out and System.err output streams
//   Filter, only warnings, warnings+errors, only errors, etc etc
public class DWin extends JFrame {
   
   /* Make the warnings to away */
   private static final long serialVersionUID = 1L;
   
   ////////////////////////////////////////////////////////////////////////////
   // For customized output
   // Bit masked in case filters are needed
   ////////////////////////////////////////////////////////////////////////////
   public static final int DBG = 0x00000001;
   public static final int ERR = 0x00000002;
   public static final int MSG = 0x00000004;
   
   
   public static int typeFilter = DBG|ERR|MSG;
   
   
   /*
   public static void main(String args[]) {
      for (int i=0; i < 500; i++) {
      DWin.instance().debug("abc-delicious");
      DWin.instance().msg("abc-delicious");
      DWin.instance().error("abc-delicious");
      }
   }
   */
   
   // Setup the window....once
   protected DWin() {
      super("Debug Window");   
      this.setBounds(100+1920, 100, 400, 700);      
      this.setSize(600, 700);
      this.setPreferredSize(new Dimension(600, 700));
      this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      
      
      //listModel = new DefaultListModel();
      
      //listBox = new JList(listModel);
      listBox = new DWinList();
      listModel = listBox.fm;
      listBox.setCellRenderer(new DWinCellRenderer());
      
      clearButton = new JButton("Clear");
      clearButton.addActionListener(
            new ActionListener() {
               public void actionPerformed(ActionEvent e) {
                  listModel.clear();
               }
            }
      );
      
      
      JPanel topPanel = new JPanel(); 
      topPanel.setLayout(new FlowLayout());
      
      JCheckBox check1 = new JCheckBox();
      check1.setText("Debug");
      check1.setSelected(true);
     
      JCheckBox check2 = new JCheckBox();
      check2.setText("Error");
      check2.setSelected(true);
      
      JCheckBox check3 = new JCheckBox();
      check3.setText("Msg");
      check3.setSelected(true);     
      
      JLabel filterLabel = new JLabel("Filter");
      topPanel.add(filterLabel, FlowLayout.LEFT);
      topPanel.add(listBox.ff);
      topPanel.add(check1);
      topPanel.add(check2);
      topPanel.add(check3);
      
      
      scrollPane = new JScrollPane(listBox);
      this.setLayout(new BorderLayout());
      //this.add(listBox.ff, BorderLayout.NORTH);
      this.add(topPanel, BorderLayout.NORTH);

      this.add(scrollPane, BorderLayout.CENTER);
      this.add(clearButton, BorderLayout.SOUTH);
      this.pack();
      this.setVisible(true);
   }
   
   
   public void setFilter(int maskValue) {
      this.typeFilter = maskValue;    
   }
   
   
   public void doPrint(final int level, final String s, final String fn) {
      //if (1 == 1) return;
      int mask = typeFilter & level;
      if (mask != level) return;
      
      SwingUtilities.invokeLater( new Runnable() {
         public void run() {
            if (fn == null) {
               listModel.addElement(level + "|" + s);
            } else {
               listModel.addElement(level + "|" + s + "|" + fn);
            }
            
            //if (listModel.size() > bufferSize && bufferSize > 0) {
            //   listModel.remove(0);   
            //}            
            //listBox.ensureIndexIsVisible(listModel.size()-1);
            listBox.ensureIndexIsVisible(listBox.fm.item.size()-1);
            listBox.fm.doFilter();
         }
      });
      /*
      listModel.addElement(level + "|" + s);
      if (listModel.size() > bufferSize && bufferSize > 0) {
         listModel.remove(0);   
      }
      
      listBox.ensureIndexIsVisible(listModel.size()-1);
      */
   }
      
   public void debug(String s) {
      doPrint(DBG, s, null);                                    
   }
   public void error(String s) {
     doPrint(ERR, s, null); 
   }
   public void msg(String s) {
      doPrint(MSG, s, null);
   }
   
   
   
   public static DWin instance() {
      if (instance == null) instance = new DWin();
      return instance;
   }
   
   private static DWin instance = null;
   private DWinList listBox = null;   
   private JButton clearButton = null;
   private JScrollPane scrollPane = null;
   private DefaultListModel listModel;
   
   // Number of lines to keep
   private int bufferSize = 500;     
}
