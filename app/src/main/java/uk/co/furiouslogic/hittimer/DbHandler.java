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

/**
 * Created by Barry on 09/02/2015.
 */

public class DbHandler {
    private final SQLiteDatabase _db;

    public DbHandler(SQLiteDatabase db){
        _db = db;
        CreateOrOpenDb();
    }

    private void CreateOrOpenDb(){
        //Setup Data
        _db.execSQL("CREATE TABLE IF NOT EXISTS workout(timestamp INTEGER);");
    }

    public int getWorkoutCount() {
        Cursor c = getCursor("select * from workout");
        int count = c.getCount();

        return count;
    }

    public Cursor getCursor(String sql) {
        Cursor c = _db.rawQuery(sql, null);
        if(c.getCount()==0){
            //todo: error or something
        }
        c.moveToFirst();
        return c;
    }

    public void saveNewWorkout() {
        ContentValues cv = new ContentValues();
        cv.put("timestamp", System.currentTimeMillis());
        _db.insert("workout", null, cv);
    }

    public Date getDateOfLastWorkout() {
        Cursor c = getCursor("select * from workout");
        if(c.getCount()==0) return null;

        c = getCursor("select max(timestamp) as lastWorkout from workout");
        Date dateOfLastWorkout = new Date(c.getLong(0));
        return dateOfLastWorkout;
    }
}
