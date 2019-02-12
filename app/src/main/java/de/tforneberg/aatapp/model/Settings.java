package de.tforneberg.aatapp.model;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import de.tforneberg.aatapp.R;

/**
 * Objects of this class are a representation of the saved app settings (in the SharedPreferences) at the time of instantiation.
 * When created, an object of this class gets instantiated with the currently saved app settings.
 * After this, it stays like that.
 * Modifications of the settings (the SharedPreferences) don't lead to updates of objects of this class. Therefore,
 * it should only be used temporarily and as long as the user can not change the settings. This
 * is for example the case during the AAT session execution or for exporting the current settings.
 * <br><br>
 * There are methods for exporting the current object (exportSettings(app)) and for importing settings
 * from a file (importSettings(app, uri)). The latter one is a static method that tries to
 * create a valid Settings object by de-serializing the file from the given uri.
 * If successful, it then converts the fields of the crated object into actual persistent settings.
 */
public class Settings implements Serializable {

    static final long serialVersionUID = -7588980448693010399L;

    public long userId;

    public String gestureMode;
    public boolean isLandscape;
    public int screenOrientationFlag;
    public boolean hasColoredBorder;
    public String borderColorPush;
    public String borderColorPushName;
    public int borderColorPushInt;
    public String borderColorPull;
    public String borderColorPullName;
    public int borderColorPullInt;
    public boolean hasRotationAngle;
    public int rotationAnglePush;
    public int rotationAnglePull;
    public int rounds;
    public int percentPullImages[];
    public int imagesPerRound;
    public long timeBetweenImages;
    public ResultMessageType resultMessageType;
    public boolean showInstructions;
    public String instruction;

    public enum ResultMessageType {Toast, Dialog, None}

    public Settings(Application app) {
        //get shared preferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);

        userId = prefs.getLong("user", -1);

        //get border color preferences
        hasColoredBorder = prefs.getBoolean("colored_borders", true);
        borderColorPull = prefs.getString("colored_borders_color_pull", "#33691e");
        borderColorPullName = getColorNameFromFex(app, borderColorPull);
        borderColorPullInt = Color.parseColor(borderColorPull);
        borderColorPush = prefs.getString("colored_borders_color_push", "#d50000");
        borderColorPushName = getColorNameFromFex(app, borderColorPush);
        borderColorPushInt = Color.parseColor(borderColorPush);

        //get rotation angle preferences
        hasRotationAngle = prefs.getBoolean("rotation", false);
        String rotationPullString = Objects.requireNonNull(prefs.getString("rotation_pull", "+25°"));
        rotationAnglePull = Integer.parseInt(rotationPullString.replace("°", ""));
        String rotationPushString = Objects.requireNonNull(prefs.getString("rotation_push", "-25°"));
        rotationAnglePush = Integer.parseInt(rotationPushString.replace("°", ""));

        //get screen orientation preference
        String screenOrientation = Objects.requireNonNull(prefs.getString("screen_orientation", "Portrait"));
        isLandscape = screenOrientation.equals(("Landscape"));

        if (screenOrientation.equals("Portrait")) {
            screenOrientationFlag = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        } else {
            screenOrientationFlag = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        }

        //get gesture mode preference
        String gestureModeDefault = app.getString(R.string.gestureMode_pinch_val);
        gestureMode = prefs.getString("gesture_mode", gestureModeDefault);

        //get images per round preference
        String imagesPerRoundDefault = app.getString(R.string.prefImagesPerRound_default);
        imagesPerRound = Integer.parseInt(Objects.requireNonNull(prefs.getString("imagesPerRound", imagesPerRoundDefault)));

        //get pause duration preference
        String pauseDurationDefault = app.getString(R.string.pref_timeBetweenImagesDefault);
        float pauseDurationS = Float.parseFloat(Objects.requireNonNull(prefs.getString("timeBetweenImages", pauseDurationDefault)));
        timeBetweenImages = (long) pauseDurationS * 1000;

        //get rounds preference
        rounds = Integer.parseInt(Objects.requireNonNull(prefs.getString("amountOfRounds", "1")));

        //get message preference
        String resultMessageStringDefault = app.getString(R.string.pref_messageAfterEachImageValueNone);
        String resultMessageString = prefs.getString("messageAfterEachImage", resultMessageStringDefault);
        if (resultMessageString != null && resultMessageString.equals(app.getString(R.string.pref_messageAfterEachImageValueNone))) {
            resultMessageType = ResultMessageType.None;
        } else if (resultMessageString != null && resultMessageString.equals(app.getString(R.string.pref_messageAfterEachImageValueToast))) {
            resultMessageType = ResultMessageType.Toast;
        } else if (resultMessageString != null && resultMessageString.equals(app.getString(R.string.pref_messageAfterEachImageValueDialog))) {
            resultMessageType = ResultMessageType.Dialog;
        }

        //get push / pull ratio for each round
        percentPullImages = new int[rounds];
        for (int i = 0; i < rounds; i++) {
            int j = i + 1;
            String s = Objects.requireNonNull(prefs.getString("ratio_push_pull_round"+j, "50"));
            percentPullImages[i] = Integer.parseInt(s);
        }

        //get boolean showInstructions
        showInstructions = prefs.getBoolean("show_instructions", true);

        //get instruction text
        instruction = prefs.getString("instruction", app.getResources().getString(R.string.prefInstructionText_default));
    }

