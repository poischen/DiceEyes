package acb.diceeyes.View;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import acb.diceeyes.AlarmController.ControllerService;
import acb.diceeyes.R;
import acb.diceeyes.Storage;

public class PhotoReviewTransferActivity extends AppCompatActivity {

    private static final String TAG = PhotoReviewTransferActivity.class.getSimpleName();
    private static final String FTPHOST = "ftp.mkhamis.com";
    private static final String SFTPHOST = "phoneholder.medien.ifi.lmu.de";
    private static final int FTPPORT = 21;
    private static final int SFTPPORT = 22022;
    private static final String FTPUSER = "anita@mkhamis.com";
    private static final String SFTPUSER = "phoneholder.app";
    private static final String FTPPASSWORD = "dd)WN~AfiPtF";
    private static final String SFTPPASSWORD = "gN4j+rt7s=6cRA";

    private GridView gridView;
    private PhotoReviewGridViewAdapter gridAdapter;
    private ArrayList<PhotoItem> pictureItems;
    private ArrayList<PhotoItem> taggedToDeleteItems = new ArrayList<>();
    private ProgressBar progressBar;
    private ProgressDialog uploadProgressDialog;
    private ProgressDialog connectProgressDialog;
    private FloatingActionButton uploadFBA;

    private Storage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_photo_review_transfer);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(R.integer.notification_id_reminderdatatransfer);

        uploadFBA = (FloatingActionButton) findViewById(R.id.transferPhotosFAB);
        uploadFBA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        if (!(pictureItems == null)) {
                            Log.v(TAG, "upload via ftp");
                            //AsyncTaskConnectAndUploadToFTP ftpTask = new AsyncTaskConnectAndUploadToFTP();
                            //ftpTask.execute();

                            //Feedback for connecting
                            connectProgressDialog = new ProgressDialog(PhotoReviewTransferActivity.this);
                            connectProgressDialog.setIndeterminate(false);
                            connectProgressDialog.setMessage("Connecting... ");
                            connectProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            connectProgressDialog.setCancelable(true);

                            //feedback for upload
                            uploadProgressDialog = new ProgressDialog(PhotoReviewTransferActivity.this);
                            uploadProgressDialog.setTitle("Uploading images");
                            uploadProgressDialog.setMessage("Upload in progress... ");
                            uploadProgressDialog.setProgressStyle(uploadProgressDialog.STYLE_HORIZONTAL);
                            uploadProgressDialog.setProgress(0);
                            uploadProgressDialog.setMax(pictureItems.size() + 1);

                            AsyncTaskConnectAndUploadToSFTP sftpTask = new AsyncTaskConnectAndUploadToSFTP(PhotoReviewTransferActivity.this);
                            sftpTask.execute();
                            connectProgressDialog.show();
                        }
            }
        });

        storage = new  Storage(getApplicationContext());

        AsyncTaskBuildGrid stbg = new AsyncTaskBuildGrid();
        stbg.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_ressource, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int s = gridAdapter.getDataSize();
        for (int i=0; i < s; i++){
            PhotoItem currentItem = (PhotoItem) gridAdapter.getItem(i);
            try {
                if (currentItem.getCheckbox().isChecked()){
                    taggedToDeleteItems.add(currentItem);
                }
            } catch (Exception e){

            }
        }

        for (int i=0; i < taggedToDeleteItems.size(); i++)
        {
            try {
                PhotoItem currentItem = taggedToDeleteItems.get(i);
                gridAdapter.remove(currentItem);
                File file = new File(taggedToDeleteItems.get(i).getAbsolutePath());
                file.delete();
                pictureItems.remove(currentItem);
                Log.v(TAG, "File deleted: " + file);
            }
            catch (Exception e) {
                Log.d(TAG, "file could not be deleted");
            }
        }

        gridAdapter.notifyDataSetChanged();
        taggedToDeleteItems.clear();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart(){
        super.onStart();

        gridView = (GridView) findViewById(R.id.gridView);
        ArrayList<PhotoItem> dummy = new ArrayList<PhotoItem>();
        gridAdapter = new PhotoReviewGridViewAdapter(getApplicationContext(), R.layout.picture_review_grid_item, dummy);
        gridView.setAdapter(gridAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                PhotoItem item = (PhotoItem) parent.getItemAtPosition(position);
                Log.v(TAG, "Item clicked: " + item.getAbsolutePath());
                if (!item.isTaggedToDelete()){
                    item.setTaggedToDelete(true);
                    taggedToDeleteItems.add(item);
                } else {
                    item.setTaggedToDelete(false);
                    taggedToDeleteItems.remove(item);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    private final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            ArrayList<String> result = msg.getData().getStringArrayList("data");
            for (String fileName : result) {
                Log.v(TAG, "Listed File: " + fileName);
            }
        }
    };

    private String getDatabase(){
        String sqlPath = storage.getWritableDatabase().getPath();
        Log.v(TAG, "SqlPath: " + sqlPath);
        return sqlPath;
    }

    private void getData() {
        File folder = new File(storage.getStoragePath() + File.separator);
        Log.v(TAG, "folder " + folder);
        File[] listOfFiles = folder.listFiles();

        try {
            for (File file : listOfFiles) {
                if (file.isFile()) {

                }
            }

            if (listOfFiles.length > 0) {
                //calculate scale
                Display display = getWindowManager().getDefaultDisplay();
                Point displaySize = new Point();
                display.getSize(displaySize);
                int displayWidth = displaySize.x;

                int width;
                int height;

                Bitmap picture = BitmapFactory.decodeFile(listOfFiles[0].getAbsolutePath());
                width = picture.getWidth();
                height = picture.getHeight();

                float scaledWidth = displayWidth / 3;
                float scalefactor = width / scaledWidth;
                float scaledHeight = height / scalefactor;
                int scaledWidthInt = (int) scaledWidth;
                int scaledHeightInt = (int) scaledHeight;

                int storagepathlength = storage.getStoragePath().length() + 2;

                //get pictures
                pictureItems = new ArrayList<PhotoItem>();
                for (int i = 0; i < listOfFiles.length; i++) {

                    Log.v(TAG, "Image: " + i + ": path: " + listOfFiles[i].getAbsolutePath());
                    String currentPath = listOfFiles[i].getAbsolutePath();

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 7;
                    Bitmap currentPicture = BitmapFactory.decodeFile(listOfFiles[i].getAbsolutePath(),options);

                    Bitmap scaledPicture = Bitmap.createScaledBitmap(currentPicture, scaledWidthInt, scaledHeightInt, false);

                    String picName = currentPath.substring(storagepathlength);

                    PhotoItem pictureItem = new PhotoItem(scaledPicture, currentPath, picName);
                    pictureItems.add(pictureItem);
                    gridAdapter.addData(pictureItem);
                }
            }
        } catch (Exception e) {
            Log.d(TAG, "could not load files: " + e);
        }
    }

    private class AsyncTaskBuildGrid extends AsyncTask<String, String, String> {
        private ArrayList<PhotoItem> data;
        private String resp;

        @Override
        protected String doInBackground(String... params) {
            Log.v(TAG, "doInBackground");
            getData();
            return resp;
        }

        @Override
        protected void onPostExecute(String result) {
            Log.v(TAG, "onPostExecute");
            gridAdapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }


    class AsyncTaskConnectAndUploadToSFTP extends AsyncTask<String, String, String> {

        private PhotoReviewTransferActivity pictureReviewActivity;

        public AsyncTaskConnectAndUploadToSFTP(PhotoReviewTransferActivity photoReviewActivity) {
            this.pictureReviewActivity = photoReviewActivity;
        }

        @Override
        protected String doInBackground(String... params) {
            DateFormat dateFormat = new SimpleDateFormat(getString(R.string.global_date_pattern));
            String time = dateFormat.format(new Date());

            try {
                JSch jsch = new JSch();
                Session session = jsch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
                Properties config = new Properties();
                config.put("StrictHostKeyChecking", "no");
                config.put("PreferredAuthentications", "password");
                session.setConfig(config);
                session.setPassword(SFTPPASSWORD);
                session.connect(3000);
                Channel channel = session.openChannel("sftp");
                ChannelSftp sftp = (ChannelSftp) channel;
                sftp.connect(3000);

                sftp.cd(File.separator + "upload" + File.separator + storage.getAlias());

                //feedback for upload
                showUploadFeedback();

                //upload database
                String dataBase = getDatabase();
                File databaseFile = new File(dataBase);
                FileInputStream inputDB = new FileInputStream(databaseFile);
                String remoteDB = Storage.DB_NAME + time + ".db";
                sftp.put(inputDB, remoteDB, null);
                inputDB.close();
                Log.v(TAG, "upload db successful");
                uploadProgressDialog.incrementProgressBy(1);

                ArrayList<PhotoItem> uploadedItems = new ArrayList<PhotoItem>();

                //upload pictures
                for (int i = 0; i < pictureItems.size(); i++) {
                    File file = new File(pictureItems.get(i).getAbsolutePath());
                    String remote = pictureItems.get(i).getPictureName();
                    InputStream inputStream = new FileInputStream(file);
                    sftp.put(inputStream, remote, null);
                    inputStream.close();
                    Log.v(TAG, "upload " + i + " successful");
                    uploadedItems.add(pictureItems.get(i));
                    file.delete();
                    uploadProgressDialog.incrementProgressBy(1);
                }

                channel.disconnect();
                session.disconnect();
                uploadProgressDialog.dismiss();
                notifyGrid(uploadedItems);


            } catch (JSchException e){
                Log.d(TAG, "jsch exception while connecting " + e);
                showFailFeedback();

            } catch (FileNotFoundException e) {
                e.printStackTrace();
                showFailFeedback();
            } catch (IOException e) {
                e.printStackTrace();
                showFailFeedback();
            } catch (SftpException e) {
                e.printStackTrace();
                showFailFeedback();
            } /*catch (Exception e) {
                Log.d(TAG, "exception while uploading " + e);
                showFailFeedback();
            }*/

            return null;

        }

        //workaround: feedback for connecting or uploading not successfull
        protected void showFailFeedback(){

            pictureReviewActivity.runOnUiThread(new Runnable() {
                public void run() {
                    connectProgressDialog.dismiss();
                    if (!(uploadProgressDialog== null)) {
                        uploadProgressDialog.dismiss();
                    }
                    connectProgressDialog.cancel();

                    Toast.makeText(pictureReviewActivity.getBaseContext(), "Something went wrong. Please try again later.", Toast.LENGTH_LONG).show();
                }
            });
        }

        //workaround: show upload feedback
        protected void showUploadFeedback(){
            pictureReviewActivity.runOnUiThread(new Runnable() {
                public void run() {
                    connectProgressDialog.cancel();
                    uploadProgressDialog.show();
                }
            });
        }

        //workaround: reload grid
        protected void notifyGrid(final ArrayList<PhotoItem> piUploaded){
            pictureReviewActivity.runOnUiThread(new Runnable() {
                public void run() {
                    try {
                        for (int i=0;i<piUploaded.size();i++){
                            gridAdapter.remove(piUploaded.get(i));
                        }
                        gridAdapter.notifyDataSetChanged();
                        Toast.makeText(pictureReviewActivity.getBaseContext(), "Thanks for uploading!", Toast.LENGTH_LONG).show();
                    } catch (Exception e){
                        Log.d(TAG, "notify grid dataset changed failed");
                    }
                }
            });
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }

    //unused -- ftp server while testing
    class AsyncTaskConnectAndUploadToFTP extends AsyncTask<String, String, String> {

        public AsyncTaskConnectAndUploadToFTP() {
            uploadProgressDialog = new ProgressDialog(PhotoReviewTransferActivity.this);
            uploadProgressDialog.setTitle("Uploading images to Dropbox");
            uploadProgressDialog.setMessage("Upload in progress... ");
            uploadProgressDialog.setProgressStyle(uploadProgressDialog.STYLE_HORIZONTAL);
            uploadProgressDialog.setProgress(0);
            uploadProgressDialog.setMax(pictureItems.size() + 1);
            uploadProgressDialog.show();
        }

        @Override
        protected String doInBackground(String... params) {
            FTPClient con = null;

            try {
                con = new FTPClient();
                con.connect(FTPHOST, FTPPORT);

                DateFormat dateFormat = new SimpleDateFormat(getString(R.string.global_date_pattern));
                String time = dateFormat.format(new Date());

                if (con.login(FTPUSER, FTPPASSWORD)) {
                    con.enterLocalPassiveMode();
                    con.setFileType(FTP.BINARY_FILE_TYPE);

                    //upload database
                    String dataBase = getDatabase();
                    File databaseFile = new File(dataBase);
                    FileInputStream inputDB = new FileInputStream(databaseFile);
                    String remoteDB = "DiceEyesDataBase_" + time + ".db";
                    boolean doneDB = con.storeFile(remoteDB, inputDB);
                    inputDB.close();
                    if (doneDB){
                        Log.v(TAG, "upload db successful");
                        uploadProgressDialog.incrementProgressBy(1);
                        //db leeren
                    }

                    //upload pictures
                    for (int i = 0; i < pictureItems.size(); i++) {
                        File file = new File(pictureItems.get(i).getAbsolutePath());
                        String remote = pictureItems.get(i).getPictureName();
                        InputStream inputStream = new FileInputStream(file);
                        boolean done = con.storeFile(remote, inputStream);
                        inputStream.close();
                        if (done) {
                            Log.v(TAG, "upload " + i + " successful");
                            uploadProgressDialog.incrementProgressBy(1);
                            file.delete();
                        }
                    }

                    con.logout();
                    con.disconnect();
                    uploadProgressDialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.GONE);
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onProgressUpdate(String... text) {

        }
    }

}
