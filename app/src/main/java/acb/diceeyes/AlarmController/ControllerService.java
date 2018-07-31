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
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Observable;
import java.util.Observer;

import acb.diceeyes.Collection.CapturePhotoService;
import acb.diceeyes.Collection.DataCollectionService;
import acb.diceeyes.R;
import acb.diceeyes.Storage;

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
            //TODO werden die benötig? dann als Feld
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
            final IntentFilter onOffFilter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            onOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(systemEventReceiver, onOffFilter);

            IntentFilter eventFilter = new IntentFilter("acb.diceeyes.AlarmControll.ScreenOnOffReceiver");
            systemEventReceiver = new ScreenOnOffReceiver();
            registerReceiver(systemEventReceiver, eventFilter);

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
        Every time the screen is switched on, it will be checked if there are missing shots of a period via the counter and additional random but unique alarms between 1-15 seconds will be set;
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

        //TODO: stop pending alarms for triggering photos

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
            if (action.contains("android.intent.action.SCREEN_OFF")) {
                Log.v(TAG, "screen was turned off, cancel future alarms");
                isScreenActive = false;
                handleScreenOff();
            }
            //else if (action.contains("EventAlarmReceiver")) {
              //  startCapturePictureService();
                //Log.v(TAG, "event has not changed, capture pic again");
            //remove pending intent from list
            //ObservableObject.getInstance().getPendingIntentRequestID();

//            int size = eventPendingIntentArray.size();
  //          if ( size > 0) {
    //            for (int i = 0; i < size; i++) {
      //              if (eventPendingIntentArray.get(i).get)
        //        }
          //  }

          //  }
            else if (action.contains("PhotoAlarmReceiver")) {
                Log.v(TAG, "Alarm triggers capturing photo");
                if (isScreenActive) {
                    if (startCapturePictureService()){
                        int period = (ObservableObject.getInstance().getReminderPeriod());
                        if (period > 0){
                            storage.setPhotoWasTakenInCurrentPeriod(period, true);
                            ObservableObject.getInstance().setReminderPeriod(0);
                        }
                    }

                }
            } else if (action.contains("android.intent.action.SCREEN_ON")) {
                startDataCollectionService(DataCollectorService.DCSCOMMANDREGISTER, getApplicationContext(), null, null, null, null, null, null, null, null, null, null);
                isScreenActive = true;

                appDetectionThread = new Thread(runnableAppDetector);
                appDetectionThread.start();

                this.capturingEvent = CapturingEvent.SCREENON;
                Log.v(TAG, "Event detected, capturingEvent set to: " + this.capturingEvent);
                initPictureTakingSession(capturingEvent);

                //see if a random picture with survey has to be taken in current period
                isRandomAlreadyTaken();
            } else if (action.contains("android.intent.action.CONFIGURATION_CHANGED")) {
                if (ObservableObject.getInstance().isOrientationPortrait() != lastDetectedOrientationPortrait) {
                    lastDetectedOrientationPortrait = ObservableObject.getInstance().isOrientationPortrait();
                    this.capturingEvent = CapturingEvent.ORIENTATION;
                    Log.v(TAG, "Event detected, capturingEvent set to: " + this.capturingEvent);
                    initPictureTakingSession(capturingEvent);
                }
            }
        }

    }



    private void handleScreenOff(){
        cancelFutureAlarms();
        startDataCollectionService(DataCollectorService.DCSCOMMANDUNREGISTER, getApplicationContext(), null, null, null, null, null, null, null, null, null, null);
        this.capturingEvent = STOP;
        try {
            appDetectionThread.stop();
        } catch (Exception e){
            Log.v(TAG, "appDetectionThread not interrupted, is alive: " + appDetectionThread.isAlive());
        }
    }

    private void initPictureTakingSession(CapturingEvent capturingEvent) {
        Log.v(TAG, "initPictureTakingSession() is called");

        //try to take the first picture immediately
        startCapturePictureService(capturingEvent);

        //check and cancel future taking picture alarms
        cancelFutureAlarms();


        //final int tIteration1 = 3500;
        //final int tIteration2 = 12500;
        //final int tIteration3 = 15000;
        final int tIteration1 = 2;
        final int tIteration2 = 15;
        final int tIteration3 = 30;

        //set alarms for taking pictures for the incoming event
        long currentTimeMillis = System.currentTimeMillis();


        int requestId = 80;

        Log.v(TAG, "event in initPictureTakingSession() before setting alarms: "+ capturingEvent.toString());
        //set one alarm for the events SCREENON, NOTIFICATION, APPLICATION & ORIENTATION
        if (capturingEvent.equals(CapturingEvent.SCREENON) || capturingEvent.equals(CapturingEvent.NOTIFICATION) ||
                capturingEvent.equals(CapturingEvent.APPLICATION) || capturingEvent.equals(CapturingEvent.ORIENTATION)){
            Log.v(TAG, "set alarm.");
            setEventAlarm(tIteration1, requestId, currentTimeMillis);
            requestId++;

            //set two more alarms for the event APPLICATION
            if (capturingEvent.equals(CapturingEvent.APPLICATION)){
                setEventAlarm(tIteration2, requestId, currentTimeMillis);
                requestId++;
                setEventAlarm(tIteration3, requestId, currentTimeMillis);
                requestId++;
            }
            //calendar = null;
        }
    }


    private void setEventAlarm(int iteration, int requestId, long currentTimeMillis){

        //set alarms for taking pictures for the incoming event
        Intent intent = new Intent(this, EventAlarmReceiver.class);
        intent.putExtra(REQUESTID, requestId);
        intent.setAction("com.example.anita.hdyhyp.EventAlarmReceiver");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), requestId, intent, PendingIntent.FLAG_ONE_SHOT);
        eventPendingIntentArray.add(pendingIntent);

        //new Time
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTimeInMillis(currentTimeMillis);
        Calendar calendar = (Calendar) currentTime.clone();
        calendar.set(Calendar.SECOND, calendar.get(Calendar.SECOND) + iteration);

        //alarmManager.setExact(AlarmManager.RTC, (currentTimeMillis + (iteration)), pendingIntent);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
        Log.v(TAG, "alarm set in " + iteration + " seconds.");
    }

    private void cancelFutureAlarms(){
        int randomSize = randomPendingIntentArray.size();
        if ( randomSize > 0){
            for (int i=0; i<randomSize; i++){
                try {
                    alarmManager.cancel(randomPendingIntentArray.get(i));
                } catch (Exception e){
                    Log.d(TAG, "Random alarm was not found in Array");
                }
            }
            randomPendingIntentArray.clear();
            currentRandomPendingIntent = null;
        }


        int eventSize = eventPendingIntentArray.size();
        if ( eventSize > 0){
            for (int i=0; i<eventSize; i++){
                try {
                    alarmManager.cancel(eventPendingIntentArray.get(i));
                } catch (Exception e){
                    Log.d(TAG, "Event alarm was not found in Array");
                }
            }
            eventPendingIntentArray.clear();
        }
    }

    private boolean startCapturePictureService() {
        //start taking the picture
        // the CapurePicService will run the DataCollection when it is finished taking the pic

        Intent capturePicServiceIntent = new Intent(this, CapturePhotoService.class);
        capturePicServiceIntent.putExtra(String.valueOf(R.string.extra_capturingevent), capturingEvent);
        getApplicationContext().startService(capturePicServiceIntent);
        Log.v(TAG, "CapturePicService will be started now");
        return true;

}
}