    public String getInstructionForCurrentSettings() {
        //define regular expressions
        String lineBreak = "(\\u000D\\u000A|[\\u000A\\u000B\\u000C\\u000D\\u0085\\u2028\\u2029])";
        String arbitraryContent = "(.|"+lineBreak+")*?";
        String threeOrMoreLineBreaks = "("+lineBreak+"(\\s*)){3,}?";

        //This is save bc. Strings are immutable, therefore a new String-obj is created and assigned
        //to modifiedInstruction (but not to instruction) as soon as modifiedInstruction gets modified.
        String modifiedInstruction = instruction;

        if(gestureMode.equals("pinch")) {
            //remove surrounding delcaration
            modifiedInstruction = modifiedInstruction.replaceAll("\\$ifGestureModePinchZoom\\(", "");
            modifiedInstruction = modifiedInstruction.replaceAll("\\)\\$endIfGestureModePinchZoom", "");
        } else {
            //remove specific content and its surrounding declaration
            modifiedInstruction = modifiedInstruction.replaceAll(
                    "\\$ifGestureModePinchZoom\\("+arbitraryContent+"\\)\\$endIfGestureModePinchZoom", ""
            );
        }

        if(gestureMode.equals("move")) {
            //remove surrounding delcaration
            modifiedInstruction = modifiedInstruction.replaceAll("\\$ifGestureModeMove\\(", "");
            modifiedInstruction = modifiedInstruction.replaceAll("\\)\\$endIfGestureModeMove", "");
        } else {
            //remove specific content and its surrounding declaration
            modifiedInstruction = modifiedInstruction.replaceAll(
                    "\\$ifGestureModeMove\\("+arbitraryContent+"\\)\\$endIfGestureModeMove", ""
            );
        }

        if(gestureMode.equals("angle")) {
            //remove surrounding delcaration
            modifiedInstruction = modifiedInstruction.replaceAll("\\$ifGestureModeAngle\\(", "");
            modifiedInstruction = modifiedInstruction.replaceAll("\\)\\$endIfGestureModeAngle", "");
        } else {
            //remove specific content and its surrounding declaration
            modifiedInstruction = modifiedInstruction.replaceAll(
                    "\\$ifGestureModeAngle\\("+arbitraryContent+"\\)\\$endIfGestureModeAngle", ""
            );
        }

        if(hasColoredBorder && !hasRotationAngle) {
            //remove surrounding delcaration
            modifiedInstruction = modifiedInstruction.replaceAll("\\$ifColoredBorder\\(", "");
            modifiedInstruction = modifiedInstruction.replaceAll("\\)\\$endIfColoredBorder", "");
        } else {
            //remove specific content and its surrounding declaration
            modifiedInstruction = modifiedInstruction.replaceAll(
                    "\\$ifColoredBorder\\("+arbitraryContent+"\\)\\$endIfColoredBorder", ""
            );
        }

        if(hasRotationAngle && !hasColoredBorder) {
            //remove surrounding delcaration
            modifiedInstruction = modifiedInstruction.replaceAll("\\$ifRotationAngle\\(", "");
            modifiedInstruction = modifiedInstruction.replaceAll("\\)\\$endIfRotationAngle", "");
        } else {
            //remove specific content and its surrounding declaration
            modifiedInstruction = modifiedInstruction.replaceAll(
                    "\\$ifRotationAngle\\("+arbitraryContent+"\\)\\$endIfRotationAngle", ""
            );
        }

        if(hasRotationAngle && hasColoredBorder) {
            //remove surrounding delcaration
            modifiedInstruction = modifiedInstruction.replaceAll("\\$ifRotationAndColor\\(", "");
            modifiedInstruction = modifiedInstruction.replaceAll("\\)\\$endIfRotationAndColor", "");
        } else {
            //remove specific content and its surrounding declaration
            modifiedInstruction = modifiedInstruction.replaceAll(
                    "\\$ifRotationAndColor\\("+arbitraryContent+"\\)\\$endIfRotationAndColor", ""
            );
        }

        if(!hasRotationAngle && !hasColoredBorder) {
            //remove surrounding delcaration
            modifiedInstruction = modifiedInstruction.replaceAll("\\$ifDirectAAT\\(", "");
            modifiedInstruction = modifiedInstruction.replaceAll("\\)\\$endIfDirectAAT", "");
        } else {
            //remove specific content and its surrounding declaration
            modifiedInstruction = modifiedInstruction.replaceAll(
                    "\\$ifDirectAAT\\("+arbitraryContent+"\\)\\$endIfDirectAAT", ""
            );
        }

        if(rounds > 1) {
            //remove surrounding delcaration
            modifiedInstruction = modifiedInstruction.replaceAll("\\$ifSeveralRounds\\(", "");
            modifiedInstruction = modifiedInstruction.replaceAll("\\)\\$endIfSeveralRounds", "");
        } else {
            //remove specific content and its surrounding declaration
            modifiedInstruction = modifiedInstruction.replaceAll(
                    "\\$ifSeveralRounds\\("+arbitraryContent+"\\)\\$endIfSeveralRounds", ""
            );
        }

        if(rounds == 1) {
            //remove surrounding delcaration
            modifiedInstruction = modifiedInstruction.replaceAll("\\$ifOneRound\\(", "");
            modifiedInstruction = modifiedInstruction.replaceAll("\\)\\$endIfOneRound", "");
        } else {
            //remove specific content and its surrounding declaration
            modifiedInstruction = modifiedInstruction.replaceAll(
                    "\\$ifOneRound\\("+arbitraryContent+"\\)\\$endIfOneRound", ""
            );
        }

        //replace the placeholder-variables with the correct values
        modifiedInstruction = modifiedInstruction.replace("$gestureMode", gestureMode)
                .replace("$borderColorPush", borderColorPushName != null ? borderColorPushName : "")
                .replace("$borderColorPull", borderColorPullName != null ? borderColorPullName : "")
                .replace("$rotationAnglePush", rotationAnglePush > 0 ?
                        "+"+String.valueOf(rotationAnglePush) : String.valueOf(rotationAnglePush))
                .replace("$rotationAnglePull", rotationAnglePull > 0 ?
                        "+"+String.valueOf(rotationAnglePull) : String.valueOf(rotationAnglePull))
                .replace("$rounds", String.valueOf(rounds))
                .replace("$images", String.valueOf(imagesPerRound))
                .replace("$timeBetweenImages", String.valueOf(timeBetweenImages));
        for (int i = 0; i < percentPullImages.length; i++) {
            String string = "$round"+(i+1)+"PercentPull";
            String string2 = "$round"+(i+1)+"PercentPush";
            modifiedInstruction = modifiedInstruction.replace(string, String.valueOf(percentPullImages[i]))
                    .replace(string2, String.valueOf(100 - percentPullImages[i]));
        }

        //replace more than three line breaks with two line breaks (removes big gaps between paragraphs)
        modifiedInstruction = modifiedInstruction.replaceAll(threeOrMoreLineBreaks, "\n\n");

        return modifiedInstruction;
    }

