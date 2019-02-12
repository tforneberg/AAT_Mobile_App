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
 * The PushPullGestureProvider handles the recognition of move gestures for the gesture mode "Move device".
 * It uses the acceleration sensor. After initialization, it needs to get started by calling its start()-method.
 * Also, it needs to be stopped again (unregisters it as sensor listener) when it is not needed, anymore. This is done by calling stop().
 * <br/><br/>
 * If it detects a gesture, it calls viewModel.getGestureListener().userReacted(...).
 */
public class PushPullGestureProvider implements GestureProvider, SensorEventListener {

    final static private int samplingPeriodInMicroSeconds = 5000; //every 5000 microsends (5 ms)
    final static private float filterFrequency = 1.f / samplingPeriodInMicroSeconds;
    final static private float filterTimeConstant = filterFrequency * 4;
    final static private float filterAlpha = filterTimeConstant / (filterTimeConstant + filterFrequency);
    final static private float noiseThreshold = 0.9f;
    final static private int formerValuesToRemember = 200;
    final static private int valuesToPrintForDebugging = 30;
    final static private int valuesUsedToGetAverage = 10;
    final static private long oneMillion = 1000000;

    private boolean started = false;
    private long startTime;
    private float gravity = 0f;

    private SensorManager sensorManager;
    private Sensor accSensor;

    private ArrayList<Float> formerValues = new ArrayList<>(formerValuesToRemember);
    private ArrayList<Long> formerValueTimes = new ArrayList<>(formerValuesToRemember);

    private long timeSinceLastSensorValue;
    private long sensorValueLastTime;

    private long lastPositivePeak;
    private long lastNegativePeak;

    private GestureListener listener;
    private SessionRunningViewModel viewModel;

    private String sensorDescription = "";

    public PushPullGestureProvider(SessionRunningViewModel viewModel)  {
        this.viewModel = viewModel;
        listener = viewModel.getGestureListener();

        initSensors(listener.getContext());
    }

    private void initSensors(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (accSensor == null) {
            sensorDescription = "No acc sensor at all!";
        }
    }

    public String getSensorDescription() {
        return sensorDescription;
    }

    @Override
    public void start(long startTime) {
        if(accSensor != null) {
            resetMeasurementValues();
            this.startTime = startTime;
            started = true;
            registerListener();
        }
    }

    @Override
    public void stop() {
        started = false;
        sensorManager.unregisterListener(this);
    }

    private void resetMeasurementValues() {
        lastPositivePeak = lastNegativePeak = 0;
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
    }

    private void registerListener() {
        //last argument is time between events in microseconds (1000 microseconds = 1 millisecond)
        sensorManager.registerListener(this, accSensor, samplingPeriodInMicroSeconds);
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) { }

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (started) {
            //handle value in z direction, since this is the relevant axis for this application
            handleNewSensorValue(event.values[2]);
        }
    }

    private void handleNewSensorValue(float val) {
        //filter out gravity
        //finds gravity as constant value with low-pass-filter and subtracts it from the signal)
        gravity = filterAlpha * gravity + (1 - filterAlpha) * val;
        val = val - gravity;

        //filter out noise with threshold values
        if (val > -noiseThreshold && val < noiseThreshold) {
            val = 0;
        }

        //get current Time, calculate times since last sensorValue from it
        long currTimeNano = System.nanoTime(); //used for calculating firstReactionTime, later
        long currTimeMilli = currTimeNano / oneMillion;                 //used for debugging atm, only
        timeSinceLastSensorValue = currTimeMilli - sensorValueLastTime; //used for debugging atm, only
        sensorValueLastTime = currTimeMilli;                            //used for debugging atm, only

        //throw away oldest value and add the new one instead
        formerValues.remove(0);
        formerValues.add(val);
        formerValueTimes.remove(0);
        formerValueTimes.add(currTimeNano);

        //generate the sum of the last values
        float summedValue = 0;
        for(int i = formerValues.size() - 1; i > Math.max(0, formerValues.size() - valuesUsedToGetAverage); i--) {
            summedValue += formerValues.get(i);
        }

        if (summedValue > 3.1*noiseThreshold*valuesUsedToGetAverage) {
            //positive peak detected, remember the timestamp!
            if (Long.compare(lastPositivePeak, 0) == 0) { //no lastPositivePeak saved yet, save its timestamp
                lastPositivePeak = formerValueTimes.get(formerValues.size() - 1);
                //printDebugString(null);
            }
        } else if (summedValue < -2.9*noiseThreshold*valuesUsedToGetAverage) {
            //negative peak detected, remember the timestamp!
            if (Long.compare(lastNegativePeak, 0) == 0) { //no lastNegativePeak saved yet, save its timestamp
                lastNegativePeak = formerValueTimes.get(formerValues.size() - 1);
                //printDebugString(null);
            }
        }

        if (lastPositivePeak != 0 && lastNegativePeak != 0) {
            if (lastNegativePeak > lastPositivePeak) {
                //positive peak, then negative peak -> possible push movement detected
                if ((lastNegativePeak - lastPositivePeak) / oneMillion < 500) {
                    //time difference of maximum 500ms between the negative and positive peak
                    //-->reaction!
                    userReacted(true, summedValue);
                } else {
                    //time difference > 500ms --> too long, remove the peaks
                    lastPositivePeak = lastNegativePeak = 0;
                }
            } else if (lastPositivePeak > lastNegativePeak) {
                //negative peak, then positive peak -> possible pull movement detected
                if ((lastPositivePeak - lastNegativePeak) / oneMillion < 500) {
                    //time difference of maximum 500ms between the negative and positive peak
                    //-->reaction!
                    userReacted(false, summedValue);
                } else {
                    //time difference > 500ms --> too long, remove the peaks
                    lastPositivePeak = lastNegativePeak = 0;
                }
            }
        }
    }

    /**
     * Returns the timestamp of the first acceleration peak.
     */
    private Long getFirstReactionTimeStamp(boolean push) {
        if (push) {
            return lastPositivePeak;
        } else {
            return lastNegativePeak;
        }
    }

    private Boolean userReacted(boolean push, float value) {
        Boolean correctReaction = viewModel.getCurrentImage().getPush() == push;
        if (correctReaction) {
            long finalReactionTimeStamp = System.nanoTime();
            long finalReactionTime = (finalReactionTimeStamp - startTime) / oneMillion;
            if (finalReactionTime > 150) { //filters out events with times smaller 150ms
                long firstReactionTimeStamp = getFirstReactionTimeStamp(push);
                listener.userReacted(push, value, finalReactionTimeStamp, firstReactionTimeStamp);
                //printDebugString(true);
                stop();
            }
        } else {
            //printDebugString(false);
        }
        resetMeasurementValues();
        return correctReaction;
    }

    private void printDebugString(Boolean wasCorrect) {
        String wasCorrectString = "";
        if (wasCorrect != null) {
            wasCorrectString = wasCorrect ? "Correct reaction! " : "Wrong reaction! ";
        }
        StringBuilder debugString = new StringBuilder(wasCorrectString
                + "Time since last sensor value: " + timeSinceLastSensorValue + "ms, SensorValues: "
        );
        for (int i = Math.max(0, formerValues.size() - (valuesToPrintForDebugging + 1)); i < formerValues.size(); i++) {
            String valueAsString = Float.toString(formerValues.get(i));
            debugString.append(valueAsString.substring(0, Math.min(5, valueAsString.length() - 1)));
            if (i != formerValues.size() - 1) {
                debugString.append(", ");
            }
        }
        System.out.println(debugString);
    }
}
