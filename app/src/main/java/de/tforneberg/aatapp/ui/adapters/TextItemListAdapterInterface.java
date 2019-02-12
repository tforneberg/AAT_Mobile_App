package de.tforneberg.aatapp.ui.adapters;

public interface TextItemListAdapterInterface {
    void onListItemSelected(Object item);

    void handleListItemAction(Object item, TextItemListAdapter.Action action, String customActionName);

    String getListItemTitle(Object item);

    String getListItemDescription(Object item);
}
