import com.google.gson.Gson;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * PasswordsTreeView: A view for displaying JSON structured password data in a
 * tree view.
 */
public class PasswordsTreeView
    extends javax.swing.JComponent
    implements IPasswordsView {

  /**
   * Types
   * ---------------------------------------------------------------------------
   */

  /**
   * Instance Properties
   * ---------------------------------------------------------------------------
   */

  private JTree mTree;
  private JScrollPane mTreeScrollPane;
  private Button mAddItemButton;
  private JPanel mDetailPanel;
  private Label mTitleLabel;
  private TextField mTitleTextField;
  private Label mDescriptionLabel;
  private TextField mDescriptionTextField;
  private Label mUsernameLabel;
  private TextField mUsernameTextField;
  private Button mCopyUsernameButton;
  private Label mPasswordLabel;
  private TextField mPasswordTextField;
  private Button mCopyPasswordButton;
  private Button mToggleHidePasswordButton;
  private TextArea mNotesTextArea;
  private Button mToggleHideNotesButton;

  private boolean mInitialized = false;
  private DefaultTreeModel mUnfilteredTreeModel = null;

  /**
   * Instance Constructors
   * ---------------------------------------------------------------------------
   */

  // constructor
  public PasswordsTreeView() {

  }

  /**
   * Instance Methods
   * ---------------------------------------------------------------------------
   */

  public void init() {

    // instantiate components
    mTree = new JTree();
    mTreeScrollPane = new JScrollPane(mTree);
    mAddItemButton = new Button();
    mDetailPanel = new JPanel();
    mTitleLabel = new Label();
    mTitleTextField = new TextField();
    mDescriptionLabel = new Label();
    mDescriptionTextField = new TextField();
    mUsernameLabel = new Label();
    mUsernameTextField = new TextField();
    mCopyUsernameButton = new Button();
    mPasswordLabel = new Label();
    mPasswordTextField = new TextField();
    mCopyPasswordButton = new Button();
    mToggleHidePasswordButton = new Button();
    mNotesTextArea = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
    mToggleHideNotesButton = new Button();

    // setup components
    mTree.setRootVisible(false);
    mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    mAddItemButton.setLabel("+");
    mDetailPanel.setBackground(getBackground());
    mDetailPanel.setVisible(false);
    mDetailPanel.setLayout(null); // get rid of layout manger
    mTitleLabel.setText("Title");
    mTitleLabel.setAlignment(Label.RIGHT);
    mDescriptionLabel.setText("Description");
    mDescriptionLabel.setAlignment(Label.RIGHT);
    mUsernameLabel.setText("Username");
    mUsernameLabel.setAlignment(Label.RIGHT);
    mCopyUsernameButton.setLabel("Copy");
    mPasswordLabel.setText("Password");
    mPasswordLabel.setAlignment(Label.RIGHT);
    setTextFieldEcho(mPasswordTextField, false);
    mCopyPasswordButton.setLabel("Copy");
    setButtonLabelBasedOnEcho(mToggleHidePasswordButton, mPasswordTextField);
    hideNotes(true);

    // add components
    this.add(mTreeScrollPane);
    this.add(mAddItemButton);
    this.add(mDetailPanel);
    mDetailPanel.add(mTitleLabel);
    mDetailPanel.add(mTitleTextField);
    mDetailPanel.add(mDescriptionLabel);
    mDetailPanel.add(mDescriptionTextField);
    mDetailPanel.add(mUsernameLabel);
    mDetailPanel.add(mUsernameTextField);
    mDetailPanel.add(mCopyUsernameButton);
    mDetailPanel.add(mPasswordLabel);
    mDetailPanel.add(mPasswordTextField);
    mDetailPanel.add(mCopyPasswordButton);
    mDetailPanel.add(mToggleHidePasswordButton);
    mDetailPanel.add(mNotesTextArea);
    mDetailPanel.add(mToggleHideNotesButton);

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
    mTree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
        onTreeSelectionChanged(evt);
      }
    });
    mAddItemButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        addNewPasswordItem();
      }
    });

    mDetailPanel.addComponentListener(new ComponentListener() {
      public void componentResized(ComponentEvent evt) {
      }

      public void componentMoved(ComponentEvent evt) {
      }

      public void componentShown(ComponentEvent evt) {
      }

      public void componentHidden(ComponentEvent evt) {
      }

    });
    mTitleTextField.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent evt) {
      }

      public void keyPressed(KeyEvent evt) {
      }

      public void keyReleased(KeyEvent evt) {
        onTitleTextKeyReleased(evt);
      }
    });
    mDescriptionTextField.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent evt) {
      }

      public void keyPressed(KeyEvent evt) {
      }

      public void keyReleased(KeyEvent evt) {
        onDescriptionTextKeyReleased(evt);
      }
    });
    mUsernameTextField.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent evt) {
      }

      public void keyPressed(KeyEvent evt) {
      }

      public void keyReleased(KeyEvent evt) {
        onUsernameTextKeyReleased(evt);
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
    mCopyUsernameButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        copyTextToClipboard(mUsernameTextField.getText());
      }
    });
    mCopyPasswordButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        copyTextToClipboard(mPasswordTextField.getText());
      }
    });
    mToggleHidePasswordButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        toggleHidePassword();
      }
    });
    mNotesTextArea.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent evt) {
      }

      public void keyPressed(KeyEvent evt) {
      }

      public void keyReleased(KeyEvent evt) {
        if (notesAreHidden()) {
          return; // don't do anything if notes are hidden
        }
        onNotesTextKeyReleased(evt);
      }
    });
    mToggleHideNotesButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        hideNotes(!notesAreHidden());
      }
    });

    // finish
    mInitialized = true;
  }

  /**
   * Instance Methods - Layout
   * ---------------------------------------------------------------------------
   */

  public void onMainComponentResized(ComponentEvent evt) {
    if (!mInitialized) {
      return;
    }
    Dimension ctrsiz = this.getSize();

    // main area dimensions: used for tree scroll pane and detail panel
    int maiX = 0;
    int maiY = PasswordsView.INNER_PAD;
    int maiW = ctrsiz.width;
    int maiH = ctrsiz.height - PasswordsView.INNER_PAD;

    // tree scroll pane and detail panel
    double lftpct = 0.333;
    double rgtpct = 1.0 - lftpct;
    mTreeScrollPane.setBounds(maiX, maiY, (int) (maiW * lftpct),
        maiH - (PasswordsView.TEXTFIELD_HEIGHT * 3) - PasswordsView.INNER_PAD);
    mDetailPanel.setBounds(
        ((int) (maiW * lftpct)) + PasswordsView.INNER_PAD,
        maiY,
        ((int) (maiW * rgtpct)) - PasswordsView.INNER_PAD,
        maiH);
    Dimension dtlpnlsiz = mDetailPanel.getSize();

    // add item button
    mAddItemButton.setBounds(
        maiX,
        maiY + mTreeScrollPane.getSize().height + PasswordsView.INNER_PAD,
        60,
        PasswordsView.TEXTFIELD_HEIGHT);

    // layout fields in detail panel
    int top = 0;
    PasswordsView.layoutLabelAndField(top, dtlpnlsiz.width, mTitleLabel, mTitleTextField);
    top += PasswordsView.TEXTFIELD_HEIGHT + PasswordsView.INNER_PAD;
    PasswordsView.layoutLabelAndField(top, dtlpnlsiz.width, mDescriptionLabel, mDescriptionTextField);
    top += PasswordsView.TEXTFIELD_HEIGHT + PasswordsView.INNER_PAD;
    PasswordsView.layoutLabelAndField(top, dtlpnlsiz.width, mUsernameLabel, mUsernameTextField);
    top += PasswordsView.TEXTFIELD_HEIGHT + PasswordsView.INNER_PAD;
    layoutActionButtons(top, dtlpnlsiz.width, new Button[] { mCopyUsernameButton });
    top += PasswordsView.TEXTFIELD_HEIGHT + PasswordsView.INNER_PAD;
    PasswordsView.layoutLabelAndField(top, dtlpnlsiz.width, mPasswordLabel, mPasswordTextField);
    top += PasswordsView.TEXTFIELD_HEIGHT + PasswordsView.INNER_PAD;
    layoutActionButtons(top, dtlpnlsiz.width, new Button[] { mCopyPasswordButton, mToggleHidePasswordButton });
    top += PasswordsView.TEXTFIELD_HEIGHT + PasswordsView.INNER_PAD;

    // layout notes
    mNotesTextArea.setBounds(0, top, dtlpnlsiz.width,
        (dtlpnlsiz.height - top) - (PasswordsView.TEXTFIELD_HEIGHT * 3) - PasswordsView.INNER_PAD);
    top += mNotesTextArea.getSize().height + PasswordsView.INNER_PAD;
    layoutActionButtons(top, dtlpnlsiz.width, new Button[] { mToggleHideNotesButton });
  }

  private void layoutActionButtons(int top, int containerWidth, Button[] buttons) {
    int btnwth = 60;
    for (int i = 0; i < buttons.length; i++) {
      buttons[i].setBounds(
          containerWidth - btnwth - (PasswordsView.INNER_PAD * i) - (btnwth * i),
          top,
          btnwth,
          PasswordsView.TEXTFIELD_HEIGHT);
    }
  }

  /**
   * Instance Methods - TextField utility
   * ---------------------------------------------------------------------------
   */

  private void setTextFieldEcho(TextField textField, boolean show) {
    if (show) {
      textField.setEchoChar((char) 0);
    } else {
      textField.setEchoChar('*');
    }
  }

  private void toggleTextFieldEcho(TextField textField) {
    setTextFieldEcho(textField, !isEchoingPlainText(textField));
  }

  private boolean isEchoingPlainText(TextField textField) {
    return textField.getEchoChar() == (char) 0;
  }

  private void setButtonLabelBasedOnEcho(Button button, TextField textField) {
    if (isEchoingPlainText(textField)) {
      button.setLabel("Hide");
    } else {
      button.setLabel("Show");
    }
  }

  private void toggleHidePassword() {
    toggleTextFieldEcho(mPasswordTextField);
    setButtonLabelBasedOnEcho(mToggleHidePasswordButton, mPasswordTextField);
  }

  /**
   * Instance Methods - TextArea utility
   * ---------------------------------------------------------------------------
   */

  private void hideNotes(boolean hide) {
    if (hide) {
      // hide
      mToggleHideNotesButton.setLabel("Show");
      mNotesTextArea.setText("--- hidden ---");
      mNotesTextArea.setEditable(false);
    } else {
      // show
      mToggleHideNotesButton.setLabel("Hide");
      PasswordItem passwordItem = getSelectedPasswordItem();
      mNotesTextArea.setText(passwordItem != null ? passwordItem.nts : "");
      mNotesTextArea.setEditable(true);
    }
  }

  private boolean notesAreHidden() {
    return mToggleHideNotesButton.getLabel() == "Show";
  }

  /**
   * Instance Methods - IPasswordsView interface
   * ---------------------------------------------------------------------------
   */

  /**
   * Parse text as JSON; may throw an error if unsuccessful.
   */
  public void setText(String text) {
    PasswordCollection passwordCollection = fromJson(text);
    TreeNode root = treeFromCollection(passwordCollection);
    mUnfilteredTreeModel = new DefaultTreeModel(root);
    mTree.setModel(mUnfilteredTreeModel);
  }

  /**
   * Convert current tree data to JSON; return as a String.
   */
  public String getText() {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) mTree.getModel().getRoot();
    PasswordCollection passwordCollection = collectionFromTree(root);
    return toJson(passwordCollection);
  }

  public int getSelectedIndex() {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) mTree.getModel().getRoot();
    DefaultMutableTreeNode selectedNode = getSelectedTreeNode();
    if (selectedNode == null) {
      return 0;
    }
    int index = root.getIndex(selectedNode);
    return (index == -1) ? 0 : index;
  }

  public void searchAndSelect(String searchText, int startIndex, SearchDirection direction) {
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) mTree.getModel().getRoot();

    if (searchText.length() == 0) {
      mTree.clearSelection();
      return;
    }
    if (startIndex == -1) {
      startIndex = root.getChildCount() - 1;
    }

    for (int index = startIndex; index < root.getChildCount() && index >= 0;) {
      DefaultMutableTreeNode searchNode = (DefaultMutableTreeNode) root.getChildAt(index);
      Object userObject = searchNode.getUserObject();
      if (userObject instanceof PasswordItem) {

        PasswordItem item = (PasswordItem) userObject;

        if (item.containsText(searchText)) {
          TreeNode[] nodes = ((DefaultTreeModel) mTree.getModel()).getPathToRoot(searchNode);
          TreePath tpath = new TreePath(nodes);
          mTree.scrollPathToVisible(tpath);
          mTree.setSelectionPath(tpath);
          return;
        }

      }
      // increment or decrement based on search direction
      index = (direction == SearchDirection.FORWARD) ? index + 1 : index - 1;
    }

    // select nothing!
    mTree.clearSelection();
  }

  public void filter(String filterText) {
    // filter the tree and set a new model
    TreeModel model = filterText.length() <= 0
        ? mUnfilteredTreeModel
        : filterTree(mUnfilteredTreeModel, filterText);
    mTree.setModel(model);

    // select first item if there is one
    DefaultMutableTreeNode newRoot = (DefaultMutableTreeNode) model.getRoot();
    if (newRoot.getChildCount() == 1) {
      // there is only one!
      DefaultMutableTreeNode firstNode = (DefaultMutableTreeNode) newRoot.getChildAt(0);
      mTree.setSelectionPath(new TreePath(firstNode.getPath()));
    }
  }

  public void reset() {
    // reset tree model to unfiltered
    mTree.setModel(mUnfilteredTreeModel);
  }

  /**
   * Instance Methods: Tree
   * ---------------------------------------------------------------------------
   */

  /** Create a PasswordCollection object give a tree root */
  private PasswordCollection collectionFromTree(DefaultMutableTreeNode root) {

    // create a new PasswordCollection and an array of items
    PasswordCollection newPasswordCollection = new PasswordCollection();
    newPasswordCollection.items = new PasswordItem[root.getChildCount()];

    // iterate through the tree and populate the PasswordCollection and items array
    int itemIndex = 0;
    for (Enumeration<?> e = root.breadthFirstEnumeration(); e.hasMoreElements();) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
      Object userObject = node.getUserObject();
      if (userObject instanceof PasswordCollection) {
        PasswordCollection originalPasswordCollection = (PasswordCollection) userObject;
        newPasswordCollection.version = originalPasswordCollection.version;
        newPasswordCollection.title = originalPasswordCollection.title;
      } else if (userObject instanceof PasswordItem) {
        PasswordItem item = (PasswordItem) userObject;
        newPasswordCollection.items[itemIndex] = item;
        itemIndex++;
      }
    }

    // return the new PasswordCollection
    return newPasswordCollection;
  }

  private TreeNode treeFromCollection(PasswordCollection passwordCollection) {
    // create the root node and give it the original password collection
    DefaultMutableTreeNode root = new DefaultMutableTreeNode(passwordCollection);

    // create a child node for each password item
    for (PasswordItem passwordItem : passwordCollection.items) {
      DefaultMutableTreeNode passwordItemNode = new DefaultMutableTreeNode(passwordItem);
      root.add(passwordItemNode);
    }

    // return the root node
    return root;
  }

  public TreeModel filterTree(TreeModel treeModel, String filterText) {

    // create the new root node, and give it the same root user object
    DefaultMutableTreeNode currentRoot = (DefaultMutableTreeNode) treeModel.getRoot();
    DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(currentRoot.getUserObject());

    // iterate the old tree's children and create a new tree, but only with
    // the children that match the filter text
    for (int index = 0; index < currentRoot.getChildCount(); index++) {

      DefaultMutableTreeNode nextNode = (DefaultMutableTreeNode) currentRoot.getChildAt(index);
      Object userObject = nextNode.getUserObject();
      if (userObject instanceof PasswordItem) {

        PasswordItem item = (PasswordItem) userObject;

        if (item.containsText(filterText)) {
          DefaultMutableTreeNode newMatchingNode = new DefaultMutableTreeNode(item);
          newRoot.add(newMatchingNode);
        }
      }
    }

    // return the new filtered tree model
    return new DefaultTreeModel(newRoot);
  }

  private void onTreeSelectionChanged(TreeSelectionEvent evt) {
    PasswordItem passwordItem = getSelectedPasswordItem();
    boolean isItemSelected = passwordItem != null;
    mDetailPanel.setVisible(isItemSelected);
    if (!isItemSelected) {
      return;
    }

    // update the detail panel items
    mTitleTextField.setText(passwordItem.ttl);
    mDescriptionTextField.setText(passwordItem.dsc);
    mUsernameTextField.setText(passwordItem.usr);
    mPasswordTextField.setText(passwordItem.pwd);
    mNotesTextArea.setText(passwordItem.nts);
    hideNotes(true);
  }

  private DefaultMutableTreeNode getSelectedTreeNode() {
    return (DefaultMutableTreeNode) mTree.getLastSelectedPathComponent();
  }

  private PasswordItem getSelectedPasswordItem() {
    // get the selected node; do nothing if nothing is selected
    DefaultMutableTreeNode node = getSelectedTreeNode();
    if (node == null) {
      return null;
    }
    // return the password item
    return (PasswordItem) node.getUserObject();
  }

  private void addNewPasswordItem() {
    // clear any filters
    this.reset();

    // create a new password item
    PasswordItem passwordItem = new PasswordItem();
    passwordItem.ttl = "New Item";

    // create a new node and add it to the tree
    DefaultMutableTreeNode passwordItemNode = new DefaultMutableTreeNode(passwordItem);
    DefaultMutableTreeNode root = (DefaultMutableTreeNode) mUnfilteredTreeModel.getRoot();
    mUnfilteredTreeModel.insertNodeInto(passwordItemNode, root, 0);

    // scroll to and select the new node
    mTree.scrollPathToVisible(new TreePath(passwordItemNode.getPath()));
    mTree.setSelectionPath(new TreePath(passwordItemNode.getPath()));
  }

  /**
   * Instance Methods: Password Item Input
   * ---------------------------------------------------------------------------
   */

  private interface KeyReleasedHandlerLambda {
    void handleKeyReleased(PasswordItem passwordItem);
  }

  private void onKeyReleasedGeneric(KeyEvent evt, KeyReleasedHandlerLambda handler) {
    // get selected password item
    PasswordItem passwordItem = getSelectedPasswordItem();
    if (passwordItem == null) {
      return;
    }

    // invoke handler
    handler.handleKeyReleased(passwordItem);

    // refresh tree UI
    mTree.getModel().valueForPathChanged(mTree.getSelectionPath(), passwordItem);
  }

  private void onTitleTextKeyReleased(KeyEvent evt) {
    onKeyReleasedGeneric(evt, (passwordItem) -> {
      passwordItem.ttl = mTitleTextField.getText();
    });
  }

  private void onDescriptionTextKeyReleased(KeyEvent evt) {
    onKeyReleasedGeneric(evt, (passwordItem) -> {
      passwordItem.dsc = mDescriptionTextField.getText();
    });
  }

  private void onUsernameTextKeyReleased(KeyEvent evt) {
    onKeyReleasedGeneric(evt, (passwordItem) -> {
      passwordItem.usr = mUsernameTextField.getText();
    });
  }

  private void onPasswordTextKeyReleased(KeyEvent evt) {
    onKeyReleasedGeneric(evt, (passwordItem) -> {
      passwordItem.pwd = mPasswordTextField.getText();
    });
  }

  private void onNotesTextKeyReleased(KeyEvent evt) {
    onKeyReleasedGeneric(evt, (passwordItem) -> {
      passwordItem.nts = mNotesTextArea.getText();
    });
  }

  /**
   * Instance Methods - general utility
   * ---------------------------------------------------------------------------
   */

  private void copyTextToClipboard(String text) {
    Toolkit.getDefaultToolkit()
        .getSystemClipboard()
        .setContents(
            new StringSelection(text),
            null);
  }

  /**
   * Inner Classes
   * ---------------------------------------------------------------------------
   */

  class PasswordCollection {
    private String title = ""; // title
    private String version = ""; // file version
    private PasswordItem[] items = null; // password items

    PasswordCollection() {
    } // no-args constructor
  }

  class PasswordItem {
    private String ttl = ""; // title
    private String dsc = ""; // description
    private String usr = ""; // username
    private String pwd = ""; // password
    private String nts = ""; // notes

    public String toString() {
      return ttl;
    }

    private boolean containsText(String text) {
      String lowerText = text.toLowerCase();
      return ttl.toLowerCase().contains(lowerText)
          || dsc.toLowerCase().contains(lowerText)
          || usr.toLowerCase().contains(lowerText)
          || pwd.toLowerCase().contains(lowerText)
          || nts.toLowerCase().contains(lowerText);
    }

    PasswordItem() {
    } // no-args constructor
  }

  /**
   * Static Properties
   * ---------------------------------------------------------------------------
   */

  /**
   * Static Init & Main
   * ---------------------------------------------------------------------------
   */

  /**
   * Static Methods
   * ---------------------------------------------------------------------------
   */

  private static PasswordCollection fromJson(String text) {
    Gson gson = new Gson();
    PasswordCollection passwordCollection = gson.fromJson(text, PasswordCollection.class);
    return passwordCollection;
  }

  private static String toJson(PasswordCollection collection) {
    Gson gson = new Gson();
    return gson.toJson(collection);
  }

} // End Public Class