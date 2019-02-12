package de.tforneberg.aatapp.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.tforneberg.aatapp.R;

/**
 * This is an adapter for the RecyclerView on which all lists in this app base upon.
 * Its constructor needs a TextItemListAdapterInterface instance. Whoever registers as this interface gets
 * notified about data changes through the respective interface callback methods.
 * <br/><br/>
 * It can also be parameterized with a List of Actions (TextItemListAdapter.Action).
 * These actions show up if the multi-select mode gets engaged. They are also submitted in the callback methods,
 * so that the listener can check which action was started and act accordingly.
 */
public class TextItemListAdapter extends RecyclerView.Adapter<TextItemListAdapter.TextViewHolder> {
    private List<?> listObjects; // Cached copy of objects
    private boolean multiSelect = false; // multi selection for delete mode
    private ArrayList<Object> selectedItems = new ArrayList<>(); // selected items for delete
    private Menu actionModeMenu;
    TextItemListAdapterInterface adapterInterface;
    LayoutInflater inflater;

    public enum Action { Delete, Rename, Copy, Custom }
    private ArrayList<Action> actions = new ArrayList<>();
    private List<String> customActionsForSingleItems = new ArrayList<>();
    private List<String> customActionsForSeveralItems = new ArrayList<>();

    TextItemListAdapter(Context context, TextItemListAdapterInterface adapterInterface,
                               Action... actions) {
        inflater = LayoutInflater.from(context);
        this.adapterInterface = adapterInterface;
        this.actions.addAll(Arrays.asList(actions));
    }

    public TextItemListAdapter(Context context, TextItemListAdapterInterface adapterInterface,
                               List<String> customActionsForSingleItems,
                               List<String> customActionsForSeveralItems,
                               Action... actions) {
        this(context, adapterInterface, actions);
        if (customActionsForSingleItems != null) {
            this.customActionsForSingleItems = customActionsForSingleItems;
        }
        if (customActionsForSeveralItems != null) {
            this.customActionsForSeveralItems = customActionsForSeveralItems;
        }
    }

    public void setListObjects(List<?> listObjects){
        this.listObjects = listObjects;
        notifyDataSetChanged();
    }

    @Override @NonNull
    public TextViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TextViewHolder(inflater.inflate(R.layout.recyclerview_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TextViewHolder holder, int position) {
        if (listObjects != null) { holder.update(listObjects.get(position)); }
    }

    @Override
    public int getItemCount() {
        return listObjects != null ? listObjects.size() : 0;
    }

    class TextViewHolder extends RecyclerView.ViewHolder {

        private final TextView textViewTitle;
        private final TextView textViewDesc;

        private AppCompatActivity activity;
        private Resources resources;
        private ViewGroup layout;

        TextViewHolder(View itemView) {
            super(itemView); // super constructor makes this.itemView = itemView
            activity = (AppCompatActivity) itemView.getContext();
            resources = activity.getResources();
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDesc = itemView.findViewById(R.id.textViewDesc);
            layout = itemView.findViewById(R.id.recyclerView_topLevelLayout);
        }

        private ActionMode.Callback actionModeCallbacks = new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                multiSelect = true;
                actionModeMenu = menu;
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) { return false; }

            @Override // called when one of the menu entries is pressed, calls the interface method
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                String title = (String) item.getTitle();
                if (title.equals(resources.getString(R.string.delete))) {
                    for (Object i : selectedItems) {
                        adapterInterface.handleListItemAction(i, Action.Delete, null);
                    }
                } else if(title.equals(resources.getString(R.string.menu_action_rename))) {
                    for (Object i : selectedItems) {
                        adapterInterface.handleListItemAction(i, Action.Rename, null);
                    }
                } else if(title.equals(resources.getString(R.string.menu_action_copy))) {
                    for (Object i : selectedItems) {
                        adapterInterface.handleListItemAction(i, Action.Copy, null);
                    }
                }

                if(selectedItems.size() == 1) {
                    for (String s : customActionsForSingleItems) {
                        if (title.equals(s)) {
                            adapterInterface.handleListItemAction(selectedItems.get(0), Action.Custom, s);
                        }
                    }
                } else if (selectedItems.size() > 1) {
                    for (String s : customActionsForSeveralItems) {
                        if (title.equals(s)) {
                            adapterInterface.handleListItemAction(selectedItems, Action.Custom, s);
                        }
                    }
                }

                mode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                multiSelect = false;
                selectedItems.clear();
                notifyDataSetChanged();
            }
        };

        void selectItem(Object item) {
            if (multiSelect) {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                    layout.setBackgroundColor(resources.getColor(R.color.colorPrimary));
                } else {
                    selectedItems.add(item);
                    layout.setBackgroundColor(resources.getColor(R.color.colorAccent2));
                }
                updateActionMenu();
            } else {
                adapterInterface.onListItemSelected(item);
            }
        }

        private void updateActionMenu() {
            if (selectedItems.size() >= 1) {
                actionModeMenu.clear();
                if (selectedItems.size() == 1) {
                    //Add menu items for actions with only apply to one list item (e.g. Rename)
                    if (actions.contains(Action.Rename)) {
                        actionModeMenu.add(R.string.menu_action_rename)
                                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    }
                    if (actions.contains(Action.Copy)) {
                        actionModeMenu.add(R.string.menu_action_copy)
                                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    }
                    for (String s : customActionsForSingleItems) {
                        actionModeMenu.add(s).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    }
                } else {
                    //Add menu items for actions only with several list items
                    for (String s : customActionsForSeveralItems) {
                        actionModeMenu.add(s).setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                    }
                }
                //Add menu items for actions with one or several list items (e.g. Delete)
                if (actions.contains(Action.Delete)) {
                    actionModeMenu.add(R.string.delete)
                            .setIcon(android.R.drawable.ic_menu_delete)
                            .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }
            } else {
                actionModeMenu.clear();
            }
        }

        void update(final Object object) {
            textViewTitle.setText(adapterInterface.getListItemTitle(object));
            textViewDesc.setText(adapterInterface.getListItemDescription(object));
            if (selectedItems.contains(object)) {
                layout.setBackgroundColor(resources.getColor(R.color.colorAccent2));
            } else {
                layout.setBackgroundColor(resources.getColor(R.color.colorPrimary));
            }
            itemView.setOnClickListener(view -> selectItem(object));
            itemView.setOnLongClickListener(view -> {
                activity.startSupportActionMode(actionModeCallbacks); //sets multiSelect = true
                selectItem(object);
                return true;
            });
        }
    }
}
