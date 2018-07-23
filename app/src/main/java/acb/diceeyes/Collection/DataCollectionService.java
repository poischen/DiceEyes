package acb.diceeyes.Collection;

/**
 * Created by anita_000 on 23.07.2018.
 */

public class DataCollectionService  extends Service implements LocationListener, SensorEventListener {

    private static final String TAG = DataCollectorService.class.getSimpleName();
    public static final String NASTRING = "n./a.";
    public static final int NAINT = -11;
    public static final String DCSCOMMAND = "command";
    public static final String DCSCOMMANDREGISTER = "register";
    public static final String DCSCOMMANDUNREGISTER = "unregister";
    public static final String DCSCOMMANDCOLLECT = "collect";
    public static final String CAPTURINGEVENT = "capturingEvent";
    public static final String FOREGROUNDAPP = "foregroundApp";
    public static final String PICTURENAME = "pictureName";
    public static final String ORIENTATION = "orientation";
    public static final String FDLEFTEYE = "faceDetectionLeftEye";
    public static final String FDLEFTEYEOPEN = "faceDetectionLeftEyeOpen";
    public static final String FDRIGHTEYE = "faceDetectionRightEye";
    public static final String FDRIGHTEYEOPEN = "faceDetectionRightEyeOpen";
    public static final String FDMOUTH = "faceDetectionMouth";
    public static final String FDEULERY = "faceDetectionMouthY";
    public static final String FDEULERZ = "faceDetectionMouthZ";
    public static final String PORTAIT = "portrait";
    public static final String LANDSCAPE = "landscape";
    public static final String REQUESTID = "requestID";
    public static final String PATH = "path";
    public static final String SUBMITTIME = "submitTime";

    private SQLiteDatabase database;
    private Storage storage = ControllerService.storage;
    private SensorManager sensorManager;

    private LocationManager locationManager;
    private Location latestLocation;

    String accelerometerSensor = NASTRING;
    String gyroscopeSensor = NASTRING;
    String lightSensor = NASTRING;
    String orientationSensor = NASTRING;

    String photoName = NASTRING; //CapturePictureService
    String foregroundApp = NASTRING; //ControllerService
    String proximity = NASTRING; //DataColletorService - SensorListener
    int locationLatitude = NAINT; //DataColletorService - locationManager
    int locationLongitude = NAINT; //DataColletorService - locationManager
    String locationRoad = NASTRING; //DataColletorService - locationManager
    String locationPLZ = NASTRING; //DataColletorService - locationManager
    //String accelerometerX = NASTRING; //DataColletorService - SensorListener
    //String accelerometerY = NASTRING; //DataColletorService - SensorListener
    //String accelerometerZ = NASTRING; //DataColletorService - SensorListener
    LinkedList<String> accelerometerX = new LinkedList<>();
    LinkedList<String> accelerometerY = new LinkedList<>();
    LinkedList<String> accelerometerZ = new LinkedList<>();
    String gyroscopeX = NASTRING; //DataColletorService - Sensor
    String gyroscopeY = NASTRING; //DataColletorService - Sensor
    String gyroscopeZ = NASTRING; //DataColletorService - Sensor
    String orientation = NASTRING; //from EventBroadcastReceiver
    String ambientLight = NASTRING; //DataColletorService - SensorListener
    int screenBrightness = NAINT;  //DataColletorService - read directly
    String batteryStatus = NASTRING; //DataColletorService - read directly
    int batteryLevel = NAINT; //DataColletorService - read directly
    String faceDetectionLeftEye = NASTRING; //CapturePictureService
    String faceDetectionLeftEyeOpen = NASTRING; //CapturePictureService
    String faceDetectionRightEye = NASTRING; //CapturePictureService
    String faceDetectionRightEyeOpen = NASTRING; //CapturePictureService
    String faceDetectionMouth = NASTRING; //CapturePictureService
    String faceDetectionEulerY = NASTRING; //CapturePictureService
    String faceDetectionEulerZ = NASTRING; //CapturePictureService
    ControllerService.CapturingEvent capturingEvent = INIT; //ControllerService

    private int requestID = 790;

    public DataCollectorService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        Log.v(TAG, "starting data collection");

        //register necessary listener---------------------------------------------------------------
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        //locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        registerListener();

