package uk.co.furiouslogic.hittimer;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

//import com.google.android.gms.games.internal.constants.TimeSpan;
import com.pollfish.main.PollFish;
import com.pollfish.constants.Position;

import java.util.Date;

//todo: Make beeps distinct - make beeps myself (Ruby's keyboard?)
//todo: track workouts over a week
//todo: notifications on workouts to do this week

public class HIT_Timer extends ActionBarActivity {
    private static final int RESULT_SETTINGS = 1;
    //Workout flags
    private Boolean warmUpDone = false;
    private Boolean powerDone = false;
    private Boolean restDone = false;
    private Boolean coolDownDone = false;
    private int currentRep = 0;
    private int currentSecondsToGo = 0;
    private boolean allowRepToIncrease = false;
    private boolean isRunning = false;

    //Workout colours
    final String bWarmUp = "#ffd699";
    final String tWarmUp =  "#000000";
    final String bOn = "#4c0000";
    final String tOn =  "#ff6666";
    final String bRest = "#d6eb99";
    final String tRest =  "#000000";
    final String bCoolDown = "#d1f0ff";
    final String tCoolDown =  "#000000";
    final String bFinished = "#ffffff";
    final String tFinished =  "#000000";

    //Controls
    private RelativeLayout rlHitTimer;
    private TextView tvStage;
    private TextView tvCountDown;
    private TextView tvRep;
    private TextView tvAthleteName;
    private Button btnStartTimer;

