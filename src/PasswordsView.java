/**
 * Imports
 * -----------------------------------------------------------------------------
 */
import com.google.gson.Gson;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

public class PasswordsView
extends javax.swing.JComponent
{

/**
 * Types
 * -----------------------------------------------------------------------------
 */

private enum Mode { TEXT, TREE }

/**
 * Instance Properties
 * -----------------------------------------------------------------------------
 */

private Label                mSearchLabel;
private TextField            mSearchTextField;
private TextArea             mMainTextArea;
private JTree                mTree;
private JScrollPane          mTreeScrollPane;

private boolean              mInitialized=false;
private Mode                 mMode=Mode.TEXT;

/**
 * Instance Constructors
 * -----------------------------------------------------------------------------
 */

// constructor
public PasswordsView()
{

}

/**
 * Instance Methods
 * -----------------------------------------------------------------------------
 */

public void init() {

    // instantiate components
    mSearchLabel=new Label();
    mSearchTextField=new TextField("");
    mMainTextArea=new TextArea("",0,0,TextArea.SCROLLBARS_VERTICAL_ONLY);
    mTree=new JTree();
    mTreeScrollPane=new JScrollPane(mTree);

    // setup components
    mSearchLabel.setText("Search");
    mSearchLabel.setAlignment(Label.RIGHT);
    mTree.setRootVisible(false);
    mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    // add components
    this.add(mSearchLabel);
    this.add(mSearchTextField);
    this.add(mMainTextArea);
    this.add(mTreeScrollPane);

    // add listeners
    this.addComponentListener(new ComponentListener() {
        public void componentResized(ComponentEvent evt) { onMainComponentResized(evt); }
        public void componentMoved(ComponentEvent evt) { }
        public void componentShown(ComponentEvent evt) { }
        public void componentHidden(ComponentEvent evt) { }
    });
    mSearchTextField.addKeyListener(new KeyListener() {
        public void keyTyped(KeyEvent evt) {}
        public void keyPressed(KeyEvent evt) {}
        public void keyReleased(KeyEvent evt) { onSearchTextKeyReleased(evt); }
    });
    mTree.addTreeSelectionListener(new TreeSelectionListener() {
        public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
           onTreeSelectionChanged(evt);
        }
    });

    // finish
    mInitialized=true;
}

public void reset() {
    mMainTextArea.setCaretPosition(0);
    mSearchTextField.requestFocus();
}

public void onMainComponentResized(ComponentEvent evt)
{
    int            wth;                // width
    int            hgt;                // height
    int            edgePad = 0;        // edge padding value
    int            innerPad = 5;       // inner padding value

    if(!mInitialized) { return; }
    wth=this.getSize().width;
    hgt=this.getSize().height;

    mSearchLabel.setBounds(
        edgePad,
        edgePad,
        Math.max(mSearchLabel.getPreferredSize().width,70),
        20
    );
    mSearchTextField.setBounds(
        edgePad+mSearchLabel.getSize().width+(innerPad*2),
        edgePad,
        wth-edgePad-mSearchLabel.getSize().width-(innerPad*2)-edgePad,
        20
    );

    int mainAreaX = edgePad;
    int mainAreaY = edgePad+mSearchTextField.getSize().height+innerPad;
    int mainAreaW = wth-(edgePad*2);
    int mainAreaH = hgt-edgePad-mSearchTextField.getSize().height-innerPad-edgePad;
    mMainTextArea.setBounds(mainAreaX,mainAreaY,mainAreaW,mainAreaH);
    mTreeScrollPane.setBounds(mainAreaX,mainAreaY,mainAreaW,mainAreaH);
}

public void setText(String text) {
    // first, try to parse it as JSON
    try {
        PasswordCollection passwordCollection = parseJson(text);
        TreeNode root = buildTree(passwordCollection);
        mTree.setModel(new javax.swing.tree.DefaultTreeModel(root));
        mMode=Mode.TREE;
    } catch (Exception e) {
        // if it fails, just set the text
        System.out.println("Failed to parse JSON, reverting to text view.");
        mMainTextArea.setText(text);
        mMode=Mode.TEXT;
    }

    // set visibility
    mTreeScrollPane.setVisible(mMode==Mode.TREE);
    mMainTextArea.setVisible(mMode==Mode.TEXT);
}

public String getText() {
    return mMainTextArea.getText();
}