    public static String getColorNameFromFex(Context context, String hex) {
        Resources resources = context.getResources();

        String[] colorNames = resources.getStringArray(R.array.coloredBorders);
        String[] colorHexValues = resources.getStringArray(R.array.coloredBordersValues);

        for (int i = 0; i < colorHexValues.length; i++) {
            if (colorHexValues[i].equals(hex)) {
                return colorNames[i];
            }
        }

        return null;
    }

    public Uri exportSettings(Application app) {
        //Create file object with correct name and path
        String filename = "aat_app_settings.ser";
        File dir = new File(app.getCacheDir(), "exportedSettings");
        if (!dir.exists()) { dir.mkdir(); }
        File file = new File(dir, filename);

        try {
            //Create object output stream on the newly created file
            FileOutputStream fos = new FileOutputStream (file);
            ObjectOutputStream oos = new ObjectOutputStream (fos);

            //serialize THIS object to the file
            oos.writeObject(this);
        } catch (IOException e) {
            return null;
        }

        //offer file URI
        return FileProvider.getUriForFile(app.getApplicationContext(), "de.tforneberg.aatapp.fileprovider", file);
    }

    public static boolean importSettings(Application app, Uri uri, boolean importGestureMode) {
        boolean result;
        try {
            //get settings object from Uri
            FileInputStream fis = (FileInputStream) app.getContentResolver().openInputStream(uri);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Settings settings = (Settings) ois.readObject();
            result = importSettings(app, settings, importGestureMode);
        } catch (IOException | ClassNotFoundException | ClassCastException e) {
            if (e.getMessage() != null) {
                Log.println(Log.ERROR, "SettingsImport", e.getMessage());
            } else {
                Log.println(Log.ERROR, "SettingsImport", "Settings import failed");
            }
            result = false;
        }
        return result;
    }

