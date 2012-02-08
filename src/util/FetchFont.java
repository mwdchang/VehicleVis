package util;

import java.awt.GraphicsEnvironment;



/////////////////////////////////////////////////////////////////////////////////
// Check the available fonts on the system
/////////////////////////////////////////////////////////////////////////////////
public class FetchFont {
   public static void main(String args[]) {
     GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
     String s[] = g.getAvailableFontFamilyNames();
     for (int i=0; i < s.length; i++) {
        if (i % 2 == 0) {
           DWin.instance().doPrint(DWin.DBG, s[i], s[i]);
        } else {
           DWin.instance().doPrint(DWin.MSG, s[i], s[i]);
        }
     }
   }
}
