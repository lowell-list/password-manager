/* -----------------------------------------------------------------------------
 * Description : PasswordManagerApplet - Applet to Encrypt/Decrypt Simple Text
 * Author      : Lowell List
 * Date        : 29 Jun 2002
 * Origin OS   : Windows 2000
 * -----------------------------------------------------------------------------
 * Copyright (c) 2002, 2023 Lowell List
 * -------------------------------------------------------------------------- */

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class PasswordManagerApplet
    extends java.applet.Applet {

  /**
   * Instance Properties
   * ---------------------------------------------------------------------------
   */

  private PasswordsView mPasswordsView;
  private Label mPasswordLabel;
  private TextField mPasswordTextField;
  private Button mEncryptButton;
  private Button mDecryptButton;
  private Button mSaveAndCloseButton;
  private Label mStatusLabel;

  private boolean mInitialized = false;
  private Properties mProperties = null;
  private boolean mIsModified = false;
  private boolean mIsDecrypted = false;
  private int mOriginalContentHash = -1; // the hash code of the original content

  /**
   * Instance Constructors
   * ---------------------------------------------------------------------------
   */

  // construct the applet
  public PasswordManagerApplet() {
  }

  /**
   * Instance Methods - Applet
   * ---------------------------------------------------------------------------
   */

  // initialize the applet
  public void init() {
    String tmptxt;

    // applet setup
    setBackground(Color.white);
    this.setLayout(null);

    // instantiate components
    mPasswordsView = new PasswordsView();
    mPasswordLabel = new Label();
    mPasswordTextField = new TextField();
    mEncryptButton = new Button();
    mDecryptButton = new Button();
    mSaveAndCloseButton = new Button();
    mStatusLabel = new Label();

    // setup components
    mPasswordsView.init();
    mPasswordLabel.setText("Password");
    mPasswordLabel.setAlignment(Label.RIGHT);
    mPasswordTextField.setEchoChar('*');
    mEncryptButton.setLabel("Encrypt");
    mDecryptButton.setLabel("Decrypt");
    mSaveAndCloseButton.setLabel("Save and Close");

    // add components
    this.add(mPasswordsView);
    this.add(mPasswordLabel);
    this.add(mPasswordTextField);
    this.add(mEncryptButton);
    this.add(mDecryptButton);
    this.add(mSaveAndCloseButton);
    this.add(mStatusLabel);

    // add listeners
    this.addComponentListener(new ComponentListener() {
      public void componentResized(ComponentEvent evt) {
        onMainComponentResized(evt);
      }

      public void componentMoved(ComponentEvent evt) {
      }

      public void componentShown(ComponentEvent evt) {
      }

      public void componentHidden(ComponentEvent evt) {
      }
    });
    mPasswordTextField.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent evt) {
      }

      public void keyPressed(KeyEvent evt) {
      }

      public void keyReleased(KeyEvent evt) {
        onPasswordTextKeyReleased(evt);
      }
    });
    mPasswordsView.addModifiedObserver(new ModifiedObserver() {
      public void onModified(int hashCode) {
        mIsModified = hashCode != mOriginalContentHash;
      }
    });
    mEncryptButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        onEncryptButtonAction(evt);
      }
    });
    mDecryptButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        onDecryptButtonAction(evt);
      }
    });
    mSaveAndCloseButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        saveAndClose();
      }
    });

    // load the properties file
    loadProperties();

    // load password file
    tmptxt = loadPasswordFileContents();
    setMainText(tmptxt, false, false);

    // set focus to the password input box
    mPasswordTextField.requestFocus();

    // finish
    mInitialized = true;
  }

  // initialize the applet
  public void start() {
    this.onMainComponentResized(null);
  }

  /**
   * Instance Methods - Main Component
   * ---------------------------------------------------------------------------
   */

  public void onMainComponentResized(ComponentEvent evt) {
    int wth; // applet width
    int hgt; // applet height
    int btnwth; // button width
    int btnhgt; // button height

    if (!mInitialized) {
      return;
    }
    wth = Math.max(this.getSize().width, 400);
    hgt = Math.max(this.getSize().height, 300);
    btnwth = PasswordsView.BUTTON_WIDTH;
    btnhgt = PasswordsView.BUTTON_HEIGHT;

    // TODO: use INNER_PAD constant from PasswordsView here
    mPasswordsView.setBounds(5, 5, wth - 10, hgt - 60);
    mEncryptButton.setBounds(wth - ((btnwth * 2) + 10), hgt - ((btnhgt * 2) + 10), btnwth, btnhgt);
    mDecryptButton.setBounds(wth - ((btnwth) + 5), hgt - ((btnhgt * 2) + 10), btnwth, btnhgt);
    mStatusLabel.setBounds(5, hgt - 25, wth - 160, 20);
    mSaveAndCloseButton.setBounds(wth - ((btnwth * 2) + 10), hgt - btnhgt - 5, ((btnwth * 2) + 5), btnhgt);
    mPasswordLabel.setBounds(5, hgt - 50, Math.max(mPasswordLabel.getPreferredSize().width, 70), 20);
    mPasswordTextField.setBounds(5 + mPasswordLabel.getSize().width + 10, hgt - 50, 100, 20);
  }

  /**
   * Respond to user pressing the window close button.
   */
  public void onExitRequested() {
    // just exit if nothing has changed
    if (!mIsModified) {
      System.exit(0);
    }

    // prompt for save and close
    if (mIsDecrypted) {
      // decrypted: prompt for encrypt, save and close
      int rsp = JOptionPane.showConfirmDialog(
          this.getParent(),
          "Encrypt and save changes before closing?",
          "Warning",
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.WARNING_MESSAGE);
      if (rsp == JOptionPane.YES_OPTION) {
        if (onEncryptButtonAction(null)) {
          saveAndClose();
        }
      } else if (rsp == JOptionPane.CANCEL_OPTION) {
        return; // cancel: do not exit
      }
    } else {
      // encrypted: prompt for save and close
      int rsp = JOptionPane.showConfirmDialog(
          this.getParent(),
          "Save changes before closing?",
          "Warning",
          JOptionPane.YES_NO_CANCEL_OPTION,
          JOptionPane.WARNING_MESSAGE);
      if (rsp == JOptionPane.YES_OPTION) {
        saveAndClose();
      } else if (rsp == JOptionPane.CANCEL_OPTION) {
        return; // cancel: do not exit
      }
    }

    // exit!
    System.exit(0);
  }

  /**
   * Instance Methods - General Utility
   * ---------------------------------------------------------------------------
   */

  /**
   * Set the main area text and related tracking variables.
   * 
   * @param txt         The text to set.
   * @param isDecrypted True if the text is already decrypted.
   * @param isModified  True if the text should be considered as modified.
   */
  private void setMainText(String txt, boolean isDecrypted, boolean isModified) {
    mPasswordsView.setText((txt != null) ? txt : "");
    if (!isModified) {
      mOriginalContentHash = mPasswordsView.getText().hashCode();
    }
    mIsDecrypted = isDecrypted;
    mIsModified = isModified;
  }

  private void setStatusText(String txt) {
    mStatusLabel.setText(txt);
  }

  private void enableControls(boolean flg) {
    mPasswordsView.setEnabled(flg);
    mPasswordTextField.setEnabled(flg);
    mEncryptButton.setEnabled(flg);
    mDecryptButton.setEnabled(flg);
    mSaveAndCloseButton.setEnabled(flg);
  }

  private String formatThrowable(Throwable thr) {
    return thr.getClass().getName() + " : " + thr.getMessage();
  }

  /**
   * Instance Methods - File IO
   * ---------------------------------------------------------------------------
   */

  private void loadProperties() {
    Properties defprp; // default properties

    defprp = new Properties();
    defprp.setProperty(PRPNAM_PWDFILPTH, "Passwords.txt");
    mProperties = new Properties(defprp);

    try {
      BufferedReader br;

      // load the existing properties file
      br = new BufferedReader(new FileReader(PROPERTIES_FILENAME));
      mProperties.load(br);
      br.close();
      setStatusText("Loaded the properties file.");
    } catch (FileNotFoundException exp) {
      // the properties file was not found, so create a new one with default values
      try {
        File f;
        FileWriter fw;

        f = new File(PROPERTIES_FILENAME);
        f.createNewFile();
        fw = new FileWriter(f);
        defprp.store(fw, "Password Manager Property File");
        fw.close();
        setStatusText("Default properties file created.");
      } catch (Exception exp2) {
        setStatusText(formatThrowable(exp2));
      }
    } catch (Exception exp) {
      setStatusText(formatThrowable(exp));
      mProperties = new Properties(defprp); // use default properties if there was an error reading the file
    }
  }

  // returns the contents of the password file or null if it could not be opened.
  private String loadPasswordFileContents() {
    String pwdfilpth; // password file path
    String pwdfiltxt = null; // contents of the password file

    // get the password file path
    pwdfilpth = mProperties.getProperty(PRPNAM_PWDFILPTH);

    // open the passwords file and read its entire contents
    try {
      BufferedReader br;
      char[] buf;
      int len;

      pwdfiltxt = new String("");
      buf = new char[2000];
      br = new BufferedReader(new FileReader(pwdfilpth));
      while ((len = br.read(buf)) > 0) {
        pwdfiltxt += String.valueOf(buf, 0, len);
      }
      br.close();
    } catch (FileNotFoundException exp) {
      setStatusText("Password file not found.");
      return null;
    } catch (Exception exp) {
      setStatusText(formatThrowable(exp));
      return null;
    }

    // return the password file contents
    setStatusText("Loaded " + pwdfilpth);
    return pwdfiltxt;
  }

  /**
   * Instance Methods - Encryption / Decryption
   * ---------------------------------------------------------------------------
   */

  private void onPasswordTextKeyReleased(KeyEvent evt) {
    if (evt.getKeyCode() == KeyEvent.VK_ENTER && mPasswordTextField.getText().length() > 0) {
      // trigger decrypt
      onDecryptButtonAction(null);
    }
  }

  private void onDecryptButtonAction(ActionEvent evt) {
    String wndtxt; // window text
    String pwdtxt; // password text
    String dcrstr; // decrypted String

    // get window and password text
    enableControls(false);
    wndtxt = mPasswordsView.getText();
    if (wndtxt.length() == 0) {
      setStatusText("Text Area cannot be blank.");
      enableControls(true);
      return;
    }
    pwdtxt = mPasswordTextField.getText();
    if (pwdtxt.length() == 0) {
      setStatusText("Password cannot be blank.");
      enableControls(true);
      return;
    }

    // decrypt
    setStatusText("Decrypting...");
    try {
      dcrstr = decrypt(pwdtxt, wndtxt);
    } catch (Exception exp) {
      setStatusText("Could not decrypt.");
      // exp.printStackTrace();
      enableControls(true);
      return;
    }

    // done
    setMainText(dcrstr, true, false);
    enableControls(true);
    mPasswordsView.reset();
    setStatusText("Decrypted (" + dcrstr.length() + ") characters.");
  }

  /**
   * Handle the Encrypt button action.
   * 
   * @return true if the action was successful, false otherwise.
   */
  private boolean onEncryptButtonAction(ActionEvent evt) {
    String wndtxt; // window text
    String pwdtxt; // password text
    String cphtxt; // cipher text

    // get window and password text
    enableControls(false);
    wndtxt = mPasswordsView.getText();
    if (wndtxt.length() == 0) {
      setStatusText("Text Area cannot be blank.");
      enableControls(true);
      return false;
    }
    pwdtxt = mPasswordTextField.getText();
    if (pwdtxt.length() == 0) {
      setStatusText("Password cannot be blank.");
      enableControls(true);
      return false;
    }

    // encrypt
    setStatusText("Encrypting...");
    try {
      cphtxt = encrypt(pwdtxt, wndtxt);
    } catch (Exception exp) {
      setStatusText("Could not encrypt.");
      // exp.printStackTrace();
      enableControls(true);
      return false;
    }

    // done
    setMainText(cphtxt, false, true);
    enableControls(true);
    mPasswordsView.reset();
    setStatusText("Encrypted (" + wndtxt.length() + ") characters.");
    return true;
  }

  /**
   * Handle the Save and Close button action.
   */
  private void saveAndClose() {
    File filpwd;
    File filbak;
    FileWriter fw;

    // backup existing passwords file
    try {
      // get the filenames
      filbak = new File("PasswordsBackup.txt");
      filpwd = new File(mProperties.getProperty(PRPNAM_PWDFILPTH));

      // if not encrypted, confirm save and close
      if (mIsDecrypted) {
        int rsp = JOptionPane.showConfirmDialog(
            this.getParent(),
            "Not encrypted. Continue with save?",
            "Warning",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        if (rsp != JOptionPane.YES_OPTION) {
          return;
        }
      }

      // if the password file already exists, confirm overwrite
      // if (filpwd.exists()) {
      // int rsp = JOptionPane.showConfirmDialog(
      // this.getParent(),
      // "Continue with save? Existing file will be overwritten.",
      // "Warning",
      // JOptionPane.YES_NO_OPTION,
      // JOptionPane.WARNING_MESSAGE);
      // if (rsp != JOptionPane.YES_OPTION) {
      // return;
      // }
      // }

      // delete and rename
      if (filbak.exists()) {
        filbak.delete();
      }
      filpwd.renameTo(filbak);

      // write the new file contents
      fw = new FileWriter(filpwd);
      fw.write(mPasswordsView.getText());
      fw.close();
    } catch (Exception exp) {
      JOptionPane.showMessageDialog(
          this.getParent(),
          formatThrowable(exp),
          "Error during file backup",
          JOptionPane.ERROR_MESSAGE);
    }

    // exit!
    System.exit(0);
  }

  /**
   * Given a password and plain text, returns encrypted text (RC4/AES-256 combo).
   */
  private String encrypt(String password, String plainText) throws Exception {
    byte[] pwdbyt; // password bytes
    byte[] txtbyt; // plain text bytes
    byte[] encbyt; // encrypted bytes
    RC4Cipher rc4cph; // RC4 cipher

    // convert password and plain text to bytes
    pwdbyt = password.getBytes(ENCODING);
    txtbyt = plainText.getBytes(ENCODING);

    // 1) encrypt plain text with RC4 (no length change)
    rc4cph = new RC4Cipher(pwdbyt);
    rc4cph.encrypt(txtbyt, txtbyt);

    // 2) encrypt with AES-256 (length change)
    encbyt = AES256Cipher.encrypt(password.toCharArray(), txtbyt);

    // 3) obfuscate AES-256 properties with RC4 and hardcoded password
    rc4cph = new RC4Cipher(OBFUSCATE_PASSWORD.getBytes(ENCODING));
    rc4cph.encrypt(encbyt, encbyt);

    // convert encrypted bytes to base 64 String
    return Base64.getEncoder().encodeToString(encbyt);
  }

  /**
   * Given a password and cipher text (RC4/AES-256 combo), returns plain text.
   */
  private String decrypt(String password, String cipherText) throws Exception {
    byte[] pwdbyt; // password bytes
    byte[] cphbyt; // cipher text bytes
    byte[] dcrbyt; // decrypted bytes
    RC4Cipher rc4cph; // RC4 cipher

    // convert password and cipher text to bytes
    pwdbyt = password.getBytes(ENCODING);
    cphbyt = Base64.getDecoder().decode(cipherText);

    // 1) de-obfuscate AES-256 properties with RC4 and hardcoded password
    rc4cph = new RC4Cipher(OBFUSCATE_PASSWORD.getBytes(ENCODING));
    rc4cph.decrypt(cphbyt, cphbyt);

    // 2) AES-256 decrypt
    dcrbyt = AES256Cipher.decrypt(password.toCharArray(), cphbyt);

    // 3) RC4 decrypt
    rc4cph = new RC4Cipher(pwdbyt);
    rc4cph.decrypt(dcrbyt, dcrbyt);

    // return
    return new String(dcrbyt, ENCODING);
  }

  /**
   * Inner Classes
   * ---------------------------------------------------------------------------
   */

  /**
   * Static Properties
   * ---------------------------------------------------------------------------
   */

  private static final String PROPERTIES_FILENAME = "PasswordManager.cfg";
  private static final String PRPNAM_PWDFILPTH = "pwdfilpth"; // property: password file path
  private static final String ENCODING = "UTF8";
  private static final String OBFUSCATE_PASSWORD = "obfuscate";

  /**
   * Static Init & Main
   * ---------------------------------------------------------------------------
   */

  /**
   * Static Methods
   * ---------------------------------------------------------------------------
   */

} // End Public Class