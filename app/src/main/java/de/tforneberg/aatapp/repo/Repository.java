package de.tforneberg.aatapp.repo;

import android.content.Context;

import de.tforneberg.aatapp.data.AatDatabase;

/**
 * Connection between logic and data. Grants access to persistent data storage. Singleton class,
 * call Repository.getInstance(Context context) to retrieve a reference. Then, use the public
 * sub-repository fields to access the methods you wish to use.
 */
public class Repository {

    public ImageRepository IMAGES;
    public UserRepository USERS;
    public SessionRepository SESSIONS;

    private static Repository INSTANCE;

    private Repository(Context context) {
        //get the database
        AatDatabase database = AatDatabase.getDatabase(context);

        //create the sub repositories
        IMAGES = new ImageRepository(database);
        USERS = new UserRepository(database);
        SESSIONS = new SessionRepository(database);
    }

    public static Repository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = new Repository(context);
                }
            }
        }
        return INSTANCE;
    }
}
