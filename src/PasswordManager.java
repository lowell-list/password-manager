/* -----------------------------------------------------------------------------
 * Description : PasswordManager - Java Application to Manage Passwords
 * Author      : Lowell List
 * Date        : 27 Dec 2008
 * Origin OS   : Windows XP
 * -----------------------------------------------------------------------------
 * Copyright (c) 2008, 2025 Lowell List
 * -------------------------------------------------------------------------- */

import java.awt.*;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.jar.Manifest;
import java.util.jar.Attributes;

public class PasswordManager
    extends java.lang.Object {

  /**
   * Instance Properties
   * ---------------------------------------------------------------------------
   */

  /**
   * Instance Constructors
   * ---------------------------------------------------------------------------
   */

  /**
   * Instance Methods
   * ---------------------------------------------------------------------------
   */

  /**
   * Inner Classes
   * ---------------------------------------------------------------------------
   */

  /**
   * Static Properties
   * ---------------------------------------------------------------------------
   */

  /**
   * Static Init & Main
   * ---------------------------------------------------------------------------
   */

  public static void main(String[] args) {
    Frame frm; // main frame
    Insets ins; // temp object
    PasswordManagerApplet apt; // the applet we are converting to an application
    int wid = 600, hgt = 600; // initial (and minimum) width and height for this application

    // get properties from the manifest
    String appVersion = getManifestValue("Version-Number", "??");
    String gitCommitHash = getManifestValue("Git-Commit-Hash", "??");
    System.out.println("Password Manager - " + appVersion + "+" + gitCommitHash);

    // create the main frame
    frm = new Frame();

    // init
    apt = new PasswordManagerApplet();
    apt.setSize(wid, hgt);
    frm.add(apt);
    frm.pack();
    apt.init();
    apt.start();
    ins = frm.getInsets();
    frm.setSize(wid + ins.left + ins.right, hgt + ins.top + ins.bottom);
    frm.setMinimumSize(frm.getSize());
    frm.setTitle("🔑 Password Manager - " + appVersion + " 🔑");
    frm.setLocationByPlatform(true);
    frm.setVisible(true);

    // add frame listeners
    frm.addWindowListener(
        new java.awt.event.WindowAdapter() {
          public void windowClosing(java.awt.event.WindowEvent e) {
            apt.onExitRequested();
          };
        });
  }

  /**
   * Static Methods
   * ---------------------------------------------------------------------------
   */

  /**
   * Read a value from the manifest.
   * 
   * @param name         The name of the value to read.
   * @param defaultValue The default value to return if the value is not found.
   * @return The value read from the manifest, or the default value if not found.
   */
  private static String getManifestValue(String name, String defaultValue) {
    String className = PasswordManager.class.getSimpleName() + ".class";
    String classPath = PasswordManager.class.getResource(className).toString();
    if (!classPath.startsWith("jar")) {
      return defaultValue;
    }
    try {
      URL url = (new URI(classPath)).toURL();
      JarURLConnection jarConnection = (JarURLConnection) url.openConnection();
      Manifest manifest = jarConnection.getManifest();
      Attributes attributes = manifest.getMainAttributes();
      return attributes.getValue(name);
    } catch (Exception e) {
      System.out.println(e);
      return defaultValue;
    }
  }

} // End Public Class