        Log.v(TAG, "listener registered.");
    }

    public void registerListener() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        //sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_FASTEST);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

    }

    public void unregisterListener() {
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        //sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
        accelerometerX.clear();
        accelerometerY.clear();
        accelerometerZ.clear();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String command = "";
        try {
            Log.v(TAG, "intent " + intent);
            Log.v(TAG, "intent extras " + intent.getExtras());
            command = (String) intent.getExtras().get(DataCollectorService.DCSCOMMAND);
            if (command.equals(DCSCOMMANDCOLLECT)) {
                capturingEvent = (ControllerService.CapturingEvent) intent.getExtras().get(CAPTURINGEVENT);
            }
        } catch (Exception e) {
            Log.v(TAG, "onStartCommand intent null");
        }

        switch (command) {
            case DCSCOMMANDREGISTER:
                registerListener();
                break;
            case DCSCOMMANDUNREGISTER:
                unregisterListener();
                break;
            case DCSCOMMANDCOLLECT:
                ContentValues cv = new ContentValues();
                if (capturingEvent != null) {
                    if (capturingEvent.equals(INIT)) {
                        //read general sensor information and store to db---------------------------------------
                        try {
                            photoName = (String) intent.getExtras().get(PICTURENAME);
                        } catch (NullPointerException e) {
                        }
                        cv.put(Storage.COLUMN_PHOTO, photoName);
                        Log.v(TAG, "photoName" + photoName);

                        cv.put(Storage.COLUMN_CAPTUREEVENT, capturingEvent.toString());
                        Log.v(TAG, "captureEvent: " + capturingEvent);

                        cv.put(Storage.COLUMN_FOREGROUNDAPP, foregroundApp);
                        Log.v(TAG, "foregroundApp: " + foregroundApp);

                        cv.put(Storage.COLUMN_LOCATIONLATITUDE, locationLatitude);
                        cv.put(Storage.COLUMN_LOCATIONLONGITUDE, locationLongitude);
                        cv.put(Storage.COLUMN_LOCATIONROAD, locationRoad);
                        cv.put(Storage.COLUMN_LOCATIONPOSTALCODE, locationPLZ);
                        Log.v(TAG, "location: latitude: " + locationLatitude + ", longitude: " + locationLongitude + ", road: " + locationRoad + ", postalcode: " + locationPLZ);

                        try {
                            accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).toString();
                        } catch (Exception e) {
                            Log.d(TAG, "no accelerometerSensor found");
                        }

                        cv.put(Storage.COLUMN_ACCELEROMETERX, accelerometerSensor);
                        cv.put(Storage.COLUMN_ACCELEROMETERY, NASTRING);
                        cv.put(Storage.COLUMN_ACCELEROMETERZ, NASTRING);
                        Log.v(TAG, "accelerometer Sensor:" + accelerometerSensor);

                        try {
                            gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE).toString();
                        } catch (Exception e) {
                            Log.d(TAG, "no gyroscopeSensor found");
                        }
                        cv.put(Storage.COLUMN_GYROSCOPEX, gyroscopeSensor);
                        cv.put(Storage.COLUMN_GYROSCOPEY, gyroscopeY);
                        cv.put(Storage.COLUMN_GYROSCOPEZ, gyroscopeZ);
                        Log.v(TAG, "gyroscope Sensor:" + gyroscopeSensor);

                        try {
                            lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).toString();
                        } catch (Exception e) {
                            Log.d(TAG, "no lightSensor found");
                        }
                        cv.put(Storage.COLUMN_LIGHT, lightSensor);
                        Log.v(TAG, "light Sensor:" + lightSensor);


                        int minScreenBrightness = 0;
                        int maxScreenBrightness = 255;
                        final Resources resources = Resources.getSystem();

                        int idMin = resources.getIdentifier("config_screenBrightnessSettingMinimum", "integer", "android");
                        if (idMin != 0) {
                            try {
                                minScreenBrightness = resources.getInteger(idMin);
                            } catch (Resources.NotFoundException e) {
                            }
                        }

                        int idMax = resources.getIdentifier("config_screenBrightnessSettingMaximum", "integer", "android");
                        if (idMax != 0) {
                            try {
                                maxScreenBrightness = resources.getInteger(idMax);
                            } catch (Resources.NotFoundException e) {
                            }
                        }

                        String screenBrightnessString = minScreenBrightness + "0" + maxScreenBrightness;
                        screenBrightness = Integer.parseInt(screenBrightnessString);

                        cv.put(Storage.COLUMN_BRIGHTNESS, screenBrightness);
                        Log.v(TAG, "screenBrightness: " + screenBrightness);

                        try {
                            orientationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION).toString();
                        } catch (Exception e) {
                            Log.d(TAG, "no orientationSensor found");
                        }
                        cv.put(Storage.COLUMN_ORIENTATION, orientationSensor);
                        Log.v(TAG, "orientation Sensor:" + orientationSensor);

            /*String proximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY).toString();
            cv.put(Storage.COLUMN_PROXIMITY, proximity);
            Log.v(TAG, "proximity Sensor:" + proximity);*/

                        cv.put(Storage.COLUMN_BATTERYSTATUS, batteryStatus);
                        Log.v(TAG, "batteryStatus: " + batteryStatus);

                        cv.put(Storage.COLUMN_BATTERYLEVEL, batteryLevel);
                        Log.v(TAG, "batteryLevel: " + batteryLevel);

                        cv.put(Storage.COLUMN_LEFT, faceDetectionLeftEye);
                        Log.v(TAG, "faceDetectionLeftEye: " + faceDetectionLeftEye);

                        cv.put(Storage.COLUMN_LEFTOPEN, faceDetectionLeftEyeOpen);
                        Log.v(TAG, "faceDetectionLeftEyeOpen: " + faceDetectionLeftEyeOpen);

                        cv.put(Storage.COLUMN_RIGHT, faceDetectionRightEye);
                        Log.v(TAG, "faceDetectionRightEye: " + faceDetectionRightEye);

                        cv.put(Storage.COLUMN_RIGHTOPEN, faceDetectionRightEyeOpen);
                        Log.v(TAG, "faceDetectionRightEyeOpen: " + faceDetectionRightEyeOpen);

                        cv.put(Storage.COLUMN_MOUTH, faceDetectionMouth);
                        Log.v(TAG, "faceDetectionMouth: " + faceDetectionMouth);

                        cv.put(Storage.COLUMN_EULERY, faceDetectionEulerY);
                        Log.v(TAG, "faceDetectionEulerY: " + faceDetectionEulerY);

                        cv.put(Storage.COLUMN_EULERZ, faceDetectionEulerZ);
                        Log.v(TAG, "faceDetectionEulerZ: " + faceDetectionEulerZ);

                        Log.v(TAG, "VALUES " + cv.toString());
                    } else {
                        //Read and store current data-----------------------------------------------------------
                        //picture name--------------------------------------------------------------------------
                        try {
                            photoName = (String) intent.getExtras().get(PICTURENAME);
                        } catch (NullPointerException e) {
                            Log.v(TAG, "photoName was null");
                        }
                        cv.put(Storage.COLUMN_PHOTO, photoName);
                        Log.v(TAG, "photoName" + photoName);

                        //capture Event-------------------------------------------------------------------------
                        cv.put(Storage.COLUMN_CAPTUREEVENT, capturingEvent.toString());
                        Log.v(TAG, "captureEvent: " + capturingEvent);

                        //foreground App------------------------------------------------------------------------
                        try {
                            foregroundApp = (String) intent.getExtras().get(FOREGROUNDAPP);
                        } catch (NullPointerException e) {
                        }
                        cv.put(Storage.COLUMN_FOREGROUNDAPP, foregroundApp);
                        Log.v(TAG, "foregroundApp: " + foregroundApp);

                        //location------------------------------------------------------------------------------
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        }

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                2000, 1, this);

                        latestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        if (latestLocation != null) {
                            Log.v(TAG, "latest location: " + latestLocation);
                            Geocoder geocoder = new Geocoder(this);
                            double locaLat = latestLocation.getLatitude();
                            double locLong = latestLocation.getLongitude();

                            locationLatitude = (int) (locaLat*1000000);
                            locationLongitude = (int) (locLong*10000000);
                            try {
                                List<Address> addressList = null;
                                addressList = geocoder.getFromLocation(locaLat, locLong, 1);
                                Address address = addressList.get(0);
                                locationRoad = address.getThoroughfare();
                                locationPLZ = address.getPostalCode();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            latestLocation = null;
                        }

                        locationManager.removeUpdates(this);

                        cv.put(Storage.COLUMN_LOCATIONLATITUDE, locationLatitude);
                        cv.put(Storage.COLUMN_LOCATIONLONGITUDE, locationLongitude);
                        cv.put(Storage.COLUMN_LOCATIONROAD, locationRoad);
                        cv.put(Storage.COLUMN_LOCATIONPOSTALCODE, locationPLZ);
                        Log.v(TAG, "location: latitude: " + locationLatitude + ", longitude: " + locationLongitude + ", road: " + locationRoad + ", postalcode: " + locationPLZ);

                        //accelerometer-------------------------------------------------------------------------
                        cv.put(Storage.COLUMN_ACCELEROMETERX, accelerometerX.toString());
                        cv.put(Storage.COLUMN_ACCELEROMETERY, accelerometerY.toString());
                        cv.put(Storage.COLUMN_ACCELEROMETERZ, accelerometerZ.toString());
                        Log.v(TAG, "accelerometer: " + accelerometerX + " " + accelerometerY + " " + accelerometerZ);

                        //rotation------------------------------------------------------------------------------
                        cv.put(Storage.COLUMN_GYROSCOPEX, gyroscopeX);
                        cv.put(Storage.COLUMN_GYROSCOPEY, gyroscopeY);
                        cv.put(Storage.COLUMN_GYROSCOPEZ, gyroscopeZ);
                        Log.v(TAG, "gyroscope: " + gyroscopeX + " " + gyroscopeY + " " + gyroscopeZ);

                        //light---------------------------------------------------------------------------------
                        cv.put(Storage.COLUMN_LIGHT, ambientLight);
                        Log.v(TAG, "light:" + ambientLight);

                        //screen brightness---------------------------------------------------------------------
                        try {
                            screenBrightness = android.provider.Settings.System.getInt(
                                    getContentResolver(),
                                    android.provider.Settings.System.SCREEN_BRIGHTNESS);
                        } catch (Settings.SettingNotFoundException e) {
                            e.printStackTrace();
                        }
                        cv.put(Storage.COLUMN_BRIGHTNESS, screenBrightness);
                        Log.v(TAG, "screen brightness: " + screenBrightness);

                        //orientation---------------------------------------------------------------------------
                        try {
                            orientation = (String) intent.getExtras().get(ORIENTATION);
                        } catch (NullPointerException e) {

                        }
                        cv.put(Storage.COLUMN_ORIENTATION, orientation);
                        Log.v(TAG, "orientation Sensor:" + orientation);

                        //proximity-----------------------------------------------------------------------------
                        //cv.put(Storage.COLUMN_PROXIMITY, proximity);
                        //Log.v(TAG, "proximity Sensor:" + proximity);

                        //battery level & status----------------------------------------------------------------
                        if (Build.VERSION.SDK_INT >= 23) {
                            BatteryManager batteryManager = (BatteryManager) getSystemService(BATTERY_SERVICE);
                            batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
                            if (batteryManager.isCharging()) {
                                batteryStatus = "charging";
                            } else {
                                batteryStatus = "nothing";
                            }
                        }
                        //not used due to increased api level
                /*else {
                        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                        Intent batteryIntent = registerReceiver(null, filter);

                        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                        int status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);

                        float batteryPct = level / (float) scale;
                        batteryLevel = (int) (batteryPct * 100);

                        if (status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == BatteryManager.BATTERY_STATUS_FULL) {
                            batteryStatus = "charging";
                        } else {
                            batteryStatus = "not charging";
                        }
                    }*/

                        cv.put(Storage.COLUMN_BATTERYSTATUS, batteryStatus);
                        Log.v(TAG, "batteryStatus: " + batteryStatus);

                        cv.put(Storage.COLUMN_BATTERYLEVEL, batteryLevel);
                        Log.v(TAG, "batteryLevel: " + batteryLevel);

                        //face detection------------------------------------------------------------------------
                        try {
                            faceDetectionLeftEye = (String) intent.getExtras().get(FDLEFTEYE);
                        } catch (NullPointerException e) {
                        }
                        try {
                            faceDetectionRightEye = (String) intent.getExtras().get(FDRIGHTEYE);
                        } catch (NullPointerException e) {
                        }
                        try {
                            faceDetectionMouth = (String) intent.getExtras().get(FDMOUTH);
                        } catch (NullPointerException e) {
                        }
                        try {
                            faceDetectionLeftEyeOpen = (String) intent.getExtras().get(FDLEFTEYEOPEN);
                        } catch (NullPointerException e) {
                        }
                        try {
                            faceDetectionRightEyeOpen = (String) intent.getExtras().get(FDRIGHTEYEOPEN);
                        } catch (NullPointerException e) {
                        }
                        try {
                            faceDetectionEulerY = (String) intent.getExtras().get(FDEULERY);
                        } catch (NullPointerException e) {
                        }
                        try {
                            faceDetectionEulerZ = (String) intent.getExtras().get(FDEULERZ);
                        } catch (NullPointerException e) {
                        }

                        cv.put(Storage.COLUMN_LEFT, faceDetectionLeftEye);
                        cv.put(Storage.COLUMN_LEFTOPEN, faceDetectionLeftEyeOpen);
                        cv.put(Storage.COLUMN_RIGHT, faceDetectionRightEye);
                        cv.put(Storage.COLUMN_RIGHTOPEN, faceDetectionRightEyeOpen);
                        cv.put(Storage.COLUMN_MOUTH, faceDetectionMouth);
                        cv.put(Storage.COLUMN_EULERY, faceDetectionEulerY);
                        cv.put(Storage.COLUMN_EULERZ, faceDetectionEulerZ);
                        Log.v(TAG, "face detection: left: " + faceDetectionLeftEye + ", right: " + faceDetectionRightEye + ", mouth: " + faceDetectionMouth+ ", left open: " + faceDetectionLeftEyeOpen + ", right open: " + faceDetectionRightEyeOpen + ", euler y: " + faceDetectionEulerZ + ", euler z: " + faceDetectionEulerZ);
                    }


                    //write data to database--------------------------------------------------------------------
                    storage = new Storage(getApplicationContext());
                    database = storage.getWritableDatabase();
                    long insertId = database.insert(Storage.DB_TABLE, null, cv);
                    Log.v(TAG, "data stored to db");
                    database.close();

                    //create survey
                    if (capturingEvent.equals(RANDOM)) {
                        Log.v(TAG, "create survey");
                        Intent surveyIntent = new Intent(getApplication().getApplicationContext(), SurveyActivity.class);
                        surveyIntent.putExtra(REQUESTID, requestID);
                        requestID = requestID + 1;
                        surveyIntent.putExtra(PICTURENAME, photoName);
                        surveyIntent.putExtra(PATH, storage.getStoragePath());
                        surveyIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(surveyIntent);
                    }

                    //reset values not depending from sensor listener
                    photoName = foregroundApp = locationRoad = locationPLZ = orientation = batteryStatus = faceDetectionLeftEye = faceDetectionRightEye = faceDetectionMouth = NASTRING;
                    locationLatitude = locationLongitude = screenBrightness = NAINT;
                    capturingEvent = INIT;
                }
                break;
            default:
                Log.d(TAG, "no valid command for DataCollectorService");
        }

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        /*if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            proximity = String.valueOf(event.values[0]);
            Log.v(TAG, "new proximity: " + proximity);
        }*/
        switch (event.sensor.getType()){
            case (Sensor.TYPE_LIGHT):
                ambientLight = String.valueOf(event.values[0]);
                break;
            case (Sensor.TYPE_LINEAR_ACCELERATION):
                if (accelerometerX.size() > 0){
                    accelerometerX.removeLast();
                }
                accelerometerX.addFirst(String.valueOf(event.values[0]));

                if (accelerometerY.size() > 0){
                    accelerometerY.removeLast();
                }
                accelerometerY.addFirst(String.valueOf(event.values[1]));

                if (accelerometerZ.size() > 0){
                    accelerometerZ.removeLast();
                }
                accelerometerZ.addFirst(String.valueOf(event.values[2]));
                break;
            case (Sensor.TYPE_GYROSCOPE):
                gyroscopeX = String.valueOf(event.values[0]);
                gyroscopeY = String.valueOf(event.values[1]);
                gyroscopeZ = String.valueOf(event.values[2]);
                break;
            case (Sensor.TYPE_ORIENTATION):
                gyroscopeX = String.valueOf(event.values[0]);
                gyroscopeY = String.valueOf(event.values[1]);
                gyroscopeZ = String.valueOf(event.values[2]);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }


    public void onDestroy() {
        unregisterListener();
    }