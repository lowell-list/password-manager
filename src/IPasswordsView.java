enum SearchDirection {
    FORWARD,
    BACKWARD,
}

public interface IPasswordsView {
    public String getText();

    public void setText(String text);

    public int getSelectedIndex();

    public void searchAndSelect(String searchText, int startIndex, SearchDirection direction);

    public void reset();
}
