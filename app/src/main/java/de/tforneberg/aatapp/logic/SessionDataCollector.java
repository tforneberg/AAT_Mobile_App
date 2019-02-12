package de.tforneberg.aatapp.logic;

import android.app.Application;
import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.Date;

import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.model.Reaction;
import de.tforneberg.aatapp.model.Session;
import de.tforneberg.aatapp.model.SessionRound;
import de.tforneberg.aatapp.model.Settings;
import de.tforneberg.aatapp.repo.Repository;

/**
 * This class is used to save all the data that is created while the AAT Session is running.
 * For this purpose, it has some public methods. See the Javadoc on these methods for further details.
 * At the end of the session, it submits all the saved data to the database.
 */
public class SessionDataCollector {

    private static class DataPerRound {
        private long id;
        private long[] firstTimesArr;
        private long[] finalTimesArr;
        private boolean[] pushPullArr;
        private Image[] imagesArr;
        private boolean[] correctArr;

        DataPerRound(Settings settings) {
            firstTimesArr = new long[settings.imagesPerRound];
            finalTimesArr = new long[settings.imagesPerRound];
            pushPullArr = new boolean[settings.imagesPerRound];
            correctArr = new boolean[settings.imagesPerRound];
            imagesArr = new Image[settings.imagesPerRound];
        }
    }

    private DataPerRound[] rounds;
    private int roundsInDatabase;

    private int currRound;
    private int currImage;
    private long imageStartingTime;

    private Session session;
    private long sessionId;

    private Repository repo;
    private Settings settings;

    public SessionDataCollector(Settings settings, long imageSetId, Application app) {
        repo = Repository.getInstance(app);
        this.settings = settings;

        currRound = 0;
        currImage = 0;

        session = new Session(settings.userId, imageSetId,
            settings.gestureMode, settings.isLandscape,
            settings.hasColoredBorder, settings.borderColorPush, settings.borderColorPull,
            settings.hasRotationAngle, settings.rotationAnglePush, settings.rotationAnglePull,
            settings.timeBetweenImages, settings.resultMessageType.toString(), new Date()
        );

        //setup the rounds array
        roundsInDatabase = 0;
        rounds = new DataPerRound[settings.rounds];

        //setup the first round object
        rounds[currRound] = new DataPerRound(settings);
    }

    private long nanoTime() {
        return System.nanoTime();
    }

    public long assignNewStartingTimeToCurrentImage() {
        imageStartingTime = nanoTime();
        return imageStartingTime;
    }

    public void startNewRound() {
        currImage = 0;
        rounds[currRound] = new DataPerRound(settings);
    }

    public void increaseCurrentRound() {
        currRound = ++currRound;
    }

    /**
     * Saves the initial reaction time in milliseconds in memory (not on hard drive, yet!)
     * At the end of the session, initDatabaseSave() needs to be called to persist all saved reaction data
     * to non-volatile memory.
     *
     * Returns the reaction time that just got saved in milliseconds.
     */
    public long saveImageFirstReactionTime() {
        rounds[currRound].firstTimesArr[currImage] = (nanoTime() - imageStartingTime) / 1000000;
        return rounds[currRound].firstTimesArr[currImage];
    }

    /**
     * This should be called if it turns out that a previously recognized firstReactionTime was
     * actually not the correct firstReactionTime (e.g. because the user uplifted the finger again, afterwards)
     */
    public void resetImageFirstReactionTime() {
        rounds[currRound].firstTimesArr[currImage] = 0;
    }

    /**
     * Saves data for the last image reaction in memory (not on hard drive, yet!),
     * including the final time the user took to react to it.
     * Returns the final time the user took to react to it in milliseconds (for displaying it in the gui)
     * At the end of the session, initDatabaseSave() needs to be called to persist all saved reaction data
     * to non-volatile memory.
     */
    public long saveImageReactionData(boolean pushPull, boolean correct, long finalReactionTime, Image image) {
         //save final reaction time
        rounds[currRound].finalTimesArr[currImage] = (finalReactionTime - imageStartingTime) / 1000000;

        //save other data
        rounds[currRound].imagesArr[currImage] = image;
        rounds[currRound].pushPullArr[currImage] = pushPull;
        rounds[currRound].correctArr[currImage] = correct;

        //increase image number
        currImage++;

        //return final reaction time
        return rounds[currRound].finalTimesArr[currImage-1];
    }

