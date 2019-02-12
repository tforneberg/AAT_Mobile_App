package de.tforneberg.aatapp.ui.activities;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.viewModels.InstructionViewModel;

public class InstructionActivity extends AppCompatActivity {

    private InstructionViewModel viewModel;
    private FloatingActionButton okButton;

    private TextView instructionHeader;
    private TextView instructionText;

    private TextView textViewRoundNumber;

    private long roundNumber = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this).get(InstructionViewModel.class);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            roundNumber = extras.getInt("roundNumber");
        }

        //Set screen orientation (must happen before setContentView!)
        setRequestedOrientation(viewModel.getSettings().screenOrientationFlag);

        //Remove notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_instruction);

        okButton = findViewById(R.id.btn_ok);
        okButton.setOnClickListener(view -> finish());

        instructionHeader = findViewById(R.id.instructionHeader);

        instructionText = findViewById(R.id.instructionText);

        textViewRoundNumber = findViewById(R.id.textViewRoundNumber);

        if(roundNumber == 0) {
            //display main instruction
            instructionText.setVisibility(View.VISIBLE);
            instructionText.setText(viewModel.getInstruction());

            textViewRoundNumber.setVisibility(View.INVISIBLE);

        } else {
            //just display next round number
            textViewRoundNumber.setVisibility(View.VISIBLE);

            String roundText = getString(R.string.round)+" "+((int)roundNumber+1);
            textViewRoundNumber.setText(roundText);

            instructionHeader.setVisibility(View.INVISIBLE);
            instructionText.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        //do nothing.
    }
}
