package com.example.jorge.androidapp.users;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.jorge.androidapp.R;
import com.example.jorge.androidapp.entities.User;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class UsersDataBaseHelper extends SQLiteOpenHelper {

    private Context context;
    //database values
    private static final String DATABASE_NAME = "friends.db";
    private static final int DATABASE_VERSION = 1;


    private static final String TABLE_FRIENDS = "FRIENDS";
    private static final String COLUMN_ID = "ID";
    private static final String COLUMN_USERNAME = "USERNAME";
    private static final String COLUMN_ID_ANDROID = "ID_ANDROID";
    private static final String COLUMN_DEVICE_NAME = "DEVICE_NAME";
    private static final String COLUMN_BLOCKED = "BLOCKED";
    private static final String COLUMN_USER_IMAGE = "USER_IMAGE";

    private static final String DELETE_TABLE = "DROP TABLE IF EXISTS FRIENDS";
    private static final String DATABASE_CREATE_FRIENDS = "CREATE TABLE if not exists "
            + TABLE_FRIENDS + "(" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COLUMN_ID_ANDROID + " TEXT NOT NULL, "
            + COLUMN_USERNAME + " TEXT NOT NULL, "
            + COLUMN_DEVICE_NAME + " TEXT NOT NULL, "
            + COLUMN_BLOCKED + " INTEGER , "
            + COLUMN_USER_IMAGE + " BLOB);";


    public UsersDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        onCreate(this.getWritableDatabase());
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        //db.execSQL(DELETE_TABLE);
        db.execSQL(DATABASE_CREATE_FRIENDS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*
        if (oldVersion < 2) {
            db.execSQL(DATABASE_ALTER_TEAM_1);
        }
        if (oldVersion < 3) {
            db.execSQL(DATABASE_ALTER_TEAM_2);
        }
        */
    }

    public DatabaseResult insertFriend(User user) {
        DatabaseResult result = new DatabaseResult();
        SQLiteDatabase db = this.getWritableDatabase();
        String[] cols = new String[]{COLUMN_ID_ANDROID};
        Cursor cursor = null;
        try {

            boolean exist = getUser(user.getUserID()) != null;

            if (exist) {
                result.setTransactionOK(false);
                String message = context.getResources().getString(R.string.user_already_exist);
                result.setMessage(message);
                return result;
            }

            ContentValues contentValues = new ContentValues();
            byte[] image = user.getUserImage();
            if (image != null) {
                contentValues.put(COLUMN_USER_IMAGE, image);
            }
            contentValues.put(COLUMN_ID_ANDROID, user.getUserID());
            contentValues.put(COLUMN_USERNAME, user.getUserName());
            contentValues.put(COLUMN_DEVICE_NAME, user.getUserDeviceName());
            contentValues.put(COLUMN_BLOCKED, 0);

            long inserted = db.insert(TABLE_FRIENDS, null, contentValues);
            if (inserted == -1) {
                //error
                result.setTransactionOK(false);
                String message = context.getResources().getString(R.string.user_insert_error);
                result.setMessage(message);
            } else {
                result.setTransactionOK(true);
                String message = context.getResources().getString(R.string.user_insert_ok);
                result.setMessage(message);
            }
        } catch (Exception e) {
            result.setTransactionOK(false);
            String message = context.getResources().getString(R.string.user_insert_error);
            result.setMessage(message);
        }
        return result;
    }


    public DatabaseResult blockUser(String idUser) {
        DatabaseResult result = new DatabaseResult();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BLOCKED, "1");
        int rowsAffected = db.update(TABLE_FRIENDS, contentValues, COLUMN_ID_ANDROID + "=?", new String[]{idUser});
        if (rowsAffected == 1) {
            result.setTransactionOK(true);
            result.setMessage(context.getResources().getString(R.string.user_blocked_ok));
        } else {
            result.setTransactionOK(false);
            result.setMessage(context.getResources().getString(R.string.user_blocked_error));
        }
        return result;
    }

    public DatabaseResult unBlockUser(String idUser) {
        DatabaseResult result = new DatabaseResult();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_BLOCKED, "0");
        int rowsAffected = db.update(TABLE_FRIENDS, contentValues, COLUMN_ID_ANDROID + "=?", new String[]{idUser});
        if (rowsAffected == 1) {
            result.setTransactionOK(true);
            result.setMessage(context.getResources().getString(R.string.user_unblocked_ok));
        } else {
            result.setTransactionOK(false);
            result.setMessage(context.getResources().getString(R.string.user_unblocked_error));
        }
        return result;
    }

    /*Devuelve el usuario asociado*/
    public User getUser(String idUser) {

        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = new String[]{COLUMN_ID_ANDROID, COLUMN_DEVICE_NAME, COLUMN_USERNAME, COLUMN_BLOCKED, COLUMN_USER_IMAGE};
        Cursor mCursor = null;
        User user = null;
        try {
            mCursor = db.query(true, TABLE_FRIENDS, cols, COLUMN_ID_ANDROID + "= ?"
                    , new String[]{idUser}, null, null, null, null);

            if (mCursor != null && mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                user = getUserFromCursor(mCursor);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCursor != null)
                mCursor.close();
        }
        return user;
    }

    public DatabaseResult updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        DatabaseResult result = new DatabaseResult();
        int rowsAffected = 0;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(COLUMN_USERNAME, user.getUserName());
            if (user.getUserImage() != null && user.getUserImage().length > 0)
                contentValues.put(COLUMN_USER_IMAGE, user.getUserImage());

            rowsAffected = db.update(TABLE_FRIENDS, contentValues, COLUMN_ID + " = ?", new String[]{user.getUserID()});

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
        if (rowsAffected == 0) {
            result.setTransactionOK(false);
            result.setMessage("No updates in user");
            return result;
        } else {
            result.setTransactionOK(true);
            result.setMessage("User update successfully");
            return result;
        }
    }

    public User[] getFriendsRegistered(boolean blocked) {
        Cursor cursor = cursorToFriends(blocked);
        ArrayList<User> users = new ArrayList<>();

        if (cursor.getCount() == 0)
            return users.toArray(new User[0]);

        User first = getUserFromCursor(cursor);
        users.add(first);
        try {
            while (cursor.moveToNext()) {
                User user = getUserFromCursor(cursor);
                users.add(user);
            }
            return users.toArray(new User[0]);
        } finally {
            cursor.close();
        }
    }

    private Cursor cursorToFriends(boolean isBlocked) {
        String blockParam = !isBlocked ? "0" : "1";
        SQLiteDatabase db = this.getReadableDatabase();
        String[] cols = new String[]{COLUMN_ID_ANDROID, COLUMN_DEVICE_NAME, COLUMN_USERNAME, COLUMN_BLOCKED, COLUMN_USER_IMAGE};

        Cursor mCursor = db.query(true, TABLE_FRIENDS, cols, COLUMN_BLOCKED + "= ?"
                , new String[]{blockParam}, null, null, null, null);

        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    private User getUserFromCursor(Cursor cursor) {
        String id = cursor.getString(cursor.getColumnIndex(COLUMN_ID_ANDROID));
        String username = cursor.getString(cursor.getColumnIndex(COLUMN_USERNAME));
        String deviceName = cursor.getString(cursor.getColumnIndex(COLUMN_DEVICE_NAME));
        String isBlocked = cursor.getString(cursor.getColumnIndex(COLUMN_BLOCKED));
        User user = new User(id, username, deviceName, !isBlocked.equals("0"));
        byte[] imageBytes = cursor.getBlob(cursor.getColumnIndex(COLUMN_USER_IMAGE));
        if (imageBytes != null && imageBytes.length > 0) {
            user.setUserImage(imageBytes);
        }

        return user;
    }

    public DatabaseResult deleteFriend(User user) {
        return deleteFriend(user.getUserID());
    }

    public DatabaseResult deleteFriend(String IDuser) {
        DatabaseResult result = new DatabaseResult();
        SQLiteDatabase db = this.getWritableDatabase();
        long delete = db.delete(TABLE_FRIENDS, COLUMN_ID_ANDROID + "= ?", new String[]{IDuser});
        if (delete == 0) {
            result.setTransactionOK(false);
            String message = context.getResources().getString(R.string.user_delete_error);
            result.setMessage(message);
            return result;
        } else {
            result.setTransactionOK(true);
            String message = context.getResources().getString(R.string.user_delete_ok);
            result.setMessage(message);
            return result;
        }
    }

    public DatabaseResult updateProfile(String androidId, File file) {


        DatabaseResult dbResult = new DatabaseResult();
        try {

            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues contentValues = new ContentValues();
            FileInputStream fis = new FileInputStream(file);
            long longfich = file.length();

            byte[] buffer_fich = new byte[(int) longfich];
            int bytesleidos = fis.read(buffer_fich);

            fis.close();
            contentValues.put(COLUMN_USER_IMAGE, buffer_fich);

            long result = db.insert(TABLE_FRIENDS, null, contentValues);

            if (result == -1) {
                dbResult.setTransactionOK(false);
                dbResult.setMessage(context.getString(R.string.user_profile_error));
                return dbResult;
            } else {

                dbResult.setTransactionOK(false);
                dbResult.setMessage(context.getString(R.string.user_profile_error));
                return dbResult;
            }


        } catch (Exception e) {

        }

        dbResult.setTransactionOK(false);
        dbResult.setMessage(context.getString(R.string.user_profile_error));
        return dbResult;
    }


}


