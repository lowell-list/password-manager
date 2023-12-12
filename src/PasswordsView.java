import java.awt.*;
import java.awt.event.*;

/**
 * PasswordsView: A view for displaying, searching, and editing passwords.
 * Depending on the type of data, it will either display a text area or a tree
 * view.
 */
public class PasswordsView
        extends javax.swing.JComponent {

    /**
     * Types
     * -------------------------------------------------------------------------
     */

    private enum Mode {
        TEXT, TREE
    }

    /**
     * Instance Properties
     * -------------------------------------------------------------------------
     */

    private Label mSearchLabel;
    private TextField mSearchTextField;
    private TextArea mMainTextArea;
    private PasswordsTreeView mPasswordsTreeView;

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
        mPasswordsTreeView = new PasswordsTreeView();

        // setup components
        mSearchLabel.setText("Search");
        mSearchLabel.setAlignment(Label.RIGHT);
        mPasswordsTreeView.init();

        // add components
        this.add(mSearchLabel);
        this.add(mSearchTextField);
        this.add(mMainTextArea);
        this.add(mPasswordsTreeView);

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

        // set full size components
        mMainTextArea.setBounds(maiX, maiY, maiW, maiH);
        mPasswordsTreeView.setBounds(maiX, maiY, maiW, maiH);
    }

    public void setText(String text) {
        // first, try to parse it as JSON
        try {
            mPasswordsTreeView.setText(text);
            mMode = Mode.TREE;
        } catch (Exception e) {
            // if it fails, just set the text
            System.out.println("Failed to parse JSON, reverting to text view.");
            mMainTextArea.setText(text);
            mMode = Mode.TEXT;
        }

        // set visibility
        mMainTextArea.setVisible(mMode == Mode.TEXT);
        mPasswordsTreeView.setVisible(mMode == Mode.TREE);
    }

    public String getText() {
        String text;
        if (mMode == Mode.TREE) {
            text = mPasswordsTreeView.getText();
        } else {
            text = mMainTextArea.getText();
        }
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
     * Inner Classes
     * -------------------------------------------------------------------------
     */

    /**
     * Static Properties
     * -------------------------------------------------------------------------
     */

    private static final int SEARCH_DIRECTION_FORWARD = 1;
    private static final int SEARCH_DIRECTION_BACKWARD = 2;

    public static final int TEXTFIELD_HEIGHT = 20;
    public static final int INNER_PAD = 5;

    /**
     * Static Init & Main
     * -------------------------------------------------------------------------
     */

    /**
     * Static Methods
     * -------------------------------------------------------------------------
     */

    public static void layoutLabelAndField(int top, int containerWidth, Label label, TextField textField) {
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

} // END PUBLIC CLASS