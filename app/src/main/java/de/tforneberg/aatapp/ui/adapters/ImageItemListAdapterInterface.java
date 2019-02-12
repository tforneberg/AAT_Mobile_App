package de.tforneberg.aatapp.ui.adapters;

import java.io.File;

public interface ImageItemListAdapterInterface extends TextItemListAdapterInterface {

//    Inherited:
//    void onListItemSelected(Object item);
//    void deleteListItem(Object item);
//    String getListItemTitle(Object item);
//    String getListItemDescription(Object item);

    File getListItemImageFile(Object item);
}
