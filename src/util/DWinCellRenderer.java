package util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;


/* List renderer for debug window, with customized logic to handle message types and selection logic */
public class DWinCellRenderer extends JLabel implements ListCellRenderer {
   
   /* Makes warning go away */
   private static final long serialVersionUID = 1L;

   public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean hasFocus) {
      this.setOpaque(true);
      
      String vals[] = value.toString().split("\\|");
      int msgType = Integer.valueOf(vals[0]);
      this.setText(vals[1]);
      
      
      // Change the font colour based on message type
      switch (msgType) {
         case DWin.DBG: this.setForeground(Color.BLACK); break;
         case DWin.ERR: this.setForeground(Color.RED); break;
         case DWin.MSG: this.setForeground(Color.BLUE); break;
         default: this.setForeground(Color.BLACK); break;
      }
      
      // Make the font bigger if the label is selected (clicked)
      if (isSelected) {
         this.setBackground(Color.YELLOW);   
         if ( vals.length == 3) {
            this.setFont(new Font(vals[2], Font.PLAIN,  18));
         } else {
            this.setFont(new Font("Courier", Font.PLAIN,  18));
         }
      } else {
         this.setBackground(Color.WHITE);   
         if ( vals.length == 3) {
            this.setFont(new Font(vals[2], Font.PLAIN,  12));
         } else {
            this.setFont(new Font("Courier", Font.PLAIN,  12));
         }
      }
      
      
      return this;
   }
}
