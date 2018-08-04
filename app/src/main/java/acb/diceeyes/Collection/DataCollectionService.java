package acb.diceeyes.Collection;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import acb.diceeyes.R;
import acb.diceeyes.Storage;

/**
 * Created by anita_000 on 23.07.2018.
 */

public class DataCollectionService extends Service implements LocationListener, SensorEventListener {
    public static final String NASTRING = "n./a.";
    public static final int NAINT = -11;
    public static final String PICTURENAME = "pictureName";
    public static final String PICTUREVALUE = "pictureValue";
    public static final String PORTAIT = "portrait";
    public static final String LANDSCAPE = "landscape";
    public static final String GAZEPOINTPOSITION = "gazePointPosition";
    public static final String COMMAND_REGISTER = "register";
    public static final String COMMAND_UNREGISTER = "unregister";
    public static final String COMMAND_UPDATE = "update";

    private static final String TAG = DataCollectionService.class.getSimpleName();
    String accelerometerSensor = NASTRING;
    String gyroscopeSensor = NASTRING;
    String lightSensor = NASTRING;
    String rotationVectorSensor = NASTRING;
    String photoName = NASTRING;
    int locationLatitude = NAINT;
    int locationLongitude = NAINT;
    String locationRoad = NASTRING;
    String locationPLZ = NASTRING;
    LinkedList<String> accelerometerX = new LinkedList<>();
    LinkedList<String> accelerometerY = new LinkedList<>();
    LinkedList<String> accelerometerZ = new LinkedList<>();
    String gyroscopeX = NASTRING;
    String gyroscopeY = NASTRING;
    String gyroscopeZ = NASTRING;
    String orientation = NASTRING;
    String ambientLight = NASTRING;
    int screenBrightness = NAINT;
    String batteryStatus = NASTRING;
    int batteryLevel = NAINT;
    int gazePoint = NAINT;
    String pictureValidity = "";
    private SQLiteDatabase database;
    private Storage storage;
    private SensorManager sensorManager;
    private LocationManager locationManager;
    private Location latestLocation;

