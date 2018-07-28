package acb.diceeyes.AlarmControll;

/*
Controlls when and how often photos are captures and holds all necessary information
it is realized as a foreground service so it won't be killed by Android and the participant has feedback, that the Service is still running
*/

import android.app.AlarmManager;

import acb.diceeyes.Storage;

//public class ControlleService extends Service implements Observer {
public class ControlleService {

    private static final String TAG = ControlleService.class.getSimpleName();

    private AlarmManager alarmManager;
    private AlarmReceiver alarmReceiver;
    public static Storage storage;
    private String storagePath;

    public ControlleService() {
        super();
    }

  /*  @Override
    public void onCreate() {
        super.onCreate();
        Log.v(TAG, "ControlleService created.");
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //Starting the Shooting after inizialising the user name in order to test if everything works
            //Bundle extras = intent.getExtras();
            storage = new Storage(getApplicationContext());
            storagePath = storage.getStoragePath();

            setRandomPictureAndDatatransferAlarms();

            // Notification about starting the controller service foreground according to the design guidelines
            Notification notification = new Notification.Builder(getApplicationContext())
                    .setContentTitle(String.valueOf(R.string.notifications_title))
                    .setContentText(String.valueOf(R.string.notifications_generic))
                    .setSmallIcon(R.drawable.dice)
                    .build();
            startForeground(getResources().getInteger(R.integer.notification_id_service_running), notification);

            IntentFilter alarmFilter = new IntentFilter("com.example.anita.diceeyes.AlarmReceiver");
            alarmReceiver = new AlarmReceiver();
            registerReceiver(alarmReceiver, alarmFilter);

        return START_STICKY;
    }
*/
    //TODO: Überarbeiten nach neuer Logik
    /*
   sets 6 random daily alarms between 10 am and 22 pm, where pictures should be taken
   sets a daily alarm to remind transfering data
    */
    private void setRandomPictureAndDatatransferAlarms() {

        /*
        set random alarms
        version 1 - most user did not receive enough surveys
        version 2 - every time the sceen is switched on, it will be checked if a survey has already taken place in the current intervall
         */
        /*IntentFilter filter = new IntentFilter("com.example.anita.hdyhyp.RandomAlarmReceiver");
        randomAlarmReceiver = new RandomAlarmReceiver();
        registerReceiver(randomAlarmReceiver, filter);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        //calculate and set random alarms
        //ArrayList<PendingIntent> eventPendingIntentArray = new ArrayList<PendingIntent>();
        int[] alarmHour = new int[6];
        int[] alarmMinutes = new int[6];
        //alarm between 10 am and 22 pm in intervals of 2 hours
        int hourCounter = 10;
        for (int i = 0; i <= 5; i++) {
            alarmHour[i] = hourCounter + ((int) (Math.random() * 2));
            alarmMinutes[i] = (int) (Math.random() * 59) + 1;
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(System.currentTimeMillis());
            c.set(Calendar.HOUR_OF_DAY, alarmHour[i]);
            c.set(Calendar.MINUTE, alarmMinutes[i]);
            c.set(Calendar.SECOND, 0);
            alarm[i] = c;
            Log.v(TAG, "Alarm No. " + i + ": " + alarmHour[i] + ":" + alarmMinutes[i]);
            hourCounter = hourCounter + 2;

            Intent intent = new Intent(this, RandomAlarmReceiver.class);
            intent.putExtra("requestID", i);
            intent.putExtra("time", c.getTimeInMillis());
            intent.setAction("com.example.anita.hdyhyp.RandomAlarmReceiver");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), i, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            randomPendingIntentArray.add(pendingIntent);
            alarmManager.setRepeating(AlarmManager.RTC, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }*/

        //TODO: AB heir überarbeiten
/*        //set data transfer alarm
        IntentFilter reminderFilter = new IntentFilter("com.example.anita.hdyhyp.ReminderAlarmReceiver");
        reminderAlarmReceiver = new ReminderAlarmReceiver();
        registerReceiver(reminderAlarmReceiver, reminderFilter);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.set(Calendar.HOUR_OF_DAY, 22);
        c.set(Calendar.MINUTE, 1);
        c.set(Calendar.SECOND, 0);

        Intent intent = new Intent(this, ReminderAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 70, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.setInexactRepeating(AlarmManager.RTC, c.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    //TODO: Überarbeiten bzw Daten vbereitstellen
    public static void startDataCollectionService(String command, Context context, String foregroundApp, CapturingEvent capturingEvent, String pictureName, String faceDetectionLeftEye, String faceDetectionRightEye, String faceDetectionMouth, String eulerY, String eulerZ, String rightEyeOpen, String leftEyeOpen) {
        Intent dataCollectionIntent = new Intent(context, DataCollectorService.class);
        dataCollectionIntent.putExtra(DataCollectorService.DCSCOMMAND, command);
        if (command.equals("collect")){
            dataCollectionIntent.putExtra(DataCollectorService.FOREGROUNDAPP, foregroundApp);
            dataCollectionIntent.putExtra(DataCollectorService.CAPTURINGEVENT, capturingEvent);
            dataCollectionIntent.putExtra(DataCollectorService.PICTURENAME, pictureName);
            if (ObservableObject.getInstance().isOrientationPortrait()) {
                dataCollectionIntent.putExtra(DataCollectorService.ORIENTATION, DataCollectorService.PORTAIT);
            } else {
                dataCollectionIntent.putExtra(DataCollectorService.ORIENTATION, DataCollectorService.LANDSCAPE);
            }
            dataCollectionIntent.putExtra(DataCollectorService.FDLEFTEYE, faceDetectionLeftEye);
            dataCollectionIntent.putExtra(DataCollectorService.FDRIGHTEYE, faceDetectionRightEye);
            dataCollectionIntent.putExtra(DataCollectorService.FDMOUTH, faceDetectionMouth);
            dataCollectionIntent.putExtra(DataCollectorService.FDLEFTEYEOPEN, leftEyeOpen);
            dataCollectionIntent.putExtra(DataCollectorService.FDRIGHTEYEOPEN, rightEyeOpen);
            dataCollectionIntent.putExtra(DataCollectorService.FDEULERY, eulerY);
            dataCollectionIntent.putExtra(DataCollectorService.FDEULERZ, eulerZ);
        }
        context.startService(dataCollectionIntent);
    }

    //TODO: Überarbeiten
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy()");
        //cancel receiver
        if (broadcastReceiver != null) {
            try {
                unregisterReceiver(broadcastReceiver);
                broadcastReceiver = null;
                Log.v(TAG, "broadcastReceiver unregistered");
            } catch (Exception e){
                Log.d(TAG, "broadcastReceiver could not be unregistered");
            }

        }
        if (reminderAlarmReceiver != null) {
            try {
                unregisterReceiver(reminderAlarmReceiver);
                reminderAlarmReceiver = null;
                Log.v(TAG, "reminderAlarmReceiver unregistered");
            } catch (Exception e){
                Log.d(TAG, "reminderAlarmReceiver could not be unregistered");
            }

        }
        if (eventAlarmReceiver != null) {
            try {
                unregisterReceiver(eventAlarmReceiver);
                eventAlarmReceiver = null;
                Log.v(TAG, "eventAlarmReceiver unregistered");
            } catch (Exception e){
                Log.d(TAG, "eventAlarmReceiver could not be unregistered");
            }
        }

        //stop DataCollectorService
        Intent intent = new Intent(getApplicationContext(), DataCollectorService.class);
        stopService(intent);
        Log.v(TAG, "DataCollectorService stopped");

        //cancel data transfer reminder
        PendingIntent reminderPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 70, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmManager.cancel(reminderPendingIntent);
        Log.v(TAG, "data transfer reminder canceled");

        //cancel alarms of capturing session
        cancelFutureAlarms();
        Log.v(TAG, "future capture session alarms canceled");

        //cancel random alarms
        // not necessary since new algorithm

        isScreenActive = false;

        //stop Controller Service
        stopSelf();
    }

    //TODO: Überarbeiten
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
            } else if (action.contains("EventAlarmReceiver")) {
                startCapturePictureService(capturingEvent);
                Log.v(TAG, "event has not changed, capture pic again");
            remove pending intent from list
            ObservableObject.getInstance().getPendingIntentRequestID();

            int size = eventPendingIntentArray.size();
            if ( size > 0) {
                for (int i = 0; i < size; i++) {
                    if (eventPendingIntentArray.get(i).get)
                }
            }

            } else if (action.contains("StatusBarNotification")) {
                //check if notification is new
                if (!(lastAsNewDetectedNotification.equals(currentNotification))){
                    if (isScreenActive){
                        capturingEvent = CapturingEvent.NOTIFICATION;
                        lastAsNewDetectedNotification = currentNotification;
                        Log.v(TAG, "Notification " + currentNotification + " detected, capturingEvent set to: " + this.capturingEvent);
                        initPictureTakingSession(capturingEvent);
                    }
                }
            } else if (action.contains("RandomAlarmReceiver")) {
                this.capturingEvent = CapturingEvent.RANDOM;
                Log.v(TAG, "Event detected, capturingEvent set to: " + this.capturingEvent);
                if (isScreenActive) {
                    cancelFutureAlarms();

                    if (startCapturePictureService(capturingEvent)){
                        int period = (ObservableObject.getInstance().getReminderPeriod());

                        if (period > 0){
                            storage.setRandomWasTakenInCurrentPeriod(period, true);
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

    private boolean startCapturePictureService(CapturingEvent capturingEvent) {
        //start taking the picture, if there is not already being taken one. the CapurePicService will run the DataCollection when it
        // is finnished taking the pic

        //if (!pictureIsCurrentlyTaken){
        Intent capturePicServiceIntent = new Intent(this, CapturePicService.class);
        capturePicServiceIntent.putExtra("storagePath", storagePath);
        capturePicServiceIntent.putExtra("userName", userName);
        capturePicServiceIntent.putExtra("foregroundApp", currentForegroundApp);
        capturePicServiceIntent.putExtra(DataCollectorService.CAPTURINGEVENT, capturingEvent);
        getApplicationContext().startService(capturePicServiceIntent);
        Log.v(TAG, "CapturePicService will be started now");
        return true;
        //}
        //collect and write data to emphasize that a picture of the session is missing
        /* else {
            Log.v(TAG, "CapturePicService will not be started, because another picture is currently taken");
            if (!capturingEvent.equals(RANDOM)){
                DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH:mm:ss");
                String timeString = dateFormat.format(new Date());
                String s = NOPICSTRING + timeString;
                startDataCollectionService("collect", getApplicationContext(), currentForegroundApp, capturingEvent, s, NASTRING, NASTRING, NASTRING, NASTRING, NASTRING, NASTRING, NASTRING);
            }
            return false;
        }
    }*/

}}
