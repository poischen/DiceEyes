package acb.diceeyes.AlarmController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import acb.diceeyes.R;
import acb.diceeyes.Storage;

//TODO: ANPASSEN!
public class AlarmReceiver extends BroadcastReceiver {
    private boolean[] wasRescheduled = new boolean[6];
    private long[] startTime = new long[6];
    private int[] rescheduleCounter = new int[] {0, 0, 0, 0, 0, 0};
    private int shiftMillisScreenOff = 20000;
    private int shiftMillisPicIsCurrentlyTaken = 5000;

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    public AlarmReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        int requestID = (int) intent.getExtras().get(String.valueOf(R.string.extra_requestid));

        Storage storage = new Storage(context);

        boolean wasAlreadyTaken = storage.getRandomWasTakenInCurrentPeriod(requestID);
        if (!wasAlreadyTaken){
            ObservableObject.getInstance().setReminderPeriod(requestID);
            ObservableObject.getInstance().updateValue(intent);
        }

    }
}
