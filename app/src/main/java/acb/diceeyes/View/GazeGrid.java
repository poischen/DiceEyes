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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaze_grid);

        View gestureDetectorView = findViewById(R.id.gestureDetectorView);
        gestureDetector = new GestureDetector(this, new GridViewGestureListener());
        gestureDetectorView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                gestureDetector.onTouchEvent(event);
                return true;
            }
        });

        //TODO: dots an entsprechner Stelle setzen
        //TODO: dot sinvoll anzeigen
        ImageView imageView = findViewById(R.id.ivmm);
        imageView.setImageResource(R.drawable.circle);

    }

    private void startCapturePhotoService() {
        //start taking the picture, the CapurePicService will run the DataCollection when it is finished taking the pic
        Intent capturePhotoServiceIntent = new Intent(this, CapturePhotoService.class);
        startService(capturePhotoServiceIntent);
        Log.v(TAG, "CapturePhotoService will be started now");

        //ask if the user really looked at the circle
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
                finish();
            }
        });
        builder.setNegativeButton(R.string.gazegrid_alertdialog_buttonredo, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //TODO: delete picture & mark as not usable
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