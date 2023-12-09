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
 * PasswordsView: A view for displaying passwords.
 * Includes a search field for searching.
 * Depending on the type of data, it will either display a text area or a tree.
 */
public class PasswordsView
        extends javax.swing.JComponent {

    /**
     * Types
     * -----------------------------------------------------------------------------
     */

    private enum Mode {
        TEXT, TREE
    }

    /**
     * Instance Properties
     * -----------------------------------------------------------------------------
     */

    private Label mSearchLabel;
    private TextField mSearchTextField;
    private TextArea mMainTextArea;
    private JTree mTree;
    private JScrollPane mTreeScrollPane;
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
    private Mode mMode = Mode.TEXT;

    /**
     * Instance Constructors
     * -------------------------------------------------------------------------
     */

    // constructor
    public PasswordsView() {

    }

    /**
     * Instance Methods
     * -------------------------------------------------------------------------
     */

    public void init() {

        // instantiate components
        mSearchLabel = new Label();
        mSearchTextField = new TextField("");
        mMainTextArea = new TextArea("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
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
        mSearchLabel.setText("Search");
        mSearchLabel.setAlignment(Label.RIGHT);
        mTree.setRootVisible(false);
        mTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        // mDetailPanel.setBackground(getBackground());
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
        this.add(mSearchLabel);
        this.add(mSearchTextField);
        this.add(mMainTextArea);
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
            }

            public void componentHidden(ComponentEvent evt) {
            }
        });
        mSearchTextField.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent evt) {
            }

            public void keyPressed(KeyEvent evt) {
            }

            public void keyReleased(KeyEvent evt) {
                onSearchTextKeyReleased(evt);
            }
        });
        mTree.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                onTreeSelectionChanged(evt);
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

    public void reset() {
        mMainTextArea.setCaretPosition(0);
        mSearchTextField.requestFocus();
    }

    public void onMainComponentResized(ComponentEvent evt) {
        if (!mInitialized) {
            return;
        }
        Dimension ctrsiz = this.getSize();

        // search label and field (top)
        layoutLabelAndField(0, ctrsiz.width, mSearchLabel, mSearchTextField);
        Dimension schlblsiz = mSearchLabel.getSize();

        // main area dimensions: used for text area, tree scroll pane, and detail panel
        int maiX = 0;
        int maiY = schlblsiz.height + INNER_PAD;
        int maiW = ctrsiz.width;
        int maiH = ctrsiz.height - schlblsiz.height - INNER_PAD;

        // text area
        mMainTextArea.setBounds(maiX, maiY, maiW, maiH); // full size

        // tree scroll pane and detail panel
        double lftpct = 0.333;
        double rgtpct = 1.0 - lftpct;
        mTreeScrollPane.setBounds(maiX, maiY, (int) (maiW * lftpct), maiH);
        mDetailPanel.setBounds(
                ((int) (maiW * lftpct)) + INNER_PAD,
                maiY,
                ((int) (maiW * rgtpct)) - INNER_PAD,
                maiH);
        Dimension dtlpnlsiz = mDetailPanel.getSize();

        // layout fields in detail panel
        int top = 0;
        layoutLabelAndField(top, dtlpnlsiz.width, mTitleLabel, mTitleTextField);
        top += TEXTFIELD_HEIGHT + INNER_PAD;
        layoutLabelAndField(top, dtlpnlsiz.width, mDescriptionLabel, mDescriptionTextField);
        top += TEXTFIELD_HEIGHT + INNER_PAD;
        layoutLabelAndField(top, dtlpnlsiz.width, mUsernameLabel, mUsernameTextField);
        top += TEXTFIELD_HEIGHT + INNER_PAD;
        layoutActionButtons(top, dtlpnlsiz.width, new Button[] { mCopyUsernameButton });
        top += TEXTFIELD_HEIGHT + INNER_PAD;
        layoutLabelAndField(top, dtlpnlsiz.width, mPasswordLabel, mPasswordTextField);
        top += TEXTFIELD_HEIGHT + INNER_PAD;
        layoutActionButtons(top, dtlpnlsiz.width, new Button[] { mCopyPasswordButton, mToggleHidePasswordButton });
    }

    private void layoutLabelAndField(int top, int containerWidth, Label label, TextField textField) {
        label.setBounds(
                0,
                top,
                Math.max(label.getPreferredSize().width, 100),
                TEXTFIELD_HEIGHT);
        Dimension lblsiz = label.getSize();
        textField.setBounds(
                lblsiz.width + INNER_PAD,
                top,
                containerWidth - (lblsiz.width + INNER_PAD),
                TEXTFIELD_HEIGHT);
    }

    private void layoutActionButtons(int top, int containerWidth, Button[] buttons) {
        int btnwth = 60;
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setBounds(
                    containerWidth - btnwth - (INNER_PAD * i) - (btnwth * i),
                    top,
                    btnwth,
                    TEXTFIELD_HEIGHT);
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

    public void setText(String text) {
        // first, try to parse it as JSON
        try {
            PasswordCollection passwordCollection = parseJson(text);
            TreeNode root = buildTree(passwordCollection);
            mTree.setModel(new javax.swing.tree.DefaultTreeModel(root));
            mMode = Mode.TREE;
        } catch (Exception e) {
            // if it fails, just set the text
            System.out.println("Failed to parse JSON, reverting to text view.");
            mMainTextArea.setText(text);
            mMode = Mode.TEXT;
        }

        // set visibility
        mTreeScrollPane.setVisible(mMode == Mode.TREE);
        mMainTextArea.setVisible(mMode == Mode.TEXT);
    }

    public String getText() {
        String text;
        if (mMode == Mode.TREE) {

            DefaultMutableTreeNode root = (DefaultMutableTreeNode) mTree.getModel().getRoot();
            PasswordCollection passwordCollection = fromTree(root);
            String json = toJSON(passwordCollection);

            // TODO: convert tree to JSON!
            // String path = mTree.getSelectionPath();
            // text = mTree.getSelectionPath().toString();
            text = json;
        } else {
            text = mMainTextArea.getText();
        }
        System.out.println("getText(): text is");
        System.out.println(text);
        return text;
    }

    /**
     * Instance Methods - Search
     * -------------------------------------------------------------------------
     */

    private void onSearchTextKeyReleased(KeyEvent evt) {
        if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
            // search for the next occurrence
            if ((evt.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
                searchAndSelect(mMainTextArea.getSelectionStart() - 1, SEARCH_DIRECTION_BACKWARD);
            } else {
                searchAndSelect(mMainTextArea.getSelectionStart() + 1, SEARCH_DIRECTION_FORWARD);
            }
        } else {
            // search as letters are typed
            searchAndSelect(mMainTextArea.getSelectionStart(), SEARCH_DIRECTION_FORWARD);
        }
    }

    /**
     * Search for the current search text in the main text area, and select the
     * first matching text found.
     * Wrap if necessary.
     *
     * @param startIndex The index at which to start searching.
     * @param direction  A valid SEARCH_DIRECTION_ constant which indicates which
     *                   direction to search.
     */
    private void searchAndSelect(int startIndex, int direction) {
        String mantxt; // main text, converted to lowercase
        String schtxt; // search text, converted to lowercase
        int schidx; // search index

        // get search text and main text
        mantxt = mMainTextArea.getText().toLowerCase();
        schtxt = mSearchTextField.getText().toLowerCase();

        // search for text
        if (direction == SEARCH_DIRECTION_FORWARD) {
            // search forward, wrapping if necessary
            schidx = mantxt.indexOf(schtxt, startIndex);
            if (schidx == -1) {
                schidx = mantxt.indexOf(schtxt);
            } // start at the beginning
        } else {
            // search backward, wrapping if necessary
            schidx = mantxt.lastIndexOf(schtxt, startIndex);
            if (schidx == -1) {
                schidx = mantxt.lastIndexOf(schtxt);
            } // start at the end
        }

        // select text if found
        if (schidx >= 0) {
            mMainTextArea.setSelectionStart(schidx);
            mMainTextArea.setSelectionEnd(schidx + schtxt.length());
        }
    }

    /**
     * Inner Classes: Tree
     * -------------------------------------------------------------------------
     */

    private String toJSON(PasswordCollection passwordCollection) {
        String output = "";
        for (PasswordItem passwordItem : passwordCollection.items) {
            output += passwordItem.ttl + "\n";
        }
        return output;
    }

    private PasswordCollection parseJson(String text) {
        // input
        // System.out.println(text);
        Gson gson = new Gson();
        PasswordCollection passwordCollection = gson.fromJson(text, PasswordCollection.class);
        // System.out.println(passwordCollection.items[0]);
        // System.out.println(passwordCollection.items[0].ttl);

        // output
        // String output = gson.toJson(passwordCollection);
        // System.out.println(output);

        return passwordCollection;
    }

    private PasswordCollection fromTree(DefaultMutableTreeNode root) {

        System.out.println(root.getChildCount());
        PasswordItem items[] = new PasswordItem[root.getChildCount()];

        int index = 0;
        for (Enumeration<?> e = root.breadthFirstEnumeration(); e.hasMoreElements();) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            Object userObject = node.getUserObject();
            if (userObject instanceof String) {
                System.out.println("Found String userObject: [" + userObject + "]");
                continue;
            }
            if (userObject instanceof PasswordItem) {
                PasswordItem item = (PasswordItem) userObject;
                System.out.println("Found PasswordItem with title: [" + item.ttl + "]");
                items[index] = item;
                index++;
            }
        }
        System.out.println("Found [" + String.valueOf(index) + "] PasswordItems");

        // return the root node
        PasswordCollection passwordCollection = new PasswordCollection();
        passwordCollection.items = items;
        passwordCollection.version = "1.0";
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
        if (node == null)
            return;

        // retrieve the node that was selected
        Object nodeInfo = node.getUserObject();
        PasswordItem passwordItem = (PasswordItem) nodeInfo;

        // update the detail panel
        mTitleTextField.setText(passwordItem.ttl);
        mDescriptionTextField.setText(passwordItem.dsc);
        mUsernameTextField.setText(passwordItem.usr);
        mPasswordTextField.setText(passwordItem.pwd);
    }

    /**
     * Inner Classes
     * -------------------------------------------------------------------------
     */

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

    class PasswordCollection {
        private String title = ""; // title
        private String version = ""; // file version
        private PasswordItem[] items = null; // password items

        PasswordCollection() {
        } // no-args constructor
    }

    /**
     * Static Properties
     * -------------------------------------------------------------------------
     */

    private static final int SEARCH_DIRECTION_FORWARD = 1;
    private static final int SEARCH_DIRECTION_BACKWARD = 2;

    private static final int TEXTFIELD_HEIGHT = 20;
    private static final int INNER_PAD = 5;

    /**
     * Static Init & Main
     * -------------------------------------------------------------------------
     */

    /**
     * Static Methods
     * -------------------------------------------------------------------------
     */

} // END PUBLIC CLASS