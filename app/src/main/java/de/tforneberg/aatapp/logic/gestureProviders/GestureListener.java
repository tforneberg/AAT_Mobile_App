package de.tforneberg.aatapp.logic.gestureProviders;

import android.content.Context;

public interface GestureListener {

    boolean userReacted(boolean push, float value, long finalReactionTime, Long... firstReactionTimestamp);

    void userStartedReacting();

    void userAbortedReacting();

    float getStartingZoom();

    float getCurrentZoom();

    void resetZoom();

    Context getContext();

    void updateTextView(String newText);
}
