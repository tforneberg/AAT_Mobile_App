package de.tforneberg.aatapp.logic.viewModels;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.List;

import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.model.ImageSet;
import de.tforneberg.aatapp.model.Reaction;
import de.tforneberg.aatapp.model.Session;
import de.tforneberg.aatapp.model.SessionRound;
import de.tforneberg.aatapp.model.User;
import de.tforneberg.aatapp.repo.Repository;

public class SessionResultsViewModel extends AndroidViewModel {

    private Repository repo;
    private long sessionId = 0L;

    /**
     * A LiveData<Boolean> to which the SessionResultsActivity subscribes as an observer.
     * As soon as everything is setup and ready in the view model, its value changes to true.
     */
    private LiveData<Boolean> isReadyLiveData = new MutableLiveData<>();
    private Session session;
    private List<SessionRound> sessionRounds = new ArrayList<>();
    private List<List<Reaction>> reactions = new ArrayList<>();
    private User user;
    private ImageSet imageSet;
    private List<Image> images;

    private int currentTab = 0;

    public SessionResultsViewModel(Application app) {
        super(app);
        repo = Repository.getInstance(app);
    }

    public void initializeWithSessionId(long sessionId) {
        this.sessionId = sessionId;
        new LoadDataAsyncTask(this).execute();
    }

    private static class LoadDataAsyncTask extends AsyncTask<Void, Void, Void> {
        private SessionResultsViewModel vm;

        LoadDataAsyncTask(SessionResultsViewModel vm) {
            this.vm = vm;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            //load all the data synchronously here
            vm.session = vm.repo.SESSIONS.getSessionByIdSync(vm.sessionId);
            if (vm.session != null) {
                vm.sessionRounds = vm.repo.SESSIONS.getSessionRoundsBySessionIdSync(vm.sessionId);
                for (SessionRound round : vm.sessionRounds) {
                    vm.reactions.add(vm.repo.SESSIONS.getReactionsBySessionRoundIdSync(round.getId()));
                }

                vm.user = vm.repo.USERS.getUserByIdSync(vm.session.getUserId());
                vm.imageSet = vm.repo.IMAGES.getImageSetByIdSync(vm.session.getImageSetId());

                if (vm.imageSet != null) {
                    vm.images = vm.repo.IMAGES.getImagesByImageSetIdSync(vm.imageSet.getId());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ((MutableLiveData<Boolean>)vm.isReadyLiveData).setValue(true);
        }
    }

    public static long calculateApproachAvoidanceBias(List<List<Reaction>> reactions) {
        ArrayList<Long> pushTimes = new ArrayList<>(), pullTimes = new ArrayList<>();
        for(List<Reaction> round : reactions) {
            for(Reaction r : round) {
                if (r.isActionPush()) {
                    pushTimes.add(r.getFinalReactionTime());
                } else {
                    pullTimes.add(r.getFinalReactionTime());
                }
            }
        }
        long pushMedian = median(pushTimes);
        long pullMedian = median(pullTimes);

        return pushMedian - pullMedian;
    }

    /**
     * Calculates the median of the given list of long values.
     * This implementation is heavily inspired by this solution: https://stackoverflow.com/a/28822243/6653385
     * @param list the list of long values of which the median should be calculated
     * @return the median value of the given list of values
     */
    private static long median(ArrayList<Long> list) {
        int n = list.size()/2;

        if (list.size() % 2 == 0)  {
            //even number of elements --> calculate the average of the two median elements
            return (nth(list, n - 1) + nth(list, n)) / 2;
        } else {
            // odd number of elements --> just return the median
            return nth(list, n);
        }
    }

    /**
     * Calculates the nth biggest element of the given list of long values. Used in this class to calculate the median.
     * This implementation is heavily inspired by this solution: https://stackoverflow.com/a/28822243/6653385
     * @param list the list of long values of which the nth biggest element should be returned
     * @return the nth biggest value of the given list of values
     */
    private static Long nth(ArrayList<Long> list, int n) {
        Long result, pivot;
        ArrayList<Long> underPivot = new ArrayList<>(), overPivot = new ArrayList<>(), equalPivot = new ArrayList<>();

        pivot = list.get(n/2); //just choose the middle object as pivot
        //divide list into 3 lists based on comparison with the pivot
        for (Long obj : list) {
            int order = Long.compare(obj, pivot);
            if (order < 0) {       // obj < pivot
                underPivot.add(obj);
            } else if (order > 0) {  // obj > pivot
                overPivot.add(obj);
            } else {                 // obj = pivot
                equalPivot.add(obj);
            }
        }

        //recursive call on the list containing the median
        if (n < underPivot.size()) {
            result = nth(underPivot, n);
        } else if (n < underPivot.size() + equalPivot.size()) {
            result = pivot;
        } else {
            result = nth(overPivot, n - underPivot.size() - equalPivot.size());
        }
        return result;
    }

    public long getSessionId() {
        return sessionId;
    }

    public LiveData<Boolean> getIsReadyLiveData() {
        return isReadyLiveData;
    }

    public Session getSession() {
        return session;
    }

    public List<SessionRound> getSessionRounds() {
        return sessionRounds;
    }

    public List<List<Reaction>> getReactions() {
        return reactions;
    }

    public User getUser() { return user; }

    public List<Image> getImages() { return images; }

    public ImageSet getImageSet() { return imageSet; }

    public int getCurrentTab() {
        return currentTab;
    }

    public void setCurrentTab(int currentTab) {
        this.currentTab = currentTab;
    }
}
