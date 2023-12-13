import java.awt.*;

/**
 * PasswordsTextView: A view for displaying password text data in a text field.
 */
public class PasswordsTextView
        extends TextArea
        implements IPasswordsView {

    /**
     * Types
     * -------------------------------------------------------------------------
     */

    /**
     * Instance Properties
     * -------------------------------------------------------------------------
     */

    /**
     * Instance Constructors
     * -------------------------------------------------------------------------
     */

    public PasswordsTextView(String text, int rows, int columns, int scrollbars) {
        super(text, rows, columns, scrollbars);
    }

    /**
     * Instance Methods
     * -------------------------------------------------------------------------
     */

    /**
     * Instance Methods - IPasswordsView interface
     * -------------------------------------------------------------------------
     */

    public int getSelectedIndex() {
        return this.getSelectionStart();
    }

    /**
     * Search for the current search text in the main text area, and select the
     * first matching text found.
     * Wrap if necessary.
     *
     * @param searchText The text to search for.
     * @param startIndex The index at which to start searching.
     * @param direction  A valid SEARCH_DIRECTION_ constant which indicates which
     *                   direction to search.
     */
    public void searchAndSelect(String searchText, int startIndex, SearchDirection direction) {
        String mantxt; // main text, converted to lowercase
        String schtxt; // search text, converted to lowercase
        int schidx; // search index

        // get search text and main text
        mantxt = this.getText().toLowerCase();
        schtxt = searchText.toLowerCase();

        // search for text
        if (direction == SearchDirection.FORWARD) {
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
            this.setSelectionStart(schidx);
            this.setSelectionEnd(schidx + schtxt.length());
        }
    }

    public void reset() {
        this.setCaretPosition(0);
    }

    /**
     * Inner Classes
     * -------------------------------------------------------------------------
     */

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

} // END PUBLIC CLASS