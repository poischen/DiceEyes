package acb.diceeyes.Collection;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import acb.diceeyes.AlarmControll.ObservableObject;
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

    public CapturePhotoService()
    {
        super();
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
            capturePhoto();

        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "camera instance not found" + e);
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
            Log.d(TAG, "No camera found. Error code 1.");
        } else {
            if (camId < 0) {
                Log.d(TAG, "No camera found. Error code 2, camera ID " + camId);
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
    private synchronized void capturePhoto() throws InterruptedException {
        Log.v(TAG, "capturePhoto() hase been called.");
        //pictureIsCurrentlyTaken = true;

        CameraPictureCallback pictureCallBack = new CameraPictureCallback(this);
        camera.startPreview();

        /*if (Build.VERSION.SDK_INT < 17) {
            Camera.Parameters params = camera.getParameters();
            if (params.getMaxNumDetectedFaces() > 0) {
                camera.setFaceDetectionListener(new FaceDetectionListener());
                camera.startFaceDetection();
            } else {
                Log.e(TAG, "Face detection is not supported.");
            }
        }*/

        //taking the picture
        try {
            camera.takePicture(null, null, pictureCallBack);
        } catch (Exception e) {
            Log.d(TAG, "camera.takePicture failed");
        }
        //pictureIsCurrentlyTaken = false;
    }

    /* after picture was taken, the method detects face landmarks in the detected faces, which should be stored in the surveys database,
    * it releases the camera and the detector and starts the DataCollectorService
    */
    public void finishCapturing(Bitmap picture, File capturedPhotoFile){

     /*   Canvas canvas = new Canvas(picture);
        Paint paint = new Paint();
        paint.setColor(Color.YELLOW);
*/

        try {
            FileOutputStream fos = new FileOutputStream(capturedPhotoFile);
                        picture.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.getMessage());
            e.getStackTrace();
        } catch (IOException e) {
            Log.e(TAG, "I/O error writing file: " + e.getMessage());
            e.getStackTrace();
        }

        try {
            camera.release();
        } catch (Exception e){
            Log.d(TAG, "Camera could not be released: " + e);
        }

        startDataCollection();
        ControllerService.startDataCollectionService("collect", getApplicationContext(), foregroundApp, capturingEvent, pictureName, leftEyePoints, rightEyePoints, mouthPoints, eulerY, eulerZ, rightEyeOpen, leftEyeOpen);
        camera = null;
    }


    public void setPictureName(String pictureName) {
        this.pictureName = pictureName;
    }

    //processes the picture while taking the photo and detects the faces on the bitmap, calls finishCapturing() after processing is done
    public class CameraPictureCallback implements Camera.PictureCallback {

        private String pictureName;
        private CapturePhotoService cps;

        public CameraPictureCallback (CapturePhotoService cps){
            this.cps = cps;
        }

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            //pictureIsCurrentlyTaken = true;
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

            //detect faces on the bitmap with google play service
            if (!(faceDetector == null) && faceDetector.isOperational()) {
                Log.v(TAG, "face detector is operational");
                Frame frame = new Frame.Builder().setBitmap(rotatedImage).build();
                faces = faceDetector.detect(frame);
                Log.v(TAG, "face detector detected number of faces: " + faces.size());
            } else {
                Log.v(TAG, "face detector is not operational.");
            }
            cps.setPictureName(pictureName);
            cps.finishCapturing(rotatedImage, capturedPhotoFile);
        }

        //helpermethod
        private File getOutputMediaFile() {
            Log.v(TAG, "getOutputMediaFile() called.");
            File filePath = new File(storagePath);
            Log.v(TAG, "File created.");
            DateFormat dateFormat = new SimpleDateFormat("dd-MM-yy_HH:mm:ss");
            String timeString = dateFormat.format(new Date());
            pictureName = userName + "_" + timeString + ".jpg";
            return new File(filePath.getPath() + File.separator + pictureName);
        }

    }

    public static void startDataCollectionService(Context context, String foregroundApp, String photoName) {
       //TODO: implement DataCollectorService
        /* Intent dataCollectionIntent = new Intent(context, DataCollectorService.class);
            dataCollectionIntent.putExtra(DataCollectorService.FOREGROUNDAPP, foregroundApp);
            dataCollectionIntent.putExtra(DataCollectorService.PHOTONAME, photoName);
            if (ObservableObject.getInstance().isOrientationPortrait()) {
                dataCollectionIntent.putExtra(DataCollectorService.ORIENTATION, DataCollectorService.PORTAIT);
            } else {
                dataCollectionIntent.putExtra(DataCollectorService.ORIENTATION, DataCollectorService.LANDSCAPE);
            }
        context.startService(dataCollectionIntent);*/
    }

}
