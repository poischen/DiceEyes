package acb.diceeyes.AlarmController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import acb.diceeyes.R;
import acb.diceeyes.Storage;

/*
Receives alarms to initiate taking a photo
 */
public class PhotoAlarmReceiver extends BroadcastReceiver {

    private static final String TAG = PhotoAlarmReceiver.class.getSimpleName();

    public PhotoAlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "Photo Alarm received");
        int periodForCapture = (int) intent.getExtras().get(String.valueOf(R.string.extra_period));
        ObservableObject.getInstance().setPeriod(periodForCapture);
        ObservableObject.getInstance().updateValue(intent);
    }
}
