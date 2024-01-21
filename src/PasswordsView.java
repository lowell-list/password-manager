import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PasswordsView: A view for displaying, searching, and editing passwords.
 * Depending on the type of data, it will either display a text area or a tree
 * view.
 */
public class PasswordsView
    extends javax.swing.JComponent implements ModifiedObserver {

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

  private Button mTextTreeToggleButton;
  private Label mFindLabel;
  private TextField mFindTextField;
  private PasswordsTextView mMainTextArea;
  private PasswordsTreeView mPasswordsTreeView;

  private boolean mInitialized = false;
  private ViewMode mViewMode = ViewMode.TEXT;
  private FindMode mFindMode = FindMode.SEARCH;
  private List<ModifiedObserver> mObservers = new ArrayList<>();

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
    mTextTreeToggleButton = new Button();
    mFindLabel = new Label();
    mFindTextField = new TextField();
    mMainTextArea = new PasswordsTextView("", 0, 0, TextArea.SCROLLBARS_VERTICAL_ONLY);
    mPasswordsTreeView = new PasswordsTreeView();

    // setup components
    mTextTreeToggleButton.setLabel(getTextTreeToggleButtonLabelText());
    mFindLabel.setText(getFindLabelText());
    mFindLabel.setAlignment(Label.RIGHT);
    mPasswordsTreeView.init();

    // add components
    this.add(mTextTreeToggleButton);
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
    mTextTreeToggleButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        onTextTreeToggleButtonAction(evt);
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
    mPasswordsTreeView.addModifiedObserver(this);
    mMainTextArea.addTextListener(new TextListener() {
      public void textValueChanged(TextEvent evt) {
        onModified(getText().hashCode());
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

    // text/tree toggle button (top right)
    mTextTreeToggleButton.setBounds(ctrsiz.width - 70, 0, 70, 20);

    // search label and field (top)
    layoutLabelAndField(0, ctrsiz.width - (70 + INNER_PAD), mFindLabel, mFindTextField);
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

  /**
   * Set the passwords view text. If it can be parsed as JSON, then auto-display
   * the tree view. Otherwise, fallback to plain text view.
   */
  public void setText(String text) {
    setText(text, ViewMode.TREE);
  }

  /**
   * Set the passwords view text, but try to use the given view mode.
   */
  private void setText(String text, ViewMode desriedViewMode) {
    if (desriedViewMode == ViewMode.TREE) {
      try {
        // attempt to parse JSON and use tree view
        mPasswordsTreeView.setText(text);
        setViewMode(ViewMode.TREE);
        return; // done
      } catch (Exception e) {
        // ignore and fall through to text mode
      }
    }

    // use text view mode by default
    mMainTextArea.setText(text);
    setViewMode(ViewMode.TEXT);
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
    setFindMode(mViewMode == ViewMode.TEXT ? FindMode.SEARCH : FindMode.FILTER);
    updateTextTreeToggleButtonLabel();
  }

  /**
   * Instance Methods - Text / JSON view toggle button
   * ---------------------------------------------------------------------------
   */

  private String getTextTreeToggleButtonLabelText() {
    // the button label
    return mViewMode == ViewMode.TEXT ? "Tree" : "Text";
  }

  private void updateTextTreeToggleButtonLabel() {
    mTextTreeToggleButton.setLabel(getTextTreeToggleButtonLabelText());

  }

  private void onTextTreeToggleButtonAction(ActionEvent evt) {
    String text = getText();
    setText(text, mViewMode == ViewMode.TEXT ? ViewMode.TREE : ViewMode.TEXT);
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
   * Instance Methods - Observers
   * ---------------------------------------------------------------------------
   */

  public void addModifiedObserver(ModifiedObserver observer) {
    mObservers.add(observer);
  }

  public void removeModifiedObserver(ModifiedObserver observer) {
    mObservers.remove(observer);
  }

  public void onModified(int hashCode) {
    // pass it on to our own observers
    for (ModifiedObserver observer : mObservers) {
      observer.onModified(hashCode);
    }
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