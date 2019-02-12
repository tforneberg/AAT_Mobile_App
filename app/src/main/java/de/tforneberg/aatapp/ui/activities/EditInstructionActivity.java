package de.tforneberg.aatapp.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.Objects;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.viewModels.EditInstructionViewModel;

public class EditInstructionActivity extends AppCompatActivity {

    EditText textField;
    EditInstructionViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_instruction);

        setSupportActionBar(findViewById(R.id.toolbar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        viewModel = ViewModelProviders.of(this).get(EditInstructionViewModel.class);

        textField = findViewById(R.id.instructionText);
        textField.setText(viewModel.getInstruction());

        textField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setInstruction(s.toString());
            }
        });
    }

    @Override
    protected void onPause() {
        viewModel.saveInstruction();
        super.onPause();
    }
}
