package acb.diceeyes;

/**
 * Created by anita on 16.07.2018.
 */

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

import static android.database.sqlite.SQLiteDatabase.openOrCreateDatabase;

/**
 * Knows all storage information
 * Shared Preferences for username + dominant hand
 * Internal Storage for pictures
 * SQL Database for collected data
 */

public class Storage extends SQLiteOpenHelper {

    private static final String TAG = Storage.class.getSimpleName();

    public static final int DB_VERSION = 1;

    public static final String DB_NAME = "DiceEyesDataBase.db";
    public static final String DB_TABLE = "DiceEyesDataCollection";
    public static final String COLUMN_CAPTURE_ID = "_id";
    public static final String COLUMN_PHOTO = "photoName";
    public static final String COLUMN_CAPTUREEVENT = "captureEvent";
    public static final String COLUMN_FOREGROUNDAPP = "foregroundApp";
    public static final String COLUMN_GYROSCOPEX = "gyroscopeX";
    public static final String COLUMN_GYROSCOPEY = "gyroscopeY";
    public static final String COLUMN_GYROSCOPEZ = "gyroscopeZ";
    public static final String COLUMN_ACCELEROMETERX = "accelerometerX";
    public static final String COLUMN_ACCELEROMETERY = "accelerometerY";
    public static final String COLUMN_ACCELEROMETERZ = "accelerometerZ";
    public static final String COLUMN_LIGHT = "light";
    public static final String COLUMN_BRIGHTNESS = "screenLightness";
    public static final String COLUMN_ORIENTATION = "orientation";
    public static final String COLUMN_BATTERYSTATUS = "batteryStatus";
    public static final String COLUMN_BATTERYLEVEL = "batteryLevel";
    public static final String COLUMN_LOCATIONLATITUDE = "LocationLatitude";
    public static final String COLUMN_LOCATIONLONGITUDE = "LocationLongitude";
    public static final String COLUMN_LOCATIONROAD = "LocationRoad";
    public static final String COLUMN_LOCATIONPOSTALCODE = "LocationPLZ";
    public static final String COLUMN_GAZEPOINT = "gazepoint";
    public static final String COLUMN_VALID = "validity";

    public static final String storage_user_pref = "Alias Storage";
    public static final String storage_user_name = "Alias";
    public static final String storage_user_index = "Alias Index";
    public static final String storage_alarm_pref = "Alarm Storage";

    public static final String SQL_CREATEDATA =
            "CREATE TABLE " + DB_TABLE +
                    "(" + COLUMN_CAPTURE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PHOTO + " TEXT, " +
                    COLUMN_CAPTUREEVENT + " TEXT, " +
                    COLUMN_FOREGROUNDAPP + " TEXT, " +
                    COLUMN_LOCATIONLATITUDE + " INTEGER, " +
                    COLUMN_LOCATIONLONGITUDE + " INTEGER, " +
                    COLUMN_LOCATIONROAD + " TEXT, " +
                    COLUMN_LOCATIONPOSTALCODE + " TEXT, " +
                    COLUMN_ACCELEROMETERX + " TEXT, " +
                    COLUMN_ACCELEROMETERY + " TEXT, " +
                    COLUMN_ACCELEROMETERZ + " TEXT, " +
                    COLUMN_GYROSCOPEX + " TEXT, " +
                    COLUMN_GYROSCOPEY + " TEXT, " +
                    COLUMN_GYROSCOPEZ + " TEXT, " +
                    COLUMN_LIGHT + " TEXT, " +
                    COLUMN_BRIGHTNESS + " INTEGER, " +
                    COLUMN_ORIENTATION + " TEXT, " +
                    COLUMN_BATTERYSTATUS + " TEXT, " +
                    COLUMN_BATTERYLEVEL + " INTEGER, " +
                    COLUMN_GAZEPOINT + " TEXT " +
                    COLUMN_VALID + " TEXT);";



    public static final String STORAGEPATHIMG = "storage/emulated/0/DiceEyes/images";
    public static final String STORAGEPATHLOG = "storage/emulated/0/DiceEyes/debug";

    private SharedPreferences userAliasStorage;
    private SharedPreferences.Editor userAliasEditor;

    private SharedPreferences photoAlarmsStorage;
    private SharedPreferences.Editor photoAlarmsEditor;

    private int gazePoint = 0;

    public Storage(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        Log.v(TAG, "Database created " + getDatabaseName());
        new File(STORAGEPATHIMG).mkdirs();
        new File(STORAGEPATHLOG).mkdirs();
        userAliasStorage = context.getSharedPreferences(storage_user_pref, 0);
        userAliasEditor = userAliasStorage.edit();

        photoAlarmsStorage = context.getSharedPreferences(storage_alarm_pref, 0);
        photoAlarmsEditor = photoAlarmsStorage.edit();
    }

