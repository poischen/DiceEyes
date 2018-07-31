package acb.diceeyes.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import acb.diceeyes.R;
import acb.diceeyes.Storage;
import acb.diceeyes.Collection.*;

public class GazeGrid extends AppCompatActivity {

    private static final String TAG = AppCompatActivity.class.getSimpleName();

    private GestureDetector gestureDetector;
    private Storage storage;
    private int gazePointPosition;
    public static String GAZEPOINTPOSITION;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaze_grid);

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
            }
        } catch (Exception e) {
            gazePointPosition = 0;
        }
        ImageView imageView;
        switch (gazePointPosition){
            case 0: imageView = findViewById(R.id.iv0); break;
            case 2: imageView = findViewById(R.id.iv2); break;
            case 4: imageView = findViewById(R.id.iv4); break;
            case 6: imageView = findViewById(R.id.iv6); break;
            case 8: imageView = findViewById(R.id.iv8); break;
            default: imageView = findViewById(R.id.iv0);
        }
        imageView.setVisibility(View.VISIBLE);
    }

    private void startCapturePhotoService() {
        //start taking the picture, the CapurePicService will run the DataCollection when it is finished taking the pic
        Intent capturePhotoServiceIntent = new Intent(this, CapturePhotoService.class);
        capturePhotoServiceIntent.putExtra(String.valueOf(R.string.extra_capturingevent), String.valueOf(R.string.extra_capturingevent_normal));
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
                //TODO: mark as valid in db
                finish();
            }
        });
        builder.setNegativeButton(R.string.gazegrid_alertdialog_buttonredo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: mark as non valid in db
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    class GridViewGestureListener extends GestureDetector.SimpleOnGestureListener {

        //recognize swiping, so that the alarm/trigger is rescheduled and the activity will be destroyed
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2,
                               float velocityX, float velocityY) {
            Toast.makeText(GazeGrid.this, R.string.toast_rescheduled, Toast.LENGTH_SHORT).show();
            //TODO: reschedule trigger
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