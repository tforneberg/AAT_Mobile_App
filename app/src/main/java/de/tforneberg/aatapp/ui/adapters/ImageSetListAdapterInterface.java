package de.tforneberg.aatapp.ui.adapters;

public interface ImageSetListAdapterInterface extends TextItemListAdapterInterface {

//    Inherited:
//    void onListItemSelected(Object item);
//    void deleteListItem(Object item);
//    String getListItemTitle(Object item);
//    String getListItemDescription(Object item);

    boolean getActiveState(Object item);

    void onSwitchToggled(Object item, boolean isChecked);
}
