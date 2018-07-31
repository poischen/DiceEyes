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
                    COLUMN_GAZEPOINT + " TEXT);";



    public static final String STORAGEPATHIMG = "storage/emulated/0/DiceEyes/images";
    public static final String STORAGEPATHLOG = "storage/emulated/0/DiceEyes/debug";

    private SharedPreferences userAliasStorage;
    private SharedPreferences.Editor userAliasEditor;

    private SharedPreferences photoAlarmsStorage;
    private SharedPreferences.Editor photoAlarmsEditor;

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

    public void setPhotoWasTakenInCurrentPeriod(int period, boolean wasTaken){
        String p = period + "";
        photoAlarmsEditor.putBoolean(p, wasTaken);
        int counterValue = photoAlarmsStorage.getInt("counter", 0);
        Log.v(TAG, "counter value: " + counterValue);
        photoAlarmsEditor.putInt("counter", (counterValue+1));
        photoAlarmsEditor.commit();
    }

    public boolean getPhotoWasTakenInCurrentPeriod(int period) {
        String p = period + "";
        boolean getPhotoWasTakenInCurrentPeriod = photoAlarmsStorage.getBoolean(p, false);
        return getPhotoWasTakenInCurrentPeriod;
    }

    //TODO: -1 checken/bedenken
    public int getMissedPeriods(int period){
        int countedPeriods = photoAlarmsStorage.getInt("counter", 0);
        int missedPeriods = 0;
        if (countedPeriods < (period - 1)){
            missedPeriods = period - 1 - countedPeriods;
        }
        return missedPeriods;
    }

   /* public void setAllPhotosWereTakenInCurrentPeriod(boolean wasTaken){
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);
        photoAlarmsEditor.putBoolean(String.valueOf(R.string.alarm_period_1), wasTaken);

        photoAlarmsEditor.commit();
    }*/

    public String getStoragePath(){
        return STORAGEPATHIMG;
    }

    //TODO: set start mit date
    public void setAlarmStart(Context context, String input, int index){
        /*try {
            userAliasEditor.putString(storage_user_name, input);
            userAliasEditor.putInt(storage_user_index, index);
            userAliasEditor.commit();
            Log.d(TAG, "Alias stored: " + input);
        } catch (Exception e){
        }*/
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
