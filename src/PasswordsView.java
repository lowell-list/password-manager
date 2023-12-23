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
   * ---------------------------------------------------------------------------
   */

  private enum ViewMode {
    TEXT, TREE
  }

  private enum FindMode {
    SEARCH, FILTER
  }

  /**
   * Instance Properties
   * ---------------------------------------------------------------------------
   */

  private Label mFindLabel;
  private TextField mFindTextField;
  private PasswordsTextView mMainTextArea;
  private PasswordsTreeView mPasswordsTreeView;

  private boolean mInitialized = false;
  private ViewMode mViewMode = ViewMode.TEXT;
  private FindMode mFindMode = FindMode.SEARCH;

  /**
   * Instance Constructors
   * ---------------------------------------------------------------------------
   */

  // constructor
  public PasswordsView() {

  }

  /**
   * Instance Methods
   * ---------------------------------------------------------------------------
   */

  public void init() {

    // instantiate components
    mFindLabel = new Label();
    mFindTextField = new TextField();
    mMainTextArea = new PasswordsTextView("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
    mPasswordsTreeView = new PasswordsTreeView();

    // setup components
    mFindLabel.setText(getFindLabelText());
    mFindLabel.setAlignment(Label.RIGHT);
    mPasswordsTreeView.init();

    // add components
    this.add(mFindLabel);
    this.add(mFindTextField);
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
    mFindLabel.addMouseListener(new MouseListener() {
      public void mouseClicked(MouseEvent evt) {
        toggleFindMode();
      }

      public void mousePressed(MouseEvent evt) {
      }

      public void mouseReleased(MouseEvent evt) {
      }

      public void mouseEntered(MouseEvent evt) {
      }

      public void mouseExited(MouseEvent evt) {
      }
    });
    mFindTextField.addKeyListener(new KeyListener() {
      public void keyTyped(KeyEvent evt) {
      }

      public void keyPressed(KeyEvent evt) {
      }

      public void keyReleased(KeyEvent evt) {
        onFindTextKeyReleased(evt);
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

    // search label and field (top)
    layoutLabelAndField(0, ctrsiz.width, mFindLabel, mFindTextField);
    Dimension schlblsiz = mFindLabel.getSize();

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
      setViewMode(ViewMode.TREE);
      setFindMode(FindMode.FILTER);
    } catch (Exception e) {
      // if it fails, just set the text
      mMainTextArea.setText(text);
      setViewMode(ViewMode.TEXT);
      setFindMode(FindMode.SEARCH);
    }
  }

  public IPasswordsView getCurrentPasswordsView() {
    return (mViewMode == ViewMode.TREE) ? mPasswordsTreeView : mMainTextArea;
  }

  public String getText() {
    return getCurrentPasswordsView().getText();
  }

  public void reset() {
    getCurrentPasswordsView().reset();
    mFindTextField.requestFocus();
  }

  private void setViewMode(ViewMode newMode) {
    mViewMode = newMode;
    mMainTextArea.setVisible(mViewMode == ViewMode.TEXT);
    mPasswordsTreeView.setVisible(mViewMode == ViewMode.TREE);
  }

  /**
   * Instance Methods - Find (Search / Filter)
   * ---------------------------------------------------------------------------
   */

  private void toggleFindMode() {
    setFindMode((mFindMode == FindMode.SEARCH) ? FindMode.FILTER : FindMode.SEARCH);
    this.reset(); // also reset any search/filter that may be in progress
  }

  private void setFindMode(FindMode newMode) {
    mFindMode = newMode;
    mFindLabel.setText(getFindLabelText());
  }

  private String getFindLabelText() {
    return mFindMode == FindMode.SEARCH ? "Search" : "Filter";
  }

  private void onFindTextKeyReleased(KeyEvent evt) {
    // get current view and "find text"
    IPasswordsView currentView = getCurrentPasswordsView();
    String findText = mFindTextField.getText();

    // if filtering, just pass it through directly
    if (mFindMode == FindMode.FILTER) {
      currentView.filter(findText);
      return;
    }

    // search
    // DEFAULT: search as the user types forward from the current selection
    int selectedIndex = currentView.getSelectedIndex();
    SearchDirection searchDirection = SearchDirection.FORWARD;
    if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
      if ((evt.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
        // SHIFT + ENTER: search for the previous occurrence
        selectedIndex = currentView.getSelectedIndex() - 1;
        searchDirection = SearchDirection.BACKWARD;
      } else {
        // ENTER: search for the next occurrence
        selectedIndex = currentView.getSelectedIndex() + 1;
        searchDirection = SearchDirection.FORWARD;
      }
    }
    currentView.searchAndSelect(findText, selectedIndex, searchDirection);
  }

  /**
   * Inner Classes
   * ---------------------------------------------------------------------------
   */

  /**
   * Static Properties
   * ---------------------------------------------------------------------------
   */

  public static final int TEXTFIELD_HEIGHT = 20;
  public static final int INNER_PAD = 5;

  /**
   * Static Init & Main
   * ---------------------------------------------------------------------------
   */

  /**
   * Static Methods
   * ---------------------------------------------------------------------------
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

} // End Public Class