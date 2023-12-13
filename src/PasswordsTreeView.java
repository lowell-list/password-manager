import com.google.gson.Gson;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import java.util.Enumeration;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
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
     * -------------------------------------------------------------------------
     */

    /**
     * Instance Properties
     * -------------------------------------------------------------------------
     */

    private JScrollPane mTreeScrollPane;
    private JTree mTree;
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

    private boolean mInitialized = false;

    /**
     * Instance Constructors
     * -------------------------------------------------------------------------
     */

    // constructor
    public PasswordsTreeView() {

    }

    /**
     * Instance Methods
     * -------------------------------------------------------------------------
     */

    public void init() {

        // instantiate components
        mTree = new JTree();
        mTreeScrollPane = new JScrollPane(mTree);
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

        // setup components
        mTree.setRootVisible(false);
        mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        mDetailPanel.setBackground(getBackground());
        mDetailPanel.setVisible(false);
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

        // add components
        this.add(mTreeScrollPane);
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

        // add listeners
        this.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent evt) {
                onMainComponentResized(evt);
            }

            public void componentMoved(ComponentEvent evt) {
            }

            public void componentShown(ComponentEvent evt) {
                onMainComponentResized(evt);
            }

            public void componentHidden(ComponentEvent evt) {
            }
        });
        mTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                onTreeSelectionChanged(evt);
            }
        });
        mDetailPanel.addComponentListener(new ComponentListener() {
            public void componentResized(ComponentEvent evt) {
            }

            public void componentMoved(ComponentEvent evt) {
            }

            public void componentShown(ComponentEvent evt) {
                onMainComponentResized(evt);
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

        // finish
        mInitialized = true;
    }

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
        mTreeScrollPane.setBounds(maiX, maiY, (int) (maiW * lftpct), maiH);
        mDetailPanel.setBounds(
                ((int) (maiW * lftpct)) + PasswordsView.INNER_PAD,
                maiY,
                ((int) (maiW * rgtpct)) - PasswordsView.INNER_PAD,
                maiH);
        Dimension dtlpnlsiz = mDetailPanel.getSize();

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

    private void copyTextToClipboard(String text) {
        Toolkit.getDefaultToolkit()
                .getSystemClipboard()
                .setContents(
                        new StringSelection(text),
                        null);
    }

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
     * Instance Methods - IPasswordsView interface
     * -------------------------------------------------------------------------
     */

    /**
     * Parse text as JSON; may throw an error if unsuccessful.
     */
    public void setText(String text) {
        PasswordCollection passwordCollection = fromJson(text);
        TreeNode root = treeFromCollection(passwordCollection);
        mTree.setModel(new javax.swing.tree.DefaultTreeModel(root));
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
        return 0;
    }

    public void searchAndSelect(String searchText, int startIndex, SearchDirection direction) {
        return;
    }

    public void reset() {
    }

    /**
     * Instance Methods: Tree
     * -------------------------------------------------------------------------
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
        System.out.println("found " + String.valueOf(itemIndex) + " PasswordItems");

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

    /**
     * Instance Methods: Password Item Input
     * -------------------------------------------------------------------------
     */

    private void onTitleTextKeyReleased(KeyEvent evt) {
        // get selected password item
        PasswordItem passwordItem = getSelectedPasswordItem();
        if (passwordItem == null) {
            return;
        }

        // update its title field value
        passwordItem.ttl = mTitleTextField.getText();

        // refresh tree UI
        mTree.getModel().valueForPathChanged(mTree.getSelectionPath(), passwordItem);
    }

    /**
     * Inner Classes
     * -------------------------------------------------------------------------
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

        PasswordItem() {
        } // no-args constructor
    }

    /**
     * Static Properties
     * -------------------------------------------------------------------------
     */

    /**
     * Static Init & Main
     * -------------------------------------------------------------------------
     */

    /**
     * Static Methods
     * -------------------------------------------------------------------------
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

} // END PUBLIC CLASS