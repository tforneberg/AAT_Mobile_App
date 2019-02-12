package de.tforneberg.aatapp.data;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;

import de.tforneberg.aatapp.R;
import de.tforneberg.aatapp.data.dao.ImageDao;
import de.tforneberg.aatapp.data.dao.ImageSetDao;
import de.tforneberg.aatapp.data.dao.SessionDao;
import de.tforneberg.aatapp.data.dao.UserDao;
import de.tforneberg.aatapp.model.Image;
import de.tforneberg.aatapp.model.ImageSet;
import de.tforneberg.aatapp.model.ImageSetImageConnection;
import de.tforneberg.aatapp.model.Reaction;
import de.tforneberg.aatapp.model.Session;
import de.tforneberg.aatapp.model.SessionRound;
import de.tforneberg.aatapp.model.User;


/**
 * This class manages the device-local database definition and setup using the Room library. It
 * also gives access to the actual database object through its static method getDatabase(Context c).
 */
@Database(entities = {
        Image.class,
        ImageSet.class,
        ImageSetImageConnection.class,
        User.class,
        SessionRound.class,
        Session.class,
        Reaction.class
        }, version = 1)
@TypeConverters({Converters.class})
public abstract class AatDatabase extends RoomDatabase {
    public abstract ImageDao imageDao();
    public abstract ImageSetDao imageSetDao();
    public abstract UserDao userDao();
    public abstract SessionDao sessionDao();

    private static volatile AatDatabase INSTANCE;

    public static AatDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AatDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), AatDatabase.class, "aat_database")
                        .addCallback(new RoomDatabase.Callback() {
                            //init the db at first startup
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                                new InitDbAsync(INSTANCE, context).execute();
                            }
                        })
                        .build();
                }
            }
        }
        return INSTANCE;
    }

    /**
     * This AsyncTask adds some pre-defined values to the database after its initial creation.
     */
    private static class InitDbAsync extends AsyncTask<Void, Void, Void> {
        private final ImageSetDao imageSetDao;
        private final UserDao userDao;
        private String defaultImageSet;

        InitDbAsync(AatDatabase db, Context context) {
            //initialize the needed DAOs
            imageSetDao = db.imageSetDao();
            userDao = db.userDao();
            defaultImageSet = context.getString(R.string.default_image_set);
        }

        @Override
        protected Void doInBackground(final Void... params) {
            byte[] pw = new byte[]{ //the hashed admin password
                (byte)0x6c, (byte)0xea, (byte)0xba, (byte)0x54, (byte)0x0f, (byte)0x49, (byte)0x26, (byte)0x4a,
                (byte)0x5f, (byte)0xb0, (byte)0xf8, (byte)0x74, (byte)0x81, (byte)0xd2, (byte)0x6e, (byte)0x3c,
                (byte)0xdd, (byte)0x3d, (byte)0xaa, (byte)0xf6, (byte)0xc9, (byte)0x33, (byte)0xbd, (byte)0x70,
                (byte)0x41, (byte)0xbc, (byte)0x7c, (byte)0x37, (byte)0x22, (byte)0x38, (byte)0x7e, (byte)0xcf,
                (byte)0xd2, (byte)0x35, (byte)0xc6, (byte)0x6e, (byte)0xfa, (byte)0xcb, (byte)0x4a, (byte)0x24,
                (byte)0x22, (byte)0x81, (byte)0xb0, (byte)0x46, (byte)0x44, (byte)0x61, (byte)0xe4, (byte)0xd1,
                (byte)0x62, (byte)0x59, (byte)0xbd, (byte)0x10, (byte)0x49, (byte)0x79, (byte)0xbe, (byte)0x48,
                (byte)0x73, (byte)0x97, (byte)0x3f, (byte)0xe7, (byte)0xe2, (byte)0x87, (byte)0x9e, (byte)0x08
            };
            byte[] salt = new byte[]{ //the hashed admin password salt
                (byte)0xab, (byte)0xfa, (byte)0xd8, (byte)0x44, (byte)0x82, (byte)0x43, (byte)0xce, (byte)0x3f,
                (byte)0x9a, (byte)0xa6, (byte)0x2f, (byte)0xb2, (byte)0x61, (byte)0xe1, (byte)0xcf, (byte)0xe1,
            };
            User user = new User(1,"admin", pw, salt, true);
            userDao.insertUser(user);

            ImageSet imageSet = new ImageSet(1, defaultImageSet, true);
            imageSetDao.insertImageSet(imageSet);
            return null;
        }
    }
}