    public DataCollectionService() {
        super();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onCreate() {
        //register necessary listener---------------------------------------------------------------
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        registerListener();
    }

    public void registerListener() {
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_FASTEST);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR), SensorManager.SENSOR_DELAY_FASTEST);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
    }

    public void unregisterListener() {
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE));
        sensorManager.unregisterListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT));
        accelerometerX.clear();
        accelerometerY.clear();
        accelerometerZ.clear();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ContentValues cv = new ContentValues();
        String command = "";

        try {
            command = (String) intent.getExtras().get(getString(R.string.extra_datacollection_command));

            try {
                photoName = (String) intent.getExtras().get(PICTURENAME);
            } catch (NullPointerException e) {
                Log.v(TAG, "photoName was null");
            }

            Log.v(TAG, "command " + command);
            switch (command) {
                case COMMAND_UPDATE:
                    String value = (String) intent.getExtras().get(PICTUREVALUE);

                    storage = new Storage(getApplicationContext());
                    database = storage.getWritableDatabase();
                    String updateSQL = "UPDATE " + Storage.DB_TABLE + " SET " + Storage.COLUMN_VALID + " = " + value + " WHERE " + Storage.COLUMN_PHOTO + " = " + photoName;
                    database.execSQL(updateSQL);
                    Log.v(TAG, "db udpate string " + updateSQL);
                    Log.v(TAG, "validity update");
                    database.close();
                    break;
                case COMMAND_REGISTER:
                    registerListener();
                    break;
                case (COMMAND_UNREGISTER):
                    unregisterListener();
                    break;
                default:
                    try {
                        gazePoint = (int) intent.getExtras().get(GAZEPOINTPOSITION);
                        Log.v(TAG, "gazePoint int onStartCommand: " + gazePoint);
                    } catch (Exception e) {
                        Log.v(TAG, "onStartCommand intent null");
                    }

                    if (command.equals(getString(R.string.extra_capturingevent_normal)) || command.equals(null)) {
                        //Read and store current data-----------------------------------------------------------
                        //photo name--------------------------------------------------------------------------
                        cv.put(Storage.COLUMN_PHOTO, photoName);
                        Log.v(TAG, "photoName" + photoName);

                        //capture Event-------------------------------------------------------------------------
                        cv.put(Storage.COLUMN_CAPTURECOMMAND, command);
                        Log.v(TAG, "captureEvent: " + command);

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

                            locationLatitude = (int) (locaLat * 1000000);
                            locationLongitude = (int) (locLong * 10000000);
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
                        cv.put(Storage.COLUMN_ORIENTATION, orientation);
                        Log.v(TAG, "orientation: " + orientation);

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

                        cv.put(Storage.COLUMN_BATTERYSTATUS, batteryStatus);
                        Log.v(TAG, "batteryStatus: " + batteryStatus);

                        cv.put(Storage.COLUMN_BATTERYLEVEL, batteryLevel);
                        Log.v(TAG, "batteryLevel: " + batteryLevel);

                        //gazepoint----------------------------------------------------------------
                        cv.put(Storage.COLUMN_GAZEPOINT, gazePoint);
                        cv.put(Storage.COLUMN_VALID, pictureValidity);
                        Log.v(TAG, "gaze Point: " + gazePoint);

                    } else {//read general sensor information and store to db---------------------------------------
                        try {
                            photoName = (String) intent.getExtras().get(PICTURENAME);
                        } catch (NullPointerException e) {
                        }
                        cv.put(Storage.COLUMN_PHOTO, photoName);
                        Log.v(TAG, "photoName" + photoName);

                        cv.put(Storage.COLUMN_CAPTURECOMMAND, command);
                        Log.v(TAG, "captureEvent: " + command);

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
                            rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR).toString();
                        } catch (Exception e) {
                            Log.d(TAG, "no rotationVectorSensor found");
                        }
                        cv.put(Storage.COLUMN_ORIENTATION, rotationVectorSensor);
                        Log.v(TAG, "orientation Sensor:" + rotationVectorSensor);

                        cv.put(Storage.COLUMN_BATTERYSTATUS, batteryStatus);
                        Log.v(TAG, "batteryStatus: " + batteryStatus);

                        cv.put(Storage.COLUMN_BATTERYLEVEL, batteryLevel);
                        Log.v(TAG, "batteryLevel: " + batteryLevel);

                        //gazepoint & valitidy----------------------------------------------------------------
                        cv.put(Storage.COLUMN_GAZEPOINT, gazePoint);
                        cv.put(Storage.COLUMN_VALID, pictureValidity);
                        Log.v(TAG, "gaze Point: " + gazePoint);

                        Log.v(TAG, "VALUES " + cv.toString());
                    }

                    //write data to database--------------------------------------------------------------------
                    storage = new Storage(getApplicationContext());
                    database = storage.getWritableDatabase();
                    long insertIdWrite = database.insert(Storage.DB_TABLE, null, cv);
                    Log.v(TAG, "data stored to db");
                    database.close();

                    //reset values not depending from sensor listener
                    photoName = locationRoad = locationPLZ = orientation = batteryStatus = NASTRING;
                    locationLatitude = locationLongitude = screenBrightness = NAINT;
            }

        } catch (Exception e) {
            Log.v(TAG, "onStartCommand intent null");
        }

        return START_STICKY;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case (Sensor.TYPE_LIGHT):
                ambientLight = String.valueOf(event.values[0]);
                break;
            case (Sensor.TYPE_LINEAR_ACCELERATION):
                if (accelerometerX.size() > 0) {
                    accelerometerX.removeLast();
                }
                accelerometerX.addFirst(String.valueOf(event.values[0]));

                if (accelerometerY.size() > 0) {
                    accelerometerY.removeLast();
                }
                accelerometerY.addFirst(String.valueOf(event.values[1]));

                if (accelerometerZ.size() > 0) {
                    accelerometerZ.removeLast();
                }
                accelerometerZ.addFirst(String.valueOf(event.values[2]));
                break;
            case (Sensor.TYPE_GYROSCOPE):
                gyroscopeX = String.valueOf(event.values[0]);
                gyroscopeY = String.valueOf(event.values[1]);
                gyroscopeZ = String.valueOf(event.values[2]);
                break;
            case (Sensor.TYPE_ROTATION_VECTOR):
                if (event.values.length > 4) {
                    float[] truncatedRotationVector = new float[4];
                    System.arraycopy(event.values, 0, truncatedRotationVector, 0, 4);
                    updateRotation(truncatedRotationVector);
                } else {
                    updateRotation(event.values);
                }
                break;
        }
    }


    private void updateRotation(float[] vectors) {
        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(rotationMatrix, vectors);
        int worldAxisX = SensorManager.AXIS_X;
        int worldAxisZ = SensorManager.AXIS_Z;
        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisX, worldAxisZ, adjustedRotationMatrix);
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);
        float roll = (float) Math.toDegrees(orientation[2]);

        if(roll >= -75 && roll <= 75){
            this.orientation = PORTAIT;
        }else{
            this.orientation = LANDSCAPE;
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
}