/**
 * Instance Methods - Search
 * -----------------------------------------------------------------------------
 */

private void onSearchTextKeyReleased(KeyEvent evt) {
    if(evt.getKeyCode()==KeyEvent.VK_ENTER) {
        // search for the next occurrence
        if((evt.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK)==KeyEvent.SHIFT_DOWN_MASK) {
            searchAndSelect(mMainTextArea.getSelectionStart()-1,SEARCH_DIRECTION_BACKWARD);
            }
        else {
            searchAndSelect(mMainTextArea.getSelectionStart()+1,SEARCH_DIRECTION_FORWARD);
            }
        }
    else {
        // search as letters are typed
        searchAndSelect(mMainTextArea.getSelectionStart(),SEARCH_DIRECTION_FORWARD);
        }
    }

/**
 * Search for the current search text in the main text area, and select the first matching text found.
 * Wrap if necessary.
 *
 * @param startIndex    The index at which to start searching.
 * @param direction     A valid SEARCH_DIRECTION_ constant which indicates which direction to search.
 */
private void searchAndSelect(int startIndex, int direction) {
    String         mantxt;             // main text, converted to lowercase
    String         schtxt;             // search text, converted to lowercase
    int            schidx;             // search index

    // get search text and main text
    mantxt=mMainTextArea.getText().toLowerCase();
    schtxt=mSearchTextField.getText().toLowerCase();

    // search for text
    if(direction==SEARCH_DIRECTION_FORWARD) {
        // search forward, wrapping if necessary
        schidx=mantxt.indexOf(schtxt,startIndex);
        if(schidx==-1) { schidx=mantxt.indexOf(schtxt); } // start at the beginning
        }
    else {
        // search backward, wrapping if necessary
        schidx=mantxt.lastIndexOf(schtxt,startIndex);
        if(schidx==-1) { schidx=mantxt.lastIndexOf(schtxt); } // start at the end
        }

    // select text if found
    if(schidx>=0) {
        mMainTextArea.setSelectionStart(schidx);
        mMainTextArea.setSelectionEnd(schidx+schtxt.length());
        }
    }




private PasswordCollection parseJson(String text) {
    // input
    // System.out.println(text);
    Gson gson = new Gson();
    PasswordCollection passwordCollection = gson.fromJson(text,PasswordCollection.class);
    // System.out.println(passwordCollection.items[0]);
    // System.out.println(passwordCollection.items[0].ttl);

    // output
    // String output = gson.toJson(passwordCollection);
    // System.out.println(output);

    return passwordCollection;
}

private TreeNode buildTree(PasswordCollection passwordCollection) {
    // create the root node
    DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");

    // create the child nodes
    for (PasswordItem passwordItem : passwordCollection.items) {
        DefaultMutableTreeNode passwordItemNode = new DefaultMutableTreeNode(passwordItem);
        root.add(passwordItemNode);
    }

    // return the root node
    return root;
}

private void onTreeSelectionChanged(TreeSelectionEvent evt) {
    DefaultMutableTreeNode node = (DefaultMutableTreeNode) mTree.getLastSelectedPathComponent();

    // if nothing is selected, do nothing
    if (node == null) return;

    // retrieve the node that was selected
    Object nodeInfo = node.getUserObject();
    PasswordItem passwordItem = (PasswordItem)nodeInfo;
    System.out.println(passwordItem.ttl);
    System.out.println(passwordItem.usr);
}

/**
 * Inner Classes
 * -----------------------------------------------------------------------------
 */

class PasswordItem {
    private String ttl = ""; // title
    private String dsc = ""; // description
    private String usr = ""; // username
    private String pwd = ""; // password
    private String nts = ""; // notes
    public String toString() { return ttl; }
    PasswordItem() {} // no-args constructor
  }

class PasswordCollection {
    private String version = ""; // file version
    private PasswordItem[] items = null; // password items
    PasswordCollection() {} // no-args constructor
    }

/**
 * Static Properties
 * -----------------------------------------------------------------------------
 */

private static final int               SEARCH_DIRECTION_FORWARD=1;
private static final int               SEARCH_DIRECTION_BACKWARD=2;

/**
 * Static Init & Main
 * -----------------------------------------------------------------------------
 */

/**
 * Static Methods
 * -----------------------------------------------------------------------------
 */

} // END PUBLIC CLASS