enum SearchDirection {
    FORWARD,
    BACKWARD,
}

public interface IPasswordsView {
    public String getText();

    public void setText(String text);

    /**
     * Get the currently selected index, or 0 if there is no selection.
     */
    public int getSelectedIndex();

    /**
     * Search for the current search text in the passwords view, and select the
     * first matching item found.
     * Wrap if necessary.
     *
     * @param searchText The text to search for.
     * @param startIndex The index at which to start searching.
     * @param direction  A valid SearchDirection enum.
     */
    public void searchAndSelect(String searchText, int startIndex, SearchDirection direction);

    public void filter(String filterText);

    public void reset();
}
