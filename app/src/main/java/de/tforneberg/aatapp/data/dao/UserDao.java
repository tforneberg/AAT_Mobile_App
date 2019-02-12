package de.tforneberg.aatapp.data.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import de.tforneberg.aatapp.model.User;

/**
 * The DataAccessObject (Dao) interface definition for User related database content.
 * It defines all methods to access the database for this content and (via Annotations)
 * connects them with SQL-Queries.
 */
@Dao
public interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    LiveData<User> getUserById(long id);

    @Query("SELECT * FROM users WHERE id = :id")
    User getUserByIdSync(long id);

    @Query("SELECT * FROM users WHERE name = :name")
    LiveData<User> getUserByName(String name);

    @Query("SELECT * FROM users WHERE name = :name")
    User getUserByNameSync(String name);

    @Query("SELECT * FROM users")
    LiveData<List<User>> getAllUsers();

    @Query("SELECT COUNT(*) FROM users")
    LiveData<Integer> getAllUsersCount();

    @Insert
    long insertUser(User user);

    @Update
    void updateUsers(User... users);

    @Delete
    void deleteUsers(User... users);

    @Query("DELETE FROM users")
    void deleteAllUsers();
}
