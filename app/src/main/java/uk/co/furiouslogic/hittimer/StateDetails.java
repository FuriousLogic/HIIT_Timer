package uk.co.furiouslogic.hittimer;

/**
 * Created by Barry on 04/03/2015.
 */
public class StateDetails {
    private final int _secondsWarmUp;
    private final int _secondsPower;
    private final int _secondsRest;
    private final int _secondsCoolDown;
    private final int _repsCount;
    private Integer _totalSecondsInWorkout;
    private Integer _currentRep;

    //Workout colours
    final String bWarmUp = "#ffd699";
    final String tWarmUp = "#000000";
    final String bOn = "#4c0000";
    final String tOn = "#ff6666";
    final String bRest = "#d6eb99";
    final String tRest = "#000000";
    final String bCoolDown = "#d1f0ff";
    final String tCoolDown = "#000000";
    final String bFinished = "#ffffff";
    final String tFinished = "#000000";

    //Properties
    private int _stageSecond;
    private String _backgroundColour;
    private String _foregroundColour;
    private String _stageName;

    public StateDetails(int secondsWarmUp, int secondsPower, int secondsRest, int secondsCoolDown, int repsCount) {

        //Store Details
        _secondsWarmUp = secondsWarmUp;
        _secondsPower = secondsPower;
        _secondsRest = secondsRest;
        _secondsCoolDown = secondsCoolDown;
        _repsCount = repsCount;

        //Get total workout length
        _totalSecondsInWorkout = secondsWarmUp;
        _totalSecondsInWorkout += secondsPower * repsCount;
        _totalSecondsInWorkout += secondsRest * (repsCount - 1);
        _totalSecondsInWorkout += secondsCoolDown;

    }

    public int TotalSecondsInWorkout() {
        return _totalSecondsInWorkout;
    }

    public void WorkoutSecondsGone(Integer workoutSecondsGone, boolean isRunning) {
        _currentRep = 0;
        if (!isRunning) {
            _stageSecond = 0;
            _backgroundColour = bFinished;
            _foregroundColour = tFinished;
            _stageName = "Inactivity";
            return;
        }

        if (workoutSecondsGone < _secondsWarmUp) {
            _stageSecond = _secondsWarmUp - workoutSecondsGone;
            _backgroundColour = bWarmUp;
            _foregroundColour = tWarmUp;
            _stageName = "Warm Up";
        } else {
            int secondTracker = _secondsWarmUp;
            while (_currentRep <= _repsCount) {
                _currentRep++;

                secondTracker += _secondsPower;
                if (workoutSecondsGone < secondTracker) {
                    if (!_stageName.equals("On")) SoundsSingleton.playStartSound();

                    _stageSecond = secondTracker - workoutSecondsGone;
                    _backgroundColour = bOn;
                    _foregroundColour = tOn;
                    _stageName = "On";
                    return;
                }

                if (_currentRep >= _repsCount) {
                    break;
                }
                secondTracker += _secondsRest;
                if (workoutSecondsGone < secondTracker) {
                    if (_stageName.equals("On")) SoundsSingleton.playStopSound();

                    _stageSecond = secondTracker - workoutSecondsGone;
                    _backgroundColour = bRest;
                    _foregroundColour = tRest;
                    _stageName = "Rest";
                    return;
                }

            }
            SoundsSingleton.playStopSound();
            secondTracker += _secondsCoolDown;
            _currentRep = 0;
            _stageSecond = secondTracker - workoutSecondsGone;
            _backgroundColour = bCoolDown;
            _foregroundColour = tCoolDown;
            _stageName = "Cool Down";
        }
    }

    public String StageName() {
        return _stageName;
    }

    public int StageSecond() {
        return _stageSecond;
    }

    public String BackgroundColour() {
        return _backgroundColour;
    }

    public String ForegroundColour() {
        return _foregroundColour;
    }

    public int RepsCount() {
        return _repsCount;
    }

    public Integer CurrentRep() {
        return _currentRep;
    }
}
