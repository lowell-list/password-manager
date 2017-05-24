/* --------------------------------------------------------------------------
 * Description : SecretMessageApplet - Applet to Encrypt/Decrypt Simple Text
 * Author      : Lowell List
 * Date        : 29 Jun 2002
 * Origin OS   : Windows 2000
 * --------------------------------------------------------------------------
 * Copyright (c)  2002 Lowell List
 * -------------------------------------------------------------------------- */

import java.awt.*;
import java.awt.event.*;
import java.applet.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class SecretMessageApplet
extends java.applet.Applet
{

/**************************************************************************/
/* INSTANCE PROPERTIES                                                    */
/**************************************************************************/

private TextField            txtfldSearch;
private TextArea             txtaraMain;
private Label                lblPassword;
private TextField            txtfldPassword;
private Button               btnEncrypt;
private Button               btnDecrypt;
private Button               btnSaveAndClose;
private Label                lblStatus;

private boolean              initialized=false;
private Properties           appProperties=null;

/**************************************************************************/
/* INSTANCE CONSTRUCTORS                                                  */
/**************************************************************************/

// construct the applet
public SecretMessageApplet() {
    }

/**************************************************************************/
/* INSTANCE METHODS - APPLET                                              */
/**************************************************************************/

// initialize the applet
public void init() {
    String         tmptxt;
    
    // applet setup
    setBackground(Color.white);
    this.setLayout(null);

    // instantiate components
    txtfldSearch=new TextField("");
    txtaraMain=new TextArea("",0,0,TextArea.SCROLLBARS_VERTICAL_ONLY);
    lblPassword=new Label();
    txtfldPassword=new TextField();
    btnEncrypt=new Button();
    btnDecrypt=new Button();
    btnSaveAndClose=new Button();
    lblStatus=new Label();

    // setup components
    lblPassword.setText("Password");
    lblPassword.setAlignment(Label.RIGHT);
    txtfldPassword.setEchoChar('*');
    btnEncrypt.setLabel("Encrypt");
    btnDecrypt.setLabel("Decrypt");
    btnSaveAndClose.setLabel("Save and Close");

    // add components
    this.add(txtfldSearch);
    this.add(txtaraMain);
    this.add(lblPassword);
    this.add(txtfldPassword);
    this.add(btnEncrypt);
    this.add(btnDecrypt);
    this.add(btnSaveAndClose);
    this.add(lblStatus);

    // add listeners
    this.addComponentListener(new ComponentListener() {
        public void componentResized(ComponentEvent evt) { onMainComponentResized(evt); }
        public void componentMoved(ComponentEvent evt) { }
        public void componentShown(ComponentEvent evt) { }
        public void componentHidden(ComponentEvent evt) { }
    });
    txtfldSearch.addKeyListener(new KeyListener() {
        public void keyTyped(KeyEvent evt) {}
        public void keyPressed(KeyEvent evt) {}
        public void keyReleased(KeyEvent evt) { onSearchTextKeyEvent(evt); }
    });
    btnEncrypt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) { onEncryptButtonAction(evt); }
    });
    btnDecrypt.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) { onDecryptButtonAction(evt); }
    });
    btnSaveAndClose.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) { onSaveButtonAction(evt); }
    });

    // load the properties file
    loadProperties();

    // load password file
    tmptxt=loadPasswordFileContents();
    if(tmptxt!=null) { txtaraMain.setText(tmptxt); }
    
    // set focus to the password input box
    txtfldPassword.requestFocus();

    // finish
    initialized=true;
    }

// initialize the applet
public void start() {
    this.onMainComponentResized(null);
    }

/**************************************************************************/
/* INSTANCE METHODS - MAIN COMPONENT                                      */
/**************************************************************************/

public void onMainComponentResized(ComponentEvent evt) {
    int            wth;                // applet width
    int            hgt;                // applet height

    if(!initialized) { return; }
    wth=Math.max(this.getSize().width,400);
    hgt=Math.max(this.getSize().height,300);

    txtfldSearch.setBounds(5,5,wth-10,20);
    txtaraMain.setBounds(5,5+txtfldSearch.getBounds().height+5,wth-10,hgt-60-txtfldSearch.getBounds().height-5);
    btnEncrypt.setBounds(wth-150,hgt-50,70,20);
    btnDecrypt.setBounds(wth-75,hgt-50,70,20);
    lblStatus.setBounds(5,hgt-25,wth-160,20);
    btnSaveAndClose.setBounds(wth-150,hgt-25,145,20);
    lblPassword.setBounds(5,hgt-50,Math.max(lblPassword.getPreferredSize().width,70),20);
    txtfldPassword.setBounds(5+lblPassword.getSize().width+10,hgt-50,100,20);
    }

/**************************************************************************/
/* INSTANCE METHODS - GENERAL UTILITY                                     */
/**************************************************************************/

private void setStatusText(String txt) {
    lblStatus.setText(txt);
    }

private void enableControls(boolean flg) {
    txtaraMain.setEnabled(flg);
    txtfldPassword.setEnabled(flg);
    btnEncrypt.setEnabled(flg);
    btnDecrypt.setEnabled(flg);
    }

