package de.tforneberg.aatapp.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import de.tforneberg.aatapp.R;

/**
 * Extends the TextItemListAdapter with images.
 */
public class ImageItemListAdapter extends TextItemListAdapter {

    public ImageItemListAdapter(Context context, TextItemListAdapterInterface adapterInterface, List<String> customActionsForSingleItems, List<String> customActionsForSeveralItems, Action... actions) {
        super(context, adapterInterface, customActionsForSingleItems, customActionsForSeveralItems, actions);
    }

    class ImageItemHolder extends TextViewHolder {
        private final ImageView imageView;

        ImageItemHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }

        @Override
        void update(Object object) {
            super.update(object);
            Glide.with(imageView.getContext())
                    .load(((ImageItemListAdapterInterface)adapterInterface).getListItemImageFile(object))
                    .into(imageView);
        }

    }

    @Override @NonNull
    public ImageItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageItemHolder(this.inflater.inflate(R.layout.recyclerview_item_with_image, parent, false));
    }
}
