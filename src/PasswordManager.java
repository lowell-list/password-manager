/* --------------------------------------------------------------------------
 * Description : PasswordManager - Java Application to Manage Passwords
 * Author      : Lowell List
 * Date        : 27 Dec 2008
 * Origin OS   : Windows XP
 * --------------------------------------------------------------------------
 * Copyright (c)  2008 Lowell List
 * -------------------------------------------------------------------------- */

import java.awt.*;
import java.awt.event.*;

public class PasswordManager
extends java.lang.Object
{

/**************************************************************************/
/* INSTANCE PROPERTIES                                                    */
/**************************************************************************/

/**************************************************************************/
/* INSTANCE CONSTRUCTORS                                                  */
/**************************************************************************/

/**************************************************************************/
/* INSTANCE METHODS                                                       */
/**************************************************************************/

/**************************************************************************/
/* INNER CLASSES                                                          */
/**************************************************************************/

/**************************************************************************/
/* STATIC PROPERTIES                                                      */
/**************************************************************************/

/**************************************************************************/
/* STATIC INIT & MAIN                                                     */
/**************************************************************************/

public static void main(String[] args) {
    Frame          frm;                // main frame
    Insets         ins;                // temp object    
    SecretMessageApplet apt;           // the applet we are converting to an application
    int            wid=500,hgt=600;    // initial (and mimimum) width and height for this application
    
    // create the main frame and add the appropriate listener
    frm=new Frame();
    frm.addWindowListener(
        new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) { System.exit(0); };
            }
        );
        
    // init
    apt=new SecretMessageApplet();
    apt.setSize(wid,hgt);
    frm.add(apt);
    frm.pack();
    apt.init();
    apt.start();
    ins=frm.getInsets();
    frm.setSize(wid+ins.left+ins.right,hgt+ins.top+ins.bottom);
    frm.setMinimumSize(frm.getSize());
    frm.setTitle("Simple Password Manager");
    frm.setLocationByPlatform(true);
    frm.setVisible(true);
    }

/**************************************************************************/
/* STATIC METHODS                                                         */
/**************************************************************************/

} /* END PUBLIC CLASS */