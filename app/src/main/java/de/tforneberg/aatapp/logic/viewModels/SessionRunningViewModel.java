package de.tforneberg.aatapp.logic.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.content.Context;
import android.content.res.Resources;
import android.view.MotionEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.logic.SessionDataCollector;
import de.tforneberg.aatapp.logic.gestureProviders.AngleGestureProvider;
import de.tforneberg.aatapp.logic.gestureProviders.GestureListener;
import de.tforneberg.aatapp.logic.gestureProviders.GestureProvider;
import de.tforneberg.aatapp.logic.gestureProviders.PinchZoomGestureProvider;
import de.tforneberg.aatapp.logic.gestureProviders.PushPullGestureProvider;
import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.model.ImageSet;
import de.tforneberg.aatapp.model.Settings;
import de.tforneberg.aatapp.repo.Repository;

public class SessionRunningViewModel extends AndroidViewModel {

    private Application app;
    private Repository repository;

    /**
     * A LiveData<Boolean> to which the SessionRunningActivity subscribes as an observer.
     * As soon as everything is setup and ready in the view model, its value changes to true
     * and the activity can start with the session.
     */
    private LiveData<Boolean> isReadyLiveData = new MutableLiveData<>();

    private Random randomNumGen = new Random();
    private int currentImageNumber = 0;
    private int currentRoundNumber = 0;

    private Settings settings;

    private ImageSet imageSet;
    private List<Image> allImagesInImageSet;
    private List<Image> imagesForRound;
    private Image currentImage;

    private SessionDataCollector dataCollector;
    private GestureProvider gestureProvider;
    private GestureListener gestureListener;

    private String resultMessage;

    private boolean sessionCanStart = false;
    private boolean sessionIsRunning = false;

    @SuppressWarnings("ConstantConditions")
    public SessionRunningViewModel (Application app) {
        super(app);
        this.app = app;
        repository = Repository.getInstance(app);

        settings = new Settings(app);

        setupImagesList();
        //put nothing after this, since setupImagesList() performs async operations which then
        //at some point trigger the start of the session
    }

    public void setGestureListener(GestureListener listener) {
        this.gestureListener = listener;
    }

    private void setupImagesList() {
        //get imageSet and images data from the database
        LiveData<ImageSet> liveDataImageSet = repository.IMAGES.getActiveImageSet();
        LiveData<List<Image>> liveDataImages = Transformations.switchMap(liveDataImageSet, imageSet -> {
            this.imageSet = imageSet;

            //new solution to determine if the imageSet is default image set
            if (imageSet == null || imageSet.getId() == 1) {
                //default image set, no need for further database calls
                dataCollector = new SessionDataCollector(settings, 1, app);

                setupDefaultImageSetPossibleImages();
                setupSessionRoundImages();

                ((MutableLiveData<Boolean>) isReadyLiveData).setValue(true); //Triggers activity.startSession()
                return null;
            } else {
                //not default image set, load the image data from the database first
                return repository.IMAGES.getImagesByImageSetId(imageSet.getId());
            }
        });

        //when everything is loaded from the database, do the rest of the setup and notify the activity
        isReadyLiveData = Transformations.switchMap(liveDataImages, this::setupCustomImageSetPossibleImages);
    }

    private void setupDefaultImageSetPossibleImages() {
        allImagesInImageSet = new ArrayList<>();
        for (int i = 0; i < 50; i++) {
            if (i < 10) {
                allImagesInImageSet.add(new Image(i, "default_image_pos0"+i, false));
                allImagesInImageSet.add(new Image(2*i, "default_image_smoke0"+i, true));
            } else {
                allImagesInImageSet.add(new Image(i, "default_image_pos"+i, false));
                allImagesInImageSet.add(new Image(2*i, "default_image_smoke"+i, true));
            }
        }
    }