private String formatThrowable(Throwable thr) {
    return thr.getClass().getName()+" : "+thr.getMessage();
    }
    
/**************************************************************************/
/* INSTANCE METHODS - FILE IO                                             */
/**************************************************************************/

private void loadProperties() {
    Properties     defprp;             // default properties

    defprp=new Properties();
    defprp.setProperty(PRPNAM_PWDFILPTH,"Passwords.txt");
    appProperties=new Properties(defprp);
    
    try {
        BufferedReader br;
        String         tmpstr;
        
        // load the existing properties file
        br=new BufferedReader(new FileReader(propertiesFilename));
        appProperties.load(br);
        br.close();
        setStatusText("Loaded the properties file."); 
        }
    catch(FileNotFoundException exp) {
        // the properties file was not found, so create a new one with default values
        try { 
            File            f;
            FileWriter      fw;
            
            f=new File(propertiesFilename);
            f.createNewFile();
            fw=new FileWriter(f);
            defprp.store(fw,"Password Manager Property File");
            fw.close();
            setStatusText("Default properties file created."); 
            }
        catch(Exception exp2) { 
            setStatusText(formatThrowable(exp2)); 
            }
        }    
    catch(Exception exp) {
        setStatusText(formatThrowable(exp));
        appProperties=new Properties(defprp); // use default properties if there was an error reading the file
        }
    }

// returns the contents of the password file or null if it could not be opened.
private String loadPasswordFileContents() {
    String         pwdfilpth;          // password file path
    String         pwdfiltxt=null;     // contents of the password file
    
    // get the password file path
    pwdfilpth=appProperties.getProperty(PRPNAM_PWDFILPTH);
    
    // open the passwords file and read its entire contents
    try {
        BufferedReader br;
        char[]     buf;
        int        len;
        
        pwdfiltxt=new String("");
        buf=new char[2000];
        br=new BufferedReader(new FileReader(pwdfilpth));
        while((len=br.read(buf))>0) { pwdfiltxt+=String.valueOf(buf,0,len); }
        br.close();
        }
    catch(FileNotFoundException exp) {
        setStatusText("Password file not found.");
        return null;
        }
    catch(Exception exp) {
        setStatusText(formatThrowable(exp));
        return null;
        }

    // return the password file contents    
    setStatusText("Loaded "+pwdfilpth);
    return pwdfiltxt;
    }

/**************************************************************************/
/* INSTANCE METHODS - ENCRYPTION/DECRYPTION                               */
/**************************************************************************/

private void onSearchTextKeyEvent(KeyEvent evt) {
    System.out.println(evt);
    }

private void onDecryptButtonAction(ActionEvent evt) {
    String         wndtxt;             // window text
    String         pwdtxt;             // password text
    byte[]         pwdbyt;             // password bytes
    byte[]         bytbuf;             // byte buffer
    SecretMessageCipher cph;           // RC4 cipher
    String         decstr;             // decrypted String

    // get window and password text
    enableControls(false);
    wndtxt=txtaraMain.getText();
    if(wndtxt.length()==0) { setStatusText("Text Area cannot be blank."); enableControls(true); return; }
    pwdtxt=txtfldPassword.getText();
    if(pwdtxt.length()==0) { setStatusText("Password cannot be blank."); enableControls(true); return; }

    // convert password to bytes
    setStatusText("Converting password to bytes...");
    try {
        pwdbyt=pwdtxt.getBytes(encoding);
        }
    catch(UnsupportedEncodingException exp) {
        setStatusText(formatThrowable(exp));
        enableControls(true);
        return;
        }

    // convert hex characters to byte array
    if((bytbuf=getBytesFromHexString(wndtxt))==null) {
        // note: error message already set
        enableControls(true);
        return;
        }

    // decrypt the bytes using the RC4 cipher
    setStatusText("Decrypting ("+bytbuf.length+") bytes...");
    cph=new SecretMessageCipher(pwdbyt);
    cph.decrypt(bytbuf,bytbuf);

    // create the final string from the decrypted bytes
    setStatusText("Converting decrypted bytes to a string...");
    try {
        txtaraMain.setText(new String(bytbuf,encoding));
        }
    catch(UnsupportedEncodingException exp) {
        setStatusText(formatThrowable(exp));
        enableControls(true);
        return;
        }

    // done
    enableControls(true);
    txtaraMain.setCaretPosition(0);
    txtaraMain.requestFocus();
    setStatusText("Decrypted ("+bytbuf.length+") bytes.");
    }

