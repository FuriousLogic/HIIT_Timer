package uk.co.furiouslogic.hittimer;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdView;
import com.pollfish.constants.Position;
import com.pollfish.main.PollFish;

//import com.google.android.gms.games.internal.constants.TimeSpan;

//todo: Make beeps distinct - make beeps myself (Ruby's keyboard?)
//todo: track workouts over a week
//todo: notifications on workouts to do this week

public class HIT_Timer extends ActionBarActivity {
    //Workout flags
    private Boolean warmUpDone = false;
    private Boolean powerDone = false;
    private Boolean restDone = false;
    private Boolean coolDownDone = false;
    private int currentRep = 0;
    private int currentSecondsToGo = 0;
    private boolean allowRepToIncrease = false;
    private boolean isRunning = false;

    //Instance State Value Names
    final String workoutSecondsGoneKey = "workoutSecondsGoneKey";
    final String isRunningKey = "isRunningKey";

    //Controls
    private RelativeLayout rlHitTimer;
    private TextView tvStage;
    private TextView tvCountDown;
    private TextView tvRep;
    private TextView tvAthleteName;
    private Button btnStartTimer;

    //Properties
    private Integer workoutSecondsGone = 0;
    private Workout workout;
    private StateDetails _stateDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DbHandlerSingleton.SaveToLog("onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hit__timer);

        //Admob
        AdView avTimerBanner = (AdView) findViewById(R.id.avTimerBanner);
        avTimerBanner.loadAd(AdMobSingleton.GetAdRequest());

        //Get Controls
        rlHitTimer = (RelativeLayout) findViewById(R.id.rlHitTimer);
        tvStage = (TextView) findViewById(R.id.tvStage);
        tvCountDown = (TextView) findViewById(R.id.tvCountDown);
        tvRep = (TextView) findViewById(R.id.tvRep);
        tvAthleteName = (TextView) findViewById(R.id.tvAthleteName);
        btnStartTimer = (Button) findViewById(R.id.btnStartTimer);

        //Time since last workout
//        Date now = new Date(System.currentTimeMillis());
//        Date lastWorkout = dbh.getDateOfLastWorkout();
//        String timeSinceLastWorkoutMessage;
//        if(lastWorkout != null) {
//            long diffInMs = now.getTime() - lastWorkout.getTime();
//            long seconds = diffInMs / 1000;
//            seconds /= 60;
//            long minutesLeft = seconds % 60;
//            seconds /= 60;
//            long hoursLeft = seconds % 24;
//            seconds /= 24;
//            long daysLeft = seconds;
//            timeSinceLastWorkoutMessage = "Time Since Last Workout: \r\n";
//            if (daysLeft > 0) timeSinceLastWorkoutMessage += daysLeft + " days, ";
//            timeSinceLastWorkoutMessage += hoursLeft + " hours, ";
//            timeSinceLastWorkoutMessage += minutesLeft + " minutes";
//        } else{
//            timeSinceLastWorkoutMessage = "No workouts completed";
//        }
//
//        showPopupMessage("Last Workout", timeSinceLastWorkoutMessage);
    }

    private void showState() {
        DbHandlerSingleton.SaveToLog("showState");
        if (!isRunning) workoutSecondsGone = 0;
        _stateDetails = getStageDetailsClass();

        DbHandlerSingleton.SaveToLog("showState: _stateDetails.WorkoutSecondsGone(" + workoutSecondsGone + ");");
        _stateDetails.WorkoutSecondsGone(workoutSecondsGone, isRunning);

        //Paint Screen
        String repText = String.valueOf(_stateDetails.CurrentRep()) + " / " + String.valueOf(_stateDetails.RepsCount());
        tvStage.setText(_stateDetails.StageName());
        tvCountDown.setText((String.valueOf(_stateDetails.StageSecond())));
        tvRep.setText(repText);
        setStageColour(_stateDetails.BackgroundColour(), _stateDetails.ForegroundColour());
    }

    @Override
    public void onStart() {
        DbHandlerSingleton.SaveToLog("onStart");
        super.onStart();
        showAthleteNameAndWorkoutCount();
    }

    @Override
    public void onResume() {
        DbHandlerSingleton.SaveToLog("onResume");
        super.onResume();
        showState();
        PollFish.init(this, "d196dcd2-c1c9-48bd-908b-b371cc3bcd89", Position.TOP_LEFT, 50);
    }