    private MutableLiveData<Boolean> setupCustomImageSetPossibleImages(List<Image> images) {
        MutableLiveData<Boolean> newIsReadyLiveData = new MutableLiveData<>();
        if (images != null && images.size() > 0) {
            allImagesInImageSet = images;

            dataCollector = new SessionDataCollector(settings, imageSet.getId(), app);

            boolean success = setupSessionRoundImages();
            newIsReadyLiveData.setValue(success);
        } else if (imageSet.getId() == 1) {
            newIsReadyLiveData.setValue(true);
        } else {
            newIsReadyLiveData.setValue(false);
        }
        return newIsReadyLiveData;
    }

    public void initGestureProvider() {
        Resources res = app.getResources();
        if (settings.gestureMode.equals(res.getString(R.string.gestureMode_move_val))) {
            gestureProvider = new PushPullGestureProvider(this);
        } else if (settings.gestureMode.equals(res.getString(R.string.gestureMode_pinch_val))) {
            gestureProvider = new PinchZoomGestureProvider(this);
        } else if (settings.gestureMode.equals(res.getString(R.string.gestureMode_angle_val))) {
            gestureProvider = new AngleGestureProvider(this);
        }
    }

    private boolean setupSessionRoundImages() {
        imagesForRound = new ArrayList<>();

        int totalAmountGoodInImageSet = 0;
        int totalAmountBadInImageSet = 0;
        for (Image i : allImagesInImageSet) {
            if (i.getPush()) {
                ++totalAmountBadInImageSet;
            } else {
                ++totalAmountGoodInImageSet;
            }
        }
        if (totalAmountBadInImageSet == 0 | totalAmountGoodInImageSet == 0) {
            return false;
        }

        //get percentages and total amount for good / bad images
        int percentPullCurr = settings.percentPullImages[currentRoundNumber];
        int amountGood = Math.round(settings.imagesPerRound * percentPullCurr / 100);

        ArrayList<Image> addedAlreadyGood = new ArrayList<>();
        ArrayList<Image> addedAlreadyBad = new ArrayList<>();
        ArrayList<Image> tempSessionList = new ArrayList<>();

        int i = 0;
        while (i < amountGood) {
            int random = randomNumGen.nextInt(allImagesInImageSet.size());
            Image image = allImagesInImageSet.get(random);

            if ( ! image.getPush() && !addedAlreadyGood.contains(image)) {
                tempSessionList.add(image);

                addedAlreadyGood.add(image);
                if (addedAlreadyGood.size() == totalAmountGoodInImageSet) {
                    addedAlreadyGood.clear();
                }

                ++i;
            }
        }
        while (i < settings.imagesPerRound) {
            int random = randomNumGen.nextInt(allImagesInImageSet.size());
            Image image = allImagesInImageSet.get(random);

            if (image.getPush() && !addedAlreadyBad.contains(image)) {
                tempSessionList.add(image);

                addedAlreadyBad.add(image);
                if (addedAlreadyBad.size() == totalAmountBadInImageSet) {
                    addedAlreadyBad.clear();
                }

                ++i;
            }
        }

        i = 0;
        Image lastImage = null;
        int tries = 0;
        while (i < settings.imagesPerRound) {
            int random = randomNumGen.nextInt(tempSessionList.size());
            Image currImage = tempSessionList.get(random);
            if (!currImage.equals(lastImage) || tries > 2) {
                lastImage = currImage;
                tempSessionList.remove(currImage);
                imagesForRound.add(currImage);
                tries = 0;
                ++i;
            } else {
                ++tries;
            }
        }

        return true;
    }

    public GestureProvider getGestureProvider() {
        return gestureProvider;
    }

    public GestureListener getGestureListener() {
        return gestureListener;
    }

    public void stop() {
        if (gestureProvider != null) {
            gestureProvider.stop();
        }
    }

