package acb.diceeyes.View;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import acb.diceeyes.AlarmControll.ControlleService;
import acb.diceeyes.R;
import acb.diceeyes.Storage;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Storage storage;

    private ImageButton startStopButton;
    private Button testbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storage = new Storage(getApplicationContext());

        startStopButton = (ImageButton) findViewById(R.id.startStopButton);

        testbutton = (Button) findViewById(R.id.testbutton);
        testbutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GazeGrid.class);
                MainActivity.this.startActivity(intent);
            }
        });

        startStopButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startAlarmService();
            }
        });
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
            case R.id.menu_item_settings:
                //give user the opportunity to set a period of time, when DiceEyes should appear
                SettingsFragment settingsFragment = new SettingsFragment();
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, settingsFragment);
                transaction.commit();

                break;
            case R.id.menu_item_permissions:
                //show requiered permissions
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

            DateFormat dateFormat = new SimpleDateFormat(String.valueOf(R.string.global_date_pattern));
            String timeString = dateFormat.format(new Date());
            String test = String.valueOf(R.string.log_file_filename);

            path = File.separator
                    + storage.getAlias() + String.valueOf(R.string.log_file_filename) + timeString + String.valueOf(R.string.log_file_filespec);
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
        mailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{String.valueOf(R.string.log_mail_address)});
        mailIntent.putExtra(Intent.EXTRA_SUBJECT, String.valueOf(R.string.log_mail_subject) + storage.getAlias());
        ArrayList<CharSequence> text = new ArrayList<CharSequence>();
        text.add(String.valueOf(R.string.log_mail_msg));
        mailIntent.putExtra(Intent.EXTRA_TEXT, text);
        ArrayList<Uri> uris = new ArrayList<Uri>();
        File logfile = new File(createLogcat());
        Uri u = Uri.fromFile(logfile);
        uris.add(u);
        mailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        Intent shareIntent = Intent.createChooser(mailIntent, String.valueOf(R.string.log_mail_intent_title));
        shareIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(shareIntent);
    }

    /*Starts a longlasting Service which controlls and triggers the gaze grid */
    private void startAlarmService(){
        Intent controllerIntent = new Intent(this, ControlleService.class);
        getApplicationContext().startService(controllerIntent);
    }

    public Storage getStorage(){
        return this.storage;
    }

}
