package acb.diceeyes.AlarmController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by anita_000 on 31.07.2018.
 */

public class EventBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = EventBroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case Intent.ACTION_SCREEN_ON:
                Log.v(TAG, "Screen is on now.");
                break;
            case Intent.ACTION_SCREEN_OFF:
                Log.v(TAG, "Screen is off now.");
                //TODO: ALARME canceln ObservableObject.getInstance().setIsScreenOn(false);
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                //restarts the controller service after device reboot, which also sets new random alarms (which would be removed after device restart)
                Log.v(TAG, "restart controller service after reboot");
                Intent controllerIntent = new Intent(context, ControllerService.class);
                context.startService(controllerIntent);
                break;
            default:
        }
        ObservableObject.getInstance().updateValue(intent);
    }
}
