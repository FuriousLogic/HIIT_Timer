package uk.co.furiouslogic.hittimer;

/**
 * Created by Barry on 17/02/2015.
 */
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Prefs extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);
    }
}