    @Override
    public void onStop() {
        DbHandlerSingleton.SaveToLog("onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        DbHandlerSingleton.SaveToLog("onDestroy");
        super.onDestroy();
        isRunning = false;
        workout.cancel(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        DbHandlerSingleton.SaveToLog("onCreateOptionsMenu");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_hit__timer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        DbHandlerSingleton.SaveToLog("onOptionsItemSelected");
        int id = item.getItemId();

        switch (id) {
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

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        DbHandlerSingleton.SaveToLog("onSaveInstanceState");
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(workoutSecondsGoneKey, workoutSecondsGone);
        savedInstanceState.putBoolean(isRunningKey, isRunning);

        if (isRunning)
            workout.cancel(true);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        DbHandlerSingleton.SaveToLog("onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);

        //Get data from state
        workoutSecondsGone = savedInstanceState.getInt(workoutSecondsGoneKey);
        isRunning = savedInstanceState.getBoolean(isRunningKey);

        //Zero the state
        savedInstanceState.clear();

        //Do we need to restart the timer?
        if (!isRunning) {
            return;
        }
        _stateDetails = getStageDetailsClass();
        workout = (Workout) new Workout().execute(workoutSecondsGone, _stateDetails.TotalSecondsInWorkout());
    }

    private void showAthleteNameAndWorkoutCount() {
        int workoutCount = DbHandlerSingleton.getWorkoutCount();

        String statusData = PreferenceSingleton.GetPrefString("pref_AthleteName");
        tvAthleteName.setText(statusData + " (" + workoutCount + ")");
    }


    private void setStageColour(String backgroundColour, String textColour) {
        rlHitTimer.setBackgroundColor(Color.parseColor(backgroundColour));
        tvStage.setTextColor(Color.parseColor(textColour));
        tvCountDown.setTextColor(Color.parseColor(textColour));
        tvRep.setTextColor(Color.parseColor(textColour));
        tvAthleteName.setTextColor(Color.parseColor(textColour));
    }

//    private void saveWorkoutToDB() {
//        DbHandlerSingleton.saveNewWorkout();
//    }

    private void showInfo() {
        String msg = "High Intensity Interval Training (HIIT) is an enhanced form of interval training, an exercise strategy " +
                "alternating periods of short intense anaerobic exercise with less-intense recovery periods.\r\n" +
                "This app defaults to the Timmons Regimen as covers by BBC Horizon in Feb 2012.  " +
                "This program involves approximately 21 minutes of exercise a week.\r\n" +
                "http://www.gpnotebook.co.uk/simplepage.cfm?ID=x20130328210031685340" +
                "http://www.medicalnewstoday.com/articles/242498.php";
        showPopupMessage("High Intensity Interval Training", msg);
    }

    private void showPopupMessage(String subject, String message) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(subject);
        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void showSettings() {
        Intent i = new Intent(this, Prefs.class);
        startActivity(i);
    }

    public void btnStartTimer_Click(View view) {

        if (btnStartTimer.getText() == getString(R.string.startTimerButtonText)) {
            btnStartTimer.setText(getString(R.string.cancelTimerButtonText));
            isRunning = true;
            _stateDetails = getStageDetailsClass();

            workout = (Workout) new Workout().execute(0, _stateDetails.TotalSecondsInWorkout());
        } else {
            if (workout != null) workout.cancel(true);
            isRunning = false;
            btnStartTimer.setText(R.string.startTimerButtonText);
            showState();
        }
    }

    private StateDetails getStageDetailsClass() {

        if (isRunning) return _stateDetails;

        //Get prefs
        String sWarmUp = PreferenceSingleton.GetPrefString("pref_warmup");
        String sPower = PreferenceSingleton.GetPrefString("pref_power");
        String sRest = PreferenceSingleton.GetPrefString("pref_rest");
        String sCoolDown = PreferenceSingleton.GetPrefString("pref_cooldown");
        String sReps = PreferenceSingleton.GetPrefString("pref_reps");
        int secondsWarmUp = Integer.parseInt(sWarmUp);
        int secondsPower = Integer.parseInt(sPower);
        int secondsRest = Integer.parseInt(sRest);
        int secondsCoolDown = Integer.parseInt(sCoolDown);
        int repsCount = Integer.parseInt(sReps);
        StateDetails sd = new StateDetails(secondsWarmUp, secondsPower, secondsRest, secondsCoolDown, repsCount);
        return sd;

    }

    public void displayWorkoutState(Integer secondsGone) {
        workoutSecondsGone = secondsGone;
        showState();
    }

    public void workoutIsFinished() {
        isRunning = false;
        tvCountDown.setText("YAY");
    }

    private class Workout extends AsyncTask<Integer, Integer, Boolean> {
//        HIT_Timer _context;

        @Override
        protected Boolean doInBackground(Integer... params) {
            int startSecond = params[0];
            int currentSecond = startSecond;
            int finalSecond = params[1];
            long initialMillis = System.currentTimeMillis();

            while (currentSecond <= finalSecond) {
                long currentMillis = System.currentTimeMillis();

                //Have we moved on a second yet?
                if (initialMillis + ((currentSecond + 1) * 1000) <= currentMillis) {
                    if (isCancelled()) {
                        return null;
                    }
                    DbHandlerSingleton.SaveToLog("doInBackground. publishProgress(" + currentSecond + ") ");
                    publishProgress(currentSecond);
                    currentSecond++;
                }
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            DbHandlerSingleton.SaveToLog("onProgressUpdate: displayWorkoutState(" + values[0] + ");");
            displayWorkoutState(values[0]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            SoundsSingleton.playStopSound();
            workoutIsFinished();
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
    }
}

