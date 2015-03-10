package uk.co.furiouslogic.hittimer;

import android.app.Activity;
import android.content.Intent;
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

        //Initialise DB
        TextView splashStatus = (TextView) findViewById(R.id.tvSplashStatus);
        splashStatus.setText("Initialising Database");
        SQLiteDatabase db = openOrCreateDatabase("uk.co.furiouslogic.hit_timer", MODE_PRIVATE, null);
        DbHandlerSingleton.Initialise(db);

        //Define Sounds
        splashStatus.setText("Defining Sounds");
        SoundsSingleton.Initialise(this);

        //Setup Admob
        splashStatus.setText("Setting up Admob");
        AdMobSingleton.Initialise();

        //Initialise Preferences
        splashStatus.setText("Initialising Preferences");
        PreferenceSingleton.Initialise(getApplicationContext());

        //Residual timer
        splashStatus.setText("");
        long timeTakenSoFarMs = System.currentTimeMillis() - startTimeMillis;
        long timeLeftToWaitMs = totalMsDelayRequired - timeTakenSoFarMs;
        if(timeLeftToWaitMs <= 0) return;

        long myTimer = timeLeftToWaitMs;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, HIT_Timer.class);
                startActivity(i);
                finish();
            }
        },myTimer);
    }

}
