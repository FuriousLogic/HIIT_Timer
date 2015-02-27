package uk.co.furiouslogic.hittimer;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.View;

import java.util.Date;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

/**
 * Created by Barry on 09/02/2015.
 */

public class DbHandlerSingleton {
    private static SQLiteDatabase _db;

    public DbHandlerSingleton(){}

    public static void Initialise(SQLiteDatabase db){
        _db = db;
        CreateOrOpenDb();
    }

    private static void CreateOrOpenDb(){
        //Setup Data
        _db.execSQL("CREATE TABLE IF NOT EXISTS workout(timestamp INTEGER);");
        _db.execSQL("CREATE TABLE IF NOT EXISTS log(message TEXT);");

        _db.execSQL("delete from log");
    }

    public static void SaveToLog(String message){
        ContentValues cv = new ContentValues();
        cv.put("message", message);
        _db.insert("log", null, cv);
    }

    public static int getWorkoutCount() {
        Cursor c = getCursor("select * from workout");
        int count = c.getCount();

        return count;
    }

    public static Cursor getCursor(String sql) {
        Cursor c = _db.rawQuery(sql, null);
        if(c.getCount()==0){
            //todo: error or something
        }
        c.moveToFirst();
        return c;
    }

    public static void saveNewWorkout() {
        ContentValues cv = new ContentValues();
        cv.put("timestamp", System.currentTimeMillis());
        _db.insert("workout", null, cv);
    }

    public static Date getDateOfLastWorkout() {
        Cursor c = getCursor("select * from workout");
        if(c.getCount()==0) return null;

        c = getCursor("select max(timestamp) as lastWorkout from workout");
        Date dateOfLastWorkout = new Date(c.getLong(0));
        return dateOfLastWorkout;
    }
}
