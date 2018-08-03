package acb.diceeyes.AlarmController;

/*
Controlls when and how often photos are captures and holds all necessary information
it is realized as a foreground service so it won't be killed by Android and the participant has feedback, that the Service is still running
*/

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import acb.diceeyes.Collection.CapturePhotoService;
import acb.diceeyes.Collection.DataCollectionService;
import acb.diceeyes.R;
import acb.diceeyes.Storage;
import acb.diceeyes.View.GazeGrid;

public class ControllerService extends Service implements Observer {

    private static final String TAG = ControllerService.class.getSimpleName();

    public static Storage storage;
    private String capturingEvent = String.valueOf(R.string.extra_capturingevent_normal);

    private boolean firstTrySuccessfullyFlag;
    private boolean isScreenActive = true;

    private AlarmManager alarmManager;
    private ScreenOnOffReceiver systemEventReceiver;
    private TransferReminderAlarmReceiver transferReminderAlarmReceiver;
    private PhotoAlarmReceiver photoAlarmReceiver;

    private ArrayList<PendingIntent> pendingIntentArray = new ArrayList<PendingIntent>();
    private int requestIdCounter = 3;

    public ControllerService() {
        super();
    }

   @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "ControllerService created.");
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            storage = new Storage(getApplicationContext());
            capturingEvent = String.valueOf(R.string.extra_capturingevent_init);
            startCapturePictureService();
            capturingEvent = String.valueOf(R.string.extra_capturingevent_normal);
            String storagePath = storage.getStoragePath();
            String userAlias = storage.getAlias();
            firstTrySuccessfullyFlag = true;

        } catch (Exception e) {
            Log.v(TAG, "CapturePicService could not be started by the Controller.");
            firstTrySuccessfullyFlag = false;
        }

        if (firstTrySuccessfullyFlag) {
            ObservableObject.getInstance().addObserver(this);
            setPhotoAndTransferAlarms();

            //Register Broadcast Receiver for listening if the screen is on/off & if system was rebooted
            systemEventReceiver = new ScreenOnOffReceiver();
            IntentFilter onOffFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            onOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(systemEventReceiver, onOffFilter);

            // Notification about starting the controller service foreground according to the design guidelines
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(String.valueOf(R.string.notifications_title))
                    .setContentText(String.valueOf(R.string.notifications_generic))
                    .setSmallIcon(R.drawable.dice)
                    .build();
            startForeground(getResources().getInteger(R.integer.notification_id_service_running), notification);

        } else {
            Toast.makeText(this, "An error occured.", Toast.LENGTH_LONG).show();
            stopSelf();
        }
        return START_STICKY;
    }

    /*
   sets 20 random daily alarms between start and end time for taking photos (4 for each gaze point)
   sets a daily alarm to remind transferring data
    */
    private void setPhotoAndTransferAlarms() {
        /*
        set random alarms:
        Every time the screen is switched on, it will be checked if a photo has already been taken in the current period (30 minutes, 20 periods);
        If not a alarm will be set randomly between 1-15 seconds;
        Every taken picture increases a persistently stored counter;
        Every time the screen is switched on and it is not necessary to capture a photo in this perios, it will be checked if there are missing shots of a period via the counter and additional random alarms between 1-15 seconds will be set;
        If the screen is switched off all alarms will be canceled (cancelFutureAlarms())
         */
        IntentFilter filter = new IntentFilter("acb.diceeyes.AlarmControll.PhotoAlarmReceiver");
        photoAlarmReceiver = new PhotoAlarmReceiver();
        registerReceiver(photoAlarmReceiver, filter);
      /*      Intent intent = new Intent(this, RandomAlarmReceiver.class);
            intent.putExtra("requestID", i);
            intent.putExtra("time", c.getTimeInMillis());
            intent.setAction("com.example.anita.hdyhyp.RandomAlarmReceiver");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            randomPendingIntentArray.add(pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
       }*/

   //set data transfer alarm
        IntentFilter dataTransferReminderFilter = new IntentFilter("acb.diceeyes.AlarmControll.TransferReminderAlarmReceiver");
        transferReminderAlarmReceiver = new TransferReminderAlarmReceiver();
        registerReceiver(transferReminderAlarmReceiver, dataTransferReminderFilter);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 21);
        c.set(Calendar.MINUTE, 1);
        c.set(Calendar.SECOND, 0);

        Intent transferReminderIntent = new Intent(this, TransferReminderAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), getResources().getInteger(R.integer.id_intent_requestcode_reminderdatatransfer), transferReminderIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");
        //cancel receiver
        if (systemEventReceiver != null) {
            try {
                unregisterReceiver(systemEventReceiver);
                systemEventReceiver = null;
                Log.v(TAG, "systemEventReceiver unregistered");
            } catch (Exception e){
                Log.d(TAG, "systemEventReceiver could not be unregistered");
            }

        }
        if (transferReminderAlarmReceiver != null) {
            try {
                unregisterReceiver(transferReminderAlarmReceiver);
                transferReminderAlarmReceiver = null;
                Log.v(TAG, "reminderAlarmReceiver unregistered");
            } catch (Exception e){
                Log.d(TAG, "reminderAlarmReceiver could not be unregistered");
            }

        }
        if (photoAlarmReceiver != null) {
            try {
                unregisterReceiver(photoAlarmReceiver);
                photoAlarmReceiver = null;
                Log.v(TAG, "eventAlarmReceiver unregistered");
            } catch (Exception e){
                Log.d(TAG, "eventAlarmReceiver could not be unregistered");
            }
        }

        //stop DataCollectorService
        Intent intent = new Intent(getApplicationContext(), DataCollectionService.class);
        stopService(intent);
        Log.v(TAG, "DataCollectorService stopped");

        //cancel data transfer reminder
        PendingIntent reminderPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), getResources().getInteger(R.integer.id_intent_requestcode_reminderdatatransfer), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(reminderPendingIntent);
        Log.v(TAG, "data transfer reminder canceled");

        isScreenActive = false;
        //stop Controller Service
        stopSelf();
    }

    @Override
    public void update(Observable observable, Object data) {
        String action = "empty";
        try {
            action = data.toString();
        } catch (Exception e) {
            Log.d(TAG, "could not get action; " + e);
        }

        Log.v(TAG, "update action: " + action);
         if (!(action.equals("empty"))) {
             if (action.contains("android.intent.action.SCREEN_ON")) {
                 isScreenActive = true;

                 //check if in relevant time
                 Calendar currentTime = Calendar.getInstance();
                 currentTime.setTimeInMillis(System.currentTimeMillis());
                 int hour = currentTime.get(Calendar.HOUR);

                 if (hour > 7 && hour < 21){
                     //check if it is necessary to take a picture in this period and set alarms if so
                     int period = calculatePeriod();
                     if (!wasAlreadyTakenInPeriod(period) && hour < 20){
                         setGridAlarm(period);
                     } else {
                         //check if there were missed periods and set alarm
                         int missedAlarms = checkMissedPeriods(calculatePeriod());
                         if (missedAlarms > 0){
                             setGridAlarm(0);
                         }
                     }
                 } else {
                     //reset counter at night
                     requestIdCounter = 3;
                 }
             }
             else if (action.contains("android.intent.action.SCREEN_OFF")) {
                Log.v(TAG, "screen was turned off, cancel future alarms");
                isScreenActive = false;
                handleScreenOff();
            }
            else if (action.contains("PhotoAlarmReceiver")) {
                Log.v(TAG, "Photo Alarm triggers capturing photo");
                if (isScreenActive) {
                    //start GazeGrid
                    Intent intent = new Intent(this, GazeGrid.class);
                    intent.putExtra(String.valueOf(R.string.extra_period), ObservableObject.getInstance().getPeriod());
                    intent.putExtra(String.valueOf(GazeGrid.GAZEPOINTPOSITION), storage.getGazePoint());
                    startActivity(intent);
                }
            }
        }
    }

    private boolean wasAlreadyTakenInPeriod(int period){
        return storage.getPhotoWasTakenInCurrentPeriod(period);
    }

    private int checkMissedPeriods(int period){
        int periodsSoll;
        switch (period){
            case 10:
                periodsSoll = 1;
                break;
            case 105:
                periodsSoll = 2;
                break;
            case 11:
                periodsSoll = 3;
                break;
            case 115:
                periodsSoll = 4;
                break;
            case 12:
                periodsSoll = 5;
                break;
            case 125:
                periodsSoll = 6;
                break;
            case 13:
                periodsSoll = 7;
                break;
            case 135:
                periodsSoll = 8;
                break;
            case 14:
                periodsSoll = 9;
                break;
            case 145:
                periodsSoll = 10;
                break;
            case 15:
                periodsSoll = 11;
                break;
            case 155:
                periodsSoll = 12;
                break;
            case 16:
                periodsSoll = 13;
                break;
            case 165:
                periodsSoll = 14;
                break;
            case 17:
                periodsSoll = 15;
                break;
            case 175:
                periodsSoll = 16;
                break;
            case 18:
                periodsSoll = 17;
                break;
            case 185:
                periodsSoll = 18;
                break;
            case 19:
                periodsSoll = 19;
                break;
            case 195:
                periodsSoll = 20;
                break;
            /*case 20:
                periodsSoll = 20;
                break;
            case 205:
                periodsSoll = 20;
                break;
            case 215:
                periodsSoll = 20;
                break;*/
            default:
                periodsSoll = 20;
        }
        return storage.getMissedPeriods(periodsSoll);
    }

    /*
       set alarms for taking a photo in randomly 1-15 seconds
     */
    private void setGridAlarm(int period){
        //tell datacollection service to register sensors
        startDataCollectionService(DataCollectionService.COMMAND_REGISTER);

        //create Intent and store in case of need to cancel the alarm
        Intent intent = new Intent(this, PhotoAlarmReceiver.class);
        intent.setAction("acb.diceeyes.AlarmController.PhotoAlarmReceiver");
        intent.putExtra(String.valueOf(R.string.extra_period), period);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestIdCounter, intent, PendingIntent.FLAG_ONE_SHOT);
        requestIdCounter++;
        pendingIntentArray.add(pendingIntent);

        //calculate random delay of 1-5 seconds
        int randomSec = (int) Math.random() * 16;
        int randomMilisec = (int) Math.random() * 1000;

        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        Calendar calendar = (Calendar) currentTime.clone();
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + randomSec);
        calendar.set(Calendar.MILLISECOND, calendar.get(Calendar.MILLISECOND) + randomMilisec);

        //set alarm
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        Log.v(TAG, "alarm set in " + randomSec + "," + randomMilisec + " seconds.");
    }

    public static int calculatePeriod() {
        int period = 0;

        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        int hour = currentTime.get(Calendar.HOUR);
        int minute = currentTime.get(Calendar.MINUTE);

        int minuteRounded;
        if (minute < 30){
            period = hour;
        } if (minute >= 30){
            String periodString = hour + "5";
            period = Integer.valueOf(periodString);
        }
        Log.v(TAG, "period: " + period);

        return period;
    }

    private void cancelFutureAlarms(){
        //TODO: loop necessary? theoretisch kann es nur ein pending intent sein
        int pendingIntentSize = pendingIntentArray.size();
        if ( pendingIntentSize > 0){
            for (int i=0; i<pendingIntentSize; i++){
                try {
                    alarmManager.cancel(pendingIntentArray.get(i));
                } catch (Exception e){
                    Log.d(TAG, "alarm was not found in Array");
                }
            }
            pendingIntentArray.clear();
        }
    }

    private boolean startCapturePictureService() {
        Intent capturePicServiceIntent = new Intent(this, CapturePhotoService.class);
        capturePicServiceIntent.putExtra(String.valueOf(R.string.extra_capturingevent), capturingEvent);
        getApplicationContext().startService(capturePicServiceIntent);
        Log.v(TAG, "CapturePicService will be started now");
        return true;
}

    private void handleScreenOff(){
        cancelFutureAlarms();
        startDataCollectionService(DataCollectionService.COMMAND_UNREGISTER);
    }

    /*
    trigger onStartCommand inDataCollectionService in order to register/unregister listeners
     */
    public void startDataCollectionService(String command) {
        Intent dataCollectionIntent = new Intent(getApplicationContext(), DataCollectionService.class);
        dataCollectionIntent.putExtra(getResources().getString(R.string.extra_datacollection_command), command);
        getApplication().startService(dataCollectionIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}