    /**
     * Saves data for the last image reaction in memory (not on hard drive, yet!),
     * including the final time the user took to react to it.
     * Returns the final time the user took to react to it in milliseconds (for displaying it in the gui)
     * At the end of the session, initDatabaseSave() needs to be called to persist all saved reaction data
     * to non-volatile memory.
     */
    public long saveImageReactionData(boolean pushPull, boolean correct, Image image, long finalReactionTime, Long... firstReactionTime) {
        long finalReactionTimeMS = saveImageReactionData(pushPull, correct, finalReactionTime, image);

        if (firstReactionTime != null && firstReactionTime.length > 0 && firstReactionTime[0] != null) {
            rounds[currRound].firstTimesArr[currImage-1] = (firstReactionTime[0] - imageStartingTime) / 1000000;
        }
        return finalReactionTimeMS;
    }

    /**
     * Creates a Session and the correct amount of SessionRounds in the database.
     * Calls a async methods to talk to the db.
     * After the insertions, the private method saveAllReactionsToDatabase(id) is called with
     * the id of the newly generated session.
     */
    public void initDatabaseSave() {
        new InsertSessionAsyncTask(repo, this).execute();
    }

    /**
     * This method is called at the end of a session and saves all the reaction data
     * for all rounds of the session into the database. For this to work, a Session and the correct
     * amount of SessionRound objects/rows must have been initialized in the database.
     */
    private void saveAllReactionsToDatabase() {
        for (int i = 0; i < settings.rounds; i++) {
            //Create Reaction objects from the DataPerRound object array entries
            ArrayList<Reaction> reactions = new ArrayList<>();
            for (int j = 0; j < settings.imagesPerRound; j++) {
                Reaction r = new Reaction(rounds[i].id,
                        rounds[i].firstTimesArr[j],
                        rounds[i].finalTimesArr[j],
                        rounds[i].pushPullArr[j],
                        rounds[i].correctArr[j],
                        rounds[i].imagesArr[j].getId(),
                        rounds[i].imagesArr[j].getPath());
                reactions.add(r);
            }

            //Insert the created Reaction objects into the database
            repo.SESSIONS.insertReaction(reactions.toArray(new Reaction[0]));
        }
    }

    /**
     * Creates a Session in the database, saves the retrieved ID in the SessionDataCollector's
     * sessionId field and starts the first InsertSessionRoundAsyncTask
     */
    public static class InsertSessionAsyncTask extends AsyncTask<Void, Void, Long> {
        Repository repo;
        SessionDataCollector collector;

        InsertSessionAsyncTask(Repository repo, SessionDataCollector collector) {
            super();
            this.repo = repo;
            this.collector = collector;
        }

        @Override
        protected Long doInBackground(Void... args) {
            return repo.SESSIONS.insertSesionSync(collector.session);
        }

        @Override
        protected void onPostExecute(Long id) {
            super.onPostExecute(id);
            collector.sessionId = id;

            //after generating a session and retrieving the id, generate the SessionRounds
            new InsertSessionRoundAsyncTask(repo, collector).execute();
        }
    }

    /**
     * Creates a SessionRound in the database. After that, it also checks if the correct amount
     * of SessionRounds have been inserted into the database. If so, it starts the saveAllReactionsToDatabase() method.
     * If not, it starts another instance of itself.
     * It also saves the retrieved ID from the generated SessionRound in the corresponding DataPerRounds' id field.
     */
    public static class InsertSessionRoundAsyncTask extends AsyncTask<Void, Void, Long> {
        Repository repo;
        SessionDataCollector collector;

        InsertSessionRoundAsyncTask(Repository repo, SessionDataCollector collector) {
            super();
            this.repo = repo;
            this.collector = collector;
        }

        @Override
        protected Long doInBackground(Void... args) {
            //insert a new SessionRound with the correctly associated session id
            return repo.SESSIONS.insertSessionRoundSync(new SessionRound(collector.sessionId));
        }

        @Override
        protected void onPostExecute(Long id) {
            super.onPostExecute(id);

            //Save the newly generated round id in the DataPerRound object's id field
            collector.rounds[collector.roundsInDatabase].id = id;

            //increment roundsInDatabase (=rounds initialized) counter
            ++collector.roundsInDatabase;

            if (collector.roundsInDatabase < collector.settings.rounds) {
                //if not all rounds are initialized in the db, yet, start another InsertSessionRoundAsyncTask
                new InsertSessionRoundAsyncTask(repo, collector).execute();
            } else {
                //start writing the actual reaction data to the database
                collector.saveAllReactionsToDatabase();
            }
        }
    }
}
