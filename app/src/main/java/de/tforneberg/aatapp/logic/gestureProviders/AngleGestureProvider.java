package de.tforneberg.aatapp.logic.gestureProviders;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.Arrays;

import de.tforneberg.aatapp.logic.viewModels.SessionRunningViewModel;

/**
 * The AngleGestureProvider handles the recognition of angle gestures for the gesture mode "Angle device".
 * It uses the geomagnetic sensor and the acceleration sensor. After initialization, it needs to get started by calling its start()-method.
 * Also, it needs to be stopped again (unregisters it as a listener) when it is not needed, anymore. This is done by calling stop().
 * <br/><br/>
 * If it detects a gesture, it calls viewModel.getGestureListener().userReacted(...).
 */
public class AngleGestureProvider implements GestureProvider, SensorEventListener {
    final static private int samplingPeriodInMicroSeconds = 1000;
    final static private int formerValuesToRemember = 100;
    final static private int valuesToPrintForDebugging = 20;
    final static private long oneMillion = 1000000;

    private long timeSinceLastSensorValue;
    private long sensorValueLastTime;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private Sensor geomagneticSensor;

    private float[] accValues;
    private float[] magValues;

    private ArrayList<Float> formerValues = new ArrayList<>(formerValuesToRemember);
    private ArrayList<Long> formerValueTimes = new ArrayList<>(formerValuesToRemember);

    private boolean started = false;
    private long startTime; //value in nanoseconds

    private GestureListener listener;
    private SessionRunningViewModel viewModel;

    public AngleGestureProvider(SessionRunningViewModel viewModel) {
        this.viewModel = viewModel;
        listener = viewModel.getGestureListener();

        initSensors(listener.getContext());
    }

    private void initSensors(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        geomagneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    private void registerListener() {
        //last argument is time between events in microseconds (1000 microseconds = 1 millisecond)
        sensorManager.registerListener(this, accelerometer, samplingPeriodInMicroSeconds);
        sensorManager.registerListener(this, geomagneticSensor, samplingPeriodInMicroSeconds);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (started) {
            int sensorType = event.sensor.getType();

            if (sensorType == Sensor.TYPE_ACCELEROMETER) {
                accValues = event.values;
                handleSensorValues();
            } else if (sensorType == Sensor.TYPE_MAGNETIC_FIELD) {
                magValues = event.values;
            }
        }
    }

    private void handleSensorValues() {
        if (accValues != null && magValues != null) {
            float[] finalValues = new float[3];
            float[] rotMatrix = new float[9];
            float[] incMatrix = new float[9];

            //calculate the relevant rotation values from the sensor values
            SensorManager.getRotationMatrix(rotMatrix, incMatrix, accValues, magValues);
            float[] remapedR = new float[9];
            SensorManager.remapCoordinateSystem(rotMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapedR);
            SensorManager.getOrientation(remapedR, finalValues);

            //finalValues{1] contains information about the rotation in the relevant axis.
            //At 0° rotation, its' value is 0. At +45°, its' value is 1, at -45°, its'value is -1
            float val = finalValues[1];

            long currentTimeNS = System.nanoTime();
            long currentTimeMS = currentTimeNS / oneMillion;
            timeSinceLastSensorValue = currentTimeMS - sensorValueLastTime;
            sensorValueLastTime = currentTimeMS;

            //save current value as last value of the former values array.
            //delete the oldest former value in exchange.
            formerValues.remove(0);
            formerValues.add(val);
            formerValueTimes.remove(0);
            formerValueTimes.add(currentTimeNS);

            //prints debug string once every second
            //if (currentTimeMS - 1000 > timeSinceLastDebugPrint) {
            //    timeSinceLastDebugPrint = currentTimeMS;
            //    printDebugString(null);
            //}

            if (val > 1) {
                //pushed
                userReacted(true, finalValues[1]);
            } else if (val < -0.7) {
                //pulled
                userReacted(false, finalValues[1]);
            }
        }
    }

    private Long getFirstReactionTimeStamp(boolean push) {
        int currFirstReactionTimeIndex = formerValuesToRemember - 1;

        for(int i = formerValuesToRemember - 1; i > 1; i--) {
            float currVal = formerValues.get(i);
            float beforeVal = formerValues.get(i-1);
            if (push && currVal > 0.5 && currVal > beforeVal - 0.1) {
                currFirstReactionTimeIndex = i;
            } else if (!push && currVal < -0.375 && currVal < beforeVal + 0.05) {
                currFirstReactionTimeIndex = i;
            } else {
                break;
            }
        }
        return formerValueTimes.get(currFirstReactionTimeIndex);
    }

    private Boolean userReacted(boolean push, float value) {
        Boolean correctReaction = viewModel.getCurrentImage().getPush() == push;
        if (correctReaction) {
            long finalReactionTimeStamp = System.nanoTime();
            long finalReactionTime = (finalReactionTimeStamp - startTime) / oneMillion;
            if (finalReactionTime > 150) { //filters out events with times smaller 150ms (errors)
                stop();
                long firstReactionTimeStamp = getFirstReactionTimeStamp(push);
                listener.userReacted(push, value, finalReactionTimeStamp, firstReactionTimeStamp);
            }
        }
        //printDebugString(correctReaction);
        return correctReaction;
    }

    private void printDebugString(Boolean wasCorrect) {
        String wasCorrectString = "";
        if (wasCorrect != null) {
            wasCorrectString = wasCorrect ? "Correct reaction! " : "Wrong reaction! ";
        }
        StringBuilder debugString = new StringBuilder(wasCorrectString + "Time since last sensor value: " + timeSinceLastSensorValue + "ms, SensorValues: ");
        for (int i = Math.max(0, formerValues.size() - (valuesToPrintForDebugging + 1)); i < formerValues.size(); i++) {
            String valueAsString = Float.toString(formerValues.get(i));
            debugString.append(i).append(": ").append(valueAsString.substring(0, Math.min(5, valueAsString.length() - 1)));
            if (i != formerValues.size() - 1) {
                debugString.append(", ");
            }
        }
        System.out.println(debugString);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public void start(long startTime) {
        accValues = null;
        magValues = null;

        formerValues.clear();
        Float[] valuesArr = new Float[formerValuesToRemember];
        Long[] valueTimesArr = new Long[formerValuesToRemember];
        for (int i = 0; i < formerValuesToRemember; i++) {
            valuesArr[i] = (float) 0;
            valueTimesArr[i] = (long) 0;
        }
        formerValues.addAll(Arrays.asList(valuesArr));
        formerValueTimes.clear();
        formerValueTimes.addAll(Arrays.asList(valueTimesArr));

        this.startTime = startTime;
        sensorValueLastTime = startTime / oneMillion;

        started = true;
        registerListener();
    }

    @Override
    public void stop() {
        started = false;
        sensorManager.unregisterListener(this);
    }
}
