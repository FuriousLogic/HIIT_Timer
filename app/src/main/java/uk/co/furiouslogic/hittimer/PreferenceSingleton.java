package uk.co.furiouslogic.hittimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by Barry on 27/02/2015.
 */
public class PreferenceSingleton {
    private static Context _applicationContext;

    //todo: don't save string if it's not
    public static void Initialise(Context context) {
        _applicationContext = context;

        //Set initial values
        SetInitialPreference("pref_AthleteName","Your Name");
        SetInitialPreference("pref_warmup", "120");
        SetInitialPreference("pref_power", "20");
        SetInitialPreference("pref_rest", "120");
        SetInitialPreference("pref_cooldown", "120");
        SetInitialPreference("pref_reps", "3");

//        SetInitialPreference("pref_sound", "0");
//        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_applicationContext);
//        settings.getBoolean("pref_sound",null);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putBoolean("pref_sound", false);
//        editor.apply();

    }

    private static void SetInitialPreference(String key, String defaultValue) {
        String currentValue = GetPrefString(key);
        if(currentValue.equals(""))
            SetPrefString(key, defaultValue);
    }

    public static String GetPrefString(String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_applicationContext);
        return pref.getString(key, "");
    }

    public static boolean GetPrefBool(String key) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(_applicationContext);
        return pref.getBoolean(key, false);
    }

    public static void SetPrefString(String key, final String value) {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(_applicationContext);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

}
