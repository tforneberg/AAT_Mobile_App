package de.tforneberg.aatapp.ui.dialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;

import de.tforneberg.aatapp.R;

/**
 * Implementation of a text input dialog using a EditText view. You can customize the title and hint strings
 * and provide a text length for the EditText.
 * Usage: <br>
 * TextInputDialog dialog = TextInputDialog.create(context, title, hint, textLength);<br>
 * dialog.setPositiveButton(...) <br>
 * dialog.setNegativeButton(...) <br>
 * dialog.show();<br>
 * <br>
 * To retrieve the entered text, call dialog.getInput().
 */
public class TextInputDialog extends AlertDialog.Builder {

    private EditText text;

    private TextInputDialog(@NonNull Context context) {
        super(context);
        text = new EditText(context);
    }

    public EditText getEditText() {
        return this.text;
    }

    public String getInput() {
        return this.text.getText().toString();
    }

    private static void setupDialog(TextInputDialog dialog, Context context, int textLength) {
        FrameLayout container = new FrameLayout(context);
        FrameLayout.LayoutParams params = new  FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.leftMargin = params.rightMargin = context.getResources().getDimensionPixelSize(R.dimen.dialog_margin);
        dialog.text.setLayoutParams(params);
        container.addView(dialog.text);
        dialog.text.setInputType(InputType.TYPE_CLASS_TEXT);
        dialog.text.setFilters(new InputFilter[]{new InputFilter.LengthFilter(textLength)});
        dialog.setView(container);
    }

    public static TextInputDialog create(Context context, int titleResId, int hintId, int textLength) {
        TextInputDialog dialog = new TextInputDialog(context);
        dialog.setTitle(titleResId);
        dialog.text.setHint(hintId);
        setupDialog(dialog, context, textLength);
        return dialog;
    }

    public static TextInputDialog create(Context context, int titleResId, String hint, int textLength) {
        TextInputDialog dialog = new TextInputDialog(context);
        dialog.setTitle(titleResId);
        dialog.text.setHint(hint);
        setupDialog(dialog, context, textLength);
        return dialog;
    }
    public static TextInputDialog create(Context context, String title, int hintResId, int textLength) {
        TextInputDialog dialog = new TextInputDialog(context);
        dialog.setTitle(title);
        dialog.text.setHint(hintResId);
        setupDialog(dialog, context, textLength);
        return dialog;
    }

    public static TextInputDialog create(Context context, String title, String hint, int textLength) {
        TextInputDialog dialog = new TextInputDialog(context);
        dialog.setTitle(title);
        dialog.text.setHint(hint);
        setupDialog(dialog, context, textLength);
        return dialog;
    }

}
