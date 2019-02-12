package de.tforneberg.aatapp.logic.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import de.tforneberg.aatapp.model.Settings;

public class InstructionViewModel extends AndroidViewModel {

    private Settings settings;

    private String instruction;

    public InstructionViewModel(Application app) {
        super(app);
        this.settings = new Settings(app);

        instruction = settings.getInstructionForCurrentSettings();
    }

    public String getInstruction() {
        return instruction;
    }

    public Settings getSettings() {
        return settings;
    }
}
