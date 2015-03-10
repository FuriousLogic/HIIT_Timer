package uk.co.furiouslogic.hittimer;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        long totalMsDelayRequired = 3000;
        long startTimeMillis = System.currentTimeMillis();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        TextView tvVersion = (TextView) findViewById(R.id.tvVersion);
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tvVersion.setText("v " + version);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("");
        }

        //Initialise DB
        SQLiteDatabase db = openOrCreateDatabase("uk.co.furiouslogic.hit_timer", MODE_PRIVATE, null);
        DbHandlerSingleton.Initialise(db);

        //Define Sounds
        SoundsSingleton.Initialise(this);

        //Setup Admob
        AdMobSingleton.Initialise();

        //Initialise Preferences
        PreferenceSingleton.Initialise(getApplicationContext());

        //Residual timer
        long timeTakenSoFarMs = System.currentTimeMillis() - startTimeMillis;
        long timeLeftToWaitMs = totalMsDelayRequired - timeTakenSoFarMs;
        if(timeLeftToWaitMs <= 0) return;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, HIT_Timer.class);
                startActivity(i);
                finish();
            }
        }, timeLeftToWaitMs);
    }

}
