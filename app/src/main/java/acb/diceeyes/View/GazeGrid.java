package acb.diceeyes.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.provider.ContactsContract;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import acb.diceeyes.R;
import acb.diceeyes.Storage;
import acb.diceeyes.Collection.*;

public class GazeGrid extends AppCompatActivity {

    private static final String TAG = AppCompatActivity.class.getSimpleName();

    private GestureDetector gestureDetector;
    private Storage storage;
    private int gazePointPosition;
    public static String GAZEPOINTPOSITION;
    public int period = 0;
    private String photoName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaze_grid);

        storage = new Storage(getApplicationContext());

        //gesture detector things
        View gestureDetectorView = findViewById(R.id.gestureDetectorView);
        gestureDetector = new GestureDetector(this, new GridViewGestureListener());
        gestureDetectorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        //get calculated gaze point position from controller service and display it
        try {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                gazePointPosition = extras.getInt(GAZEPOINTPOSITION);
                period = extras.getInt(getString(R.string.extra_period));
            }
        } catch (Exception e) {
            gazePointPosition = 0;
        }
        ImageView imageView;
        switch (gazePointPosition){
            case 0: imageView = findViewById(R.id.iv0); break;
            case 1: imageView = findViewById(R.id.iv1); break;
            case 2: imageView = findViewById(R.id.iv2); break;
            case 3: imageView = findViewById(R.id.iv3); break;
            case 4: imageView = findViewById(R.id.iv4); break;
            default: imageView = findViewById(R.id.iv0);
        }
        imageView.setVisibility(View.VISIBLE);
    }

    private void startCapturePhotoService() {
        //create photo name
        DateFormat dateFormat = new SimpleDateFormat(getString(R.string.global_date_pattern));
        String timeString = dateFormat.format(new Date());
        photoName = storage.getAlias() + "_" + timeString + getString(R.string.global_photofile_format);

        //start taking the picture, the CapurePicService will run the DataCollection when it is finished taking the pic
        Intent capturePhotoServiceIntent = new Intent(this, CapturePhotoService.class);
        capturePhotoServiceIntent.putExtra(getString(R.string.extra_datacollection_command), getString(R.string.extra_capturingevent_normal));
        capturePhotoServiceIntent.putExtra(getString(R.string.extra_datacollection_command), photoName);
        capturePhotoServiceIntent.putExtra(DataCollectionService.GAZEPOINTPOSITION, gazePointPosition);
        startService(capturePhotoServiceIntent);
        Log.v(TAG, "CapturePhotoService will be started now");

        //ask if the user really looked at the point
        showConfirmation();
    }

    public void showConfirmation(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.gazegrid_alertdialog_title);
        builder.setMessage(R.string.gazegrid_alertdialog_content);

        builder.setPositiveButton(R.string.gazegrid_alertdialog_buttonok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(GazeGrid.this, R.string.gazegrid_toast_ok, Toast.LENGTH_SHORT).show();
                markInDB("valid");
                storage.setPhotoWasTaken(period, true);
                finish();
            }
        });
        builder.setNegativeButton(R.string.gazegrid_alertdialog_buttonredo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                markInDB("nonvalid");
                finish();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void markInDB(String value){
            Intent dataCollectionIntent = new Intent(getApplicationContext(), DataCollectionService.class);
            dataCollectionIntent.putExtra(DataCollectionService.PICTURENAME, photoName);
            dataCollectionIntent.putExtra(getString(R.string.extra_datacollection_command), DataCollectionService.COMMAND_UPDATE);
            dataCollectionIntent.putExtra(DataCollectionService.PICTUREVALUE, value);
            getApplicationContext().startService(dataCollectionIntent);
    }

    class GridViewGestureListener extends GestureDetector.SimpleOnGestureListener {

        //recognize swiping, so that the alarm/trigger is rescheduled and the activity will be destroyed
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Toast.makeText(GazeGrid.this, R.string.toast_rescheduled, Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }

        //recognize long pressong for capturing the camera
        @Override
        public void onLongPress(MotionEvent e) {
            startCapturePhotoService();
        }
    }
}