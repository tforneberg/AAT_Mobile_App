package de.tforneberg.aatapp.data;

import android.content.Context;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import de.siegmar.fastcsv.writer.CsvAppender;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.tforneberg.aatapp.model.Reaction;
import de.tforneberg.aatapp.model.Session;
import de.tforneberg.aatapp.model.SessionRound;
import de.tforneberg.aatapp.repo.Repository;

/**
 * This class converts one or more Session objects into a CSV file. Therefore, it fetches all the relevant
 * data for that Sessions, its rounds and reactions, from the database.
 * <br><br>
 * The boolean parameter of the SessionToCSVConverter(Activity, Session, boolean) constructor determines the output of the conversion.
 * If true, every row contains every possible information. This means a lot of redundant information,
 * but it can be beneficial in some scenarios for analyzing the data.
 * If false, the data is converted into a much smaller CSV without any redundant information.
 * The downside is, that the structure of the data is not as straight-forward, anymore.
 * <br><br>
 * After converting the Session to a CSV file, this task opens an intent to save the generated
 * file on a user specified location on the device. To do this, it needs a reference to an activity.
 * Internally, this reference is wrapped into a WeakReference to avoid memory leaks and unpredictable behaviour.
 *
 */
public class SessionToCSVConverter extends AsyncTask<Void, Void, File> {
    private FileReadyListener listener;
    private File filesDir;
    private Repository repo;
    private List<Session> sessions;
    private Session session;
    private boolean everyRowIncludesAllInformation;

    public interface FileReadyListener {
        void onCSVFileReady(File file);
    }

    public SessionToCSVConverter(FileReadyListener listener, Context context, Session session, boolean everyRowIncludesAllInformation) {
        super();
        this.listener = listener;
        this.repo = Repository.getInstance(context);
        this.filesDir = context.getFilesDir();
        this.session = session;
        this.everyRowIncludesAllInformation = everyRowIncludesAllInformation;
    }

    public SessionToCSVConverter(FileReadyListener listener, Context context, List<Session> sessions) {
        super();
        this.listener = listener;
        this.repo = Repository.getInstance(context);
        this.filesDir = context.getFilesDir();
        this.sessions = sessions;
    }

    @Override
    protected void onPostExecute(File file) {
        super.onPostExecute(file);
        if (file != null) {
            listener.onCSVFileReady(file);
        }
    }

    @Override
    protected File doInBackground(Void... args) {
        if (session != null) {
            return createCsvFileFromSession();
        } else if (sessions != null) {
            return createCsvFileFromSessions();
        }
        return null;
    }

    private File createCsvFileFromSession() {
        String filename = "aat_session_"+session.getId()+".csv";
        File dir = new File(filesDir, "csv");
        if (!dir.exists()) { dir.mkdir(); }
        File file = new File(dir, filename);

        CsvWriter csvWriter = new CsvWriter();

        try (FileWriter fileWriter = new FileWriter(file);
             CsvAppender appender = csvWriter.append(fileWriter) ) {

            if (everyRowIncludesAllInformation){
                //header
                appender.appendLine("session_id", "user_id", "imageSet_id", "gesture_mode"
                        , "orientation", "colored_border", "border_color_push", "border_color_pull",
                        "rotation_angle", "rotation_angle_push", "rotation_angle_pull",
                        "time_between_images", "notification_type", "round_id", "reaction_id", "first_reaction_time_ms",
                        "final_reaction_time_ms", "push_or_pull", "correct", "image_id", "image_path");

                //data
                for (SessionRound round : repo.SESSIONS.getSessionRoundsBySessionIdSync(session.getId())) {
                    for (Reaction r : repo.SESSIONS.getReactionsBySessionRoundIdSync(round.getId())) {
                        appender.appendField(String.valueOf(session.getId()));
                        appender.appendField(String.valueOf(session.getUserId()));
                        appender.appendField(String.valueOf(session.getImageSetId()));
                        appender.appendField(session.getGestureMode());
                        appender.appendField(session.isLandscape() ? "Landscape" : "Portrait");
                        appender.appendField(session.isHasColoredBorder() ? "Yes" : "No");
                        appender.appendField(session.getBorderColorPush());
                        appender.appendField(session.getBorderColorPull());
                        appender.appendField(session.isHasRotationAngle() ? "Yes" : "No");
                        appender.appendField(String.valueOf(session.getRotationAnglePush()));
                        appender.appendField(String.valueOf(session.getRotationAnglePull()));
                        appender.appendField(String.valueOf(session.getTimeBetweenImages()));
                        appender.appendField(session.getNotificationType());
                        appender.appendField(String.valueOf(round.getId()));
                        appender.appendField(String.valueOf(r.getId()));
                        appender.appendField(String.valueOf(r.getFirstReactionTime()));
                        appender.appendField(String.valueOf(r.getFinalReactionTime()));
                        appender.appendField(r.isActionPush() ? "Push" : "Pull");
                        appender.appendField(r.isActionCorrect() ? "Yes" : "No");
                        appender.appendField(String.valueOf(r.getImageId()));
                        appender.appendField(r.getImageName());
                        appender.endLine();
                    }
                }
            } else {
                // header session data
                appender.appendLine("session_id", "user_id", "imageSet_id", "gesture_mode"
                        , "orientation", "colored_border", "border_color_push", "border_color_pull",
                        "rotation_angle", "rotation_angle_push", "rotation_angle_pull",
                        "time_between_images", "notification_type");

                //session data
                appender.appendField(String.valueOf(session.getId()));
                appender.appendField(String.valueOf(session.getUserId()));
                appender.appendField(String.valueOf(session.getImageSetId()));
                appender.appendField(session.getGestureMode());
                appender.appendField(session.isLandscape() ? "Landscape" : "Portrait");
                appender.appendField(session.isHasColoredBorder() ? "Yes" : "No");
                appender.appendField(session.getBorderColorPush());
                appender.appendField(session.getBorderColorPull());
                appender.appendField(session.isHasRotationAngle() ? "Yes" : "No");
                appender.appendField(String.valueOf(session.getRotationAnglePush()));
                appender.appendField(String.valueOf(session.getRotationAnglePull()));
                appender.appendField(String.valueOf(session.getTimeBetweenImages()));
                appender.appendField(session.getNotificationType());
                appender.endLine();

                for (SessionRound round : repo.SESSIONS.getSessionRoundsBySessionIdSync(session.getId())) {

                    //header session round
                    appender.appendLine("");

                    //data session round
                    appender.appendLine("Round1", "id: "+round.getId());

                    //header reactions
                    appender.appendLine("reaction_id", "first_reaction_time_ms",
                            "final_reaction_time_ms", "push_or_pull", "correct", "image_id", "image_path");

                    //data reactions
                    for (Reaction r : repo.SESSIONS.getReactionsBySessionRoundIdSync(round.getId())) {
                        appender.appendField(String.valueOf(r.getId()));
                        appender.appendField(String.valueOf(r.getFirstReactionTime()));
                        appender.appendField(String.valueOf(r.getFinalReactionTime()));
                        appender.appendField(r.isActionPush() ? "Push" : "Pull");
                        appender.appendField(r.isActionCorrect() ? "Yes" : "No");
                        appender.appendField(String.valueOf(r.getImageId()));
                        appender.appendField(r.getImageName());
                        appender.endLine();
                    }
                }
            }

        } catch (IOException e) {
            return null;
        }
        return file;
    }

