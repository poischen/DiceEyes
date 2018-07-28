package acb.diceeyes.Collection;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import acb.diceeyes.R;
import acb.diceeyes.Storage;

/**
 *
 */

public class CapturePhotoService extends Service {

    private static final String TAG = CapturePhotoService.class.getSimpleName();

    private int camId = -2;
    private Camera camera = null;
    private String storagePath;
    private String userName;
    private SurfaceTexture surfaceTexture;
    private String photoName;
    private String event;

    public CapturePhotoService() {
        super();
        Log.v(TAG, "CapturePhotoService() Constructor");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            camera = getCameraInstance();
            surfaceTexture = new SurfaceTexture(0);
            camera.setPreviewTexture(surfaceTexture);

            event = (String) intent.getExtras().get(String.valueOf(R.string.extra_capturingevent));

            capturePhoto();

        } catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, "camera instance not found" + e);
            //TODO: reschedule Alarm
        }

        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Storage storage = new Storage(getApplicationContext());
        storagePath = storage.getStoragePath();
        userName = storage.getAlias();
        findFrontFacingCam();
        Log.v(TAG, "CapturePicService created.");
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "CapturePicService destroyed.");
    }

    //Search for the front facing camera
    private void findFrontFacingCam() {
        if (camId == -2) {
            int cameras = camera.getNumberOfCameras();
            for (int i = 0; i < cameras; i++) {
                android.hardware.Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    camId = i;
                    Log.v(TAG, "Camera found. Camera id: " + camId);
                    break;
                }
            }
        }
    }

    //initialize camera
    public Camera getCameraInstance() {
        Camera c = null;
        if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.v(TAG, "No camera found. Error code 1.");
        } else {
            if (camId < 0) {
                Log.v(TAG, "No camera found. Error code 2, camera ID " + camId);
            } else {
                if (c != null) {
                    c.release();
                    c = null;
                }
                c = Camera.open(camId);
                Log.v(TAG, "Camera opened: " + c);
            }
        }
        return c;
    }

    //creates the CameraPictureCallback, starts the camera preview and let the picture be taken
    private void capturePhoto() throws InterruptedException {
        Log.v(TAG, "capturePhoto() hase been called.");

        CameraPictureCallback pictureCallBack = new CameraPictureCallback(this);
        camera.startPreview();

        //taking the picture
        try {
            camera.takePicture(null, null, pictureCallBack);
        } catch (Exception e) {
            Log.v(TAG, "camera.takePicture failed");
        }
    }


    public void setPhotoName(String name) {
        this.photoName = name;
    }

    public class CameraPictureCallback implements Camera.PictureCallback {

        private String pictureName;
        private CapturePhotoService cps;

        public CameraPictureCallback (CapturePhotoService cps){
            this.cps = cps;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File capturedPhotoFile = getOutputMediaFile();
            if (capturedPhotoFile == null) {
                Log.e(TAG, "Could not create file");
                return;
            }

            Bitmap capturedImage = BitmapFactory.decodeByteArray(data, 0, data.length);
            android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
            android.hardware.Camera.getCameraInfo(camId, info);
            int w = capturedImage.getWidth();
            int h = capturedImage.getHeight();
            float scaleWidth = ((float) (w/2)) / w;
            float scaleHeight = ((float) (h/2)) / h;
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            matrix.postRotate(info.orientation);
            Bitmap rotatedImage = Bitmap.createBitmap(capturedImage, 0, 0, w, h, matrix, true);

            try {
                FileOutputStream fos = new FileOutputStream(capturedPhotoFile);
                rotatedImage.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {
                Log.e(TAG, "File not found: " + e.getMessage());
                e.getStackTrace();
            } catch (IOException e) {
                Log.e(TAG, "I/O error writing file: " + e.getMessage());
                e.getStackTrace();
            }

            Log.v(TAG, "data collection gets started.");

            try {
                camera.release();
            } catch (Exception e){
                Log.v(TAG, "Camera could not be released: " + e);
            }
            startDataCollectionService( "foregroundApp TODO!");
            camera = null;
        }

        //helpermethod
        private File getOutputMediaFile() {
            Log.v(TAG, "getOutputMediaFile() called.");
            File filePath = new File(storagePath);
            Log.v(TAG, "File created.");
            DateFormat dateFormat = new SimpleDateFormat(String.valueOf(R.string.global_date_pattern));
            String timeString = dateFormat.format(new Date());
            photoName = userName + "_" + timeString + String.valueOf(R.string.global_photofile_format);
            return new File(filePath.getPath() + File.separator + pictureName);
        }

    }

    public void startDataCollectionService(String foregroundApp) {
        Intent dataCollectionIntent = new Intent(getApplicationContext(), DataCollectionService.class);
            dataCollectionIntent.putExtra(DataCollectionService.FOREGROUNDAPP, foregroundApp);
            dataCollectionIntent.putExtra(DataCollectionService.PICTURENAME, photoName);
            dataCollectionIntent.putExtra(String.valueOf(R.string.extra_capturingevent), event);
        getApplicationContext().startService(dataCollectionIntent);
    }

}
