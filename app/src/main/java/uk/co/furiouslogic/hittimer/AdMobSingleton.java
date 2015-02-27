package uk.co.furiouslogic.hittimer;

import com.google.android.gms.ads.AdRequest;

/**
 * Created by Barry on 27/02/2015.
 */
public class AdMobSingleton {

    private static AdRequest _ar;

    public AdMobSingleton(){}


    public static void Initialise() {
        _ar = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("A9C350CBED46BAC089E21096D5522BE6").build();
    }


    public static AdRequest GetAdRequest() {
        return _ar;
    }
}
