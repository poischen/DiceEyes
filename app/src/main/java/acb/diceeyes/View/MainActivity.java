package acb.diceeyes.View;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import acb.diceeyes.AlarmController.ControllerService;
import acb.diceeyes.Collection.DataCollectionService;
import acb.diceeyes.R;
import acb.diceeyes.Storage;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Storage storage;

    private TextView chooseAliasHLtv;
    private TextView hellotv;
    private Spinner aliasSpinner;
    private ImageButton startStopButton;

    private boolean spinnerIsActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = new Storage(getApplicationContext());

        startStopButton = (ImageButton) findViewById(R.id.startStopButton);
        aliasSpinner = (Spinner) findViewById(R.id.spinnerNames);
        chooseAliasHLtv = (TextView) findViewById(R.id.tellMeYourNameTextView);
        hellotv = (TextView) findViewById(R.id.helloTextView);

        startStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                scaleAnimation();
                if  (storage.isServiceRunning(getApplicationContext(), ControllerService.class.getName())){
                    startStopButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_is_off));
                    Intent controllerIntent = new Intent(getApplicationContext(), ControllerService.class);
                    stopService(controllerIntent);
                    Intent dataIntent = new Intent(getApplicationContext(), DataCollectionService.class);
                    stopService(dataIntent);
                } else {
                    startControllerService();
                }
            }
        });

        aliasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (storage.isServiceRunning(getApplicationContext(), ControllerService.class.getName())){
                    Toast.makeText(MainActivity.this, getString(R.string.error_error_alreadyrunning), Toast.LENGTH_LONG).show();
                } else if (storage.getAlias() == null){
                    storeAlias(aliasSpinner.getSelectedItem().toString());
                } else if (storage.getAlias() != null){
                    showConfirmation();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        //give feedback, if a userName is already stored and restart the service, if it is not running although a username is already set
        startStopButton.setEnabled(false);
        if (!(storage.getAlias() == null)) {
            aliasAlreadySet();
        }


        spinnerIsActive = true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_list, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int selectedItemId = item.getItemId();
        switch (selectedItemId) {
            case R.id.menu_item_review:
                Intent intent = new Intent(MainActivity.this, PhotoReviewTransferActivity.class);
                MainActivity.this.startActivity(intent);
                break;
            case R.id.menu_item_permissions:
                //show requiered permissions for camera, storage and location permission
                Intent permissionIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                permissionIntent.setData(uri);
                startActivity(permissionIntent);
                break;
            case R.id.menu_item_log:
                sendLogViaMail();
            default:
                Toast.makeText(this, R.string.error_try_again, Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }


    /*
    Creates a logfile
     */
    private String createLogcat(){
        String path = null;
        try {
            File logFile = new File(storage.STORAGEPATHLOG);
            if (!logFile.exists()) {
                logFile.mkdirs();
            }

            DateFormat dateFormat = new SimpleDateFormat(getString(R.string.global_date_pattern));
            String timeString = dateFormat.format(new Date());
            String test = getString(R.string.log_file_filename);

            path = File.separator
                    + storage.getAlias() + getString(R.string.log_file_filename) + timeString + getString(R.string.log_file_filespec);
            Runtime.getRuntime().exec(
                    "logcat  -d -f " + logFile + path);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return (storage.STORAGEPATHLOG + path);
    }

    /*
    Sends logfile via mail
     */
    private void sendLogViaMail() {
        Intent mailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        mailIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        mailIntent.setType("text/plain");
        mailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{getString(R.string.log_mail_address)});
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.log_mail_subject) + storage.getAlias());
        ArrayList<CharSequence> text = new ArrayList<CharSequence>();
        text.add(getString(R.string.log_mail_msg));
        mailIntent.putExtra(Intent.EXTRA_TEXT, text);
        ArrayList<Uri> uris = new ArrayList<Uri>();
        File logfile = new File(createLogcat());
        Uri u = Uri.fromFile(logfile);
        uris.add(u);
        mailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        Intent shareIntent = Intent.createChooser(mailIntent, getString(R.string.log_mail_intent_title));
        shareIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(shareIntent);
    }

    /*Starts a longlasting Service which controlls and triggers the gaze grid */
    private void startControllerService(){
        if (storage.getAlias() != null) {
            Intent controllerIntent = new Intent(this, ControllerService.class);
            getApplicationContext().startService(controllerIntent);
        } else {
            Toast.makeText(this, getString(R.string.error_missing_alias), Toast.LENGTH_SHORT).show();
        }

    }

    public Storage getStorage(){
        return this.storage;
    }

    /**
     * Stores a pseudonym of the user for identifying him and naming the photos after him
     */
    private void storeAlias(String input) {
        try {
            //get index of spinner
            int index = 0;
            for (int i=0;i<aliasSpinner.getCount();i++){
                if (aliasSpinner.getItemAtPosition(i).toString().equalsIgnoreCase(input)){
                    index = i;
                    break;
                }
            }
            storage.setAlias(getApplicationContext(), input, index);
            Log.v(TAG, "Study alias read: " + input);
            Toast.makeText(this, getString(R.string.main_toast_alias_set) + input, Toast.LENGTH_SHORT).show();
            aliasAlreadySet();
            Log.v(TAG, "Study alias successfully stored:" + input);
        } catch (NullPointerException e) {
            Toast.makeText(this, getString(R.string.error_try_again), Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Study user name not stored.");
        }

    }


    /**
     * Helper method to give the user feedback, if the name is already set
     */
    private void aliasAlreadySet() {
        String alias = storage.getAlias();
        aliasSpinner.setSelection(storage.getAliasIndex());
        hellotv.setText(getString(R.string.main_hello) + " " + alias + "!");
        chooseAliasHLtv.setEnabled(false);
        startStopButton.setEnabled(true);

        if (storage.isServiceRunning(getApplicationContext(), ControllerService.class.getName())){
            startStopButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_is_on));
        } else {
            startStopButton.setImageDrawable(getResources().getDrawable(R.drawable.icon_is_off));
        }
    }

    private void showConfirmation(){
        final String newAlias = aliasSpinner.getSelectedItem().toString();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.main_alertdialog_title));
        builder.setMessage(getString(R.string.main_alertdialog_content1) + storage.getAlias() + getString(R.string.main_alertdialog_content2) + newAlias + getString(R.string.main_alertdialog_content3));

        builder.setPositiveButton(getString(R.string.main_alertdialog_buttonok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                storeAlias(newAlias);
            }
        });
        builder.setNegativeButton(getString(R.string.main_alertdialog_buttonredo), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void scaleAnimation(){
        Animation scaleAnimation =
                AnimationUtils.loadAnimation(getApplicationContext(),
                        R.anim.scale_button);
        startStopButton.startAnimation(scaleAnimation);
    }
}