    //Properties
    private CountDownTimer countDownTimer;
    private SoundPool beepPool;
    private int beepLowId;
    private int beepHighId;
    private DbHandler dbh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hit__timer);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            beepPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(aa)
                    .build();
            beepLowId = beepPool.load(this, R.raw.beep, 1);
            beepHighId = beepPool.load(this, R.raw.beephigh, 1);
        }else{
            beepPool = new SoundPool(10, AudioManager.STREAM_ALARM, 1);
            beepLowId = beepPool.load(this, R.raw.beep, 1);
            beepHighId = beepPool.load(this, R.raw.beephigh, 1);
        }

        //Admob
        AdView avTimerBanner = (AdView) findViewById(R.id.avTimerBanner);
        com.google.android.gms.ads.AdRequest ar = new com.google.android.gms.ads.AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("A9C350CBED46BAC089E21096D5522BE6").build();
        avTimerBanner.loadAd(ar);

        //Get Controls
        rlHitTimer = (RelativeLayout) findViewById(R.id.rlHitTimer);
        tvStage = (TextView) findViewById(R.id.tvStage);
        tvCountDown = (TextView) findViewById(R.id.tvCountDown);
        tvRep = (TextView) findViewById(R.id.tvRep);
        tvAthleteName = (TextView)findViewById(R.id.tvAthleteName);
        btnStartTimer= (Button) findViewById(R.id.btnStartTimer);

        //Set initial values
        SetInitialPreference("pref_AthleteName","Your Name");
        SetInitialPreference("pref_warmup", "120");
        SetInitialPreference("pref_power", "20");
        SetInitialPreference("pref_rest", "120");
        SetInitialPreference("pref_cooldown", "120");
        SetInitialPreference("pref_reps", "3");

        //Db
        SQLiteDatabase db = openOrCreateDatabase("uk.co.furiouslogic.hit_timer", MODE_PRIVATE, null);
        dbh = new DbHandler(db);

        //Format screen
        zeroTheScreen();

        //Time since last workout
        Date now = new Date(System.currentTimeMillis());
        Date lastWorkout = dbh.getDateOfLastWorkout();
        String timeSinceLastWorkoutMessage;
        if(lastWorkout != null) {
            long diffInMs = now.getTime() - lastWorkout.getTime();
            long seconds = diffInMs / 1000;
            seconds /= 60;
            long minutesLeft = seconds % 60;
            seconds /= 60;
            long hoursLeft = seconds % 24;
            seconds /= 24;
            long daysLeft = seconds;
            timeSinceLastWorkoutMessage = "Time Since Last Workout: \r\n";
            if (daysLeft > 0) timeSinceLastWorkoutMessage += daysLeft + " days, ";
            timeSinceLastWorkoutMessage += hoursLeft + " hours, ";
            timeSinceLastWorkoutMessage += minutesLeft + " minutes";
        } else{
            timeSinceLastWorkoutMessage = "No workouts completed";
        }


    }

    @Override
    public void onStart(){
        super.onStart();
        showStatusData();
    }


    private void SetInitialPreference(String key, String defaultValue) {
        String currentValue = GetPrefString(key);
        if(currentValue == "")
            SetPrefString(key, defaultValue);
    }

    private void playStartSound(){
        beepPool.play(beepHighId, 1, 1, 1, 0, 1);
    }

    private void playStopSound(){
        beepPool.play(beepLowId, 1, 1, 1, 0, 1);
    }

    private void showStatusData() {
        int workoutCount = dbh.getWorkoutCount();

        String statusData = GetPrefString("pref_AthleteName");
        tvAthleteName.setText(statusData + " (" + workoutCount + ")");
    }

    public String GetPrefString(String key) {
        Context context = getApplicationContext();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        return pref.getString(key, "");
    }

    public void SetPrefString(String key, final String value) {
        Context context = getApplicationContext();
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private void zeroTheScreen() {
        currentRep=0;
        isRunning=false;
        warmUpDone=false;
        powerDone =false;
        restDone=false;
        coolDownDone=false;

        tvStage.setText(getString(R.string.stageInitialText));
        btnStartTimer.setText(getString(R.string.startTimerButtonText));

        tvCountDown.setText("0");

        showCurrentRep();
        showColours();
    }

    private void showColours() {
        if(!isRunning){
            setStageColour(bFinished, tFinished);
            return;
        }
        if(!warmUpDone) {
            setStageColour(bWarmUp, tWarmUp);
            return;
        }
        if(!powerDone){
            setStageColour(bOn, tOn);
            return;
        }
        if(!restDone){
            setStageColour(bRest, tRest);
            return;
        }
        setStageColour(bCoolDown, tCoolDown);
    }

    private void doWorkout() {
        //Get workout preferences
        //todo: figure out how to get int directly
        final int warmUpSeconds = Integer.parseInt(GetPrefString("pref_warmup"));
        final int powerSeconds = Integer.parseInt(GetPrefString("pref_power"));
        final int restSeconds = Integer.parseInt(GetPrefString("pref_rest"));
        final int coolDownSeconds = Integer.parseInt(GetPrefString("pref_cooldown"));
        final int totalReps = Integer.parseInt(GetPrefString("pref_reps"));

        final int workoutSeconds = warmUpSeconds+(powerSeconds* totalReps)+(restSeconds*(totalReps -1))+coolDownSeconds;

        //Workout
        countDownTimer = new CountDownTimer(workoutSeconds *1000, 200) {

            public void onTick(long millisUntilFinished) {
                isRunning=true;
                int secondsToGo = (int) (millisUntilFinished/1000);
                if(secondsToGo == currentSecondsToGo) return;
                currentSecondsToGo = secondsToGo;

                int currentSecond = workoutSeconds-secondsToGo;

                //Where are we in the sequence?
                String stage="";
                int stageCurrentSecond;
                int stageSecondsToGo=0;

                //Warm up
                if(!warmUpDone){
                    stage=getString(R.string.warmUpText);
                    showColours();
                    stageCurrentSecond = currentSecond;
                    stageSecondsToGo = warmUpSeconds-stageCurrentSecond;

                    if(stageSecondsToGo <= 1) {
                        warmUpDone = true;
                        allowRepToIncrease=true;
                    }
                }
                else{
                    //Power
                    if(!powerDone) {
                        stage=getString(R.string.powerText);
                        showColours();
                        if(allowRepToIncrease){
                            currentRep++;
                            playStartSound();
                            allowRepToIncrease = false;
                        }

                        showCurrentRep();
                        if(currentRep ==1)
                            stageCurrentSecond = currentSecond-warmUpSeconds;
                        else
                            stageCurrentSecond = currentSecond-(((powerSeconds+restSeconds)*(currentRep -1))+warmUpSeconds);
                        stageSecondsToGo = powerSeconds-stageCurrentSecond;

                        if(stageSecondsToGo <= 1) {
                            powerDone = true;
                            playStopSound();
                            if(currentRep == totalReps)
                                restDone=true;
                        }
                    }
                    else{
                        //Rest
                        if(!restDone){
                            stage=getString(R.string.restText);
                            showColours();
                            if(currentRep ==1)
                                stageCurrentSecond = currentSecond-(powerSeconds+warmUpSeconds);
                            else
                                stageCurrentSecond = currentSecond-(((powerSeconds+restSeconds)*(currentRep -1))+powerSeconds+warmUpSeconds);
                            stageSecondsToGo = restSeconds-stageCurrentSecond;

                            if(stageSecondsToGo <= 1) {

                                if(currentRep < totalReps){
                                    allowRepToIncrease = true;
                                    powerDone =false;
                                    restDone=false;
                                }
                                else{
                                    restDone=true;
                                }
                            }
                        }
                        else{
                            if(currentRep == totalReps && restDone && !coolDownDone){
                                stage = getString(R.string.coolDownText);
                                showColours();
                                showCurrentRep();
                                stageSecondsToGo = workoutSeconds - currentSecond;

                                if(currentSecond>=workoutSeconds)
                                    coolDownDone=true;
                            }
                        }
                    }
                }

                int displaySeconds = stageSecondsToGo;
                tvStage.setText(stage);
                tvCountDown.setText(Integer.toString(displaySeconds));
            }

            public void onFinish() {
                saveWorkoutToDB();
                showStatusData();
                zeroTheScreen();
            }
        };
        countDownTimer.start();
    }

    //todo: zero screen on reurn from preferences
    private void showCurrentRep() {
        int totalReps = Integer.parseInt(GetPrefString("pref_reps"));

        String repToShow = String.valueOf(currentRep);
        if(currentRep==0)repToShow="-";
        if(currentRep==totalReps && restDone)repToShow="-";

        String text = getString(R.string.repText) + " " + repToShow + " / " + totalReps;
        tvRep.setText(text);
    }

    private void setStageColour(String backgroundColour, String textColour) {
        rlHitTimer.setBackgroundColor(Color.parseColor(backgroundColour));
        tvStage.setTextColor(Color.parseColor(textColour));
        tvCountDown.setTextColor(Color.parseColor(textColour));
        tvRep.setTextColor(Color.parseColor(textColour));
        tvAthleteName.setTextColor(Color.parseColor(textColour));
    }

    private void saveWorkoutToDB() {
        dbh.saveNewWorkout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hit__timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                showSettings();
                return true;
            case R.id.action_about:
                showInfo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showInfo() {
        String msg = "High Intensity Interval Training (HIIT) is an enhanced form of interval training, an exercise strategy " +
                "alternating periods of short intense anaerobic exercise with less-intense recovery periods.\r\n" +
                "This app defaults to the Timmons Regimen as covers by BBC Horizon in Feb 2012.  " +
                "This program involves approximately 21 minutes of exercise a week.\r\n" +
                "http://www.gpnotebook.co.uk/simplepage.cfm?ID=x20130328210031685340" +
                "http://www.medicalnewstoday.com/articles/242498.php";
        showPopupMessage("High Intensity Interval Training",msg);
    }

    private void showPopupMessage(String subject, String message) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(subject);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void showSettings() {
//        Intent intent = new Intent(this, SettingsActivity.class);
//        startActivity(intent);
        Intent i = new Intent(this, Prefs.class);
        startActivity(i);
    }

    public void btnStartTimer_Click(View view) {

        if(btnStartTimer.getText()==getString(R.string.startTimerButtonText)) {
            btnStartTimer.setText(getString(R.string.cancelTimerButtonText));
            doWorkout();
        }
        else{
            countDownTimer.cancel();
            isRunning=false;
            btnStartTimer.setText(R.string.startTimerButtonText);
            zeroTheScreen();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        PollFish.init(this, "d196dcd2-c1c9-48bd-908b-b371cc3bcd89", Position.TOP_LEFT, 50);
    }
}
