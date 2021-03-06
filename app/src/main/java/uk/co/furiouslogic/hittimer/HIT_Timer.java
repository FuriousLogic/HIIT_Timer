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

import java.util.Date;

//import com.google.android.gms.games.internal.constants.TimeSpan;

//todo: Make beeps distinct - make beeps myself (Ruby's keyboard?)
//todo: track workouts over a week
//todo: notifications on workouts to do this week

public class HIT_Timer extends ActionBarActivity {
    private boolean isRunning = false;
    private boolean _lastWorkoutMessageAlreadyShown = false;

    //Instance State Value Names
    final String workoutSecondsGoneKey = "workoutSecondsGoneKey";
    final String isRunningKey = "isRunningKey";
    final String _lastWorkoutMessageAlreadyShownKey = "_lastWorkoutMessageAlreadyShown";

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
        try {
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
        } catch (Exception e) {
            processError(e);
        }
    }

    private void showState() {
        try {
            DbHandlerSingleton.SaveToLog("showState");
            if (!isRunning) {
                workoutSecondsGone = 0;
                btnStartTimer.setText(R.string.Start_Timer);
            } else
                btnStartTimer.setText(R.string.Cancel);

            _stateDetails = getStageDetailsClass();

            _stateDetails.WorkoutSecondsGone(workoutSecondsGone, isRunning);

            //Paint Screen
            String repText = String.valueOf(_stateDetails.CurrentRep()) + " / " + String.valueOf(_stateDetails.RepsCount());
            tvStage.setText(_stateDetails.StageName());
            tvCountDown.setText((String.valueOf(_stateDetails.StageSecond())));
            tvRep.setText(repText);
            setStageColour(_stateDetails.BackgroundColour(), _stateDetails.ForegroundColour());
        } catch (Exception e) {
            processError(e);
        }
    }

    @Override
    public void onStart() {
        try {
            DbHandlerSingleton.SaveToLog("onStart");
            super.onStart();
            showAthleteNameAndWorkoutCount();
        } catch (Exception e) {
            processError(e);
        }
    }

    @Override
    public void onResume() {
        try {
            DbHandlerSingleton.SaveToLog("onResume");
            super.onResume();
            showState();
            PollFish.init(this, "d196dcd2-c1c9-48bd-908b-b371cc3bcd89", Position.TOP_LEFT, 50);

            //Last Workout Message
            if (!_lastWorkoutMessageAlreadyShown) {

                _lastWorkoutMessageAlreadyShown = true;

                //Time since last workout
                Date now = new Date(System.currentTimeMillis());
                Date lastWorkout = DbHandlerSingleton.getDateOfLastWorkout();
                String timeSinceLastWorkoutMessage;
                if (lastWorkout != null) {
                    long diffInMs = now.getTime() - lastWorkout.getTime();
                    long seconds = diffInMs / 1000;
                    seconds /= 60;
                    long minutesLeft = seconds % 60;
                    seconds /= 60;
                    long hoursLeft = seconds % 24;
                    seconds /= 24;
                    long daysLeft = seconds;
                    timeSinceLastWorkoutMessage = getString(R.string.Time_Since_Last_Workout) + ":\r\n";
                    if (daysLeft > 0)
                        timeSinceLastWorkoutMessage += daysLeft + " " + getString(R.string.days) + ", ";
                    timeSinceLastWorkoutMessage += hoursLeft + " " + getString(R.string.hours) + ", ";
                    timeSinceLastWorkoutMessage += minutesLeft + " " + getString(R.string.minutes);
                } else {
                    timeSinceLastWorkoutMessage = getString(R.string.No_workouts_completed);
                }

                showPopupMessage(getString(R.string.Last_Workout), timeSinceLastWorkoutMessage);
            }
        } catch (Exception e) {
            processError(e);
        }
    }

    @Override
    public void onStop() {
        try {
            DbHandlerSingleton.SaveToLog("onStop");
            super.onStop();
        } catch (Exception e) {
            processError(e);
        }
    }

    @Override
    public void onDestroy() {
        try {
            DbHandlerSingleton.SaveToLog("onDestroy");
            super.onDestroy();
            isRunning = false;
            if (workout != null) {
                workout.cancel(true);
            }
        } catch (Exception e) {
            processError(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        try {
            DbHandlerSingleton.SaveToLog("onCreateOptionsMenu");
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_hit__timer, menu);
            return true;
        } catch (Exception e) {
            processError(e);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        try {
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
        } catch (Exception e) {
            processError(e);
        }
        return true;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        try {
            DbHandlerSingleton.SaveToLog("onSaveInstanceState");
            super.onSaveInstanceState(savedInstanceState);

            savedInstanceState.putInt(workoutSecondsGoneKey, workoutSecondsGone);
            savedInstanceState.putBoolean(isRunningKey, isRunning);
            savedInstanceState.putBoolean(_lastWorkoutMessageAlreadyShownKey, _lastWorkoutMessageAlreadyShown);

            if (isRunning)
                workout.cancel(true);
        } catch (Exception e) {
            processError(e);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        try {
            DbHandlerSingleton.SaveToLog("onRestoreInstanceState");
            super.onRestoreInstanceState(savedInstanceState);

            //Get data from state
            _stateDetails = getStageDetailsClass();
            workoutSecondsGone = savedInstanceState.getInt(workoutSecondsGoneKey);
            isRunning = savedInstanceState.getBoolean(isRunningKey);
            _lastWorkoutMessageAlreadyShown = savedInstanceState.getBoolean(_lastWorkoutMessageAlreadyShownKey);

            //Zero the state
            savedInstanceState.clear();

            //Do we need to restart the timer?
            if (!isRunning) {
                return;
            }

            int totalSecondsInWorkout = _stateDetails.TotalSecondsInWorkout();
            workout = (Workout) new Workout().execute(workoutSecondsGone, totalSecondsInWorkout);

        } catch (Exception e) {
            processError(e);
        }
    }

    private void processError(Exception e) {
        GeneralAgent.ProcessError(e);
        showPopupMessage(getString(R.string.Error), e.getMessage());
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
        String msg = getString(R.string.Show_Info_1) +
                getString(R.string.Show_Info_2) + "\r\n" +
                getString(R.string.Show_Info_3);
        showPopupMessage(getString(R.string.High_Intensity_Interval_Training), msg);
    }

    private void showPopupMessage(String subject, String message) {
        AlertDialog.Builder dlgAlert = new AlertDialog.Builder(this);
        dlgAlert.setMessage(message);
        dlgAlert.setTitle(subject);
        dlgAlert.setPositiveButton(getString(R.string.OK), null);
        dlgAlert.setCancelable(true);
        dlgAlert.create().show();
    }

    private void showSettings() {
        Intent i = new Intent(this, Prefs.class);
        startActivity(i);
    }

    public void btnStartTimer_Click(View view) {

        try {
            if (btnStartTimer.getText() == getString(R.string.Start_Timer)) {
                isRunning = true;
                _stateDetails = getStageDetailsClass();

                workout = (Workout) new Workout().execute(0, _stateDetails.TotalSecondsInWorkout());
            } else {
                if (workout != null) workout.cancel(true);
                isRunning = false;
                showState();
            }
        } catch (Exception e) {
            processError(e);
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
        return new StateDetails(getApplicationContext(), secondsWarmUp, secondsPower, secondsRest, secondsCoolDown, repsCount);

    }

    public void displayWorkoutState(Integer secondsGone) {
        try {
            workoutSecondsGone = secondsGone;
            showState();
        } catch (Exception e) {
            processError(e);
        }
    }

    public void workoutIsFinished() {
        try {
            isRunning = false;
            DbHandlerSingleton.saveNewWorkout();
            tvCountDown.setText(getString(R.string.YAY));
        } catch (Exception e) {
            processError(e);
        }
    }

    private class Workout extends AsyncTask<Integer, Integer, Boolean> {
//        HIT_Timer _context;

        @Override
        protected Boolean doInBackground(Integer... params) {
            DbHandlerSingleton.SaveToLog("doInBackground");
            int currentSecond = params[0];
            int finalSecond = params[1];
            long initialMillis = System.currentTimeMillis();

            while (currentSecond <= finalSecond) {
                long currentMillis = System.currentTimeMillis();

                //Have we moved on a second yet?
                if (initialMillis + ((currentSecond + 1) * 1000) <= currentMillis) {
                    if (isCancelled()) {
                        return null;
                    }
                    publishProgress(currentSecond);
                    currentSecond++;
                }
            }

            return true;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
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