    @SuppressLint("ApplySharedPref")
    public static boolean importSettings(Application app, Settings settings, boolean importGestureMode) {
        //get shared prefs editor
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
        SharedPreferences.Editor prefsEditor = prefs.edit();

        //write fields of imported settings object to preferences
        if (importGestureMode) {
            SensorManager sensorManager = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
            Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            Sensor geomagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            boolean gestureModeAngleAvailable = false;
            boolean gestureModeMoveAvailable = false;

            //check if all sensors for AngleGestureMode are available on the current device.
            if (accelerometer != null && geomagneticSensor != null) {
                gestureModeAngleAvailable = true;
            }
            //check if accelerometer is available for Move gesture mode. If not, don't add it as an option
            if (accelerometer != null) {
                gestureModeMoveAvailable = true;
            }

            //only if the new mode is available on this device, import it
            if ((settings.gestureMode.equals("angle") && gestureModeAngleAvailable)
                || (settings.gestureMode.equals("move") && gestureModeMoveAvailable)) {
                prefsEditor.putString("gesture_mode", settings.gestureMode);
            }
        }
        prefsEditor.putBoolean("colored_borders", settings.hasColoredBorder);
        prefsEditor.putString("colored_borders_color_pull", settings.borderColorPull);
        prefsEditor.putString("colored_borders_color_push", settings.borderColorPush);
        prefsEditor.putBoolean("rotation", settings.hasRotationAngle);
        String stringRotPull = String.valueOf(settings.rotationAnglePull)+"°";
        stringRotPull = settings.rotationAnglePull > 0 ? "+"+stringRotPull : stringRotPull;
        prefsEditor.putString("rotation_pull", stringRotPull);
        String stringRotPush = String.valueOf(settings.rotationAnglePush)+"°";
        stringRotPush = settings.rotationAnglePush > 0 ? "+"+stringRotPush : stringRotPush;
        prefsEditor.putString("rotation_push", stringRotPush);
        prefsEditor.putString("screen_orientation", settings.isLandscape ? "Landscape" : "Portrait");
        prefsEditor.putString("imagesPerRound", String.valueOf(settings.imagesPerRound));
        prefsEditor.putString("timeBetweenImages", String.valueOf(settings.timeBetweenImages/1000));
        prefsEditor.putString("amountOfRounds", String.valueOf(settings.rounds));

        String resultMessageType = settings.resultMessageType.toString();
        if (resultMessageType.equals(ResultMessageType.Dialog.toString())) {
            prefsEditor.putString("messageAfterEachImage", app.getString(R.string.pref_messageAfterEachImageValueDialog));
        } else if (resultMessageType.equals(ResultMessageType.Toast.toString())) {
            prefsEditor.putString("messageAfterEachImage", app.getString(R.string.pref_messageAfterEachImageValueToast));
        } else if (resultMessageType.equals(ResultMessageType.None.toString())) {
            prefsEditor.putString("messageAfterEachImage", app.getString(R.string.pref_messageAfterEachImageValueNone));
        }

        for (int i = 0; i < settings.percentPullImages.length; i++) {
            int j = i + 1;
            prefsEditor.putString("ratio_push_pull_round"+j, String.valueOf(settings.percentPullImages[i]));
        }
        prefsEditor.putBoolean("show_instructions", settings.showInstructions);
        prefsEditor.putString("instruction", settings.instruction);
        prefsEditor.commit();

        return true;
    }

}