private void onEncryptButtonAction(ActionEvent evt) {
    String         wndtxt;             // window text
    String         pwdtxt;             // password text
    byte[]         wndbyt;             // window bytes
    byte[]         pwdbyt;             // password bytes
    SecretMessageCipher cph;           // RC4 cipher
    String         utfstr;             // UTF8 string

    // get window and password text
    enableControls(false);
    wndtxt=txtaraMain.getText();
    if(wndtxt.length()==0) { setStatusText("Text Area cannot be blank."); enableControls(true); return; }
    pwdtxt=txtfldPassword.getText();
    if(pwdtxt.length()==0) { setStatusText("Password cannot be blank."); enableControls(true); return; }

    // convert window text and password to bytes
    setStatusText("Converting to "+encoding+" bytes...");
    try {
        wndbyt=wndtxt.getBytes(encoding);
        pwdbyt=pwdtxt.getBytes(encoding);
        }
    catch(UnsupportedEncodingException exp) {
        setStatusText(formatThrowable(exp));
        enableControls(true);
        return;
        }

    // encrypt the bytes using the RC4 cipher
    setStatusText("Encrypting ("+wndbyt.length+") bytes...");
    cph=new SecretMessageCipher(pwdbyt);
    cph.encrypt(wndbyt,wndbyt);

    // convert bytes to hex characters
    setStatusText("Converting encrypted bytes to hex...");
    txtaraMain.setText(getHexString(wndbyt));

    // done
    enableControls(true);
    txtaraMain.setCaretPosition(0);
    txtaraMain.requestFocus();
    setStatusText("Encrypted ("+wndbyt.length+") bytes.");
    }

private void onSaveButtonAction(ActionEvent evt) {
    int            rsp;                // response code
    File           filpwd;
    File           filbak;
    FileWriter     fw;

    // backup existing passwords file
    try {
        // get the filenames
        filbak=new File("PasswordsBackup.txt");
        filpwd=new File(appProperties.getProperty(PRPNAM_PWDFILPTH));

        // confirm if the password file already exists
        if(filpwd.exists()) {
            rsp=JOptionPane.showConfirmDialog(
                this.getParent(),
                "Continue with save? Existing file will be overwritten.",
                "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
                );
            if(rsp!=JOptionPane.YES_OPTION) {
                return;
                }
            }
        
        // delete and rename
        if(filbak.exists()) { filbak.delete(); }        
        filpwd.renameTo(filbak);
        
        // write the new file contents
        fw=new FileWriter(filpwd);
        fw.write(txtaraMain.getText());
        fw.close();
        }
    catch(Exception exp) {
        JOptionPane.showMessageDialog(
            this.getParent(),
            formatThrowable(exp),
            "Error during file backup",
            JOptionPane.ERROR_MESSAGE
            );
        }

    // exit!
    System.exit(0);
    }

private String getHexString(byte[] bytbuf) {
    char[]         hexchr={'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    char[]         chrbuf;             // data as hex

    chrbuf=new char[bytbuf.length*2];
    for(int bi=0,ci=0; bi<bytbuf.length; bi++,ci+=2) {
        chrbuf[ci  ]=hexchr[(bytbuf[bi]&0x00F0)>>>4 ];
        chrbuf[ci+1]=hexchr[(bytbuf[bi]&0x000F)     ];
        }
    return new String(chrbuf);
    }

private byte[] getBytesFromHexString(String wndtxt) {
    byte[]         bytbuf;             // byte buffer
    char[]         chrbuf;             // char buffer

    if((wndtxt.length()%2)!=0) {
        setStatusText("Invalid hex string length (must be even).");
        return null;
        }
    chrbuf=wndtxt.toCharArray();
    bytbuf=new byte[chrbuf.length/2];
    for(int bi=0,ci=0; bi<bytbuf.length; bi++,ci+=2) {
        try {
            bytbuf[bi]=(byte) ( (getHexDigitValue(chrbuf[ci])<<4)+getHexDigitValue(chrbuf[ci+1]) );
            }
        catch(Exception exp) {
            setStatusText(formatThrowable(exp));
            return null;
            }
        }
    return bytbuf;
    }

private int getHexDigitValue(char hexdgt) throws Exception {
    switch(hexdgt) {
        case '0': { return 0;  }
        case '1': { return 1;  }
        case '2': { return 2;  }
        case '3': { return 3;  }
        case '4': { return 4;  }
        case '5': { return 5;  }
        case '6': { return 6;  }
        case '7': { return 7;  }
        case '8': { return 8;  }
        case '9': { return 9;  }
        case 'A': { return 10; }
        case 'B': { return 11; }
        case 'C': { return 12; }
        case 'D': { return 13; }
        case 'E': { return 14; }
        case 'F': { return 15; }
        default:  {
            throw new Exception("Invalid hex character: ["+hexdgt+"]");
            }
        }
    }

/**************************************************************************/
/* INNER CLASSES                                                          */
/**************************************************************************/

/**************************************************************************/
/* STATIC PROPERTIES                                                      */
/**************************************************************************/

private static final String            encoding="UTF8";
private static final String            propertiesFilename="PasswordManager.cfg";
private static final String            PRPNAM_PWDFILPTH="pwdfilpth";           // property: password file path

/**************************************************************************/
/* STATIC INIT & MAIN                                                     */
/**************************************************************************/

/**************************************************************************/
/* STATIC METHODS                                                         */
/**************************************************************************/

} /* END PUBLIC CLASS */