    private File createCsvFileFromSessions() {
        String filename = "aat_sessions_all.csv";
        File dir = new File(filesDir, "csv");
        if (!dir.exists()) { dir.mkdir(); }
        File file = new File(dir, filename);

        CsvWriter csvWriter = new CsvWriter();
        try (FileWriter fileWriter = new FileWriter(file);
                CsvAppender writer = csvWriter.append(fileWriter)) {

            //write header
            writer.appendLine("session_id", "user_id", "imageSet_id", "gesture_mode"
                    , "orientation", "colored_border", "border_color_push", "border_color_pull",
                    "rotation_angle", "rotation_angle_push", "rotation_angle_pull",
                    "time_between_images", "notification_type", "round_id", "reaction_id", "first_reaction_time_ms",
                    "final_reaction_time_ms", "push_or_pull", "correct", "image_id", "image_path");

            //write data
            for (Session session : sessions) {
                for (SessionRound round : repo.SESSIONS.getSessionRoundsBySessionIdSync(session.getId())) {
                    for (Reaction r : repo.SESSIONS.getReactionsBySessionRoundIdSync(round.getId())) {
                        writer.appendField(String.valueOf(session.getId()));
                        writer.appendField(String.valueOf(session.getUserId()));
                        writer.appendField(String.valueOf(session.getImageSetId()));
                        writer.appendField(session.getGestureMode());
                        writer.appendField(session.isLandscape() ? "Landscape" : "Portrait");
                        writer.appendField(session.isHasColoredBorder() ? "Yes" : "No");
                        writer.appendField(session.getBorderColorPush());
                        writer.appendField(session.getBorderColorPull());
                        writer.appendField(session.isHasRotationAngle() ? "Yes" : "No");
                        writer.appendField(String.valueOf(session.getRotationAnglePush()));
                        writer.appendField(String.valueOf(session.getRotationAnglePull()));
                        writer.appendField(String.valueOf(session.getTimeBetweenImages()));
                        writer.appendField(session.getNotificationType());
                        writer.appendField(String.valueOf(round.getId()));
                        writer.appendField(String.valueOf(r.getId()));
                        writer.appendField(String.valueOf(r.getFirstReactionTime()));
                        writer.appendField(String.valueOf(r.getFinalReactionTime()));
                        writer.appendField(r.isActionPush() ? "Push" : "Pull");
                        writer.appendField(r.isActionCorrect() ? "Yes" : "No");
                        writer.appendField(String.valueOf(r.getImageId()));
                        writer.appendField(r.getImageName());
                        writer.endLine();
                    }
                }
            }
        } catch (IOException e) {
            return null;
        }
        return file;
    }
}
