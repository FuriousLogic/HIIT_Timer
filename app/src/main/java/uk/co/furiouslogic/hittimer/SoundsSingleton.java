package uk.co.furiouslogic.hittimer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

/**
 * Created by Barry on 27/02/2015.
 */
public class SoundsSingleton {
    private static SoundPool beepPool;
    private static int beepLowId;
    private static int beepHighId;

    public SoundsSingleton(){}


    public static void Initialise(Context context) {
        //Define sounds
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes aa = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_UNKNOWN)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build();

            beepPool = new SoundPool.Builder()
                    .setMaxStreams(10)
                    .setAudioAttributes(aa)
                    .build();
            beepLowId = beepPool.load(context, R.raw.beep, 1);
            beepHighId = beepPool.load(context, R.raw.beephigh, 1);
        }else{
            beepPool = new SoundPool(10, AudioManager.STREAM_ALARM, 1);
            beepLowId = beepPool.load(context, R.raw.beep, 1);
            beepHighId = beepPool.load(context, R.raw.beephigh, 1);
        }


    }

    public static void playStartSound() {
        //beepPool.play(beepHighId, 1, 1, 1, 0, 1);
    }

    public static void playStopSound() {
        //beepPool.play(beepLowId, 1, 1, 1, 0, 1);
    }
}