    /**
     * This method dispatches all touch events to the PinchZoomGestureProvider.
     * The PinchZoomGestureProvider then evaluates the touch gesture and decides whether to consume
     * it (=returns true) or not (=returns false). It consumes "wrong" gestures such as double tap
     * to zoom or scaling in the wrong direction (push direction on pull images and vice versa).
     * The PinchZoomGestureProvider also finds scaling (=pinching) gestures in the event data
     * and evaluates if the user successfully reacted to an image or not.
     *
     * If no PinchZoomGestureProvider is instantiated (meaning another gesture mode is active),
     * it always returns true to consume all touch events and prevent the imageView from reacting to them.
     * @param ev the MotionEvent
     * @return true if the touch event got consumed and should not be reacted on by the imageView
     */
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //catch the touch events and distribute them to the gestureProvider and the imageView
        if (gestureProvider instanceof PinchZoomGestureProvider) {
            return ((PinchZoomGestureProvider) gestureProvider).handleTouchEvent(ev);
        }

        //if no PinchZoomGestureProvider is active, always return true to consume the event
        //and deactivate all touchEvent handling by the imageView
        return true;
    }

    public boolean userReacted(boolean push, float value, long finalReactionTime, Long... firstReactionTime) {
        boolean correct = currentImage.getPush() == push;

        if (correct) {
            final long currRoundTime = dataCollector.saveImageReactionData(push, true, currentImage, finalReactionTime, firstReactionTime);

            String timeMessage = " - "+currRoundTime+" ms";
            resultMessage = app.getString(R.string.message_correct)+" "+timeMessage;

            increaseCurrentImageNumber();
        }
        return correct;
    }

    public long assignNewStartingTime() {
        long startTime = dataCollector.assignNewStartingTimeToCurrentImage();
        gestureProvider.start(startTime);
        return startTime;
    }

    public void startNewRound() {
        dataCollector.startNewRound();
        currentImageNumber = 0;
        setupSessionRoundImages();
    }

    public void saveToDb() {
        dataCollector.initDatabaseSave();
    }


    public String getResultMessage() {
        return resultMessage;
    }

    public void setNewCurrentImage() {
        currentImage = imagesForRound.get(currentImageNumber);
    }

    public File getFileFromImage(Context context, Image image) {
        return repository.IMAGES.getFileFromImage(context, image);
    }

    public int getIdFromDefaultImage(Context context, Image image) {
        return repository.IMAGES.getIdFromDefaultImage(context, image);
    }

    public int getRotationAngleForCurrentImage() {
        if (currentImage.getPush()) {
            return settings.rotationAnglePush;
        } else {
            return settings.rotationAnglePull;
        }
    }

    public long saveGestureStartedTime() {
        return dataCollector.saveImageFirstReactionTime();
    }

    public void resetGestureStartedTime() {
        dataCollector.resetImageFirstReactionTime();
    }

    public int getCurrentRoundNumber() {
        return currentRoundNumber;
    }

    public int getCurrentImageNumber() {
        return currentImageNumber;
    }

    private void increaseCurrentImageNumber() {
        this.currentImageNumber = ++currentImageNumber;
    }

    public void increaseCurrentRoundNumber() {
        this.currentRoundNumber = ++this.currentRoundNumber;
        dataCollector.increaseCurrentRound();
    }

    public Image getCurrentImage() {
        return currentImage;
    }

    public int getBorderColorForCurrentImage() {
        if (currentImage.getPush()) {
            return settings.borderColorPushInt;
        } else {
            return settings.borderColorPullInt;
        }
    }

    public ImageSet getImageSet() {
        return imageSet;
    }

    public void setImageSet(ImageSet imageSet) {
        this.imageSet = imageSet;
    }

    public LiveData<Boolean> getIsReadyLiveData() {
        return isReadyLiveData;
    }

    public Settings getSettings() { return settings; }

    public boolean getSessionCanStart() {
        return sessionCanStart;
    }

    public void setSessionCanStart(boolean sessionCanStart) {
        this.sessionCanStart = sessionCanStart;
    }

    public boolean getSessionIsRunning() {
        return sessionIsRunning;
    }

    public void setSessionIsRunning(boolean sessionIsRunning) {
        this.sessionIsRunning = sessionIsRunning;
    }
}
