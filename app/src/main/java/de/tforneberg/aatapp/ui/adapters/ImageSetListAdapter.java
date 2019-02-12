package de.tforneberg.aatapp.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.View;
import android.view.ViewGroup;

import de.tforneberg.aatapp.R;

/**
 * Extends the TextItemListAdapter with check boxes.
 */
public class ImageSetListAdapter extends TextItemListAdapter {

    public ImageSetListAdapter(Context context, ImageSetListAdapterInterface adapterInterface, TextItemListAdapter.Action... actions) {
        super(context, adapterInterface, actions);
    }

    class ImageSetHolder extends TextViewHolder {
        private AppCompatCheckBox activeSwitch;

        ImageSetHolder(View itemView) {
            super(itemView);
            activeSwitch = itemView.findViewById(R.id.activeSwitch);
        }

        @Override
        void update(Object object) {
            super.update(object);
            ImageSetListAdapterInterface adapterInterface = (ImageSetListAdapterInterface) ImageSetListAdapter.this.adapterInterface;
            boolean stateFromModel = adapterInterface.getActiveState(object);
            activeSwitch.setChecked(stateFromModel);
            activeSwitch.setOnClickListener(v ->
                adapterInterface.onSwitchToggled(object, activeSwitch.isChecked())
            );
        }
    }

    @Override @NonNull
    public ImageSetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageSetHolder(this.inflater.inflate(R.layout.recyclerview_item_image_set, parent, false));
    }
}