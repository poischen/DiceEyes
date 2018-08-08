package acb.diceeyes.AlarmController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import acb.diceeyes.Storage;

/**
 * Created by anita_000 on 08.08.2018.
 */

public class ResetReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {
    Storage storage = new Storage(context);
        storage.setAllPhotosWereTaken(false);
        storage.resetMissedPeriodsCounter();
    }
}