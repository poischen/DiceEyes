package acb.diceeyes.AlarmController;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by anita_000 on 31.07.2018.
 */

public class ScreenOnOffReceiver extends BroadcastReceiver {
    private static final String TAG = ScreenOnOffReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()){
            case Intent.ACTION_SCREEN_ON:
                Log.v(TAG, "Screen is on now.");
                break;
            case Intent.ACTION_SCREEN_OFF:
                Log.v(TAG, "Screen is off now.");
                ObservableObject.getInstance().setScreenOn(false);
                break;
            case Intent.ACTION_BOOT_COMPLETED:
                //restarts the controller service after device reboot
                Log.v(TAG, "restart controller service after reboot");
                Intent controllerIntent = new Intent(context, ControllerService.class);
                context.startService(controllerIntent);
                break;
            default:
        }
        ObservableObject.getInstance().updateValue(intent);
    }
}
