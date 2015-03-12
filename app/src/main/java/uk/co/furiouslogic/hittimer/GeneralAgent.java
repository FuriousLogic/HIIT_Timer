package uk.co.furiouslogic.hittimer;

import android.content.Context;

/**
 * Created by Barry on 12/03/2015.
 */
public class GeneralAgent {
    public static void ProcessError(Exception e) {
        DbHandlerSingleton.SaveToLog(e.getMessage());
    }
}
