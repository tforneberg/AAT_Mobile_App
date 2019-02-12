package de.tforneberg.aatapp.logic.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import de.tforneberg.aatapp.R;

public class EditInstructionViewModel extends AndroidViewModel {

    private SharedPreferences prefs;
    private String instruction;

    public EditInstructionViewModel(@NonNull Application app) {
        super(app);

        prefs = PreferenceManager.getDefaultSharedPreferences(app);
        String defaultInstruction = app.getResources().getString(R.string.prefInstructionText_default);

        instruction = prefs.getString("instruction", defaultInstruction);
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public void saveInstruction() {
        prefs.edit().putString("instruction", instruction).apply();
    }
}
