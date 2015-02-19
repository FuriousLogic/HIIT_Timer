package uk.co.furiouslogic.hittimer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

//todo: make proper image that includes text
public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        int myTimer = 3000;
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