    public String getAlias(){
        return userAliasStorage.getString(storage_user_name, null);
    }

    public int getAliasIndex(){
        return userAliasStorage.getInt(storage_user_index, 0);
    }

    public void setAlias(Context context, String input, int index){
        try {
            userAliasEditor.putString(storage_user_name, input);
            userAliasEditor.putInt(storage_user_index, index);
            userAliasEditor.commit();
            Log.v(TAG, "Alias stored: " + input);
        } catch (Exception e){
        }
    }

    protected void deleteAlias(){
        userAliasEditor.putString(storage_user_name, null);
        userAliasEditor.commit();
    }

    //remember a picture was taken in current period, no matter if it was a missing one of not
    //increase counter
    public void setPhotoWasTaken(int currentPeriod, boolean wasTaken){
        if (currentPeriod > 0){
            String p = currentPeriod + "";
            photoAlarmsEditor.putBoolean(p, wasTaken);
        }
        int counterValue = photoAlarmsStorage.getInt("counter", 0);
        Log.v(TAG, "counter value: " + counterValue);
        photoAlarmsEditor.putInt("counter", (counterValue+1));
        photoAlarmsEditor.commit();
    }

    public void resetMissedPeriodsCounter(){
        photoAlarmsEditor.putInt("counter", 0);
        photoAlarmsEditor.commit();
    }


    public void setAllPhotosWereTaken(boolean wasTaken){
        photoAlarmsEditor.putBoolean("10", wasTaken);
        photoAlarmsEditor.putBoolean("105", wasTaken);
        photoAlarmsEditor.putBoolean("11", wasTaken);
        photoAlarmsEditor.putBoolean("115", wasTaken);
        photoAlarmsEditor.putBoolean("12", wasTaken);
        photoAlarmsEditor.putBoolean("125", wasTaken);
        photoAlarmsEditor.putBoolean("13", wasTaken);
        photoAlarmsEditor.putBoolean("135", wasTaken);
        photoAlarmsEditor.putBoolean("14", wasTaken);
        photoAlarmsEditor.putBoolean("145", wasTaken);
        photoAlarmsEditor.putBoolean("15", wasTaken);
        photoAlarmsEditor.putBoolean("155", wasTaken);
        photoAlarmsEditor.putBoolean("16", wasTaken);
        photoAlarmsEditor.putBoolean("165", wasTaken);
        photoAlarmsEditor.putBoolean("17", wasTaken);
        photoAlarmsEditor.putBoolean("175", wasTaken);
        photoAlarmsEditor.putBoolean("18", wasTaken);
        photoAlarmsEditor.putBoolean("185", wasTaken);
        photoAlarmsEditor.putBoolean("19", wasTaken);
        photoAlarmsEditor.putBoolean("195", wasTaken);
        photoAlarmsEditor.commit();
    }

    public void setNextGazePoint(){
        gazePoint = (int) Math.random() * 5;
    }

    public int getGazePoint() {
        return gazePoint;
    }

    public boolean getPhotoWasTakenInCurrentPeriod(int period) {
        String p = period + "";
        boolean getPhotoWasTakenInCurrentPeriod = photoAlarmsStorage.getBoolean(p, false);
        return getPhotoWasTakenInCurrentPeriod;
    }

    public int getMissedPeriods(int periodSoll){
        int countedPeriods = photoAlarmsStorage.getInt("counter", 0);
        int missedPeriods = 0;
        if (countedPeriods < (periodSoll)){
            Log.v(TAG, "period soll: " + periodSoll + ", counter: " + countedPeriods);
            missedPeriods = periodSoll - countedPeriods;
            Log.v(TAG, "missed periods: " + missedPeriods);
        }
        return missedPeriods;
    }

    public String getStoragePath(){
        return STORAGEPATHIMG;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.execSQL(SQL_CREATEDATA);
        }
        catch (Exception e) {
            Log.e(TAG, String.valueOf(R.string.error_create_table)+ e.getMessage());
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(TAG, "onUpgrade called");
        if (newVersion == 1){

        }
    }

    public static boolean isServiceRunning(Context context, String serviceName){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceName.equals(service.service.getClassName())) {
                Log.v(TAG, serviceName + " is running.");
                return true;
            }
        }
        Log.v(TAG, String.valueOf(R.string.error_sercive_not_running));
        return false;
    }

}
